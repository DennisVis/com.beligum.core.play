package security;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import models.User;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import utils.FlashHelper;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.core.models.Subject;

public class MyDeadboltHandler extends AbstractDeadboltHandler
{
    public Result beforeAuthCheck(Http.Context context)
    {
	// returning null means that everything is HTTP_OK. Return a real result
	// if
	// you want a redirect to a login page or
	// somewhere else

	Http.Context.current.set(context); // <-- this is needed to have a
					   // current Http.Context

	return null;
    }

    public Subject getSubject(Http.Context context)
    {
	// in a real application, the user name would probably be in the session
	// following a login process

	return User.getCurrentUser();
    }

    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
    {
	return new MyDynamicResourceHandler();
    }

    public Result onAuthFailure(Http.Context context, String content)
    {
	Http.Context.current.set(context); // <-- this is needed to have a
					   // current Http.Context
	Result retVal = null;
	// you can return any result from here - forbidden, etc
	FlashHelper.addError("Please log in first");
	try {
	    String onAuthFailureUrl = (String) play.Play.application().configuration().getString("beligum.core.onAuthFailureUrl");
	    if (onAuthFailureUrl != null) {
		retVal = redirect(onAuthFailureUrl);
	    } else {
		retVal = unauthorized(views.html.defaultpages.unauthorized.render());
	    }
	    // return
	    // redirect(controllers.routes.UserController.login(redirect));
	} catch (Exception e) {
	    Logger.error("UnsupportedEncodingException", e);
	    retVal = internalServerError();
	}
	return retVal;
    }
}