/*******************************************************************************
 * Copyright (c) 2013 by Beligum b.v.b.a. (http://www.beligum.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * Contributors:
 *     Beligum - initial implementation
 *******************************************************************************/
package com.beligum.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeHelper
{
    public static String formatDate(Calendar date, String format)
    {
	String strdate = null;

	SimpleDateFormat sdf = new SimpleDateFormat(format);

	if (date != null) {
	    strdate = sdf.format(date.getTime());
	}
	return strdate;
    }

    public static Calendar formatStringToDate(String str_date, String format)
    {
	try {
	    SimpleDateFormat formatter;
	    Date date;
	    Calendar newDate = Calendar.getInstance();
	    formatter = new SimpleDateFormat(format);
	    date = ((Date) formatter.parse(str_date));
	    newDate.setTime(date);
	    return newDate;

	} catch (Exception e) {
	    return null;
	}

    }

    public static Calendar getCurrentTime()
    {
	return Calendar.getInstance(TimeZone.getTimeZone("Europe/Brussels"));
    }
}
