package org.ibp.api.rest.location;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationRequestDto;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.location.LocationService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Api(value = "Location Services")
@RestController
public class LocationResource {

	@Autowired
	LocationService locationService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@ApiOperation(value = "Get location")
	@RequestMapping(value = "/crops/{cropName}/locations/{locationId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<LocationDTO> getLocation(
		@PathVariable final String cropName,
		@PathVariable final Integer locationId,
		@RequestParam(required = false) final String programUUID) {

		return new ResponseEntity<>(this.locationService.getLocation(locationId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get location types")
	@RequestMapping(value = "/crops/{cropName}/location-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<LocationTypeDTO>> getLocationTypes(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID) {

		return new ResponseEntity<>(this.locationService.getLocationTypes(), HttpStatus.OK);
	}

	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.")
	})
	@ApiOperation(value = "List locations", notes = "Get a list of locations filter by types, favorites, abbreviations and location name.")
	@RequestMapping(value = "/crops/{cropname}/locations", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<LocationDto>> listLocations(
		@PathVariable final String cropname,
		@RequestParam(required = false) final String programUUID,
		@ApiParam(value = "list of location types")
		@RequestParam(required = false) final Set<Integer> locationTypes,
		@ApiParam(value = "retrieve favorite locations only", required = true)
		@RequestParam final boolean favoritesOnly,
		@ApiParam(value = "starts with name")
		@RequestParam(required = false) final String name,
		@ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		if (favoritesOnly && StringUtils.isEmpty(programUUID)) {
			errors.reject("locations.favorite.requires.program", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final String favoriteProgramUUID = (favoritesOnly) ? programUUID : null;
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest(favoriteProgramUUID, locationTypes, name);
		final PagedResult<LocationDto> pageResult =
			new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<LocationDto>() {

				@Override
				public long getCount() {
					return LocationResource.this.locationService.countLocations(cropname, locationSearchRequest);
				}

				@Override
				public long getFilteredCount() {
					return LocationResource.this.locationService
						.countLocations(cropname, locationSearchRequest);
				}

				@Override
				public List<LocationDto> getResults(final PagedResult<LocationDto> pagedResult) {
					return LocationResource.this.locationService
						.getLocations(cropname, locationSearchRequest, pageable);
				}
			});

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Filtered-Count", Long.toString(pageResult.getFilteredResults()));
		return new ResponseEntity<List<LocationDto>>(pageResult.getPageResults(), headers, HttpStatus.OK);

	}

	@ApiOperation(value = "Create a new Location", notes = "Create a new Location")
	@RequestMapping(value = "/crops/{cropName}/locations", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS')")
	@ResponseBody
	public ResponseEntity<Integer> createLocation(@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID, @RequestBody final LocationRequestDto locationRequestDto) {
		return new ResponseEntity<>(this.locationService.createLocation(locationRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Update Location", notes = "Update Location")
	@RequestMapping(value = "/crops/{cropName}/locations/{locationId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS')")
	@ResponseBody
	public ResponseEntity<Void> updateLocation(@PathVariable final String cropName,
		@PathVariable final Integer locationId,
		@RequestParam(required = false) final String programUUID, @RequestBody final LocationRequestDto locationRequestDto) {
		final boolean updateExecuted = this.locationService.updateLocation(locationId, locationRequestDto);
		return new ResponseEntity<>((updateExecuted) ? HttpStatus.OK : HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Delete Location", notes = "Delete Location")
	@RequestMapping(value = "/crops/{cropName}/locations/{locationId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'MANAGE_PROGRAMS')")
	@ResponseBody
	public ResponseEntity<Void> deleteLocation(@PathVariable final String cropName,
		@PathVariable final Integer locationId,
		@RequestParam(required = false) final String programUUID) {
		this.locationService.deleteLocation(locationId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
