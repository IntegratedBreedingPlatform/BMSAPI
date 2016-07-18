
package org.ibp.api.brapi.v1.location;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Country;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

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
	public ResponseEntity<Locations> listLocations(@PathVariable final String crop) {

		final List<org.generationcp.middleware.pojos.Location> allLocations = this.locationDataManager.getAllLocations(0, 20);
		List<Location> locations = new ArrayList<>();

		for (org.generationcp.middleware.pojos.Location mwLoc : allLocations) {
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
		Pagination pagination = new Pagination().withPageNumber(1).withPageSize(10).withTotalCount(200).withTotalPages(20);

		Metadata metadata = new Metadata().withPagination(pagination);
		Locations locationList = new Locations().withMetadata(metadata).withResult(results);

		return new ResponseEntity<Locations>(locationList, HttpStatus.OK);
	}
}
