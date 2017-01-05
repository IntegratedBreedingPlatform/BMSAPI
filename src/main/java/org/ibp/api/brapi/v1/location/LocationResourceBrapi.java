
package org.ibp.api.brapi.v1.location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.service.api.location.AdditionalInfoDto;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
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
					required = false) @RequestParam(value = "pageSize", required = false) Integer pageSize,
			@ApiParam(value = "name of location type", required = false) @RequestParam(value = "locationType",
					required = false) String locationType) {

		final HashMap<String, String> filtersMap = new HashMap<String, String>();
		
		if (!StringUtils.isBlank(locationType)) {
			final Integer locationTypeId = LocationResourceBrapi.this.locationDataManager
					.getUserDefinedFieldIdOfName(org.generationcp.middleware.pojos.UDTableType.LOCATION_LTYPE, locationType);
			if (locationTypeId != null) {
				filtersMap.put("locationType", locationTypeId.toString());

			} else {
				Map<String, String> status = new HashMap<String, String>();
				status.put("message", "the filter do not return values");
				Metadata metadata = new Metadata(null,status);
				Locations locationList = new Locations().withMetadata(metadata);
				return new ResponseEntity<>(locationList, HttpStatus.NOT_FOUND);
			}
		}

		final Map<Integer, AdditionalInfoDto> mapAdditionalInfo = this.locationDataManager.getListAdditinalInfoLocation();

		
		PagedResult<LocationDetailsDto> resultPage =
				new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<LocationDetailsDto>() {

					@Override
					public long getCount() {
						return LocationResourceBrapi.this.locationDataManager.countLocationsByFilter(filtersMap);
					}

					@Override
					public List<LocationDetailsDto> getResults(PagedResult<LocationDetailsDto> pagedResult) {
						return LocationResourceBrapi.this.locationDataManager.getLocalLocationsByFilter(pagedResult.getPageNumber(),
								pagedResult.getPageSize(), filtersMap);
					}
				});

		List<Location> locations = new ArrayList<>();

		for (org.generationcp.middleware.service.api.location.LocationDetailsDto mwLoc : resultPage.getPageResults()) {
			Location location = new Location();
			location.setLocationDbId(mwLoc.getLocationDbId());
			location.setName(mwLoc.getName());
			location.setAbbreviation(mwLoc.getAbbreviation());

			location.setLocationType(!StringUtils.isBlank(mwLoc.getLocationType())
					? WordUtils.capitalize(mwLoc.getLocationType().toLowerCase()) : "Unknown");

			location.setCountryCode(!StringUtils.isBlank(mwLoc.getCountryCode()) ? mwLoc.getCountryCode() : "Unknown");
			location.setCountryName(!StringUtils.isBlank(mwLoc.getCountryName()) ? mwLoc.getCountryName() : "Unknown");

			location.setLatitude(mwLoc.getLatitude());
			location.setLongitude(mwLoc.getLongitude());
			location.setAltitude(mwLoc.getAltitude());

			final Integer idLocation = mwLoc.getLocationDbId();
			if (!mapAdditionalInfo.isEmpty() && mapAdditionalInfo.containsKey(idLocation)) {
				final AdditionalInfoDto additionalInfo = mapAdditionalInfo.get(idLocation);
				location.setAdditionalInfo(additionalInfo.getToMap());
			}
			locations.add(location);
		}

		Result<Location> results = new Result<Location>().withData(locations);
		Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
				.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		Metadata metadata = new Metadata().withPagination(pagination);
		Locations locationList = new Locations().withMetadata(metadata).withResult(results);

		return new ResponseEntity<Locations>(locationList, HttpStatus.OK);
	}

}
