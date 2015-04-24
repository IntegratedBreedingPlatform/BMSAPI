package org.ibp;

import java.util.Random;


public final class ApiTestUtilities {

	/**
	 * Utility Method to generate random string of given length
	 *
	 * @param len length of random string
	 * @return String generated string
	 */
	public static String randomString(int len) {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(chars.charAt(rnd.nextInt(chars.length())));
		}
		return sb.toString();
	}
}
