package org.ibp.api.java.location;

import org.ibp.api.domain.location.LocationDto;

import java.util.List;
import java.util.Set;

public interface LocationService {

	List<LocationDto> getLocations(final Set<Integer> locationTypes, final String programUUID, final boolean favoriteLocations);
}
