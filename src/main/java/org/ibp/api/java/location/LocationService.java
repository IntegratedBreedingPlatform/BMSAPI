package org.ibp.api.java.location;

import org.ibp.api.domain.location.LocationDto;

import java.util.List;
import java.util.Set;

public interface LocationService {

	List<LocationDto> getLocations(String crop, String programUUID, Set<Integer> locationTypes, final List<Integer> locationIds,
		List<String> locationAbbreviations, boolean favoriteLocations);
}
