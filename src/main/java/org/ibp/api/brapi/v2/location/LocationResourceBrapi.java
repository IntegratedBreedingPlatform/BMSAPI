package org.ibp.api.brapi.v2.location;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

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
		locationSearchRequest.setLocationId(locationDbId);
		locationSearchRequest.setLocationType(locationType);

		final int finalPageNumber = page == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : page;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest = new PageRequest(finalPageNumber, finalPageSize);

		final PagedResult<Location> resultPage =
			new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<Location>() {

				@Override
				public long getCount() {
					return LocationResourceBrapi.this.locationMiddlewareService.countLocations(locationSearchRequest);
				}

				@Override
				public List<Location> getResults(final PagedResult<Location> pagedResult) {
					return LocationResourceBrapi.this.locationMiddlewareService.getLocations(locationSearchRequest, pageRequest);
				}
			});

		final Result<Location> result = new Result<Location>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final EntityListResponse<Location> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}
}
