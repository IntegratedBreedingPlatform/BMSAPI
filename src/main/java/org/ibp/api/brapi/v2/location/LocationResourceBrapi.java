package org.ibp.api.brapi.v2.location;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI v2 Location Services")
@Controller(value = "LocationResourceBrapiV2")
public class LocationResourceBrapi {

	@Autowired
	private org.generationcp.middleware.api.location.LocationService locationMiddlewareService;

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
		if(!StringUtil.isEmpty(locationDbId)) {
			locationSearchRequest.setLocationIds(Collections.singletonList(Integer.valueOf(locationDbId)));
		}
		locationSearchRequest.setLocationTypeName(locationType);

		final int finalPageNumber = page == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : page;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest = new PageRequest(finalPageNumber, finalPageSize);

		final PagedResult<Location> resultPage =
			new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<Location>() {

				@Override
				public long getCount() {
					return LocationResourceBrapi.this.locationMiddlewareService.countFilteredLocations(locationSearchRequest);
				}

				@Override
				public List<Location> getResults(final PagedResult<Location> pagedResult) {
					return LocationResourceBrapi.this.locationMiddlewareService.getLocations(locationSearchRequest, pageRequest);
				}
			});

		if (resultPage != null && resultPage.getTotalResults() > 0) {

			final Result<Location> results = new Result<Location>().withData(resultPage.getPageResults());
			final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
				.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

			final Metadata metadata = new Metadata().withPagination(pagination);
			return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);

		} else {

			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", "not found locations"));
			final Metadata metadata = new Metadata(null, status);
			return new ResponseEntity<>(new EntityListResponse().withMetadata(metadata), HttpStatus.NOT_FOUND);
		}

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
		locationSearchRequest.setLocationIds(Collections.singletonList(Integer.valueOf(locationDbId)));

		final List<Location> results = locationMiddlewareService.getLocations(locationSearchRequest, null);

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

}
