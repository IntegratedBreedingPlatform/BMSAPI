
package org.ibp.api.rest.location;

import java.util.List;
import java.util.Set;

import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.location.Location;
import org.ibp.api.domain.location.LocationType;
import org.ibp.api.java.location.LocationService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Api(value = "Location Services")
@RestController
@RequestMapping("/location")
public class LocationResource {

	@Autowired
	private LocationService locationService;

	@RequestMapping(value = "/{cropname}/types", method = RequestMethod.GET)
	@ApiOperation(value = "Get all location types", notes = "Returns all location types.")
	public ResponseEntity<Set<LocationType>> getAllLocationTypes(@PathVariable String cropname) {
		return new ResponseEntity<Set<LocationType>>(this.locationService.getAllLocationTypes(), HttpStatus.OK);
	}

	@RequestMapping(value = "/{cropname}", method = RequestMethod.GET, params = {"locationTypeId"})
	@ApiOperation(value = "Get locations by location type", notes = "Returns all locations of a given location type. Results are paginated. See pageSize and pageNumber parameters.")
	public ResponseEntity<PagedResult<Location>> getLocationsByType(@PathVariable String cropname, @RequestParam(value = "locationTypeId") final String locationTypeId, 
			@ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.", required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber, 
			@ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.", required = false) @RequestParam(value = "pageSize", required = false) Integer pageSize) {
	
		PagedResult<Location> result = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<Location>() {
			@Override
			public long getCount() {
				return LocationResource.this.locationService.countLocationByType(locationTypeId);
			}

			@Override
			public List<Location> getResults(PagedResult<Location> pagedResult) {
				return LocationResource.this.locationService.getLocationsByType(locationTypeId, pagedResult.getPageNumber(), pagedResult.getPageSize());
			}
		});
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{cropname}/search", method = RequestMethod.GET, params = {"q"})
	@ApiOperation(value = "Search locations by name", notes = "Search locations by name.")
	public ResponseEntity<PagedResult<Location>> searchLocations(@PathVariable String cropname, @RequestParam final String q,
			@ApiParam(value = "Page number to retrieve in case of multi paged results. Defaults to 1 (first page) if not supplied.", required = false) @RequestParam(value = "pageNumber", required = false) Integer pageNumber, 
			@ApiParam(value = "Number of results to retrieve per page. Defaults to 100 if not supplied. Max page size allowed is 200.", required = false) @RequestParam(value = "pageSize", required = false) Integer pageSize) {
		
		PagedResult<Location> result = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<Location>() {
			@Override
			public long getCount() {
				return LocationResource.this.locationService.countLocationsByName(q);
			}

			@Override
			public List<Location> getResults(PagedResult<Location> pagedResult) {
				return LocationResource.this.locationService.searchLocations(q, pagedResult.getPageNumber(), pagedResult.getPageSize());
			}
		});
		return new ResponseEntity<PagedResult<Location>>(result, HttpStatus.OK);
	}
}
