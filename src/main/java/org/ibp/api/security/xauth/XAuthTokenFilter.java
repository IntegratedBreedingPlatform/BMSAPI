
package org.ibp.api.security.xauth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is found.
 */
// **Important note for developers** : This class is central to the authentication framework of BMSAPI. Please do not alter it without a
// good understanding of Spring Security in general and BMS X-Auth-Token based authentication workflow in particular, otherwise there will
// be MAJOR breakages in the functioning of BMS components. Consult your friendly senior developer first if you are unsure.
public class XAuthTokenFilter extends GenericFilterBean {

	final static String XAUTH_TOKEN_HEADER_NAME = "x-auth-token";

	private final UserDetailsService detailsService;

	private final TokenProvider tokenProvider;

	public XAuthTokenFilter(final UserDetailsService detailsService, final TokenProvider tokenProvider) {
		this.detailsService = detailsService;
		this.tokenProvider = tokenProvider;
	}

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
			throws IOException, ServletException {
		try {
			final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
			final String authToken = httpServletRequest.getHeader(XAuthTokenFilter.XAUTH_TOKEN_HEADER_NAME);
			if (StringUtils.hasText(authToken)) {
				final String username = this.tokenProvider.getUserNameFromToken(authToken);
				final UserDetails details = this.detailsService.loadUserByUsername(username);
				if (this.tokenProvider.validateToken(authToken, details)) {
					final UsernamePasswordAuthenticationToken token =
							new UsernamePasswordAuthenticationToken(details, details.getPassword(), details.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(token);
				}
			}
			filterChain.doFilter(servletRequest, servletResponse);
		} catch (final Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
