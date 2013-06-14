package com.beligum.core.pagers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;

// Used to make a Bootstrap pagination object in html
// You can use this in your view to show the pagination
// and to access information and objects about the pages / the selected page

public abstract class AbstractPagerImplementation<T> implements PagerInterface
{

    private Integer page;
    private Integer totalRows;
    private Integer totalPages;
    private Integer pageSize;
    private List<Map<String, Object>> items;
    private com.avaje.ebean.Page<T> dbPage;
    protected Map<String, Object> query;

    // ------COINSTRUCTOR----------
    public AbstractPagerImplementation(com.avaje.ebean.PagingList<T> pagingList, Integer page, Map<String, Object> query)
    {
	this.query = query;
	this.page = page;
	this.pageSize = pagingList.getPageSize();
	this.totalRows = pagingList.getTotalRowCount();
	this.totalPages = pagingList.getTotalPageCount();
	this.dbPage = pagingList.getPage(page);
	Logger.debug(pagingList.toString());
	// Save all info and create a list of pages
	this.createList();
    }

    public List<Map<String, Object>> getItems()
    {
	return items;
    }

    public Integer getPageSize()
    {
	return pageSize;
    }

    // Get the objects inside the selected page
    public List<T> getPage()
    {
	return dbPage.getList();
    }

    // We save the pages you can go to in a hashmap.
    // We use this hashmap to render the pager in html
    // A pager looks like this:
    // << < 1 2 3 4 5 6 7 8 9 > >>
    //
    // Each element is saved in the hashmap
    private void createList()
    {

	items = new ArrayList<Map<String, Object>>();
	if (totalRows == 0) {
	    totalRows = 1;
	}
	// Set start and end of the pager.
	// Important if there are more then MAX_PAGE pages
	// We only show MAX_PAGES. eg if MAX_PAGES is 10 and we are on page 53
	// then show page 49 to 58 so the current page is in the middle
	Integer start = 1;
	Integer einde = totalPages;
	if (totalPages > 8) {
	    if (page > 4) {
		start = page - 3;
		einde = page + 4;
	    } else {
		einde = 8;
	    }
	    if (einde >= totalPages) {
		start = start - (einde - totalPages);
		einde = totalPages;
	    }
	}

	// Every item has 3 keys:
	// caption: the caption to show
	// referencePage: the page the item is referencing
	// isCurrentpage: if the pag is the selected page

	// Go back to first page
	Map<String, Object> item = new HashMap<String, Object>();
	item.put("caption", "<<");
	item.put("referencePage", 1);
	item.put("isCurrentpage", false);
	items.add(item);

	// Go back to previous page
	item = new HashMap<String, Object>();
	item.put("caption", "<");
	item.put("isCurrentpage", false);
	if (page > 1) {
	    item.put("referencePage", page - 1);
	} else {
	    item.put("referencePage", 1);
	}
	items.add(item);

	// All the numbered pages
	for (Integer i = start; i <= einde; i++) {
	    item = new HashMap<String, Object>();
	    item.put("caption", i.toString());
	    item.put("referencePage", i);
	    if (i.equals(page)) {
		item.put("isCurrentpage", true);
	    } else {
		item.put("isCurrentpage", false);
	    }
	    items.add(item);
	}

	// Go to next page
	item = new HashMap<String, Object>();
	item.put("caption", ">");
	item.put("isCurrentpage", false);
	if (page < totalPages) {
	    item.put("referencePage", page + 1);
	} else {
	    item.put("referencePage", totalPages);
	}
	items.add(item);

	// Go to last page
	item = new HashMap<String, Object>();
	item.put("caption", ">>");
	item.put("isCurrentpage", false);
	item.put("referencePage", totalPages);
	items.add(item);

    }

    // Render the complete pager
    public String createHtml()
    {
	StringBuilder pager = new StringBuilder();
	pager.append("<div class='pagination'>");
	pager.append("<ul>");
	for (Map<String, Object> li : this.items) {
	    pager.append(this.renderLI(li));
	}
	pager.append("</ul>");
	pager.append("</div>");
	return pager.toString();
    }

    // Render one item of the pager
    private String renderLI(Map<String, Object> li)
    {
	StringBuilder page = new StringBuilder();
	page.append("<li");
	if ((Boolean) li.get("isCurrentpage")) {
	    page.append(" class='selectedPage'>");
	    page.append("<a href =''>" + li.get("caption") + "</a>");
	    page.append("</li>");
	} else {
	    page.append(">"); // End of li element
	    page.append("<a href ='" + getUrl((Integer) li.get("referencePage")) + "'>" + li.get("caption") + "</a>");
	    page.append("</li>");
	}

	return page.toString();
    }

    protected abstract String getUrl(Integer page);

}
