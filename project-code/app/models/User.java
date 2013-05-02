package models;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import security.UserRole;
import security.UserRoles;
import utils.Cacher;
import utils.Toolkit;

import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.core.models.Permission;

@Entity
@Table(name = "users")
public class User extends BasicModel implements Subject
{
    //-----CONSTANTS-----
    private static final long serialVersionUID = 1L;

    //-----VARIABLES-----
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
    
    
    //-----CACHE-----
    @Transient
    private List<UserRole> cachedRoles = null;
    
    
    //-----GETTERS/SETTERS-----
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
	this.password = ""; // BasicHelper.hash(password,
			    // utils.DateTimeHelper.formatDate(this.created_at,
			    // "dd/MM/yyyy"));
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
    
    @Override
    public List<? extends Role> getRoles()
    {
	if (this.cachedRoles==null) {
	    this.cachedRoles = UserRoles.forLevel(this.roleLevel);
	}
	
	return this.cachedRoles;
    }
    @Override
    public List<? extends Permission> getPermissions()
    {
	return Arrays.asList();
    }
    
    //-----FACTORY FUNCTIONS-----
    public static User getCurrentUser()
    {
	return (User) Cacher.fetchSessionObject(Cacher.CURRENT_USER);
    }
    
    //-----MANAGEMENT FUNCTIONS-----
    @Override
    public String toString()
    {
	return this.getClass().getSimpleName()+" [id=" + id + ", login=" + login + "]";
    }
}
