
package org.ibp.api.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Returns a 401 error code (Unauthorized) to the client.
 */

// **Important note for developers** : This class is central to the authentication framework of BMSAPI. Please do not alter it without a
// good understanding of Spring Security in general and BMS X-Auth-Token based authentication workflow in particular, otherwise there will
// be MAJOR breakages in the functioning of BMS components. Consult your friendly senior developer first if you are unsure.
@Component
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {

	private static final Logger LOG = LoggerFactory.getLogger(Http401UnauthorizedEntryPoint.class);

	/**
	 * Always returns a 401 error code to the client.
	 */
	@Override
	public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException arg2)
			throws IOException, ServletException {

		LOG.debug("Pre-authenticated entry point called. Rejecting access");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
	}
}
