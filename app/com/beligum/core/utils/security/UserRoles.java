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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Ebean;

public class UserRoles
{
    public static final String ROOT = "root";
    public static final String ADMIN = "admin";
    public static final String BACKUP = "backup";
    public static final String USER = "user";
    public static final String GUEST = "guest";

    public static final UserRole ROOT_ROLE = new UserRole(0, ROOT, "Root user");
    public static final UserRole ADMIN_ROLE = new UserRole(10, ADMIN, "Administrator");
    public static final UserRole BACKUP_ROLE = new UserRole(20, BACKUP, "Backup manager");
    public static final UserRole USER_ROLE = new UserRole(100, USER, "User");
    public static final UserRole GUEST_ROLE = new UserRole(1000, GUEST, "Guest");

    // Note: order this from high (a lot of permissions) to low (no permissions
    // at all)
    private static List<UserRole> ALL_ROLES_CACHED = Arrays.asList(ROOT_ROLE, ADMIN_ROLE, BACKUP_ROLE, USER_ROLE, GUEST_ROLE);

    private static Map<String, UserRole> cachedRoleNameMap = null;

    public static List<UserRole> forLevel(int level)
    {
	List<UserRole> retVal = new ArrayList<UserRole>();

	for (UserRole r : UserRoles.ALL_ROLES_CACHED) {
	    if (r.getLevel() >= level) {
		retVal.add(r);
	    }
	}

	return retVal;
    }

    public static UserRole forName(String name)
    {
	return UserRoles.getRoleNameMap().get(name);
    }

    public static List<UserRole> getAllRoles()
    {
	if (ALL_ROLES_CACHED == null) {
	    ALL_ROLES_CACHED = Arrays.asList(ROOT_ROLE, ADMIN_ROLE, BACKUP_ROLE, USER_ROLE, GUEST_ROLE);
	}

	return ALL_ROLES_CACHED;
    }

    private static Map<String, UserRole> getRoleNameMap()
    {
	if (UserRoles.cachedRoleNameMap == null) {
	    UserRoles.cachedRoleNameMap = new HashMap<String, UserRole>();
	    for (UserRole r : UserRoles.ALL_ROLES_CACHED) {
		UserRoles.cachedRoleNameMap.put(r.getName(), r);
	    }
	}

	return UserRoles.cachedRoleNameMap;
    }

}
