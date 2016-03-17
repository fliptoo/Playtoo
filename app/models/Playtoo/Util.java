package models.Playtoo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

	public final static DateFormat DF = new SimpleDateFormat("MM/dd/yyyy");

	public static int length(int length) {
		if (length == 0)
			length = 10;
		return length;
	}

	public static Date parseDate(String date) throws ParseException {
		return DF.parse(date);
	}

}
