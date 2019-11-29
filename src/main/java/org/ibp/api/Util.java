package org.ibp.api;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

public class Util {

	public static boolean isNullOrEmpty(Object value) {
		return value instanceof String && Strings.isNullOrEmpty(((String) value).trim()) || value == null || value instanceof Collection
				&& ((Collection) value).isEmpty() || value instanceof Map && ((Map) value).isEmpty();
	}

	public static boolean isPositiveInteger(final String str) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		return str.chars().allMatch(Character::isDigit);
	}
}
