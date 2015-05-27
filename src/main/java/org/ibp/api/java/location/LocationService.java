package org.ibp.api.java.location;

import java.util.List;
import java.util.Set;

import org.ibp.api.domain.location.Location;
import org.ibp.api.domain.location.LocationType;


public interface LocationService {

	Set<LocationType> getAllLocationTypes();
	
	List<Location> getLocationsByType(String locationTypeId, int pageNumber, int pageSize);
	long countLocationByType(String locationTypeId);
	
	List<Location> searchLocations(String searchString, int pageNumber, int pageSize);
	long countLocationsByName(String searchString);
}
