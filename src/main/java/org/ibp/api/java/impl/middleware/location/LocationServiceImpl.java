package org.ibp.api.java.impl.middleware.location;

import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationRequestDto;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.api.program.ProgramFavoriteService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

	public static final List<Integer> RESTRICTED_LOCATION_TYPES = Arrays.asList(401, 405, 406);


	private enum DEFAULT_LOCATION_TYPE {
		BREEDING_LOCATION("BREEDING_LOCATION"), STORAGE_LOCATION("STORAGE_LOCATION");
		private final String name;

		DEFAULT_LOCATION_TYPE(final String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}


	@Autowired
	private org.generationcp.middleware.api.location.LocationService locationMiddlewareService;

	@Autowired
	private LocationValidator locationValidator;

	@Autowired
	private ProgramFavoriteService programFavoriteService;

	@Override
	public LocationDTO getLocation(final Integer locationId) {
		return this.locationMiddlewareService.getLocation(locationId);
	}

	@Override
	public List<LocationTypeDTO> getLocationTypes(final Boolean excludeRestrictedTypes) {
		if (Boolean.TRUE.equals(excludeRestrictedTypes)) {
			return this.locationMiddlewareService.getLocationTypes().stream()
				.filter((locationType) -> !LocationServiceImpl.RESTRICTED_LOCATION_TYPES.contains(locationType.getId())).collect(
					Collectors.toList());
		}
		return this.locationMiddlewareService.getLocationTypes();
	}

	@Override
	public long countLocations(
		final String crop, final LocationSearchRequest locationSearchRequest,
		final String programUUID) {
		return this.locationMiddlewareService
			.countFilteredLocations(locationSearchRequest, programUUID);
	}

	@Override
	public List<LocationDTO> searchLocations(
		final String crop, final LocationSearchRequest locationSearchRequest,
		final Pageable pageable, final String programUUID) {
		return this.locationMiddlewareService.searchLocations(locationSearchRequest, pageable, programUUID);
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

	@Override
	public List<LocationDTO> getCountries() {
		return this.locationMiddlewareService.getCountries();
	}

	@Override
	public long countFilteredLocations(final LocationSearchRequest locationSearchRequest, final String programUUID) {
		return this.locationMiddlewareService.countFilteredLocations(locationSearchRequest, programUUID);
	}

	@Override
	public List<Location> getLocations(final LocationSearchRequest locationSearchRequest, final Pageable pageable) {
		return this.locationMiddlewareService.getLocations(locationSearchRequest, pageable);
	}

	@Override
	public LocationDTO getDefaultLocation(final String programUUID, final String defaultLocationType) {
		if (DEFAULT_LOCATION_TYPE.BREEDING_LOCATION.getName().equalsIgnoreCase(defaultLocationType)) {
			return this.locationMiddlewareService.getDefaultBreedingLocation(programUUID);
		} else if (DEFAULT_LOCATION_TYPE.STORAGE_LOCATION.getName().equalsIgnoreCase(defaultLocationType)) {
			return this.locationMiddlewareService.getDefaultStorageLocation(programUUID);
		} else {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
			final List<String> defaultLocationTypes =
				Arrays.stream(DEFAULT_LOCATION_TYPE.values()).map(DEFAULT_LOCATION_TYPE::getName).collect(Collectors.toList());
			errors.reject("location.invalid.default.location.type",
				new String[] {String.join(", ", defaultLocationTypes)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
