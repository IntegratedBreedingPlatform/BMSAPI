package org.ibp.api.brapi.v1.authorize;

import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URI;

@Api(value = "BrAPI authorize external Client Services")
@Controller
public class authorizeResourceBrapi {

	@RequestMapping(value = "/brapi/authorize", method = RequestMethod.POST)
	@ResponseBody
	public void authorize(@RequestBody final AuthorizeRequest authorizeRequest) {
		ResponseEntity.status(HttpStatus.FOUND).location(URI.create(
			"/ibpworkbench/controller/auth/login?display_name=" + authorizeRequest.getDisplay_name() + "&return_url=" + authorizeRequest
				.getReturn_url())).build();
	}

}
