
package org.ibp.api.brapi.v1.location;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Country;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
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
					required = false) 
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {

		PagedResult<org.generationcp.middleware.pojos.Location> resultPage =
				new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<org.generationcp.middleware.pojos.Location>() {

					@Override
					public long getCount() {
						return LocationResourceBrapi.this.locationDataManager.countAllLocations();
					}

					@Override
					public List<org.generationcp.middleware.pojos.Location> getResults(
							PagedResult<org.generationcp.middleware.pojos.Location> pagedResult) {
						return LocationResourceBrapi.this.locationDataManager.getAllLocations(pagedResult.getPageNumber(),
								pagedResult.getPageSize());
					}
				});

		List<Location> locations = new ArrayList<>();

		for (org.generationcp.middleware.pojos.Location mwLoc : resultPage.getPageResults()) {
			Location location = new Location();
			location.setLocationDbId(mwLoc.getLocid());
			location.setName(mwLoc.getLname());

			// FIXME This is (n+1) query in loop pattern which is bad. This is just temporary. Do not follow this pattern.
			// TODO Implement a middleware method that retrieves location/country in one query.
			if (mwLoc.getCntryid() == null || mwLoc.getCntryid().equals(0)) {
				location.setCountryCode("Unknown");
				location.setCountryName("Unknown");
			} else {
				Country country = this.locationDataManager.getCountryById(mwLoc.getCntryid());
				location.setCountryCode(country.getIsothree());
				location.setCountryName(country.getIsoabbr());
			}

			location.setLatitude(mwLoc.getLatitude());
			location.setLongitude(mwLoc.getLongitude());
			location.setAltitude(mwLoc.getAltitude());
			locations.add(location);
		}
		
		Result results = new Result().withData(locations);
		Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
				.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		Metadata metadata = new Metadata().withPagination(pagination);
		Locations locationList = new Locations().withMetadata(metadata).withResult(results);

		return new ResponseEntity<Locations>(locationList, HttpStatus.OK);
	}
}
