package org.ibp.api;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Strings;

public class Util {

	public static boolean isNullOrEmpty(Object value) {
		return value instanceof String && Strings.isNullOrEmpty(((String) value).trim()) || value == null || value instanceof Collection
				&& ((Collection) value).isEmpty() || value instanceof Map && ((Map) value).isEmpty();
	}
}
