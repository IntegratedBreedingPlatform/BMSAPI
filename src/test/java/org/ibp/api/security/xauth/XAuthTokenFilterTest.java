
package org.ibp.api.security.xauth;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public class XAuthTokenFilterTest {

	@Mock
	private UserDetailsService userDetailsService;

	private TokenProvider tokenProvider = new TokenProvider("bmsXAuthSecret", 3600);

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void afterEachTest() {
		SecurityContextHolder.getContext().setAuthentication(null);
		// so that other tests dont get side effects of this test.
	}

	@Test
	public void testDoFilterValidRequest() throws IOException, ServletException {
		String testUser = "admin";

		User userDetails = new User(testUser, "password", Collections.<GrantedAuthority>emptyList());
		Mockito.when(this.userDetailsService.loadUserByUsername(testUser)).thenReturn(userDetails);

		// Generate a valid token
		Token token = this.tokenProvider.createToken(userDetails);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		// Add to x-auth-token header to the request
		request.addHeader(XAuthTokenFilter.XAUTH_TOKEN_HEADER_NAME, token.getToken());

		XAuthTokenFilter filter = new XAuthTokenFilter(this.userDetailsService, this.tokenProvider);
		filter.doFilter(request, response, filterChain);

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNotNull("Expected security context to have authenticated principal.", authentication);
		Assert.assertEquals(testUser, authentication.getName());
	}

	@Test
	public void testDoFilterValidBrAPIRequest() throws IOException, ServletException {
		String testUser = "admin";

		User userDetails = new User(testUser, "password", Collections.<GrantedAuthority>emptyList());
		Mockito.when(this.userDetailsService.loadUserByUsername(testUser)).thenReturn(userDetails);

		// Generate a valid token
		Token token = this.tokenProvider.createToken(userDetails);

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		request.setRequestURI("/brapi");
		request.addHeader(XAuthTokenFilter.AUTH_TOKEN_HEADER_NAME, XAuthTokenFilter.BEARER_PREFIX + token.getToken());

		XAuthTokenFilter filter = new XAuthTokenFilter(this.userDetailsService, this.tokenProvider);
		filter.doFilter(request, response, filterChain);

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNotNull("Expected security context to have authenticated principal.", authentication);
		Assert.assertEquals(testUser, authentication.getName());
	}

	@Test
	public void testDoFilterInValidRequest() throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest(); // No token added to request
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		XAuthTokenFilter filter = new XAuthTokenFilter(this.userDetailsService, this.tokenProvider);
		filter.doFilter(request, response, filterChain);

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNull("Expected security context to not have set authenticated principal.", authentication);
	}

}
