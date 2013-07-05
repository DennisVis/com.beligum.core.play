package com.beligum.core.data;




import play.data.validation.Constraints.Required;

import com.beligum.core.accounts.UserManager;
import com.beligum.core.models.User;
import com.beligum.core.repositories.UserRepository;
import com.beligum.core.utils.FlashHelper;

public class Login
{

    @Required
    public String email;
    public String password;

    public String validate()
    {
	User user = UserRepository.findByEmail(email);
	String retVal = "Login failed";
	if (user != null) {
	    if (UserManager.authenticate(user, password)) {
		FlashHelper.addSuccess("Welcome " + user.getEmail() + ". Your login was successfull.");
		retVal = null;
	    } else {
		retVal = "Password incorrect. Please try again.";
	    }
	} else {
	    retVal = "Email not found. Please try again";
	}
	return retVal;
    }

}
