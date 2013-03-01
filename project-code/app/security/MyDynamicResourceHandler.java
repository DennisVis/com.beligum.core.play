package security;

import models.User;
import play.Logger;
import play.mvc.Http;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Subject;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MyDynamicResourceHandler implements DynamicResourceHandler
{
    public boolean isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context)
    {
	boolean retVal = false;
	
	Subject roleHolder = deadboltHandler.getSubject(context);
	
	if (roleHolder != null && User.class.isAssignableFrom(roleHolder.getClass())) {
	    retVal = ((User)roleHolder).getRoles().contains(UserRoles.forName(name));
	}
	else if (roleHolder!=null) {
	    Logger.warn("Encountered unknown role holder entity: "+roleHolder);
	}

	return retVal;
    }

    public boolean checkPermission(String permissionValue, DeadboltHandler deadboltHandler, Http.Context context)
    {
	Logger.error("Requested a permission check for '"+permissionValue+"', but permissions are not (yet) implemented, only roles");
	return isAllowed(permissionValue, null, deadboltHandler, context);
    }
}