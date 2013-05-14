package models.repositories;

import java.util.List;

import javax.persistence.PersistenceException;

import models.User;
import play.Logger;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagingList;

public class UserRepository
{

    public static User save(User user) throws PersistenceException
    {
	try {
	    Ebean.save(user);
	    return user;
	} catch (Exception e) {
	    Logger.error("Caught error while saving a user", e);
	    throw new PersistenceException(e);
	}
    }

    public static User update(User user) throws PersistenceException
    {
	try {
	    Ebean.update(user);
	    return user;
	} catch (Exception e) {
	    Logger.error("Caught error while updating a user", e);
	    throw new PersistenceException(e);
	}
    }

    public static void delete(User user) throws PersistenceException
    {
	try {
	    Ebean.delete(user);
	} catch (Exception e) {
	    Logger.error("Caught error while deleting a user", e);
	    throw new PersistenceException(e);
	}
    }

    public static PagingList<User> findPage(String search, Integer size) throws PersistenceException
    {
	try {
	    search = "%" + search + "%";
	    return Ebean.find(User.class).where().ilike("email", search).findPagingList(size);
	} catch (Exception e) {
	    Logger.error("Caught error while searching a user by page", e);
	    throw new PersistenceException(e);
	}
    }

    public static User find(long id) throws PersistenceException
    {
	try {
	    return Ebean.find(User.class, id);
	} catch (Exception e) {
	    Logger.error("Caught error while searching a user", e);
	    throw new PersistenceException(e);
	}
    }

    public static User findByEmail(String email) throws PersistenceException
    {
	try {
	    return Ebean.find(User.class).where().eq("email", email).findUnique();
	}
	catch (Exception e) {
	    Logger.error("Caught error while searching a user by login", e);
	    throw new PersistenceException(e);
	}
    }

    public static List<User> findAll() throws PersistenceException
    {
	try {
	    return Ebean.find(User.class).findList();
	} catch (Exception e) {
	    Logger.error("Caught error while searching all users", e);
	    throw new PersistenceException(e);
	}
    }

    public static void refreshUser(User user) throws PersistenceException
    {
	try {
	    Ebean.refresh(user);
	} catch (Exception e) {
	    Logger.error("Caught error while refreshing a user", e);
	    throw new PersistenceException(e);
	}
    }
    
}