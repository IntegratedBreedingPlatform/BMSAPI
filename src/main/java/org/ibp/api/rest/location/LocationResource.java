package org.ibp.api.rest.location;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationRequestDto;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.location.validator.LocationSearchRequestValidator;
import org.ibp.api.java.location.LocationService;
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

@Api(value = "Location Services")
@RestController
public class LocationResource {

	@Autowired
	private LocationService locationService;

	@Autowired
	private LocationSearchRequestValidator locationSearchRequestValidator;

	@ApiOperation(value = "Get location")
	@RequestMapping(value = "/crops/{cropName}/locations/{locationId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<LocationDTO> getLocation(
		@PathVariable final String cropName,
		@PathVariable final Integer locationId,
		@RequestParam(required = false) final String programUUID) {

		return new ResponseEntity<>(this.locationService.getLocation(locationId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get breeding location default")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/locations/breeding-location-default", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<LocationDTO> getBreedingLocationDefault(
		@PathVariable final String cropName,
		@PathVariable final String programUUID) {
		return new ResponseEntity<>(this.locationService.getBreedingLocationDefault(programUUID), HttpStatus.OK);
	}

	@ApiOperation(value = "Get storage location default")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/locations/storage-location-default", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<LocationDTO> getStorageLocationDefault(
		@PathVariable final String cropName,
		@PathVariable final String programUUID) {
		return new ResponseEntity<>(this.locationService.getStorageLocationDefault(programUUID), HttpStatus.OK);
	}

	@ApiOperation(value = "Get location types")
	@RequestMapping(value = "/crops/{cropName}/location-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<LocationTypeDTO>> getLocationTypes(
		@PathVariable final String cropName,
		@RequestParam(required = false) final Boolean excludeRestrictedTypes,
		@RequestParam(required = false) final String programUUID) {

		return new ResponseEntity<>(this.locationService.getLocationTypes(excludeRestrictedTypes), HttpStatus.OK);
	}

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.")
	})
	@ApiOperation(value = "List locations", notes = "Get a list of locations filter by types, favorites, abbreviations and location name.")
	@RequestMapping(value = "/crops/{cropname}/locations/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<LocationDTO>> searchLocations(
		@PathVariable final String cropname,
		@RequestParam(required = false) final String programUUID,
		@ApiParam(value = "list of location types")
		@RequestBody final LocationSearchRequest request,
		@ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {

		this.locationSearchRequestValidator.validate(cropname, request);

		return new PaginatedSearch().getPagedResult(() -> this.locationService.countLocations(cropname, request, programUUID),
				() -> this.locationService.searchLocations(cropname, request, pageable, programUUID),
				pageable);
	}

	@ApiOperation(value = "Create a new Location", notes = "Create a new Location")
	@RequestMapping(value = "/crops/{cropName}/locations", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	@ResponseBody
	public ResponseEntity<LocationDTO> createLocation(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID, @RequestBody final LocationRequestDto locationRequestDto) {
		return new ResponseEntity<>(this.locationService.createLocation(locationRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Update Location", notes = "Update Location")
	@RequestMapping(value = "/crops/{cropName}/locations/{locationId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<Void> updateLocation(@PathVariable final String cropName,
		@PathVariable final Integer locationId,
		@RequestParam(required = false) final String programUUID, @RequestBody final LocationRequestDto locationRequestDto) {
		final boolean updateExecuted = this.locationService.updateLocation(locationId, locationRequestDto);
		return new ResponseEntity<>((updateExecuted) ? HttpStatus.OK : HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Delete Location", notes = "Delete Location")
	@RequestMapping(value = "/crops/{cropName}/locations/{locationId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_CROP_SETTINGS')")
	public ResponseEntity<Void> deleteLocation(@PathVariable final String cropName,
		@PathVariable final Integer locationId,
		@RequestParam(required = false) final String programUUID) {
		this.locationService.deleteLocation(locationId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// temporary resource until we remove the country table. https://ibplatform.atlassian.net/browse/IBP-5462
	@ApiIgnore
	@RequestMapping(value = "/crops/{cropName}/location-countries", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<LocationDTO>> getCountryLocations(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID) {
		return new ResponseEntity<>(this.locationService.getCountries(), HttpStatus.OK);
	}
}
