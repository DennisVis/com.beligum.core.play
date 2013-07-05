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

import be.objectify.deadbolt.core.models.Role;

public class UserRole implements Role
{
    // -----VARIABLES-----
    private int level;
    private String name;
    private String description;

    // -----CONSTRUCTORS-----
    public UserRole(int level, String name, String description)
    {
	this.level = level;
	this.name = name;
	this.description = description;
    }

    // -----PUBLIC METHODS-----
    public int getLevel()
    {
	return this.level;
    }
    @Override
    public String getName()
    {
	return this.name;
    }
    public String getDescription()
    {
	return this.description;
    }

    // -----MANAGEMENT FUNCTIONS-----
    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	return result;
    }
    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof UserRole))
	    return false;
	UserRole other = (UserRole) obj;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	return true;
    }
}
