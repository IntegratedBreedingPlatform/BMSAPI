package org.ibp.api.brapi.v1.crop;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.java.crop.CropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a>
 * Crop services.
 */
@Api(value = "BrAPI Crop Services")
@Controller
public class CropResourceBrapi {

	@Autowired
	private CropService cropService;

	@ApiOperation(value = "List of available crops", notes = "Get a list of available crops.")
	@RequestMapping(value = "/brapi/v1/crops", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<CropDto> listAvailableCrops() {

		final CropDto cropDto = new CropDto();

		// The response doesn't require pagination. The value for the "pagination" key is returned with all the keys set to zero.
		final Metadata metadata = new Metadata();
		metadata.withDatafiles(new URL[0]);
		metadata.withStatus(new HashMap<String, String>());
		metadata.withPagination(new Pagination(0, 0, 0l, 0));
		cropDto.setMetadata(metadata);

		cropDto.setResult(new Result<String>(cropService.getInstalledCrops()));

		return new ResponseEntity<>(cropDto, HttpStatus.OK);

	}

}
