package org.ibp.api.rest.location;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
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

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@ApiOperation(value = "Get location")
	@RequestMapping(value = "/crops/{cropName}/locations/{locationId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<LocationDTO> getLocation(
		@PathVariable final String cropName,
		@PathVariable final Integer locationId,
		@RequestParam(required = false) final String programUUID) {

		return new ResponseEntity<>(this.locationService.getLocation(locationId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get location types")
	@RequestMapping(value = "/crops/{cropName}/location-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<LocationTypeDTO>> getLocationTypes(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID) {

		return new ResponseEntity<>(this.locationService.getLocationTypes(), HttpStatus.OK);
	}


	@ApiOperation(value = "List locations", notes = "Get a list of locations filter by types")
	@RequestMapping(value = "/crops/{cropname}/locations", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<LocationDto>> listFavoriteLocations(
		@PathVariable final String cropname,
		@RequestParam(required = false) final String programUUID,
		@ApiParam(value = "list of location types")
		@RequestParam(required = false) final Set<Integer> locationTypes,
		@ApiParam(value = "isFavoriteLocation", required = true)
		@RequestParam final boolean favoriteLocations) {

		return new ResponseEntity<>(locationService.getLocations(cropname, programUUID, locationTypes, null, null, favoriteLocations),
			HttpStatus.OK);

	}
}
