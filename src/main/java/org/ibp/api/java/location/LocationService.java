package org.ibp.api.java.location;

import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.ibp.api.domain.location.LocationDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LocationService {

	LocationDTO getLocation(Integer locationId);

	List<LocationTypeDTO> getLocationTypes();

	long countLocations(String crop, LocationSearchRequest locationSearchRequest);

	List<LocationDto> getLocations(String crop, LocationSearchRequest locationSearchRequest, Pageable pageable);

	void deleteLocation(Integer locationId);

}
