package com.beligum.core.utils;

import java.util.HashMap;
import java.util.Map;

import play.data.DynamicForm;
import play.data.Form;

public class JsonHelper
{
    public static Map<String, Object> rootNode()
    {
	return new HashMap<String, Object>();
    }

    public static Map<String, Object> infoMessage(String message)
    {
	Map<String, Object> obj = JsonHelper.rootNode();
	obj.put("type", "info");
	obj.put("message", message);

	return obj;
    }

    public static Map<String, Object> errorMessage(String message)
    {
	Map<String, Object> obj = JsonHelper.rootNode();
	obj.put("type", "error");
	obj.put("message", message);

	return obj;
    }

    public static Map<String, Object> successMessage(String message)
    {
	Map<String, Object> obj = JsonHelper.rootNode();
	obj.put("type", "success");
	obj.put("message", message);

	return obj;
    }

    public static Map<String, Object> addObject(Map<String, Object> rootObject, String key, Object object)
    {
	rootObject.put(key, object);
	return rootObject;
    }

    public static Map<String, Object> makeTapeObject(Form form)
    {
	Map<String, Object> tape = JsonHelper.rootNode();
	addFormField(tape, form, "barcode");
	addFormField(tape, form, "class");
	addFormField(tape, form, "freeBytes");
	addFormField(tape, form, "totalBytes");
	addFormField(tape, form, "usedBytes");
	addFormField(tape, form, "volumeId");

	return tape;
    }

    private static Map<String, Object> addFormField(Map<String, Object> object, Form form, String key)
    {
	object.put(key, form.field(key).value());
	return object;
    }
}
