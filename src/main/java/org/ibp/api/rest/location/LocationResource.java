
package org.ibp.api.rest.location;

import java.util.Set;

import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.location.Location;
import org.ibp.api.domain.location.LocationType;
import org.ibp.api.java.location.LocationService;
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

	@RequestMapping(value = "/{cropname}", method = RequestMethod.GET, params = {"locationTypeId", "pageNumber", "pageSize"})
	@ApiOperation(value = "Get all location by type", notes = "Returns all location of a given type.")
	public ResponseEntity<PagedResult<Location>> getLocationByType(@PathVariable String cropname, @RequestParam(value = "locationTypeId") String locationTypeId, @RequestParam(
			value = "pageNumber") Integer pageNumber, @RequestParam(value = "pageSize") Integer pageSize) {

		// Initialise page parameters/metadata and validate.
		long totalResults = this.locationService.countLocationByType(locationTypeId);
		PagedResult<Location> result = new PagedResult<>(pageNumber, pageSize, totalResults);

		// Get results and add to page.
		result.addPageResults(this.locationService.getLocationsByType(locationTypeId, pageNumber, pageSize));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}
