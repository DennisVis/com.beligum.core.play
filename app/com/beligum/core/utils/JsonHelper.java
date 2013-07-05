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
