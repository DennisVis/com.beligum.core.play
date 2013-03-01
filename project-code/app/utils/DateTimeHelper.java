package utils;

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
