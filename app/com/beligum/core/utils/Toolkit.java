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
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.security.spec.KeySpec;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;

import play.libs.Json;
import play.libs.WS;
import play.libs.WS.WSRequestHolder;

public class Toolkit
{
    // -----CONSTANTS-----
    private static final String VALID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz";

    // -----VARIABLES-----

    // -----CONSTRUCTORS-----
    public Toolkit()
    {
    }

    // -----PUBLIC FUNCTIONS-----
    public static String readWebpage(String url, boolean addNewlines) throws Exception
    {
	BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
	String line = reader.readLine();
	String retVal = line;
	while (line != null) {
	    line = reader.readLine();
	    if (line != null) {
		if (addNewlines) {
		    retVal += Toolkit.getNewline() + line;
		} else {
		    retVal += line;
		}
	    }
	}

	reader.close();

	return retVal;
    }
    public static String getNewline()
    {
	return System.getProperty("line.separator");
    }

    public static String urlize(String input)
    {
	String t = normalizeString(input).trim().toLowerCase().replaceAll(" ", "-").replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-")
					 .replaceAll("-$", "");
	if (t.length() > 59)
	    return t.substring(0, 58);
	else
	    return t;
    }

    public static String normalizeString(String input)
    {
	return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public static String xmlToString(Document document, boolean indent) throws Exception
    {
	if (document == null) {
	    return null;
	} else {
	    try {
		StringWriter sw = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		transformer.transform(new DOMSource(document), new StreamResult(sw));
		return sw.toString();
	    } catch (Exception e) {
		throw new Exception("Error converting XML to String", e);
	    }
	}
    }
    /*
     * Watch out: this is the same code as in cindi.Toolkit.js, so make sure you
     * change both if you change anything
     */
    public static String searchMetadataWithContextName(JsonNode metadataObject, String contextKey)
    {
	String retVal = null;

	if (metadataObject.get("class").asText().equals("metadata.list") || metadataObject.get("class").asText().equals("metadata.map")) {
	    Iterator<JsonNode> metaNodes = metadataObject.get("value").getElements();
	    while (metaNodes.hasNext()) {
		JsonNode childCollection = metaNodes.next();
		retVal = Toolkit.searchMetadataWithContextName(childCollection, contextKey);
		// break loop if contextName is found
		if (retVal != null)
		    break;
	    }
	} else if (metadataObject.has("context") &&
		   metadataObject.get("context").has("key") && metadataObject.get("context").get("key").asText().equals(contextKey)) {
	    if (!metadataObject.get("class").asText().equals("metadata.null")) {
		retVal = metadataObject.get("value").asText();
	    }
	}

	return retVal;
    }

    private static String cachedAllStatusesJS = null;

    public static String getJsonContentType()
    {
	return play.api.http.ContentTypeOf.contentTypeOf_JsValue(play.api.mvc.Codec.utf_8()).mimeType().get();
    }
    public static String getXmlContentType()
    {
	return play.api.http.ContentTypeOf.contentTypeOf_Xml(play.api.mvc.Codec.utf_8()).mimeType().get();
    }
    public static WSRequestHolder buildBackendUrl(String actionConfigKey)
    {
	return WS.url(Toolkit.buildBackendUrlString(actionConfigKey));
    }
    public static WSRequestHolder buildAcceptJsonBackendUrl(String actionConfigKey)
    {
	return WS.url(Toolkit.buildBackendUrlString(actionConfigKey)).setHeader("Accept", Toolkit.getJsonContentType());
    }
    public static WSRequestHolder buildAcceptXmlBackendUrl(String actionConfigKey)
    {
	return WS.url(Toolkit.buildBackendUrlString(actionConfigKey)).setHeader("Accept", Toolkit.getXmlContentType());
    }
    public static String buildBackendUrlString(String actionConfigKey)
    {
	StringBuilder url = new StringBuilder();
	url.append(play.Play.application().configuration().getString("cindi.fileServer"));

	String actionUrl = play.Play.application().configuration().getString(actionConfigKey);
	if (actionUrl.charAt(0) != '/' && url.charAt(url.length() - 1) != '/') {
	    url.append("/");
	}
	url.append(actionUrl);

	return url.toString();
    }
    public static String createRandomString(Integer length)
    {
	Random random = new Random();
	String name = "";
	for (int i = 0; i < length; i++) {
	    int pos = random.nextInt(VALID_CHARACTERS.length());
	    name += VALID_CHARACTERS.substring(pos, pos + 1);
	}
	return name;
    }
    public static String hash(String password, String salt)
    {
	KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 2048, 160);
	try {
	    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	    byte[] hash = f.generateSecret(spec).getEncoded();
	    return new String(Hex.encodeHex(hash));
	} catch (Exception e) {
	    return null;
	}

    }
    public static Boolean isEmpty(String string)
    {
	if (string == null || string.equals("") || string.trim().equals("")) {
	    return true;
	} else {
	    return false;
	}
    }
    public static String getNiceSize(long fileSize)
    {
	String retVal = "";

	double fileSizeD = fileSize;
	String fileSizeS;
	String fileSizeEx;

	if (fileSize < 1024) {
	    fileSizeS = fileSizeD + "";
	    fileSizeEx = "B";
	} else if (fileSize < (1024 * 1024)) {
	    fileSizeS = fileSizeD / 1024.0 + "";
	    fileSizeEx = "KB";
	} else if (fileSize < (1024 * 1024 * 1024)) {
	    fileSizeS = fileSizeD / (1024.0 * 1024.0) + "";
	    fileSizeEx = "MB";
	} else if (fileSize < Math.pow(1024, 4)) {
	    fileSizeS = fileSizeD / (1024.0 * 1024.0 * 1024.0) + "";
	    fileSizeEx = "GB";
	} else if (fileSize < Math.pow(1024, 5)) {
	    fileSizeS = fileSizeD / (1024.0 * 1024.0 * 1024.0 * 1024.0) + "";
	    fileSizeEx = "TB";
	} else if (fileSize < Math.pow(1024, 6)) {
	    fileSizeS = fileSizeD / (1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0) + "";
	    fileSizeEx = "PB";
	} else {
	    return "HUGE!";
	}

	int dotPos = fileSizeS.indexOf('.');
	String fileSizeS2 = dotPos == -1 ? fileSizeS : fileSizeS.substring(0, dotPos + 2);
	return fileSizeS2 + " " + fileSizeEx;
    }

    // -----PROTECTED FUNCTIONS-----

    // -----PRIVATE FUNCTIONS-----
}
