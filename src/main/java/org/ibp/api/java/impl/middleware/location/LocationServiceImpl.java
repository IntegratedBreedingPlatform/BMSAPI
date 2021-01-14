package org.ibp.api.java.impl.middleware.location;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.location.LocationMapper;
import org.ibp.api.domain.program.ProgramSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.location.LocationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private org.generationcp.middleware.api.location.LocationService locationService;

	@Override
	public LocationDTO getLocation(final Integer locationId) {
		return this.locationService.getLocation(locationId);
	}

	@Override
	public List<LocationTypeDTO> getLocationTypes() {
		return this.locationService.getLocationTypes();
	}

	@Autowired
	private ProgramValidator programValidator;

	@Override
	public long countLocations(final String crop, final String programUUID, final Set<Integer> locationTypes,
		final List<Integer> locationIds, final List<String> locationAbbreviations, final boolean favoriteLocations,
		final String locationName) {

		this.validate(crop, programUUID, favoriteLocations);

		return this.locationDataManager
			.countFilteredLocations(programUUID, locationTypes, locationIds, locationAbbreviations, favoriteLocations, locationName);
	}

	@Override
	public List<LocationDto> getLocations(final String crop, final String programUUID, final Set<Integer> locationTypes,
		final List<Integer> locationIds,
		final List<String> locationAbbreviations, final boolean favoriteLocations, final String locationName, final Pageable pageable) {

		this.validate(crop, programUUID, favoriteLocations);

		final List<org.generationcp.middleware.pojos.Location> locations =
			this.locationDataManager
				.getFilteredLocations(programUUID, locationTypes, locationIds, locationAbbreviations, favoriteLocations, locationName,
					pageable);

		final ModelMapper mapper = LocationMapper.getInstance();
		return locations.stream().map(o -> mapper.map(o, LocationDto.class)).collect(Collectors.toList());
	}

	private void validate(final String crop, final String programUUID, final boolean favoriteLocations) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		if (favoriteLocations && StringUtils.isEmpty(programUUID)) {
			errors.reject("locations.favorite.requires.program", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (programUUID != null) {
			this.programValidator.validate(new ProgramSummary(crop, programUUID), errors);
			if (errors.hasErrors()) {
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}
}
