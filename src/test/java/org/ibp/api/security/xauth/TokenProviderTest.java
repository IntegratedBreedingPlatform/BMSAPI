package org.ibp.api.security.xauth;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class TokenProviderTest {

	@Test
	public void testCreateAndValidateToken() {

		TokenProvider tokenProvider = new TokenProvider("bmsXAuthSecret", 3600);

		final User userDetails = new User("admin", "password", Collections.<GrantedAuthority>emptyList());
		final Token token = tokenProvider.createToken(userDetails);
		Assert.assertNotNull(token);
		Assert.assertTrue(token.getToken().startsWith(userDetails.getUsername() + ":"));
		Assert.assertEquals(2, StringUtils.countMatches(token.getToken(), ":"));
		// Just asserting that expiry is sometime in future.
		Assert.assertTrue(token.getExpires() > System.currentTimeMillis());
		Assert.assertTrue(tokenProvider.validateToken(token.getToken(), userDetails));

		// Try to hack the token: change username
		final User hackerUserDetails = new User("hacker", "password", Collections.<GrantedAuthority>emptyList());
		String hackedToken = token.getToken().replace("admin", "hacker");
		Assert.assertFalse(tokenProvider.validateToken(hackedToken, hackerUserDetails));

		// Try to hack the token: extend expiry, same user name
		String[] tokenParts = StringUtils.split(token.getToken(), ":");
		long tokenExpiry = Long.valueOf(tokenParts[1]);
		long extendedExpiry = tokenExpiry + 1000 * 3600;
		String extendedExpiryToken = tokenParts[0] + ":" + String.valueOf(extendedExpiry) + ":" + tokenParts[2];
		Assert.assertFalse(tokenProvider.validateToken(extendedExpiryToken, userDetails));
	}

	@Test
	public void testGetUserNameFromToken() {
		String token = "admin:1448234197907:2dd123e11b5d397276600ac2472ed8b1";
		TokenProvider tokenProvider = new TokenProvider("bmsXAuthSecret", 3600);
		Assert.assertEquals("admin", tokenProvider.getUserNameFromToken(token));
	}

}
