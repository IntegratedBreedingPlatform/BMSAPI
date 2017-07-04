
package org.ibp.api.brapi.v1.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.location.LocationFilters;
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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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
	public ResponseEntity<Locations> listLocations(@PathVariable final String crop,
			@ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.",
					required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.",
					required = false) @RequestParam(value = "pageSize", required = false) Integer pageSize,
			@ApiParam(value = "name of location type", required = false) @RequestParam(value = "locationType",
					required = false) String locationType) {

		final Map<LocationFilters, Object> filters = new HashMap<>();
		PagedResult<LocationDetailsDto> resultPage = null;
		if (!StringUtils.isBlank(locationType)) {
			final Integer locationTypeId = this.locationDataManager
					.getUserDefinedFieldIdOfName(org.generationcp.middleware.pojos.UDTableType.LOCATION_LTYPE, locationType);
			if (locationTypeId != null) {
				filters.put(LocationFilters.LOCATION_TYPE, locationTypeId.toString());

				resultPage = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<LocationDetailsDto>() {

					@Override
					public long getCount() {
						return locationDataManager.countLocationsByFilter(filters);
					}

					@Override
					public List<LocationDetailsDto> getResults(PagedResult<LocationDetailsDto> pagedResult) {
						return locationDataManager.getLocationsByFilter(pagedResult.getPageNumber(),
								pagedResult.getPageSize(), filters);
					}
				});
			}
		}

		if (resultPage!= null && resultPage.getTotalResults() > 0) {
			
			final ModelMapper mapper = LocationMapper.getInstance();
			final List<Location> locations = new ArrayList<>();

			for (LocationDetailsDto locationDetailsDto : resultPage.getPageResults()) {
				final Location location = mapper.map(locationDetailsDto, Location.class);
				locations.add(location);
			}

			Result<Location> results = new Result<Location>().withData(locations);
			Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
					.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

			Metadata metadata = new Metadata().withPagination(pagination);
			Locations locationList = new Locations().withMetadata(metadata).withResult(results);
			return new ResponseEntity<>(locationList, HttpStatus.OK);
			
		} else {

			Map<String, String> status = new HashMap<>();
			status.put("message", "not found locations");
			Metadata metadata = new Metadata(null, status);
			Locations locationList = new Locations().withMetadata(metadata);
			return new ResponseEntity<>(locationList, HttpStatus.NOT_FOUND);
		}
	}

}
