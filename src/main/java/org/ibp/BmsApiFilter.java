
package org.ibp;

import org.generationcp.commons.hibernate.HTTPRequestAwareServletFilter;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to enable <a href="http://en.wikipedia.org/wiki/Cross-origin_resource_sharing" >Cross-origin resource sharing</a>. This is also
 * used to set the common API response header settings.
 *
 * @author Naymesh
 */
@Component
public class BmsApiFilter implements Filter {

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		final HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, PUT, PATCH, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with, x-auth-token, Authorization, Content-type");
		response.setHeader("x-frame-options", "SAMEORIGIN");
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
		response.setHeader("Feature-Policy", "self");
		response.setHeader("Content-Security-Policy", HTTPRequestAwareServletFilter.CSP_CONFIG);

		// Specific Cache control setting. An issue is caused by caching in IE9 and IE10. The GET requests to retrieve the variables,
		// properties, methods or scales after one has been added are not executed again. IE will only execute the GET request again after
		// the cache header expires, despite the response changing due to the addition of the new entity.
		response.setHeader("Cache-Control", "max-age=0, no-cache, no-store");

		chain.doFilter(req, res);
	}

	@Override
	public void init(final FilterConfig filterConfig) {
		// This filter does not needs any initialization
	}

	@Override
	public void destroy() {
		// This filter needs to do nothing on being destroyed
	}

}
