
package org.ibp.api.java.impl.middleware.common;

public class CommonUtil {

	public static Integer tryParseSafe(String value) {
		if (value == null) {
			return null;
		}

		try {
			return Integer.valueOf(value);
		} catch (Exception ignored) {

		}
		return null;
	}
}
