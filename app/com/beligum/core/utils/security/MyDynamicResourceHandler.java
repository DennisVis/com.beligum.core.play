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
package com.beligum.core.utils.security;

import java.util.List;

import com.beligum.core.models.User;






import play.Logger;
import play.mvc.Http;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

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
	    List<? extends Role> roles = ((User) roleHolder).getRoles();
	    UserRole role = UserRoles.forName(name);

	    retVal = ((User) roleHolder).getRoles().contains(UserRoles.forName(name));
	} else if (roleHolder != null) {
	    Logger.warn("Encountered unknown role holder entity: " + roleHolder);
	}

	return retVal;
    }

    public boolean checkPermission(String permissionValue, DeadboltHandler deadboltHandler, Http.Context context)
    {
	Logger.error("Requested a permission check for '" + permissionValue + "', but permissions are not (yet) implemented, only roles");
	return isAllowed(permissionValue, null, deadboltHandler, context);
    }
}