
package org.ibp.api.brapi.v1.location;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.api.brapi.v1.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.location.LocationFilters;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a> Location services.
 *
 * @author Naymesh Mistry
 *
 */
@Api(value = "BrAPI Location Services")
@Controller
public class LocationResourceBrapi {

	@Autowired
	private LocationDataManager locationDataManager;

	@ApiOperation(value = "List locations", notes = "Get a list of locations.")
	@RequestMapping(value = "/{crop}/brapi/v1/locations", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<Location>> listLocations(@PathVariable final String crop,
			@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
					required = false) final Integer currentPage,
			@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
					required = false) final Integer pageSize,
			@ApiParam(value = "name of location type", required = false) @RequestParam(value = "locationType",
					required = false) final String locationType) {

		final Map<LocationFilters, Object> filters = new EnumMap<>(LocationFilters.class);
		PagedResult<LocationDetailsDto> resultPage = null;
		final boolean validation = this.validateParameter(locationType, filters);

		if (validation) {
			resultPage = new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<LocationDetailsDto>() {

				@Override
				public long getCount() {
					return LocationResourceBrapi.this.locationDataManager.countLocationsByFilter(filters);
				}

				@Override
				public List<LocationDetailsDto> getResults(final PagedResult<LocationDetailsDto> pagedResult) {
					// BRAPI services have zero-based indexing for pages but paging for Middleware method starts at 1
					final int pageNumber = pagedResult.getPageNumber() + 1;
					return LocationResourceBrapi.this.locationDataManager.getLocationsByFilter(pageNumber, pagedResult.getPageSize(),
							filters);
				}
			});
		}

		if (resultPage != null && resultPage.getTotalResults() > 0) {

			final ModelMapper mapper = LocationMapper.getInstance();
			final List<Location> locations = new ArrayList<>();

			for (final LocationDetailsDto locationDetailsDto : resultPage.getPageResults()) {
				final Location location = mapper.map(locationDetailsDto, Location.class);
				locations.add(location);
			}

			final Result<Location> results = new Result<Location>().withData(locations);
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

	private boolean validateParameter(final String locationType, final Map<LocationFilters, Object> filters) {
		if (!StringUtils.isBlank(locationType)) {
			final Integer locationTypeId = this.locationDataManager
					.getUserDefinedFieldIdOfName(org.generationcp.middleware.pojos.UDTableType.LOCATION_LTYPE, locationType);
			if (locationTypeId != null) {
				filters.put(LocationFilters.LOCATION_TYPE, locationTypeId.toString());
			} else {
				return false;
			}
		}
		return true;
	}
}
