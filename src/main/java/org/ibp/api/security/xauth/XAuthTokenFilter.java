
package org.ibp.api.security.xauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.middleware.ContextHolder;
import org.ibp.api.domain.common.ErrorResponse;
import org.ibp.api.java.impl.middleware.common.ContextResolutionException;
import org.ibp.api.security.BMSUser;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is found.
 */
// **Important note for developers** : This class is central to the authentication framework of BMSAPI. Please do not alter it without a
// good understanding of Spring Security in general and BMS X-Auth-Token based authentication workflow in particular, otherwise there will
// be MAJOR breakages in the functioning of BMS components. Consult your friendly senior developer first if you are unsure.
public class XAuthTokenFilter extends GenericFilterBean {

	final static String XAUTH_TOKEN_HEADER_NAME = "x-auth-token";

	// OAuth / BrAPI
	final static String OAUTH_TOKEN_HEADER_NAME = "Authorization";
	final static String OAUTH_TOKEN_PREFIX = "Bearer ";

	private final UserDetailsService detailsService;

	private final TokenProvider tokenProvider;

	public XAuthTokenFilter(final UserDetailsService detailsService, final TokenProvider tokenProvider) {
		this.detailsService = detailsService;
		this.tokenProvider = tokenProvider;
	}

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
		throws IOException, ServletException {

		final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

		try {

			String authToken = null;

			final String tokenHeader = httpServletRequest.getHeader(XAuthTokenFilter.OAUTH_TOKEN_HEADER_NAME);
			if (tokenHeader != null) {
				authToken = tokenHeader.substring(OAUTH_TOKEN_PREFIX.length());
			}

			if (authToken == null) {
				authToken = httpServletRequest.getHeader(XAuthTokenFilter.XAUTH_TOKEN_HEADER_NAME);
			}

			if (StringUtils.hasText(authToken)) {
				final String username = this.tokenProvider.getUserNameFromToken(authToken);
				final UserDetails details = this.detailsService.loadUserByUsername(username);
				if (this.tokenProvider.validateToken(authToken, details)) {
					final UsernamePasswordAuthenticationToken token =
						new UsernamePasswordAuthenticationToken(details, details.getPassword(), details.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(token);
					ContextHolder.setLoggedInUserId(((BMSUser) details).getUserId());
				}
			}
			filterChain.doFilter(servletRequest, servletResponse);
		} catch (final ContextResolutionException contextResolutionException) {
			this.sendError(HttpServletResponse.SC_BAD_REQUEST, contextResolutionException.getMessage(), httpServletResponse);
		} catch (final AuthenticationServiceException authenticationServiceException) {
			this.sendError(HttpServletResponse.SC_FORBIDDEN, authenticationServiceException.getMessage(), httpServletResponse);
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	void sendError(final int status, final String message, final HttpServletResponse response) throws IOException {
		// Manually send error code and message, exception thrown here in at filter level can't be caught in ControllerAdvice (DefaultExceptionHandler).
		final ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.addError(message);
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
	}
}
