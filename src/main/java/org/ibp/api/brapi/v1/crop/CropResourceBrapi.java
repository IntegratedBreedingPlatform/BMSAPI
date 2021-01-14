package org.ibp.api.brapi.v1.crop;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a>
 * Crop services.
 */
@Api(value = "BrAPI Crop Services")
@Controller
public class CropResourceBrapi {

	@Autowired
	private CropService cropService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private HttpServletRequest request;

	@ApiOperation(value = "List of available crops.", notes = "Get a list of available crops.")
	@RequestMapping(value = "/brapi/v1/crops", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<String>> listAvailableCrops() {

		// The response doesn't require pagination. The value for the "pagination" key is returned with all the keys set to zero.
		final Metadata metadata = new Metadata();
		metadata.withDatafiles(new URL[0]);
		metadata.withStatus(Collections.singletonList(new HashMap<>()));
		metadata.withPagination(new Pagination(0, 0, 0l, 0));

		final Result<String> results;
		if (request.isUserInRole(PermissionsEnum.ADMIN.name())
			|| request.isUserInRole(PermissionsEnum.ADMINISTRATION.name())
			|| request.isUserInRole(PermissionsEnum.SITE_ADMIN.name())) {
			results = new Result<>(this.cropService.getInstalledCrops());
		} else {
			results = new Result<>(this.cropService.getAvailableCropsForUser(this.securityService.getCurrentlyLoggedInUser().getUserid()));
		}

		return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);

	}

}
