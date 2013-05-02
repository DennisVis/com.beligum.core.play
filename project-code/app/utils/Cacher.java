package utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import play.Logger;
import play.cache.Cache;
import play.mvc.Http;

/*
 * Play caching overview:
 * 
 * 1. The client side cache stores data in a cookie
 * 
 *     This cache is small (4KB) and only stores strings.
 *     There are two types: 
 * 
 *     - session caching is encrypted and flushes on browser close 
 *     - flash caching is unencrypted (!) and only survives one redirect 
 *     
 *     Note that although the session data is encrypted (no human readable content),
 *     it _can_ easily be hijacked without HTTPS.
 *  
 *  
 *  2. The server side cache stores data in a global cache
 *  
 *      It is flushed on application restart and must be implemented
 *      in a way so that all data may go missing at any time. Nevertheless
 *      it provides a very powerful cache system.
 *  
 *  
 *  =>  We implement a cache system that stores little string ID's on the client
 *      side (in the client side session cache) that are used to look up values in
 *      the global cache on the server side. We'll create three types of server side
 *      caching:
 *      
 *      - request cache
 *          Is used to cache objects during one request, we we can use expensive
 *          method calls straight from the template system without the risk
 *          of executing the method multiple times
 *      
 *      - session cache
 *          Works together with a client-side session uuid value that is passed
 *          with every request (stored as a cookie on the client side) to the server.
 *          The server-side session cache uses this uuid to look up cached values for this
 *          specific session. Since the client side is reset when the brower closes, this
 *          demarcates the longest possible life for this cache. It might be shorter,
 *          though, eg. when a user logs out (but doesn't close the browser).
 *          
 *          Note that this (together with the 'expect nothing' rule) makes sure that caching
 *          works in a loadbalanced situation; it would just mean that multiple servers may
 *          have a session cache for a single client. They are not synchronized, though. 
 *          
 *      - application cache
 *          Is stored during the entire life of a Play instance and work in a 
 *          cross-request, cross-user, cross-session way.
 *          Use with care (don't store user-specific data).
 *          
 *      - eternal cache (idea, TODO if we decide to implement this)
 *          Same as session cache, but the data are serialized and stored in a database
 *          table, so the cache survives a browser close and a Play restart.
 *          Since the caches must always be implemented in a volatile way
 *          (meaning the caller must always assume the cached value may be gone at
 *          any time), it maybe doens't make sense to implement this.
 *  
 *  Design:
 *  
 *      The server-side Cache API doesn't allow us to iterate all values,
 *      so we create a single entry that contains a Map. This map object stores
 *      all different 'scopes' in it's first level: 'REQUEST' maps to a map of request keys,
 *      'SESSION' maps to a map of session keys and 'APPLICATION' maps to a map of application keys.
 *  
 */
public class Cacher
{
    //-----CONSTANTS-----
    public static Key CURRENT_USER = new Key("cuser");
    public interface CacheKey
    {
	public int hashCode();
	public boolean equals(Object obj);
    }
    public class Key implements CacheKey
    {
	private Object key;
	public Key(Object key)
	{
	    this.key = key;
	}
	@Override
	public int hashCode()
	{
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + getOuterType().hashCode();
	    result = prime * result + ((key == null) ? 0 : key.hashCode());
	    return result;
	}
	@Override
	public boolean equals(Object obj)
	{
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    Key other = (Key) obj;
	    if (!getOuterType().equals(other.getOuterType()))
		return false;
	    if (key == null) {
		if (other.key != null)
		    return false;
	    } else if (!key.equals(other.key))
		return false;
	    return true;
	}
	private Cacher getOuterType()
	{
	    return Cacher.this;
	}
    }
    
    /*
     * This is both used as a prefix for the keys in the client side cache
     * and the keys in the server side cache to denote the corresponding
     * object or string is stored through this Cacher
     */
    private static final String GLOBAL_PREFIX = "(b)";
    
    //Client side keys
    private static final String CLIENT_SESSION_ID_KEY = "SESSION_ID";
  	
    //Raw server side key (keeping it short for performance reasons)
    private static final String SERVER_CACHE_MAP_KEY = GLOBAL_PREFIX+"c";
    private static final String SERVER_CACHE_STAMP_MAP_KEY = GLOBAL_PREFIX+"t";
    
    //these enums will be used as first-level dividers in our main cache object
    private enum Scope
    {
	REQUEST,
	SESSION,
	APPLICATION
    }
    
    //Etc
    public static final String TIMEOUT_FORMAT = "dd/MM/yyyy HH:mm:ss";
    

    //-----VARIABLES-----

    //-----CONSTRUCTORS-----

    //-----PUBLIC FUNCTIONS-----
    /**
     * Stores an object in the request cache
     */
    public static void storeRequestObject(CacheKey key, Object value)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return;
	}
	
	try {
	    Calendar nowPlusOneMinute = Calendar.getInstance();
	    nowPlusOneMinute.add(Calendar.MINUTE, 1);
	    
	    Cacher.storeObject(key, value, getCurrentRequestCache(), nowPlusOneMinute);
	}
	catch (Exception e) {
	    Logger.error("Caught exception while storing a request cache object for key "+key, e);
	}
    }
    /**
     * Fetches an object in the request cache.
     * If not present, it returns null.
     */
    public static Object fetchRequestObject(CacheKey key)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return null;
	}

	try {
	    return Cacher.fetchObject(key, getCurrentRequestCache());
	}
	catch (Exception e) {
	    Logger.error("Caught exception while fetching a request cache object for key "+key, e);
	    return null;
	}
    }
    /**
     * This can be used to check if a key is present when we store a null object.
     * (to avoid entering an expensive routine that returns null ;-)
     */
    public static boolean containsRequestObject(CacheKey key)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return false;
	}

	try {
	    return Cacher.containsObject(key, getCurrentRequestCache());
	}
	catch (Exception e) {
	    Logger.error("Caught exception while checking a request cache object for key "+key, e);
	    return false;
	}
    }
    /**
     * Stores an object in the session cache if such a cache is present (started before this call).
     * If not, it returns false.
     */
    public static boolean storeSessionObject(CacheKey key, Object value)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return false;
	}
	
	try {
	    return Cacher.storeSessionObject(key, value, null);
	}
	catch (Exception e) {
	    Logger.error("Caught exception while storing a session cache object for key "+key, e);
	    return false;
	}
    }
    /**
     * Stores an object in the session cache if such a cache is present (started before this call).
     * If not, it returns false. The expiration date is the longest date (roughly, because of rough-grained control) this
     * value may survive in the session cache.
     */
    public static boolean storeSessionObject(CacheKey key, Object value, Calendar expiration)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return false;
	}
	
	try {
	    //set a default of one day when the expiration date is null
	    if (expiration==null) {
		expiration = Calendar.getInstance();
		expiration.add(Calendar.DAY_OF_MONTH, 1);
	    }

	    return Cacher.storeObject(key, value, getCurrentSessionCache(), expiration);
	}
	catch (Exception e) {
	    Logger.error("Caught exception while storing a session cache object for key "+key, e);
	    return false;
	}
    }
    /**
     * Fetches an object in the session cache if such a cache is present (started before this call).
     * If not initialized or not present, it returns null.
     */
    public static Object fetchSessionObject(CacheKey key)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return null;
	}
	
	try {
	    return Cacher.fetchObject(key, getCurrentSessionCache());
	}
	catch (Exception e) {
	    Logger.error("Caught exception while fetching a session cache object for key "+key, e);
	    return null;
	}
    }
    /**
     * This can be used to check if a key is present when we store a null object.
     * (to avoid entering an expensive routine that returns null ;-)
     */
    public static boolean containsSessionObject(CacheKey key)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return false;
	}
	
	try {
	    return Cacher.containsObject(key, getCurrentSessionCache());
	}
	catch (Exception e) {
	    Logger.error("Caught exception while checking a session cache object for key "+key, e);
	    return false;
	}
    }
    /**
     * Stores an object in the application cache
     */
    public static void storeApplicationObject(CacheKey key, Object value)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return;
	}
	
	try {
	    Cacher.storeApplicationObject(key, value, null);
	}
	catch (Exception e) {
	    Logger.error("Caught exception while storing a application cache object for key "+key, e);
	}
    }
    /**
     * Stores an object in the application cache.
     * The expiration date is the longest date (roughly, because of rough-grained control)
     * this value may survive in the session cache.
     */
    public static void storeApplicationObject(CacheKey key, Object value, Calendar expiration)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return;
	}
		
	try {
	    //note: for application storage, we allow explicit null expiration values
	    Cacher.storeObject(key, value, getCurrentApplicationCache(), expiration);
	}
	catch (Exception e) {
	    Logger.error("Caught exception while storing a application cache object for key "+key, e);
	}
    }
    /**
     * Fetches an object in the application cache.
     * If not present, it returns null.
     */
    public static Object fetchApplicationObject(CacheKey key)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return null;
	}
		
	try {
	    return Cacher.fetchObject(key, getCurrentApplicationCache());
	}
	catch (Exception e) {
	    Logger.error("Caught exception while fetching a session cache object for key "+key, e);
	    return null;
	}
    }
    /**
     * This can be used to check if a key is present when we store a null object.
     * (to avoid entering an expensive routine that returns null ;-)
     */
    public static boolean containsApplicationObject(CacheKey key)
    {
	//before doing anything, check if there's a request available to store anything in
	if (!hasContext()) {
	    return false;
	}
		
	try {
	    return Cacher.containsObject(key, getCurrentApplicationCache());
	}
	catch (Exception e) {
	    Logger.error("Caught exception while checking a application cache object for key "+key, e);
	    return false;
	}
    }
    /**
     * Remove all cached items from the current request cache
     */
    public static void flushRequestCache()
    {
	Cacher.flushCurrentRequestCache();
    }
    /**
     * Remove all cached items from the current session cache
     */
    public static void flushSessionCache()
    {
	Cacher.flushCurrentSessionCache();
	//also remove the uuid from the client-side session cache
	Cacher.flushClientSessionCache();
    }
    /**
     * Remove all cached items from the current application cache
     */
    public static void flushApplicationCache()
    {
	Cacher.flushCurrentApplicationCache();
    }
    public static void printCurrentCache()
    {
	Logger.debug(Cacher.getMainCache().toString());
    }

    //-----PROTECTED FUNCTIONS-----

    //-----PRIVATE FUNCTIONS-----
    /**
     * This is the main cache object.
     * It contains a single entry for every scope. The values of 
     * these entries are another map, containing key/value entries for that scope
     */
    private static Map<Scope, Map<UUID, Map<CacheKey, CacheValue>>> getMainCache()
    {
	Map<Scope, Map<UUID, Map<CacheKey, CacheValue>>> retVal = (Map<Scope, Map<UUID, Map<CacheKey, CacheValue>>>)Cache.get(SERVER_CACHE_MAP_KEY);
	
	//bootstrap an empty cache if it's not present (eg. after a server reboot)
	if (retVal==null) {
	    retVal = new HashMap<Scope, Map<UUID, Map<CacheKey, CacheValue>>>();
	    retVal.put(Scope.REQUEST, new HashMap<UUID, Map<CacheKey, CacheValue>>());
	    retVal.put(Scope.SESSION, new HashMap<UUID, Map<CacheKey, CacheValue>>());
	    retVal.put(Scope.APPLICATION, new HashMap<UUID, Map<CacheKey, CacheValue>>());
	    
	    //store our new cache in the Cache API
	    Cache.set(SERVER_CACHE_MAP_KEY, retVal);
	}
	
	//let's use this opportunity to purge the cache
	Cacher.checkPurgeCache(retVal);
	
	return retVal;
    }
    /*
     * Three helper methods that point to the respective cache maps
     */
    private static Map<UUID, Map<CacheKey, CacheValue>> getMainRequestCache()
    {
	return getMainCache().get(Scope.REQUEST);
    }
    private static Map<UUID, Map<CacheKey, CacheValue>> getMainSessionCache()
    {
	return getMainCache().get(Scope.SESSION);
    }
    private static Map<UUID, Map<CacheKey, CacheValue>> getMainApplicationCache()
    {
	return getMainCache().get(Scope.APPLICATION);
    }
    /*
     * Three helper methods that point to the respective cache maps
     * for the current context. 
     * Note: these may return null when they were not initialized before
     */
    private static Map<CacheKey, CacheValue> getCurrentRequestCache()
    {
	Map<CacheKey, CacheValue> retVal = Cacher.getMainRequestCache().get(Cacher.getCurrentRequestKey());
	if (retVal==null) {
	    retVal = new HashMap<CacheKey, CacheValue>();
	    Cacher.getMainRequestCache().put(Cacher.getCurrentRequestKey(), retVal);
	}
	
	return retVal;
    }
    private static Map<CacheKey, CacheValue> getCurrentSessionCache()
    {
	//TODO: check if this automatic initialization is ok
	
	Map<CacheKey, CacheValue> retVal = null;
	UUID currentSessionKey = Cacher.getCurrentSessionKey();
	if (currentSessionKey!=null) {
	    retVal = Cacher.getMainSessionCache().get(currentSessionKey);
	}
	
	if (retVal==null) {
	    /*
	     * Boot the client-side cache.
	     * Note: this is one of the only places where we put something in the client-side session cache
	     */
	    String newSessionId = Cacher.storeClientSessionValue(CLIENT_SESSION_ID_KEY, UUID.randomUUID().toString());

	    /*
	     * Boot the server side cache, using the sessionID stored on the client side
	     */
	    if (Cacher.getMainSessionCache().get(newSessionId)!=null) {
		Logger.error("Encountered an initialized session cache when booting a new session." +
			" This means we have a clash in session UUIDs, which is bad... " +
			"I'm flushing the existing case to avoid security leaks. Existing UUID: "+newSessionId);

		Cacher.getMainSessionCache().remove(newSessionId);
	    }
	    else {
		retVal = new HashMap<CacheKey, CacheValue>();
		Cacher.getMainSessionCache().put(Cacher.getCurrentSessionKey(), retVal);
	    }
	}
	
	return retVal;
    }
    private static Map<CacheKey, CacheValue> getCurrentApplicationCache()
    {
	Map<CacheKey, CacheValue> retVal = Cacher.getMainApplicationCache().get(Cacher.getCurrentApplicationKey());
	if (retVal==null) {
	    Logger.debug("Initializing application cache...");
	    retVal = new HashMap<CacheKey, CacheValue>();
	    Cacher.getMainApplicationCache().put(Cacher.getCurrentApplicationKey(), retVal);
	}
	
	return retVal;
    }
    /*
     * Same as above (context caches), but flush methods
     */
    private static void flushCurrentRequestCache()
    {
	Cacher.getMainRequestCache().remove(Cacher.getCurrentRequestKey());
    }
    private static void flushCurrentSessionCache()
    {
	Cacher.getMainSessionCache().remove(Cacher.getCurrentSessionKey());
    }
    private static void flushCurrentApplicationCache()
    {
	Cacher.getMainApplicationCache().remove(Cacher.getCurrentApplicationKey());
    }
    /*
     * Three getters that return a unique key for the respective scope
     */
    private static UUID getCurrentRequestKey()
    {
	return UUID.nameUUIDFromBytes((""+Http.Context.current().hashCode()).getBytes());
    }
    private static UUID getCurrentSessionKey()
    {
	//this method takes care of the prefix
	String clientSessionId = Cacher.retrieveClientSessionValue(CLIENT_SESSION_ID_KEY);
	return clientSessionId==null?null:UUID.nameUUIDFromBytes(clientSessionId.getBytes());
    }
    private static UUID getCurrentApplicationKey()
    {
	return UUID.nameUUIDFromBytes("application".getBytes());
    }
    /*
     * Next come two generic store/fetch routines so we can use them from every scope
     */
    private static boolean storeObject(CacheKey key, Object value, Map<CacheKey, CacheValue> cache, Calendar expiration)
    {
	boolean retVal = false;
	
	if (cache!=null) {
	    cache.put(key, new CacheValue(value, expiration==null?null:expiration.getTime()));
	    retVal = true;
	}
	else {
	    Logger.warn("Received a request to store a value with an uninitialized cache. CacheKey: "+key);
	}
	
	return retVal;
    }
    private static Object fetchObject(CacheKey key, Map<CacheKey, CacheValue> cache)
    {
	Object retVal = null;
	
	if (cache!=null) {
	    CacheValue val = cache.get(key);
	    if (val!=null) {
		retVal = val.getValue();
	    }
	}
	else {
	    Logger.warn("Received a request to fetch a value with an uninitialized cache. CacheKey: "+key);
	}
	
	return retVal;
    }
    private static boolean containsObject(CacheKey key, Map<CacheKey, CacheValue> cache)
    {
	boolean retVal = false;
	
	if (cache!=null) {
	    retVal = cache.containsKey(key);
	}
	else {
	    Logger.warn("Received a request to check a cache key with an uninitialized cache. CacheKey: "+key);
	}
	
	return retVal;
    }
    /*
     * Next three are wrappers around the client side session storage
     * that appends a prefix to the keys so that we don't mess with any
     * possible other keys that were stored in the client cache outside of this Cacher
     */
    /**
     * Returns the actual key, as used in the client side cache.
     */
    private static String storeClientSessionValue(String key, String value)
    {
	String clientKey = GLOBAL_PREFIX+key;
	
	Http.Context.current().session().put(clientKey, value);
	
	return clientKey;
    }
    private static String retrieveClientSessionValue(String key)
    {
	return Http.Context.current().session().get(GLOBAL_PREFIX+key);
    }
    private static void flushClientSessionCache()
    {
	for(Iterator<Map.Entry<String, String>> it = Http.Context.current().session().entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry<String, String> entry = it.next();
	    if(entry.getKey().startsWith(GLOBAL_PREFIX)) {
		it.remove();
	    }
	}
    }
    /*
     * Checks if we need to purge the cache and does it if we need to
     */
    private static void checkPurgeCache(Map<Scope, Map<UUID, Map<CacheKey, CacheValue>>> mainCache)
    {	
	Calendar now = Calendar.getInstance();
	
	Calendar purgeDeadline = (Calendar)Cache.get(SERVER_CACHE_STAMP_MAP_KEY);
	//only check once every minute
	if (purgeDeadline==null || purgeDeadline.getTime().before(now.getTime())) {
	    for (Iterator<Map.Entry<Scope, Map<UUID, Map<CacheKey, CacheValue>>>> scopeCacheIt = mainCache.entrySet().iterator(); scopeCacheIt.hasNext(); ) {
		Entry<Scope, Map<UUID, Map<CacheKey, CacheValue>>> scopeCacheEntry = scopeCacheIt.next();
		for (Iterator<Map.Entry<UUID, Map<CacheKey, CacheValue>>> scopeEntriesIt = scopeCacheEntry.getValue().entrySet().iterator(); scopeEntriesIt.hasNext(); ) {
		    Entry<UUID, Map<CacheKey, CacheValue>> scopeEntriesEntry = scopeEntriesIt.next();
		    boolean emptyBeforeCheck = scopeEntriesEntry.getValue().isEmpty();
		    for (Iterator<Map.Entry<CacheKey, CacheValue>> cacheEntryIt = scopeEntriesEntry.getValue().entrySet().iterator(); cacheEntryIt.hasNext(); ) {
			Entry<CacheKey, CacheValue> cacheEntry = cacheEntryIt.next();
			if (cacheEntry.getValue().getExpirationStamp()!=null && cacheEntry.getValue().getExpirationStamp().before(now.getTime())) {
			    cacheEntryIt.remove();
			}
		    }
		    if (!emptyBeforeCheck && scopeEntriesEntry.getValue().isEmpty()) {
			scopeEntriesIt.remove();
		    }
		}
	    }
	    
	    //do the next check in one minute
	    now.add(Calendar.MINUTE, 1);
	    Cache.set(SERVER_CACHE_STAMP_MAP_KEY, now);
	}
    }
    private static boolean hasContext()
    {
	try {
	    Http.Context.current();
	    return true;
	}
	catch (Exception e) {}
	
	return false;
    }
    
    //-----PRIVATE CLASSES-----
    private static class CacheValue
    {
	//-----CONSTANTS-----
	
	//-----VARIABLES-----
	private Object value;
	private Date creationStamp;
	private Date expirationStamp;

	//-----CONSTRUCTORS-----
	public CacheValue(Object value)
	{
	    this.value = value;
	    this.creationStamp = new Date();
	    this.expirationStamp = null;
	}
	public CacheValue(Object value, Date expiration)
	{
	    this.value = value;
	    this.creationStamp = new Date();
	    this.expirationStamp = expiration;
	}
	
	//-----PUBLIC FUNCTIONS-----
	public Object getValue()
	{
	    return value;
	}
	public Date getCreationStamp()
	{
	    return creationStamp;
	}
	public Date getExpirationStamp()
	{
	    return expirationStamp;
	}
	
	//-----PRIVATE FUNCTIONS-----
	
	//-----MANAGEMENT FUNCTIONS-----
	@Override
	public int hashCode()
	{
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((creationStamp == null) ? 0 : creationStamp.hashCode());
	    result = prime * result + ((value == null) ? 0 : value.hashCode());
	    return result;
	}
	@Override
	public boolean equals(Object obj)
	{
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (!(obj instanceof CacheValue))
		return false;
	    CacheValue other = (CacheValue) obj;
	    if (creationStamp == null) {
		if (other.creationStamp != null)
		    return false;
	    } else if (!creationStamp.equals(other.creationStamp))
		return false;
	    if (value == null) {
		if (other.value != null)
		    return false;
	    } else if (!value.equals(other.value))
		return false;
	    return true;
	}
	@Override
	public String toString()
	{
	    return this.getClass().getSimpleName()+" [value=" + value + "]";
	}
    }
}
