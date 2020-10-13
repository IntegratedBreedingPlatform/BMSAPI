package org.ibp.api.java.location;

import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.ibp.api.domain.location.LocationDto;

import java.util.List;
import java.util.Set;

public interface LocationService {

	LocationDTO getLocation(Integer locationId);

	List<LocationTypeDTO> getLocationTypes();

	List<LocationDto> getLocations(String crop, String programUUID, Set<Integer> locationTypes, final List<Integer> locationIds,
		List<String> locationAbbreviations, boolean favoriteLocations);
}
