package com.beligum.core.controllers.authentication;

import play.api.templates.Html;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import com.beligum.core.accounts.AuthenticationController;
import com.beligum.core.models.User;
import com.beligum.core.repositories.UserRepository;

public class AccountController extends Controller
{
    // -----CONSTANTS-----

    // -----VARIABLES-----

    // -----CONSTRUCTORS-----

    // -----PUBLIC FUNCTIONS-----

    public static Result login()
    {
	if (UserRepository.UserCount() > 0) {
	    return ok(com.beligum.core.views.html.account.template.render(AuthenticationController.login()));
	} else {
	    Html adminPassword = com.beligum.core.views.html.account.partial.adminpassword.render();
	    return ok(com.beligum.core.views.html.account.template.render(adminPassword));
	}
    }

    public static Result logout()
    {
	return AuthenticationController.logout();
    }

    public static Result authenticate()
    {
	return AuthenticationController.authenticate();
    }
    
    public static Result createAdminPassword()
    {
	if (UserRepository.UserCount() == 0) {
	    String password = Form.form().bindFromRequest().get("password");
	    User adminUser = new User();
	    adminUser.setEmail("admin");
	    adminUser.setNewPassword(password);
	    adminUser.setFirstName("admin");
	    adminUser.setRoleLevel(0);
	    UserRepository.save(adminUser);
	    
	}
	return login();
    }

    // -----PROTECTED FUNCTIONS-----

    // -----PRIVATE FUNCTIONS-----
}
