package com.beligum.core.utils.pagers;

import java.util.Map;

import com.beligum.core.accounts.UrlResolver;
import com.beligum.core.models.User;

public class UserPager extends AbstractPagerImplementation<User>
{
    //-----CONSTANTS-----

    //-----VARIABLES-----

    //-----CONSTRUCTORS-----
    public UserPager(com.avaje.ebean.PagingList<User> pagingList, Integer page, Map<String, Object> query)
    {
	super(pagingList, page, query);
    }

    //-----PUBLIC FUNCTIONS-----
    

    //-----PROTECTED FUNCTIONS-----
    protected String getUrl(Integer page) {
	
   	return UrlResolver.getAccount().list(page).url();
      }

    //-----PRIVATE FUNCTIONS-----
}
