
package org.ibp.api.security.xauth;

/**
 * The security token.
 */
public class Token {

	private String token;
	private long expires;

	public Token(String token, long expires) {
		this.token = token;
		this.expires = expires;
	}

	public String getToken() {
		return this.token;
	}

	public long getExpires() {
		return this.expires;
	}
}
