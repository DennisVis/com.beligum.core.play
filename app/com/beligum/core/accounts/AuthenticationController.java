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
package com.beligum.core.accounts;

import play.api.templates.Html;
import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import com.beligum.core.data.Login;
import com.beligum.core.models.User;
import com.beligum.core.repositories.BasicRepository;
import com.beligum.core.repositories.UserRepository;
import com.beligum.core.utils.FlashHelper;
import com.beligum.core.utils.pagers.UserPager;

public class AuthenticationController
{
    // -----CONSTANTS-----

    // -----VARIABLES-----

    // -----PUBLIC FUNCTIONS-----
    
    public static Html create() 
    {
	return com.beligum.core.views.html.account.partial.create.render();
    }

    public static Html edit(Long id)
    {
	User user = null;
	user = BasicRepository.find(User.class, id);
	return com.beligum.core.views.html.account.partial.edit.render(user);
    }

    public static Html list(Integer page)
    {
	Html retVal = null;
	UserPager userPager = null;
	if (page > 0) {
	    page -= 1;
	}
	try {
	    userPager = new UserPager(UserRepository.findPage("", 25), page, null);
	    retVal = com.beligum.core.views.html.account.partial.list.render(userPager);

	} catch (Exception e) {

	    retVal = com.beligum.core.views.html.account.partial.list.render(null);
	}

	return retVal;

    }

    public static Result save()
    {
	Result retVal = null;
	try {
	    User user = new User();
	    // You can only set a level when you own that level
	    Integer newRoleLevel = Integer.parseInt(Form.form().bindFromRequest().get("role"));
	    if (newRoleLevel >= User.getCurrentUser().getRole().getLevel()) {
		user.setRoleLevel(newRoleLevel);
	    }

	    user.setEmail(Form.form().bindFromRequest().get("email"));
	    user.setFirstName(Form.form().bindFromRequest().get("firstname"));
	    user.setLastName(Form.form().bindFromRequest().get("lastname"));
	    String password = Form.form().bindFromRequest().get("password1");
	    if (password.equals(Form.form().bindFromRequest().get("password2"))) {
		user.setNewPassword(password);
	    }
	    if (user.getEmail() != null && user.getFirstName() != null && user.getLastName() != null) {
		UserRepository.save(user);
		FlashHelper.addSuccess("User " + user.getEmail() + " was saved");
		retVal = Results.redirect(UrlResolver.getAccount().list(1));
	    } else {
		FlashHelper.addError("Could not save user because email or name is empty.");
		retVal = Results.redirect(UrlResolver.getAccount().create());
	    }

	} catch (Exception e) {
	    FlashHelper.addError("An internal error occured. Could not save user.");
	    retVal = Results.redirect(UrlResolver.getAccount().create());
	}
	return retVal;
    }

    public static Result update(Long id)
    {
	Result retVal = null;
	try {
	    User user = UserRepository.find(id);
	    // You can only set a level when you own that level
	    Integer newRoleLevel = Integer.parseInt(Form.form().bindFromRequest().get("role"));
	    if (newRoleLevel >= User.getCurrentUser().getRole().getLevel() &&
		User.getCurrentUser().getRole().getLevel() <= user.getRole().getLevel()) {
		user.setRoleLevel(newRoleLevel);
	    }
	    user.setEmail(Form.form().bindFromRequest().get("email"));
	    user.setFirstName(Form.form().bindFromRequest().get("firstname"));
	    user.setLastName(Form.form().bindFromRequest().get("lastname"));

	    String password = Form.form().bindFromRequest().get("password");
	    if (password != null && !password.trim().isEmpty()) {
		user.setNewPassword(password);
	    }

	    if (user.getEmail() != null && user.getFirstName() != null && user.getLastName() != null) {
		UserRepository.update(user);
		FlashHelper.addSuccess("User " + user.getEmail() + " was updated");
		retVal = Results.redirect(UrlResolver.getAccount().list(1));
	    } else {
		FlashHelper.addError("Could not update user because email or name is empty.");
		retVal = Results.redirect(UrlResolver.getAccount().create());
	    }

	} catch (Exception e) {
	    FlashHelper.addError("An internal error occured. Could not update user.");
	    retVal = Results.redirect(UrlResolver.getAccount().list(1));
	}
	return retVal;
    }
    
    public static Html login() {
	Form<Login> loginForm = Form.form(Login.class);
	
	return com.beligum.core.views.html.account.partial.login.render(loginForm);
    }
    
    public static Result logout()
    {
	Result retVal = null;
	Form<Login> loginForm = Form.form(Login.class);
	UserManager.logout();
	String referer = Http.Context.current().request().getHeader("referer");
	if (referer != null) {
	    retVal = Results.redirect(referer);
	} else {
	    retVal = Results.redirect("/");
	}
	return retVal;
    }

    public static Result authenticate()
    {
	Result retVal = null;
	try {
	    String email = Form.form().bindFromRequest().get("email");
	    String password = Form.form().bindFromRequest().get("password");

	    User user = UserRepository.findByEmail(email);
	    Form<Login> loginForm = Form.form(Login.class).bindFromRequest();

	    if (!loginForm.hasErrors()) {
		retVal = Results.redirect("/");
	    } else {
		FlashHelper.addError("Wrong password or username");
		retVal = Results.redirect(UrlResolver.getAccount().login());
	    }

	} catch (Exception e) {

	}
	return retVal;
    }

    // -----PROTECTED FUNCTIONS-----

    // -----PRIVATE FUNCTIONS-----
}
