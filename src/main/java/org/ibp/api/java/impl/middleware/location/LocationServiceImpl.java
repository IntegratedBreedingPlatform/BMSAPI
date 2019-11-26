package org.ibp.api.java.impl.middleware.location;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.location.LocationMapper;
import org.ibp.api.java.location.LocationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

	private static final int SEED_STORAGE_LOCATION_TYPE = 1500;
	private static final int DEFAULT_SEED_STORAGE_LOCATION = 6000;

	@Autowired
	LocationDataManager locationDataManager;

	@Override
	public List<LocationDto> getLocations(final Set<Integer> locationTypes, final String programUUID, final boolean isFavoriteLocation) {
		final List<org.generationcp.middleware.pojos.Location> locations;
		final ModelMapper mapper = LocationMapper.getInstance();

		if (isFavoriteLocation) {
			final List<Integer> locIds = locationDataManager.getFavoriteProjectLocationIds(programUUID);
			locations = locationDataManager.getFavoriteLocationsByTypes(locationTypes, locIds);
		} else {
			locations = locationDataManager.getLocationsByTypes(locationTypes, programUUID);

		}

		final List<LocationDto> locationList = locations.stream().map(o -> mapper.map(o, LocationDto.class)).collect(Collectors.toList());
		locationList.forEach(location -> {
			if (location.getId() == LocationServiceImpl.DEFAULT_SEED_STORAGE_LOCATION && locationTypes != null && locationTypes
				.contains(LocationServiceImpl.SEED_STORAGE_LOCATION_TYPE)) {
				location.setDefaultLocation(true);
			}
		});

		return locationList;
	}
}
