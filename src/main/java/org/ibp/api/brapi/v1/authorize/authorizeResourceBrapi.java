package org.ibp.api.brapi.v1.authorize;

import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

@Api(value = "BrAPI authorize external Client Services")
@Controller
public class authorizeResourceBrapi {

	@RequestMapping(value = "/brapi/authorize", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity authorize(@RequestParam(value = "display_name") final String display_name,@RequestParam(value = "return_url") final String return_url)
		throws UnsupportedEncodingException {
		final URI loginUrl = URI.create(
			"/ibpworkbench/controller/auth/login?display_name=" + URLEncoder.encode(display_name,"UTF-8") + "&return_url=" + return_url);
		return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(loginUrl).build();
	}

}
