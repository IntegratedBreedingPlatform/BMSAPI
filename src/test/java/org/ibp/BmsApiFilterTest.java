
package org.ibp;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * The class <code>BmsApiFilterTest</code> contains tests for the class <code>{@link BmsApiFilter}</code>.
 */
public class BmsApiFilterTest {

	/**
	 * Test that http headers are set correctly. More for letting people know the reasons for certain http headers settings.
	 *
	 */
	@Test
	public void testHeaders() throws Exception {
		BmsApiFilter bmsApiFilterTest = new BmsApiFilter();

		final HttpServletResponse servletResponseMock = Mockito.mock(HttpServletResponse.class);
		bmsApiFilterTest.doFilter(Mockito.mock(ServletRequest.class), 
				servletResponseMock, 
				Mockito.mock(FilterChain.class));
		// CORS Access Control
		Mockito.verify(servletResponseMock).setHeader("Access-Control-Allow-Origin", "*");
		Mockito.verify(servletResponseMock).setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
		Mockito.verify(servletResponseMock).setHeader("Access-Control-Max-Age", "3600");
		Mockito.verify(servletResponseMock).setHeader("Access-Control-Allow-Headers", "x-requested-with, x-auth-token");
		
		// Cache control. Please see https://leafnode.atlassian.net/browse/BMS-1117
		Mockito.verify(servletResponseMock).setHeader("Cache-Control", "max-age=0, no-cache, no-store");
	}

}
