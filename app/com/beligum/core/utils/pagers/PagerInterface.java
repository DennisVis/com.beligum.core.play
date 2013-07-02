package com.beligum.core.utils.pagers;

import java.util.List;

public interface PagerInterface<T>
{
    public List<T> getPage();

    public String createHtml();

}
