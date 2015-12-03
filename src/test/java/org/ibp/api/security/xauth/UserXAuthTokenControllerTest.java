
package org.ibp.api.security.xauth;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
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

public class UserXAuthTokenControllerTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserDetailsService userDetailsService;

	@InjectMocks
	private UserXAuthTokenController controller = new UserXAuthTokenController();

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		// Not mocking the token provider, instead use a real one as it is a simple collaborating component.
		this.controller.setTokenProvider(new TokenProvider("secretKey", 3600));
	}

	@After
	public void afterEachTest() {
		SecurityContextHolder.getContext().setAuthentication(null);
		// so that other tests dont get side effects of this test.
	}

	@Test
	public void testAuthenticateSuccess() {
		String testUser = "admin";
		String testPassword = "password";
		Authentication credentials = new UsernamePasswordAuthenticationToken(testUser, testPassword);
		Mockito.when(this.authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(credentials);

		User userDetails = new User(testUser, testPassword, Collections.<GrantedAuthority>emptyList());
		Mockito.when(this.userDetailsService.loadUserByUsername(testUser)).thenReturn(userDetails);

		Token token = this.controller.authenticate(testUser, testPassword);
		Assert.assertNotNull("Expceting a non-null token for successful authetication scenario.", token);
		Assert.assertNotNull("Expceting a non-null token for successful authetication scenario.", token.getToken());
		Assert.assertTrue(token.getToken().startsWith(testUser + ":"));
		Assert.assertEquals(2, StringUtils.countMatches(token.getToken(), ":"));
		// Just asserting that expiry is sometime in future.
		Assert.assertTrue(token.getExpires() > System.currentTimeMillis());

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNotNull("Expected security context to have authenticated principal.", authentication);
		Assert.assertEquals(testUser, authentication.getName());
	}

	@Test(expected = BadCredentialsException.class)
	public void testAuthenticateFailure() {
		String testUser = "admin";
		String testPassword = "password";

		Mockito.when(this.authenticationManager.authenticate(Mockito.any(Authentication.class))).thenThrow(
				new BadCredentialsException("Authentication failure!"));

		this.controller.authenticate(testUser, testPassword);
	}

}
