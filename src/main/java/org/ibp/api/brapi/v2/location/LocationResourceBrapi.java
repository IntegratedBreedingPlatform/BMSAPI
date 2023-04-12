package org.ibp.api.brapi.v2.location;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.BrapiSearchDto;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.location.LocationService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Api(value = "BrAPI v2 Location Services")
@Controller(value = "LocationResourceBrapiV2")
public class LocationResourceBrapi {

	@Autowired
	private org.generationcp.middleware.api.location.LocationService locationMiddlewareService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private SearchRequestService searchRequestService;

	@ApiOperation(value = "Get a filtered list of Locations", notes = "Get a list of locations.")
	@RequestMapping(value = "/{crop}/brapi/v2/locations", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<Location>> listLocations(
		@PathVariable final String crop,
		@ApiParam(value = "Filter by location type specified.")
		@RequestParam(value = "locationType", required = false) final String locationType,
		@ApiParam(value = "Internal database identifier")
		@RequestParam(value = "locationDbId", required = false) final String locationDbId,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION)
		@RequestParam(value = "page", required = false) final Integer page,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION)
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		if (!StringUtil.isEmpty(locationDbId)) {
			locationSearchRequest.setLocationDbIds(Collections.singletonList(Integer.valueOf(locationDbId)));
		}

		if (!StringUtil.isEmpty(locationType)) {
			locationSearchRequest.setLocationTypes(Arrays.asList(locationType));
		}

		return this.getLocationResponseEntity(locationSearchRequest, page, pageSize);

	}

	private ResponseEntity getLocationResponseEntity(final LocationSearchRequest locationSearchRequest, final Integer page,
		final Integer pageSize) {

		final int finalPageNumber = page == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : page;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest = new PageRequest(finalPageNumber, finalPageSize);

		final PagedResult<Location> resultPage =
			new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<Location>() {

				@Override
				public long getCount() {
					return LocationResourceBrapi.this.locationService.countFilteredLocations(locationSearchRequest, null);
				}

				@Override
				public List<Location> getResults(final PagedResult<Location> pagedResult) {
					return LocationResourceBrapi.this.locationService.getLocations(locationSearchRequest, pageRequest);
				}
			});

		final Result<Location> results = new Result<Location>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);

	}

	@ApiOperation(value = "Get a location given an id", notes = "Get a location")
	@RequestMapping(value = "/{crop}/brapi/v2/locations/{locationDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<Location>> getLocation(
		@PathVariable final String crop,
		@ApiParam(value = "Internal database identifier")
		@PathVariable(value = "locationDbId") final String locationDbId) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());

		if (!NumberUtils.isNumber(locationDbId)) {
			errors.reject("brapi.location.db.id.invalid", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationDbIds(Collections.singletonList(Integer.valueOf(locationDbId)));

		final List<Location> results = this.locationService.getLocations(locationSearchRequest, null);

		if (results.isEmpty()) {
			errors.reject("brapi.location.db.id.invalid", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final Metadata metadata = new Metadata();
		final Pagination pagination = new Pagination().withPageNumber(1).withPageSize(1).withTotalCount(1L).withTotalPages(1);
		metadata.setPagination(pagination);
		metadata.setStatus(Collections.singletonList(new HashMap<>()));
		return ResponseEntity.ok(new SingleEntityResponse<>(metadata, results.get(0)));
	}

	@ApiOperation(value = "Search locations", notes = "Submit a search request for locations")
	@RequestMapping(value = "/{crop}/brapi/v2/search/locations", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<BrapiSearchDto>> postSearchLocations(
		@PathVariable final String crop,
		@RequestBody final LocationSearchRequestDto locationSearchRequest) {
		final ModelMapper modelMapper = LocationMapper.getInstance();
		final LocationSearchRequest mappedLocation = modelMapper.map(locationSearchRequest, LocationSearchRequest.class);

		final BrapiSearchDto searchDto =
			new BrapiSearchDto(this.searchRequestService.saveSearchRequest(mappedLocation, LocationSearchRequest.class)
				.toString());
		final SingleEntityResponse<BrapiSearchDto> locationSearchResponse = new SingleEntityResponse<>(searchDto);

		return new ResponseEntity<>(locationSearchResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get search location results", notes = "Get the results of locations search request")
	@RequestMapping(value = "/{crop}/brapi/v2/search/locations/{searchResultsDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<Location>> getLocationsSearchResults(
		@PathVariable final String crop,
		@PathVariable final String searchResultsDbId,
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final LocationSearchRequest locationsSearchRequest;
		try {
			locationsSearchRequest =
				(LocationSearchRequest) this.searchRequestService
					.getSearchRequest(Integer.valueOf(searchResultsDbId), LocationSearchRequest.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<Location>(new Result<>(new ArrayList<>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		return this.getLocationResponseEntity(locationsSearchRequest, currentPage, pageSize);
	}

}
