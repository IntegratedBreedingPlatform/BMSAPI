package org.ibp.api.rest.breedingview;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.design.License;
import org.ibp.api.java.design.DesignLicenseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Api(value = "BV License Service")
@Controller
public class BVLicenseResource {

	@Resource
	private DesignLicenseService designLicenseService;

	@ApiOperation(value = "Gets list of Breeding View Licenses")
	@RequestMapping(value= "/breeding-view-licenses", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<License>> getLicenseExpiryDays() {
		return new ResponseEntity<>(Collections.singletonList(
			this.designLicenseService.getLicenseInfo()), HttpStatus.OK);
	}

}
