
package org.ibp.api.java.impl.middleware.common;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ContextResolverImplTest {

	@Test(expected = ContextResolutionException.class)
	public void testResolveDatabaseFromUrlInvalidURL() {

		// Non-null request but bad URL.Expect exception.
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/locations");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		ContextResolverImpl contextResolver = new ContextResolverImpl();
		contextResolver.resolveDatabaseFromUrl();
	}

	@Test
	public void testResolveDatabaseFromUrlValidURLBMSAPI() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/locations/maize/list");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		ContextResolverImpl contextResolver = new ContextResolverImpl();
		String database = contextResolver.resolveDatabaseFromUrl();
		Assert.assertEquals("Crop database was not resolved correctly for BMSAPI request URL.", "ibdbv2_maize_merged", database);
	}

	@Test
	public void testResolveDatabaseFromUrlValidURLBrAPI() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/maize/brapi/v1/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		ContextResolverImpl contextResolver = new ContextResolverImpl();
		String database = contextResolver.resolveDatabaseFromUrl();
		Assert.assertEquals("Crop database was not resolved correctly for BrAPI request URL.", "ibdbv2_maize_merged", database);
	}

}
