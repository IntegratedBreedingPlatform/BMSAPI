package org.ibp.api.java.impl.middleware.location;

import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationRequestDto;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.location.validator.LocationSearchRequestValidator;
import org.ibp.api.java.location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

	@Autowired
	private org.generationcp.middleware.api.location.LocationService locationMiddlewareService;

	@Autowired
	private LocationValidator locationValidator;

	@Override
	public LocationDTO getLocation(final Integer locationId) {
		return this.locationMiddlewareService.getLocation(locationId);
	}

	@Override
	public List<LocationTypeDTO> getLocationTypes() {
		return this.locationMiddlewareService.getLocationTypes();
	}

	@Override
	public long countLocations(final String crop, final LocationSearchRequest locationSearchRequest,
			final String programUUID) {
		return this.locationMiddlewareService
			.countFilteredLocations(locationSearchRequest, programUUID);
	}

	@Override
	public List<LocationDTO> searchLocations(final String crop, final LocationSearchRequest locationSearchRequest,
			final Pageable pageable, final String programUUID) {
		return this.locationMiddlewareService.searchLocations(locationSearchRequest,	pageable, programUUID);
	}

	@Override
	public void deleteLocation(final Integer locationId) {
		this.locationValidator.validateCanBeDeleted(locationId);
		this.locationMiddlewareService.deleteLocation(locationId);
	}

	@Override
	public LocationDTO createLocation(final LocationRequestDto locationRequestDto) {
		this.locationValidator.validateCreation(locationRequestDto);
		return this.locationMiddlewareService.createLocation(locationRequestDto);
	}

	@Override
	public boolean updateLocation(final Integer locationId, final LocationRequestDto locationRequestDto) {
		this.locationValidator.validateUpdate(locationId, locationRequestDto);

		if (locationRequestDto.allAttributesNull()) {
			return false;
		}

		this.locationMiddlewareService.updateLocation(locationId, locationRequestDto);
		return true;
	}

}
