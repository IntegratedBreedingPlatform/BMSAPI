
package org.ibp.api.security.xauth;

public class Token {

	private String token;
	private long expires;

	public Token() {
	}

	public Token(final String token, final long expires) {
		this.token = token;
		this.expires = expires;
	}

	public String getToken() {
		return this.token;
	}

	public long getExpires() {
		return this.expires;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}

}
