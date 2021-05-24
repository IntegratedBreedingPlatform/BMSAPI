
package org.ibp.api.security.xauth;

import org.ibp.api.java.impl.middleware.common.ContextResolutionException;
import org.ibp.api.security.BMSUser;
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
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;

public class XAuthTokenFilterTest {

	private static final Integer USER_ID = new Random().nextInt();

	@Mock
	private UserDetailsService userDetailsService;

	private final TokenProvider tokenProvider = new TokenProvider("bmsXAuthSecret", 3600);

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
		final String testUser = "admin";

		final User userDetails = new BMSUser(USER_ID, testUser, "password", Collections.<GrantedAuthority>emptyList());
		Mockito.when(this.userDetailsService.loadUserByUsername(testUser)).thenReturn(userDetails);

		// Generate a valid token
		final Token token = this.tokenProvider.createToken(userDetails);

		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final MockFilterChain filterChain = new MockFilterChain();

		// Add to x-auth-token header to the request
		request.addHeader(XAuthTokenFilter.XAUTH_TOKEN_HEADER_NAME, token.getToken());

		final XAuthTokenFilter filter = new XAuthTokenFilter(this.userDetailsService, this.tokenProvider);
		filter.doFilter(request, response, filterChain);

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNotNull("Expected security context to have authenticated principal.", authentication);
		Assert.assertEquals(testUser, authentication.getName());
	}

	@Test
	public void testDoFilterValidBrAPIRequest() throws IOException, ServletException {
		final String testUser = "admin";

		final User userDetails = new BMSUser(USER_ID, testUser, "password", Collections.emptyList());
		Mockito.when(this.userDetailsService.loadUserByUsername(testUser)).thenReturn(userDetails);

		// Generate a valid token
		final Token token = this.tokenProvider.createToken(userDetails);

		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final MockFilterChain filterChain = new MockFilterChain();

		request.addHeader(XAuthTokenFilter.OAUTH_TOKEN_HEADER_NAME, XAuthTokenFilter.OAUTH_TOKEN_PREFIX + token.getToken());

		final XAuthTokenFilter filter = new XAuthTokenFilter(this.userDetailsService, this.tokenProvider);
		filter.doFilter(request, response, filterChain);

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNotNull("Expected security context to have authenticated principal.", authentication);
		Assert.assertEquals(testUser, authentication.getName());
	}

	@Test
	public void testDoFilterInValidRequest() throws IOException, ServletException {
		final MockHttpServletRequest request = new MockHttpServletRequest(); // No token added to request
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final MockFilterChain filterChain = new MockFilterChain();

		final XAuthTokenFilter filter = new XAuthTokenFilter(this.userDetailsService, this.tokenProvider);
		filter.doFilter(request, response, filterChain);

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Assert.assertNull("Expected security context to not have set authenticated principal.", authentication);
	}

	@Test
	public void testInvalidCropAndProgramUUID() throws IOException, ServletException {
		final String testUser = "admin";
		final User userDetails = new User(testUser, "password", Collections.<GrantedAuthority>emptyList());
		// Generate a valid token
		final Token token = this.tokenProvider.createToken(userDetails);

		final String errorMessage = "error message";
		final ContextResolutionException contextResolutionException = new ContextResolutionException(errorMessage);
		Mockito.when(this.userDetailsService.loadUserByUsername(testUser)).thenThrow(contextResolutionException);

		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final MockFilterChain filterChain = new MockFilterChain();

		request.addHeader(XAuthTokenFilter.OAUTH_TOKEN_HEADER_NAME, XAuthTokenFilter.OAUTH_TOKEN_PREFIX + token.getToken());

		final XAuthTokenFilter filter = new XAuthTokenFilter(this.userDetailsService, this.tokenProvider);
		filter.doFilter(request, response, filterChain);

		Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
		Assert.assertEquals("application/json", response.getContentType());
		Assert.assertEquals("UTF-8", response.getCharacterEncoding());
		Assert.assertEquals("{\"errors\":[{\"fieldNames\":[],\"message\":\"error message\"}]}", response.getContentAsString());

	}

	@Test
	public void testDoFilterUnauthorizedForCrop() throws IOException, ServletException {
		final String testUser = "admin";
		final User userDetails = new User(testUser, "password", Collections.<GrantedAuthority>emptyList());
		// Generate a valid token
		final Token token = this.tokenProvider.createToken(userDetails);

		final String errorMessage = "Access Denied: User is not authorized for crop";
		final AuthenticationServiceException authenticationServiceException = new AuthenticationServiceException(errorMessage);
		Mockito.when(this.userDetailsService.loadUserByUsername(testUser)).thenThrow(authenticationServiceException);

		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		final MockFilterChain filterChain = new MockFilterChain();

		request.addHeader(XAuthTokenFilter.OAUTH_TOKEN_HEADER_NAME, XAuthTokenFilter.OAUTH_TOKEN_PREFIX + token.getToken());

		final XAuthTokenFilter filter = new XAuthTokenFilter(this.userDetailsService, this.tokenProvider);
		filter.doFilter(request, response, filterChain);

		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		Assert.assertEquals("application/json", response.getContentType());
		Assert.assertEquals("UTF-8", response.getCharacterEncoding());
		Assert.assertEquals("{\"errors\":[{\"fieldNames\":[],\"message\":\"Access Denied: User is not authorized for crop\"}]}", response.getContentAsString());

	}

}
