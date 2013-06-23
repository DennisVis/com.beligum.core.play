package com.beligum.core.utils;

import play.mvc.Http;
import play.mvc.Http.Flash;

public class FlashHelper
{

    public static void addMessage(String message)
    {
	Flash flash = Http.Context.current().flash();

	StringBuilder s = new StringBuilder();
	String existing = "";
	if (flash.get("message") != null) {
	    existing = flash.get("message");
	}
	s.append(existing);
	String newText = "<p><strong>Warning</strong> - " + message + "</p>";
	if (s.indexOf(newText) < 0) {
	    s.append(newText);
	}
	flash.put("message", s.toString());
    }

    public static void addError(String message)
    {
	Flash flash = Http.Context.current().flash();

	StringBuilder s = new StringBuilder();
	String existing = "";
	if (flash.get("error") != null) {
	    existing = flash.get("error");
	}
	s.append(existing);
	String newText = "<p><strong>Error</strong> - " + message + "</p>";
	if (s.indexOf(newText) < 0) {
	    s.append(newText);
	}
	flash.put("error", s.toString());
    }

    public static void addSuccess(String message)
    {
	Flash flash = Http.Context.current().flash();

	StringBuilder s = new StringBuilder();
	String existing = "";
	if (flash.get("success") != null) {
	    existing = flash.get("success");
	}
	s.append(existing);
	String newText = "<p><strong>Info</strong> - " + message + "</p>";
	if (s.indexOf(newText) < 0) {
	    s.append(newText);
	}
	flash.put("success", s.toString());
    }

}
