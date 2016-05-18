
package org.ibp.api.ibpworkbench.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sun.jersey.api.view.Viewable;

@Path("/")
public class IndexController {

	@GET
	@Path("/breeding-view-index")
	@Produces("text/html")
	public Viewable index() {
		return new Viewable("/breeding-view-index");
	}

}
