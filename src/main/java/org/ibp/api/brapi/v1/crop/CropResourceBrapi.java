package org.ibp.api.brapi.v1.crop;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.pojos.workbench.Permission;
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
	public ResponseEntity<CropDto> listAvailableCrops() {

		final CropDto cropDto = new CropDto();

		// The response doesn't require pagination. The value for the "pagination" key is returned with all the keys set to zero.
		final Metadata metadata = new Metadata();
		metadata.withDatafiles(new URL[0]);
		metadata.withStatus(Collections.singletonList(new HashMap<>()));
		metadata.withPagination(new Pagination(0, 0, 0l, 0));
		cropDto.setMetadata(metadata);

		if (request.isUserInRole(Permission.Permissions.ADMIN.name())
			|| request.isUserInRole(Permission.Permissions.SITE_ADMIN.name())) {
			cropDto.setResult(new Result<>(this.cropService.getInstalledCrops()));
		} else {
			cropDto.setResult(
				new Result<>(this.cropService.getAvailableCropsForUser(this.securityService.getCurrentlyLoggedInUser().getUserid())));
		}

		return new ResponseEntity<>(cropDto, HttpStatus.OK);

	}

}
