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
