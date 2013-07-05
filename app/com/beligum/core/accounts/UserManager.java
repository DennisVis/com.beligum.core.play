package com.beligum.core.accounts;

import java.security.spec.KeySpec;
import java.util.Calendar;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;



import org.apache.commons.codec.binary.Hex;




import play.Logger;
import play.mvc.Http;

import com.beligum.core.models.User;
import com.beligum.core.repositories.UserRepository;
import com.beligum.core.utils.DateTimeHelper;

public class UserManager
{
    // -----CONSTANTS-----
    private static String SESSION_KEY_USER = "userid";
    private static String SESSION_KEY_TIMEOUT = "login-timeout";

    // -----VARIABLES-----

    // -----CONSTRUCTORS-----

    // -----PUBLIC FUNCTIONS-----

    public static boolean authenticate(User user, String password)
    {
	boolean retVal = false;
	if (password != null && user != null && user.getPassword().equals(UserManager.getPasswordForString(user, password))) {
	    UserManager.storeCurrentUser(user);
	    retVal = true;
	}
	return retVal;
    }

    public static void logout()
    {
	if (Http.Context.current().session().get(SESSION_KEY_USER) != null) {
	    Http.Context.current().session().remove(SESSION_KEY_USER);
	} else {
	    Logger.error("User that was not logged in is trying to logout");
	}
    }

    public static String getPasswordForString(User user, String password)
    {
	String retVal = null;
	if (password != null) {
	    retVal = UserManager.hash(password, DateTimeHelper.formatDate(user.getCreatedAt(), "dd-MM-yyyy"));
	} else {
	    throw new RuntimeException("Password can not be null");
	}
	return retVal;
    }

    public static void storeCurrentUser(User user)
    {
	Http.Context.current().session().put(SESSION_KEY_USER, user.getId().toString());
	Calendar timeOut = DateTimeHelper.getCurrentTime();
	timeOut.add(Calendar.HOUR, 1);
	Http.Context.current().session().put(SESSION_KEY_TIMEOUT, DateTimeHelper.formatDate(timeOut, "dd/MM/yyyy HH:mm"));
    }

    public static User getCurrentUser()
    {
	User retVal = null;
	if (Http.Context.current().session().get(SESSION_KEY_USER) != null) {
	    Integer id;
	    try {
		id = Integer.parseInt(Http.Context.current().session().get(SESSION_KEY_USER));
	    } catch (Exception e) {
		return null;
	    }
	    retVal = UserRepository.find(id);
	    if (retVal != null) {
		String currentTimeOut = Http.Context.current().session().get(SESSION_KEY_TIMEOUT);
		if (currentTimeOut == null) {
		    retVal = null;
		} else if (DateTimeHelper.getCurrentTime().before(DateTimeHelper.formatStringToDate(currentTimeOut, "dd/MM/yyyy HH:mm"))) {
		    Calendar timeOut = DateTimeHelper.getCurrentTime();
		    timeOut.add(Calendar.HOUR, 1);
		    Http.Context.current().session().put(SESSION_KEY_TIMEOUT, DateTimeHelper.formatDate(timeOut, "dd/MM/yyyy HH:mm"));
		} else {
		    retVal = null;
		}
	    }

	}
	return retVal;

    }

    // -----PROTECTED FUNCTIONS-----

    // -----PRIVATE FUNCTIONS-----

    private static String hash(String password, String salt)
    {
	KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 2048, 160);
	try {
	    SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	    byte[] hash = f.generateSecret(spec).getEncoded();
	    return new String(Hex.encodeHex(hash));
	} catch (Exception e) {
	    return null;
	}

    }
}
