package org.ibp.api.java.location;

import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationRequestDto;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LocationService {

	LocationDTO getLocation(Integer locationId);

	LocationDTO getBreedingLocationDefault(String programUUID);

	List<LocationTypeDTO> getLocationTypes(Boolean excludeRestrictedTypes);

	long countLocations(String crop, LocationSearchRequest locationSearchRequest, String programUUID);

	List<LocationDTO> searchLocations(
		String crop, LocationSearchRequest locationSearchRequest, Pageable pageable,
		String programUUID);

	void deleteLocation(Integer locationId);

	LocationDTO createLocation(LocationRequestDto locationRequestDto);

	boolean updateLocation(Integer locationId, LocationRequestDto locationRequestDto);

	List<LocationDTO> getCountries();
}
