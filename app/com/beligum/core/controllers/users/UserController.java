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
package com.beligum.core.controllers.users;

import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import be.objectify.deadbolt.java.actions.Dynamic;

import com.beligum.core.accounts.AuthenticationController;
import com.beligum.core.accounts.UrlResolver;
import com.beligum.core.models.User;
import com.beligum.core.repositories.BasicRepository;
import com.beligum.core.repositories.UserRepository;
import com.beligum.core.utils.FlashHelper;
import com.beligum.core.utils.pagers.UserPager;
import com.beligum.core.utils.security.UserRoles;

@Dynamic(UserRoles.ROOT)
public class UserController extends Controller
{
    // -----CONSTANTS-----

    // -----VARIABLES-----

    // -----CONSTRUCTORS-----

    // -----PUBLIC FUNCTIONS-----

    public static Result create()
    {
	return ok(com.beligum.core.views.html.account.template.render(AuthenticationController.create()));
    }

    public static Result edit(Long id)
    {
	Result retVal = null;
	User user = null;
	try {
	    user = BasicRepository.find(User.class, id);
	    retVal = ok(com.beligum.core.views.html.account.template.render(AuthenticationController.edit(id)));
	} catch (Exception e) {
	    FlashHelper.addError("Could not find this user");
	    Logger.error("User not found with id: " + id);
	    retVal = redirect(UrlResolver.getAccount().list(1));
	}

	return retVal;
    }

    

    public static Result list(Integer page)
    {
	Result retVal = ok(com.beligum.core.views.html.account.template.render(AuthenticationController.list(page)));

	return retVal;

    }

    public static Result save()
    {
	Result retVal = AuthenticationController.save();

	return retVal;
    }

    public static Result update(Long id)
    {
	Result retVal = AuthenticationController.update(id);

	return retVal;
    }

    // -----PROTECTED FUNCTIONS-----

    // -----PRIVATE FUNCTIONS-----
}
