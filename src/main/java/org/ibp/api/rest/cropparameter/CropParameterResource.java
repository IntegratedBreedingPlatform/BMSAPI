package org.ibp.api.rest.cropparameter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.cropparameter.CropParameterDTO;
import org.generationcp.middleware.api.cropparameter.CropParameterEnum;
import org.generationcp.middleware.api.cropparameter.CropParameterPatchRequestDTO;
import org.generationcp.middleware.service.impl.crop.CropGenotypingParameterDTO;
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

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Api("Crop Parameter services")
@RestController
@RequestMapping("/crops/{cropName}")
public class CropParameterResource {

	@Autowired
	private CropParameterService cropParameterService;

	private static final String GENOTYPING_SERVER = "gigwa";

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "page number. Start at " + PagedResult.DEFAULT_PAGE_NUMBER),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.")
	})
	@ApiOperation("list configuration")
	@RequestMapping(value = "/crop-parameters", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<List<CropParameterDTO>> getCropParameters(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@ApiIgnore @PageableDefault(page = PagedResult.DEFAULT_PAGE_NUMBER, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
	) {
		return new ResponseEntity<>(this.cropParameterService.getCropParameters(pageable), HttpStatus.OK);
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
		checkNotNull(request, "request.null");
		this.cropParameterService.modifyCropParameter(key, request);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation("Get crop parameter by key")
	@RequestMapping(value = "/crop-parameters/{key}", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<CropParameterDTO> getCropParameterByKey(
		@PathVariable final String cropName,
		@PathVariable final CropParameterEnum key,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.cropParameterService.getCropParameter(key), HttpStatus.OK);
	}

	@ApiOperation(value = "Get crop genotyping parameter", notes = "")
	@RequestMapping(value = "/crop-genotyping-parameters", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<CropGenotypingParameterDTO> getCropGenotypingParameter(@PathVariable final String cropName) {
		final CropGenotypingParameterDTO parameter = this.cropParameterService.getCropGenotypingParameter(GENOTYPING_SERVER);
		return new ResponseEntity<>(parameter, HttpStatus.OK);
	}

	@ApiOperation(value = "Generate token", notes = "Get the token using the credentials in Crop Genotype Parameter configuration")
	@RequestMapping(value = "/crop-genotyping-parameters/token", method = RequestMethod.GET)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<String> getToken(@PathVariable final String cropName) {
		return new ResponseEntity<>(this.cropParameterService.getToken(GENOTYPING_SERVER), HttpStatus.OK);
	}
}
