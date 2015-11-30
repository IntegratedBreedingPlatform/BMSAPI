
package org.ibp.api.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;

public class Http401UnauthorizedEntryPointTest {

	@Test
	public void testCommence() throws IOException, ServletException {
		Http401UnauthorizedEntryPoint entryPoint = new Http401UnauthorizedEntryPoint();
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		entryPoint.commence(request, response, null);
		Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
	}
}
