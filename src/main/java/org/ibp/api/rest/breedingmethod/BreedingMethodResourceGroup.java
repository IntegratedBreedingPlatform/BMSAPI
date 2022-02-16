package org.ibp.api.rest.breedingmethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodNewRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.breedingmethod.MethodClassDTO;
import org.generationcp.middleware.pojos.MethodGroup;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.breedingmethod.BreedingMethodService;
import org.ibp.api.java.impl.middleware.breedingmethod.validator.BreedingMethodSearchRequestValidator;
import org.ibp.api.rest.common.PaginatedSearch;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "Breeding methods Services")
@RestController
public class BreedingMethodResourceGroup {

	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private BreedingMethodSearchRequestValidator breedingMethodSearchRequestValidator;

	@ApiOperation(value = "Get breeding method")
	@RequestMapping(value = "/crops/{cropName}/breedingmethods/{breedingMethodDbId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BreedingMethodDTO> getBreedingMethod(
		@PathVariable final String cropName,
		@PathVariable final Integer breedingMethodDbId,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(this.breedingMethodService.getBreedingMethod(breedingMethodDbId), HttpStatus.OK);
	}

	@ApiOperation(value = "Create breeding method")
	@RequestMapping(value = "/crops/{cropName}/breedingmethods", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<BreedingMethodDTO> create(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final BreedingMethodNewRequest breedingMethod
	) {
		return new ResponseEntity<>(this.breedingMethodService.create(breedingMethod), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Edit breeding method")
	@RequestMapping(value = "/crops/{cropName}/breedingmethods/{breedingMethodDbId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<BreedingMethodDTO> edit(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final Integer breedingMethodDbId,
		@RequestBody final BreedingMethodNewRequest breedingMethod
	) {
		return new ResponseEntity<>(this.breedingMethodService.edit(breedingMethodDbId, breedingMethod), HttpStatus.OK);
	}

	@ApiOperation(value = "Delete breeding method")
	@RequestMapping(value = "/crops/{cropName}/breedingmethods/{breedingMethodDbId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<Void> delete(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final Integer breedingMethodDbId
	) {
		this.breedingMethodService.delete(breedingMethodDbId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "List breeding method types")
	@RequestMapping(value = "/crops/{cropName}/breedingmethod-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodType>> getBreedingMethodTypes(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(MethodType.getAll(), HttpStatus.OK);
	}

	@ApiOperation(value = "List breeding method classes")
	@RequestMapping(value = "/crops/{cropName}/breedingmethod-classes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodClassDTO>> getBreedingMethodClasses(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(this.breedingMethodService.getMethodClasses(), HttpStatus.OK);
	}

	@ApiOperation(value = "List breeding method groups")
	@RequestMapping(value = "/crops/{cropName}/breedingmethod-groups", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<MethodGroup>> getBreedingMethodGroups(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(MethodGroup.getAll(), HttpStatus.OK);
	}

	@ApiOperation(value = "List breeding method filtered by favorites")
	@RequestMapping(value = "/crops/{cropName}/breedingmethods/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<BreedingMethodDTO>> searchBreedingMethods(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final BreedingMethodSearchRequest request,
		@ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {

		this.breedingMethodSearchRequestValidator.validate(cropName, request);

		return new PaginatedSearch().getPagedResult(() -> this.breedingMethodService.countSearchBreedingMethods(request, programUUID),
				() -> this.breedingMethodService.searchBreedingMethods(request, pageable, programUUID),
				pageable);
	}

}
