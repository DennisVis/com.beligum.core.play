package security;

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
