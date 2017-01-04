
package org.ibp.api.brapi.v1.security.auth;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.ibp.api.brapi.v1.security.auth.AuthenticationControllerBrapi;
import org.ibp.api.brapi.v1.security.auth.TokenRequest;
import org.ibp.api.brapi.v1.security.auth.TokenResponse;
import org.ibp.api.security.xauth.TokenProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public class AuthenticationControllerBrapiTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserDetailsService userDetailsService;

	@InjectMocks
	private final AuthenticationControllerBrapi controller = new AuthenticationControllerBrapi();

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		// Not mocking the token provider, instead use a real one as it is a simple collaborating component.
		this.controller.setTokenProvider(new TokenProvider("secretKey", 3600));
	}

	@After
	public void afterEachTest() {
		// so that other tests don't get side effects of this test.
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Test
	public void testAuthenticateSuccess() {
		final String testUser = "admin";
		final String testPassword = "password";
		final TokenRequest tokenRequest = new TokenRequest();
		tokenRequest.setUsername(testUser);
		tokenRequest.setPassword(testPassword);

		final Authentication credentials = new UsernamePasswordAuthenticationToken(testUser, testPassword);
		Mockito.when(this.authenticationManager.authenticate(org.mockito.Matchers.any(Authentication.class))).thenReturn(credentials);

		final User userDetails = new User(testUser, testPassword, Collections.<GrantedAuthority>emptyList());
		Mockito.when(this.userDetailsService.loadUserByUsername(testUser)).thenReturn(userDetails);

		final TokenResponse token = this.controller.authenticate(tokenRequest);
		Assert.assertNotNull("Expecting a non-null token for successful authentication scenario.", token);
		Assert.assertNotNull("Expecting a non-null token for successful authentication scenario.", token.getAccessToken());
		Assert.assertTrue(token.getAccessToken().startsWith(testUser + ":"));
		Assert.assertEquals(2, StringUtils.countMatches(token.getAccessToken(), ":"));
		// Just asserting that expiry is sometime in future.
		Assert.assertTrue(token.getExpiresIn() > System.currentTimeMillis());

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNotNull("Expected security context to have authenticated principal.", authentication);
		Assert.assertEquals(testUser, authentication.getName());
	}

	@Test(expected = BadCredentialsException.class)
	public void testAuthenticateFailure() {
		final String testUser = "admin";
		final String testPassword = "password";
		final TokenRequest tokenRequest = new TokenRequest();
		tokenRequest.setUsername(testUser);
		tokenRequest.setPassword(testPassword);

		Mockito.when(this.authenticationManager.authenticate(org.mockito.Matchers.any(Authentication.class)))
				.thenThrow(new BadCredentialsException("Authentication failure!"));

		this.controller.authenticate(tokenRequest);
	}

}
