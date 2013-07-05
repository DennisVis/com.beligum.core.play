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
package com.beligum.core.models;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;




import com.beligum.core.accounts.UserManager;
import com.beligum.core.utils.Cacher;
import com.beligum.core.utils.DateTimeHelper;
import com.beligum.core.utils.Toolkit;
import com.beligum.core.utils.security.UserRole;
import com.beligum.core.utils.security.UserRoles;

import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.core.models.Permission;

@Entity
@Table(name = "users")
public class User extends BasicModel implements Subject
{
    // -----CONSTANTS-----
    private static final long serialVersionUID = 1L;
    private static User CURRENT_USER_KEY = new User(-1l);

    // -----VARIABLES-----
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String login;

    private String password;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    private String email;

    private Integer roleLevel;

    // -----CACHE-----
    @Transient
    private List<UserRole> cachedRoles = null;

    // -----CONSTRUCTORS-----
    public User()
    {
	super();
    }
    public User(Long id)
    {
	this();

	this.id = id;
    }

    // -----GETTERS/SETTERS-----
    public String getLogin()
    {
	return login;
    }
    public void setLogin(String login)
    {
	this.login = login;
    }
    public Long getId()
    {
	return id;
    }
    public void setId(Long id)
    {
	this.id = id;
    }
    public String getPassword()
    {
	return password;
    }
    public void setNewPassword(String password)
    {
	if (createdAt == null) {
	    createdAt = DateTimeHelper.getCurrentTime();
	}
	this.password = UserManager.getPasswordForString(this, password);
    }
    public String getFirstName()
    {
	if (this.firstName == null)
	    return "";
	return firstName;
    }
    public void setFirstName(String firstName)
    {
	this.firstName = firstName;
    }
    public String getLastName()
    {
	if (this.lastName == null)
	    return "";
	return lastName;
    }
    public void setLastName(String lastName)
    {
	this.lastName = lastName;
    }
    public String getEmail()
    {
	return email;
    }
    public void setEmail(String email)
    {
	this.email = email;
    }

    @Override
    public String getIdentifier()
    {
	return email;
    }

    public String getFullName()
    {
	String full = "";
	if (!Toolkit.isEmpty(this.lastName) || !Toolkit.isEmpty(this.firstName)) {
	    full += (this.getFirstName().trim() + " " + this.getLastName().trim()).trim();
	}
	return full;
    }

    public void setRoleLevel(Integer level)
    {
	this.roleLevel = level;
	this.cachedRoles = null;
    }

    @Override
    public List<? extends Role> getRoles()
    {
	if (this.roleLevel == null) {
	    this.roleLevel = UserRoles.GUEST_ROLE.getLevel();
	}
	if (this.cachedRoles == null) {
	    this.cachedRoles = UserRoles.forLevel(this.roleLevel);
	}

	return this.cachedRoles;
    }

    public UserRole getRole()
    {
	return (UserRole) this.getRoles().get(0);
    }

    @Override
    public List<? extends Permission> getPermissions()
    {
	return Arrays.asList();
    }

    // -----FACTORY FUNCTIONS-----
    public static User getCurrentUser()
    {
	return UserManager.getCurrentUser();
    }

    // -----MANAGEMENT FUNCTIONS-----
    @Override
    public String toString()
    {
	return this.getClass().getSimpleName() + " [id=" + id + ", login=" + login + "]";
    }
}
