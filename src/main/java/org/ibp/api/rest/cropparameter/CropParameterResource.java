package org.ibp.api.rest.cropparameter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.cropparameter.CropParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api("Crop Parameter services")
@RestController
@RequestMapping("/crops/{cropName}")
public class CropParameterResource {

	@Autowired
	private CropParameterService cropParameterService;

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.")
	})
	@ApiOperation("list configuration")
	@RequestMapping(value = "/crop-parameters", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<List<CropParameterDTO>> getCropParameter(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
	) {
		return new ResponseEntity<>(this.cropParameterService.getCropParameter(pageable), HttpStatus.OK);
	}

	@ApiOperation("Modify Crop parameter")
	@RequestMapping(value = "/crop-parameters/{key}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<Void> modifyCropParameter(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final String key,
		@RequestBody final CropParameterPatchRequestDTO request
	) {
		this.cropParameterService.modifyCropParameter(key, request);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}


}
