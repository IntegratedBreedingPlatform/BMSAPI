
package org.ibp.api;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.common.base.Strings;

public final class ISO8601DateParser {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	private ISO8601DateParser(){}

	public static Date parse(String input) throws ParseException {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat(ISO8601DateParser.DATE_FORMAT);
		df.setTimeZone(tz);
		return df.parse(input);
	}

	public static Date tryParse(String input) {
		if (Strings.isNullOrEmpty(input)) {
			return null;
		}
		try {
			return org.generationcp.middleware.util.ISO8601DateParser.parse(input);
		} catch (ParseException ignored) {
			return null;
		}
	}

	public static String toString(Date date) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat(ISO8601DateParser.DATE_FORMAT);
		df.setTimeZone(tz);
		return df.format(date);
	}
}
