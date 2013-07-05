package com.beligum.core.accounts;


public class UrlResolver
{
    //-----CONSTANTS-----

    //-----VARIABLES-----
    private static CoreAccountResolver account = null;


    //-----PUBLIC FUNCTIONS-----
    public static CoreAccountResolver getAccount()
    {
	if (account == null) {
	    account = new CoreAccountResolver();
	}
	return account;
    }
    
    public static void setAccount(CoreAccountResolver account)
    {
	UrlResolver.account = account;
    }

    //-----PROTECTED FUNCTIONS-----

    //-----PRIVATE FUNCTIONS-----
}
