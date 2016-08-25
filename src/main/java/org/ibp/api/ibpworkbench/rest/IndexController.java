
package org.ibp.api.ibpworkbench.rest;

import com.sun.jersey.api.view.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class IndexController {

	@GET
	@Path("/index")
	@Produces("text/html")
	public Viewable index() {
		return new Viewable("/index");
	}

}
