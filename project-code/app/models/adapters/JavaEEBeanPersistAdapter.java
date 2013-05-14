package models.adapters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PreDestroy;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;

/*
 * Copy/pasted from http://dominikdorn.com/2012/01/jpa-entity-listener-annotations-avaje-ebean/
 */
public class JavaEEBeanPersistAdapter extends BeanPersistAdapter
{
    // -----CONSTANTS-----

    // -----VARIABLES-----
    private Map<String, Method> prePersistMap = new TreeMap<String, Method>();
    private Map<String, Method> postPersistMap = new TreeMap<String, Method>();
    private Map<String, Method> preUpdateMap = new TreeMap<String, Method>();
    private Map<String, Method> postUpdateMap = new TreeMap<String, Method>();
    private Map<String, Method> preDestroyMap = new TreeMap<String, Method>();
    private Map<String, Method> postLoadMap = new TreeMap<String, Method>();

    // -----CONSTRUCTORS-----
    public JavaEEBeanPersistAdapter()
    {
    }

    @Override
    public boolean isRegisterFor(Class<?> aClass)
    {
	if (aClass.getAnnotation(Entity.class) != null) {
	    System.out.println("Registering a Entity; Type is " + aClass.toString());
	    Method[] methods = aClass.getMethods();
	    boolean hasListener = false;
	    for (Method m : methods) {

		// System.out.println("looking if method " + m.toString() +
		// " has Annotation on it. ");

		if (m.isAnnotationPresent(PrePersist.class)) {
		    prePersistMap.put(aClass.getName(), m);
		    hasListener = true;
		}

		if (m.isAnnotationPresent(PostPersist.class)) {
		    postPersistMap.put(aClass.getName(), m);
		    hasListener = true;
		}

		if (m.isAnnotationPresent(PreDestroy.class)) {
		    preDestroyMap.put(aClass.getName(), m);
		    hasListener = true;
		}

		if (m.isAnnotationPresent(PreUpdate.class)) {
		    preUpdateMap.put(aClass.getName(), m);
		    hasListener = true;
		}

		if (m.isAnnotationPresent(PostUpdate.class)) {
		    postUpdateMap.put(aClass.getName(), m);
		    hasListener = true;
		}

		if (m.isAnnotationPresent(PostLoad.class)) {
		    postLoadMap.put(aClass.getName(), m);
		    hasListener = true;
		}

	    }
	    return hasListener;
	}
	return false;
    }
    @Override
    public boolean preInsert(BeanPersistRequest<?> request)
    {
	getAndInvokeMethod(prePersistMap, request.getBean());
	return super.preInsert(request);
    }
    @Override
    public boolean preDelete(BeanPersistRequest<?> request)
    {
	getAndInvokeMethod(preDestroyMap, request.getBean());
	return super.preDelete(request);
    }
    @Override
    public boolean preUpdate(BeanPersistRequest<?> request)
    {
	getAndInvokeMethod(preUpdateMap, request.getBean());
	return super.preUpdate(request);
    }
    @Override
    public void postDelete(BeanPersistRequest<?> request)
    {
	// there is no @PostDestroy annotation in JPA 2
	super.postDelete(request);
    }
    @Override
    public void postInsert(BeanPersistRequest<?> request)
    {
	getAndInvokeMethod(postPersistMap, request.getBean());
	super.postInsert(request);
    }
    @Override
    public void postUpdate(BeanPersistRequest<?> request)
    {
	getAndInvokeMethod(postUpdateMap, request.getBean());
	super.postUpdate(request);
    }
    @Override
    public void postLoad(Object bean, Set<String> includedProperties)
    {
	getAndInvokeMethod(postLoadMap, bean);
	super.postLoad(bean, includedProperties);
    }

    // -----PUBLIC FUNCTIONS-----
    private void getAndInvokeMethod(Map<String, Method> map, Object o)
    {
	Method m = map.get(o.getClass().getName());
	if (m != null)
	    try {
		m.invoke(o);
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    } catch (InvocationTargetException e) {
		e.printStackTrace();
	    }
    }

    // -----PROTECTED FUNCTIONS-----

    // -----PRIVATE FUNCTIONS-----
}
