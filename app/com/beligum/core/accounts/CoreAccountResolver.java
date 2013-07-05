package com.beligum.core.accounts;

import play.api.mvc.Call;

public class CoreAccountResolver
{
    //-----CONSTANTS-----

    //-----VARIABLES-----
    
    //-----CONSTRUCTORS-----
    public Call edit(Long id) {
	return com.beligum.core.controllers.users.routes.UserController.edit(id);
    }
    
    public Call list(Integer page) {
	return com.beligum.core.controllers.users.routes.UserController.list(page);
    }
    
    public Call create() {
	return com.beligum.core.controllers.users.routes.UserController.save();
    }
    
    public Call save() {
	return com.beligum.core.controllers.users.routes.UserController.save();
    }
    
    public Call update(Long id) {
	return com.beligum.core.controllers.users.routes.UserController.update(id);
    }
    
    public Call login() {
	return com.beligum.core.controllers.authentication.routes.AccountController.login();
    }
    
    public Call authenticate() {
   	return com.beligum.core.controllers.authentication.routes.AccountController.authenticate();
       }
    
    public Call logout() {
	return com.beligum.core.controllers.authentication.routes.AccountController.logout();
    }

    //-----PUBLIC FUNCTIONS-----

    //-----PROTECTED FUNCTIONS-----

    //-----PRIVATE FUNCTIONS-----
}
