
package org.ibp.api.security.xauth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Hex;

public class TokenProvider {

	private final String secretKey;
	private final int tokenValidity;

	public TokenProvider(final String secretKey, final int tokenValidity) {
		this.secretKey = secretKey;
		this.tokenValidity = tokenValidity;
	}

	public Token createToken(final UserDetails userDetails) {
		final long expires = System.currentTimeMillis() + 1000L * this.tokenValidity;
		final String token = userDetails.getUsername() + ":" + expires + ":" + this.computeSignature(userDetails, expires);
		return new Token(token, expires);
	}

	public String computeSignature(final UserDetails userDetails, final long expires) {
		final StringBuilder signatureBuilder = new StringBuilder();
		signatureBuilder.append(userDetails.getUsername()).append(":");
		signatureBuilder.append(expires).append(":");
		signatureBuilder.append(userDetails.getPassword()).append(":");
		signatureBuilder.append(this.secretKey);

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available!");
		}
		return new String(Hex.encode(digest.digest(signatureBuilder.toString().getBytes())));
	}

	public String getUserNameFromToken(final String authToken) {
		if (null == authToken) {
			return null;
		}
		final String[] parts = authToken.split(":");
		return parts[0];
	}

	public boolean validateToken(final String authToken, final UserDetails userDetails) {
		final String[] parts = authToken.split(":");
		final long expires = Long.parseLong(parts[1]);
		final String signature = parts[2];
		final String signatureToMatch = this.computeSignature(userDetails, expires);
		return expires >= System.currentTimeMillis() && this.constantTimeEquals(signature, signatureToMatch);
	}

	/**
	 * String comparison that doesn't stop at the first character that is different but instead always iterates the whole string length to
	 * prevent timing attacks (http://codahale.com/a-lesson-in-timing-attacks/).
	 */
	private boolean constantTimeEquals(final String a, final String b) {
		if (a.length() != b.length()) {
			return false;
		} else {
			int equal = 0;
			for (int i = 0; i < a.length(); i++) {
				equal |= a.charAt(i) ^ b.charAt(i);
			}
			return equal == 0;
		}
	}

}
