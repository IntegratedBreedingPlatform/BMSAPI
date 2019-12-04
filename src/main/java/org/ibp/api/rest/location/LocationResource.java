package org.ibp.api.rest.location;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.java.location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Api(value = "Location Services")
@RestController
public class LocationResource {

	@Autowired
	LocationService locationService;

	@ApiOperation(value = "List locations", notes = "Get a list of locations filter by types")
	@RequestMapping(value = "/crops/{cropname}/programs/{programUUID}/locations", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<LocationDto>> listFavoriteLocations(
		@PathVariable final String cropname,
		@PathVariable final String programUUID,
		@ApiParam(value = "list of location types")
		@RequestParam final Set<Integer> locationTypes,
		@ApiParam(value = "isFavoriteLocation", required = true)
		@RequestParam final boolean favoriteLocations){

		final List<LocationDto> locations = locationService.getLocations(locationTypes, programUUID, favoriteLocations);
		return new ResponseEntity<>(locations, HttpStatus.OK);

	}
}
