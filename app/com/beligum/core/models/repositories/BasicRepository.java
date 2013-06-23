package com.beligum.core.models.repositories;

import java.util.List;

import javax.persistence.PersistenceException;

import play.Logger;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.PagingList;
import com.beligum.core.pagers.PagerInterface;

public class BasicRepository
{
    // -----CONSTANTS-----

    // -----VARIABLES-----

    // -----PUBLIC FUNCTIONS-----

    public static <T> T save(T model) throws PersistenceException
    {
	try {
	    Ebean.save(model);

	    return model;
	} catch (Exception e) {
	    Logger.error("Caught error while saving a page", e);
	    throw new PersistenceException(e);
	}
    }

    public static <T> T update(T model) throws PersistenceException
    {
	try {
	    Ebean.update(model);

	    return model;
	} catch (Exception e) {
	    Logger.error("Caught error while updating a page", e);
	    throw new PersistenceException(e);
	}
    }

    public static <T> void delete(T model) throws PersistenceException
    {
	try {
	    Ebean.delete(model);

	} catch (Exception e) {
	    Logger.error("Caught error while deleting a page", e);
	    throw new PersistenceException(e);
	}
    }

    public static <T> T find(Class<T> t, long id) throws PersistenceException
    {
	try {
	    return Ebean.find(t, id);
	} catch (Exception e) {
	    Logger.error("Caught error while searching a page", e);
	    throw new PersistenceException(e);
	}
    }

    public static <T> List<T> findAll(Class<T> t) throws PersistenceException
    {
	try {
	    return Ebean.find(t).findList();
	} catch (Exception e) {
	    Logger.error("Caught error while searching a page", e);
	    throw new PersistenceException(e);
	}
    }

    // public static <T> pagers.AbstractPagerImplementation<T> findPage(Class<T>
    // t, int page, int pageSize) {
    // try {
    // PagingList<T> list = Ebean.find(t).findPagingList(pageSize);
    // PagerInterface<T> abstractPager = new pagerInterface<T>(list, page);
    // return pagerInterface;
    // } catch (Exception e) {
    // Logger.error("Caught error while searching for a page of pages", e);
    // throw new PersistenceException(e);
    // }
    // }

    // -----PROTECTED FUNCTIONS-----

    // -----PRIVATE FUNCTIONS-----
}
