
package org.ibp.api.rest.location;

import java.util.Set;

import org.ibp.api.domain.location.LocationType;
import org.ibp.api.java.location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

}
