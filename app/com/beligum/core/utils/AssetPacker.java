/*******************************************************************************
 * Copyright (c) 2013 by Beligum b.v.b.a. (http://www.beligum.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * Contributors:
 *     Beligum - initial implementation
 *******************************************************************************/
package com.beligum.core.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class AssetPacker extends Controller
{
    //-----CONSTANTS-----
    private static final String CACHE_KEY = "pack";
    
    public static final String LOCAL_ASSETS_DIR = "public";
    public static final String LOCAL_RESOURCE_CACHE_DIR = "cache";
    //TODO: replace with dynamic path
    private static final String LOCAL_MANAGED_RESOURCE_DIR = "/target/scala-"+getScalaBinaryVersion()+"/resource_managed/main/"+LOCAL_ASSETS_DIR;
    
    private static final String PUBLIC_CACHE_DIR = "cache";
    
    private static final String CSS_TAG_TEMPLATE = "<link rel=\"stylesheet\" media=\"screen\" href=\"%s\" />";
    private static final String JS_TAG_TEMPLATE = "<script type=\"text/javascript\" src=\"%s\"></script>";
    
    private static final boolean FORCE_PACK = false;
    private static final boolean FORCE_MINIFY = false;
    
    //-----VARIABLES-----

    //-----CONSTRUCTORS-----
    public AssetPacker()
    {
    }

    //-----PUBLIC FUNCTIONS-----
    public static String pack(String[] files)
    {
	final String FILENAME_SUFFIX = "";
	
	String retVal = null;
	
	try {
	    //if not supplied, look it up if nothing is forced
	    boolean pack = !Play.isDev() || FORCE_PACK;
	    boolean minify = !Play.isDev() || FORCE_MINIFY;
	    
	    if (files!=null) {
		if (!pack) {
		    StringBuffer originalTags = new StringBuffer();
		    for (int i=0;i<files.length;i++) {
			if (files[i].endsWith(".css")) {
			    originalTags.append(String.format(CSS_TAG_TEMPLATE, getPublicUrl(files[i]))+"\n");
			}
			else if (files[i].endsWith(".js")) {
			    originalTags.append(String.format(JS_TAG_TEMPLATE, getPublicUrl(files[i]))+"\n");
			}
			else {
			    Logger.warn("Skipping asset file; "+files[i]);
			}
		    }
		    retVal = originalTags.toString();
		}
		else if (files!=null && files.length>0) {
		    int cacheHashRaw = calcHash(files);
		    //TODO: maybe this isn't the best solution, but what are the odds?
		    if (cacheHashRaw<0) {
			cacheHashRaw += Integer.MAX_VALUE;
		    }
		    String cacheHash = ""+cacheHashRaw;
		    
		    /*
		     * First, test the cache.
		     * Note: we use our own map to flush more easily 
		     */
		    Map<String, Object> cache = (Map<String, Object>) Cacher.fetchApplicationObject(CACHE_KEY);
		    if (cache==null) {
			cache = new HashMap<String, Object>();
			Cacher.storeApplicationObject(CACHE_KEY, cache);
		    }
		    /*
		     * Note: we currently have three cache-entries per hit:
		     * 
		     * - key = contains the html tags that point to the next (virtual) files
		     * - key+".css" = the content of the packed, minified and gzipped stylesheet files
		     * - key+".js" = the content of the packed, minified and gzipped javascript files
		     * 
		     * Here, we check if the plain key exists
		     */
		    String cacheValue = (String)cache.get(cacheHash);
		    
		    //cache hit
		    if (cacheValue!=null) {
			retVal = cacheValue;
		    }
		    
		    //cache miss
		    if (retVal==null) {
			Logger.debug("Generating cache file for key: "+cacheHash);
			
			List<String> jsFiles = new ArrayList<String>();
			List<String> cssFiles = new ArrayList<String>();
			for (String filename : files) {
			    if (filename.endsWith(".css")) {
				cssFiles.add(filename);
			    }
			    else if (filename.endsWith(".js")) {
				jsFiles.add(filename);
			    }
			    else {
				Logger.error("Can't pack this file, skipping. Note: I can only pack .js or .css files; "+filename);
			    }
			}
			
			/*
			 * Note: we switched from disk-based packing to memory-based caching.
			 * I've left the first solution commented out in the first block for future reference.
			 */
			StringBuffer packedTags = new StringBuffer();
			List<CachedPack> cachedPacks = new ArrayList<CachedPack>();
			if (!cssFiles.isEmpty()) {
			    /*
			    File packFile = packFiles(cssFiles, FILENAME_PREFIX+cacheHash+FILENAME_SUFFIX+".css", minify);
			    String htmlTag = String.format(CSS_TAG_TEMPLATE, getPublicUrl(PUBLIC_CACHE_DIR+"/"+packFile.getName()));
			    retVal.append(htmlTag);
			    */
			    
			    String key = cacheHash+".css";
			    cache.put(key, new CachedPack(encodeGzip(stringPack(cssFiles, minify)), "text/css"));
			    packedTags.append(String.format(CSS_TAG_TEMPLATE, getPublicUrl(PUBLIC_CACHE_DIR+"/"+key)));
			}
			if (!jsFiles.isEmpty()) {
			    String key = cacheHash+".js";
			    cache.put(key, new CachedPack(encodeGzip(stringPack(jsFiles, minify)), "application/javascript"));
			    packedTags.append(String.format(JS_TAG_TEMPLATE, getPublicUrl(PUBLIC_CACHE_DIR+"/"+key)));
			}
			
			/*
			 * Note: we return the path to virtual files, also stored in the application cache. See servePackFile() and the routes file for further details
			 */
			retVal = packedTags.toString();
			//naked key, without extension = html tags
			cache.put(cacheHash, retVal);
		    }
		}
	    }
	}
	catch (Exception e) {
	    Logger.error("Error while packing files", e);
	}
	
	return retVal;
    }
    public static Result servePackFile(String file)
    {	
	/*
	 * Old disk-based solution
	 * return ok(play.api.Play.getFile("public/cache/"+file, play.api.Play.current()));
	 */
	
	try {
	    //note: the (virtual) 'file' is used as the cache key, see pack()
	    Map<String, Object> cache = (Map<String, Object>) Cacher.fetchApplicationObject(CACHE_KEY);
	    CachedPack pack = null;
	    if (cache!=null && (pack = (CachedPack)cache.get(file))!=null) {
		/*
		 * TODO: the packed files are stored gzipped to save memory and we return it as raw bytes,
		 * but we should probably offer an alternative for browsers that don't support gzip?
		 */
		response().setHeader("Content-Encoding", "gzip");
		response().setHeader("Content-Length", pack.content.length + "");
		return ok(pack.content).as(pack.mimeType);
	    }
	    else {
		/*
		 * Flush the cache; this shouldn't happen if some client doens't want to load random asset files.
		 * Note: this can impact the performance, so I'm flagging this as 'error' so it pops up in the logs if it does. 
		 */
		Logger.error("Flushing the pack-cache, because we seem to miss an entry for "+file+" - this shouldn't happen!");
		Cacher.storeApplicationObject(CACHE_KEY, null);
		return notFound();
	    }
	}
	catch (Exception e) {
	    Logger.error("Error while serving pack file", e);
	    return internalServerError();
	}
    }
    
    private static class CachedPack
    {
	public byte[] content;
	public String mimeType;
	
	public CachedPack(byte[] content, String mimeType)
	{
	    this.content = content;
	    this.mimeType = mimeType;
	}
    }
    private static String stringPack(List<String> files, boolean minify)
    {
	StringBuilder retVal = new StringBuilder();
	
	int counter = 0;
 	if (files!=null) {
	    for (String filename : files) {
		//first try the managed, parsed version
		File file = play.api.Play.getFile(LOCAL_MANAGED_RESOURCE_DIR+"/"+filename, play.api.Play.current());
		//fall back to the public version
		if (file==null || !file.exists()) {
		    file = play.api.Play.getFile(LOCAL_ASSETS_DIR+"/"+filename, play.api.Play.current());
		}
		
		if (file!=null && file.exists()) {
		    try {
			/*
			 *  If it's not already minified, try to minify it here
			 *  Note: we could also search for the existing minified version of this file, but we ran into problems with it 
			 */
			/*
			if (!isMinifiedJs(file)) {
			    Logger.debug("Minifying file before packing it; "+file.getAbsolutePath());
			    retVal.append(minifyJs(file));
			}
			else {
			    retVal.append(FileUtils.readFileToString(file));
			}
			*/
			
			retVal.append(minify?minify(file):FileUtils.readFileToString(file));
			//don't delete this
			retVal.append("\n");
			counter++;
		    }
		    catch (Exception e) {
			Logger.error("Error while reading file during aggregation: "+filename, e);
		    }
		}
		else {
		    Logger.warn("Supplied a file to pack, but it doesn't exist on the server: "+filename+" ("+file.getAbsolutePath()+")");
		}
	    }
	}
 	
 	//Logger.debug("Aggregated "+counter+" files");
	
 	/*
 	 * Note: you have to wrap this string in a @Html() tag on the template site
 	 */
	return retVal.toString();
    }
    private static int calcHash(String[] strings)
    {
	final int prime = 31;
	int result = 1;
	for (String s : strings) {
	    result = result * prime + s.hashCode();
	}
	
	return result;
    }
    private static File testMinifiedJs(File file)
    {
	File retVal = file;
	if (file!=null && file.exists() && file.getAbsolutePath().endsWith(".js") && !file.getAbsolutePath().endsWith(".min.js")) {
	    File minFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-".js".length())+".min.js");
	    if (minFile.exists()) {
		retVal = minFile;
	    }
	}
	
	return retVal;
    }
    private static File packFiles(List<String> files, String filename, boolean minify) throws IOException
    {
	File packFile = play.api.Play.getFile(LOCAL_ASSETS_DIR+"/"+LOCAL_RESOURCE_CACHE_DIR+"/"+filename, play.api.Play.current());
	packFile.mkdirs();
	if (packFile.exists()) {
	    packFile.delete();
	}

	FileUtils.writeStringToFile(packFile, stringPack(files, minify));
	
	return packFile;
    }
    private static String minify(File file)
    {
	String retVal = "";
	
	StringReader in = null;
	StringWriter out = null;
	try {
	    retVal = FileUtils.readFileToString(file);
	    
	    /*
	     * Pre-processing
	     */
	    
	    //remove 'special comments' so they get removed anyway (for libraries à la bootstrap, jquery,...)
	    retVal = retVal.replace("/*!", "/*");
	    
	    in = new StringReader(retVal);
	    out = new StringWriter();
	    
	    if (file!=null && file.exists()) {
		
		if (file.getAbsolutePath().endsWith(".js")) {
		    JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter()
		    {
			public void warning(String message, String sourceName, int line, String lineSource, int lineOffset)
			{
			    Logger.warn(message);
			}
			public void error(String message, String sourceName, int line, String lineSource, int lineOffset)
			{
			    Logger.error(message);
			}
			public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset)
			{
			    error(message, sourceName, line, lineSource, lineOffset);
			    return new EvaluatorException(message);
			}
		    });


		    /*
		     * Display informational messages and warnings.
		     */
		    boolean verbose = false;

		    /*
		     * This minifies AND obfuscates local symbols, disable to minify only. 
		     */
		    boolean munge = true;

		    /* Preserve unnecessary semicolons (such as right before a '}') This option
		     * is useful when compressed code has to be run through JSLint (which is the
		     * case of YUI for example)
		     */
		    boolean preserveAllSemiColons = false;

		    /*
		     * Disable all the built-in micro optimizations.
		     */
		    boolean disableOptimizations = false;

		    compressor.compress(out, -1, munge, verbose, preserveAllSemiColons, disableOptimizations);
		    retVal = out.toString();
		    
		    /*
		     * For Google Closure, switched to YuiCompressor cause it also provided css support
		     * 
		    com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
		    CompilerOptions options = new CompilerOptions();

		    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
		    //WarningLevel.QUIET.setOptionsForWarningLevel(options);
		    //compiler.setLoggingLevel(Level.ALL);

		    //options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
		    //Processes goog.provide() and goog.require() calls
		    //options.closurePass = true;

		    List<SourceFile> externs = new ArrayList<SourceFile>();
		    List<SourceFile> inputs = new ArrayList<SourceFile>();
		    inputs.add(SourceFile.fromFile(file));

		    com.google.javascript.jscomp.Result compileResult = compiler.compile(externs, inputs, options);
		    if (compileResult.success) {
			retVal = compiler.toSource();
		    }
		    else {
			throw new Exception(compileResult.debugLog);
		    }
		    */
		}
		else if (file.getAbsolutePath().endsWith(".css")) {
		    File minFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-".css".length())+".min.css");
		    //we always re-minify, just to be sure...
		    CssCompressor cssCompressor = new CssCompressor(in);
		    cssCompressor.compress(out, -1);
		    retVal = out.toString();
		}
		else {
		    throw new Exception("Can't minify this file; unknown source type.");
		}
	    }
	    else {
		Logger.error("Trying to minify a file that doesn't exist: "+file.getAbsolutePath());
	    }
	}
	catch (Exception e) {
	    Logger.warn("Error while minifying file "+file.getAbsolutePath(), e);
	}
	finally  {
	    if (in!=null) {
		in.close();
	    }
	    if (out!=null) {
		try {
		    out.close();
		} catch (IOException e) {}
	    }
	}
	
	return retVal;
    }
    private static boolean isJs(File file)
    {
	return file!=null && file.exists() && file.getAbsolutePath().endsWith(".js");
    }
    private static boolean isMinifiedJs(File file)
    {
	return isJs(file) && file.getAbsolutePath().endsWith(".min.js");
    }
    private static final String getScalaBinaryVersion()
    {
	try {
	    String[] versions = play.core.PlayVersion.scalaVersion().split("\\.");
	    return versions[0]+"."+versions[1];
	}
	catch (Exception e) {
	    Logger.error("Error while building the target scala directory; all asset packing will fail");
	}
	
	return null;
    }
    private static String getPublicUrl(String filenameInAssetsDir)
    {
	//return com.beligum.core.controllers.routes.Assets.at(filenameInAssetsDir).url();
	return "/assets/" + filenameInAssetsDir;
    }
    private static byte[] encodeGzip(String content) throws IOException
    {
	byte[] retVal = null;
	
	ByteArrayOutputStream out = null;
	GZIPOutputStream gzip = null;
	try {
	    out = new ByteArrayOutputStream();
	    gzip = new GZIPOutputStream(out);
	    gzip.write(content.getBytes("UTF-8"));
	    gzip.close();
	    out.close();
	    
	    retVal = out.toByteArray();
	}
	catch (Exception e){
	    if (gzip!=null) {
		gzip.close();
	    }
	    if (out!=null) {
		out.close();
	    }
	}
	
	return retVal;
    }
    private static String decodeGzip(byte[] content) throws IOException
    {
	String retVal = null;
	
	ByteArrayInputStream in = null;
	GZIPInputStream gzip = null;
	try {
	    in = new ByteArrayInputStream(content);
	    gzip = new GZIPInputStream(in);
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, "UTF-8"));
	    StringBuilder sb = new StringBuilder();
	    String s;
	    while ((s = reader.readLine()) != null) {
		sb.append(s);
	    }
	    gzip.close();
	    in.close();
	    
	    retVal = sb.toString();
	}
	catch (Exception e){
	    if (gzip!=null) {
		gzip.close();
	    }
	    if (in!=null) {
		in.close();
	    }
	}
	
	return retVal;
    }

    //-----PROTECTED FUNCTIONS-----

    //-----PRIVATE FUNCTIONS-----
}
