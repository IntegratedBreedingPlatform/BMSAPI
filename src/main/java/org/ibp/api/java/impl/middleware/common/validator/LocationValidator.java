package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationRequestDto;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.LocationTypeDTO;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LocationValidator {

	private static final Set<Integer> STORAGE_LOCATION_TYPE = new HashSet<>(Arrays.asList(1500));
	private static final Integer LOCATION_NAME_MAX_LENGTH = 60;
	private static final Integer LOCATION_ABBR_MAX_LENGTH = 12;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private LocationService locationService;

	@Autowired
	private GermplasmNameService germplasmNameService;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private StudyService studyService;

	@Autowired
	private LotService lotService;

	private BindingResult errors;

	public void validateSeedLocationId(final BindingResult errors, final Integer locationId) {
		if (locationId == null) {
			errors.reject("location.required", "");
			return;
		}
		final LocationDTO location = this.locationService.getLocation(locationId);
		if (location == null) {
			errors.reject("location.invalid", "");
			return;
		}
		final List<Location> locationList = this.locationDataManager.getAllSeedingLocations(Lists.newArrayList(locationId));
		if (locationList.isEmpty()) {
			errors.reject("seed.location.invalid", "");
			return;
		}
	}

	public void validateSeedLocationAbbr(final BindingResult errors, final List<String> locationAbbreviations) {
		if (locationAbbreviations.stream().anyMatch(s -> StringUtils.isBlank(s))) {
			errors.reject("lot.input.list.location.null.or.empty", "");
			return;
		}

		final List<LocationDTO> existingLocations =
			this.locationService.searchLocations(
				new LocationSearchRequest(null, STORAGE_LOCATION_TYPE, null, locationAbbreviations, null), null, null);
		if (existingLocations.size() != locationAbbreviations.size()) {

			final List<String> existingAbbreviations = existingLocations.stream().map(LocationDTO::getAbbreviation).collect(Collectors.toList());
			final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
			invalidAbbreviations.removeAll(existingAbbreviations);
			errors.reject("lot.input.invalid.abbreviations", new String[] {Util.buildErrorMessageFromList(invalidAbbreviations, 3)}, "");
		}
	}

	private void validateLocationName(final String locationName) {
		if(StringUtils.isBlank(locationName)) {
			this.errors.reject("location.name.is.required", null, "");
		}

		if (locationName.length() > LocationValidator.LOCATION_NAME_MAX_LENGTH) {
			this.errors.reject("location.name.max.length", new String[] {LocationValidator.LOCATION_NAME_MAX_LENGTH.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationAbbr(final String locationAbbr) {
		if(StringUtils.isBlank(locationAbbr)) {
			this.errors.reject("location.abbr.is.required", null, "");
		}

		if (locationAbbr.length() > LocationValidator.LOCATION_ABBR_MAX_LENGTH) {
			this.errors.reject("location.abbr.max.length", new String[] {LocationValidator.LOCATION_ABBR_MAX_LENGTH.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateLocation(final BindingResult errors, final Integer locationId) {
		if (locationId == null) {
			errors.reject("location.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final LocationDTO location = this.locationService.getLocation(locationId);

		if (location == null) {
			errors.reject("location.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateCreation(final LocationRequestDto locationRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), LocationRequestDto.class.getName());

		this.validateLocationName(locationRequestDto.getName());
		this.validateLocationType(locationRequestDto.getType());
		this.validateLocationAbbr(locationRequestDto.getAbbreviation());
		this.validateLocationAbbrNotExists(locationRequestDto.getAbbreviation());
		this.validateCountryAndProvince(locationRequestDto.getCountryId(), locationRequestDto.getProvinceId());

	}

	private void validateCountryAndProvince(final Integer countryId, final Integer provinceId) {
		if (countryId != null) {
			this.validateLocationId(countryId, "location.country.invalid");
		}

		if (provinceId != null) {
			if (countryId == null) {
				this.errors.reject("location.country.required.when.province.is.defined", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
			this.validateLocationId(provinceId, "location.province.invalid");
		}

		if (countryId != null && provinceId != null && !provinceId.equals(0)) {
			this.validateIfProvinceBelongToCountry(countryId, provinceId);
		}
	}

	private void validateLocationId(final Integer locationId, final String errorCode) {
		final LocationDTO locationDTO = this.locationService.getLocation(locationId);
		if (locationDTO == null) {
			this.errors.reject(errorCode, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateIfProvinceBelongToCountry(final Integer countryId, final Integer provinceId) {
		final LocationDTO locationDTO = this.locationService.getLocation(provinceId);
		if (!locationDTO.getCountryId().equals(countryId)) {
			this.errors.reject("location.province.not.belong.to.country", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateUpdate(final Integer locationId, final LocationRequestDto locationRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), LocationRequestDto.class.getName());

		this.validateLocation(this.errors, locationId);
		this.validateLocationName(locationRequestDto.getName());
		this.validateLocationType(locationRequestDto.getType());
		this.validateLocationAbbr(locationRequestDto.getAbbreviation());
		this.validateLocationAbbrNotExists(locationId, locationRequestDto.getAbbreviation());

		final LocationDTO locationDTO = this.locationService.getLocation(locationId);

		final Integer countryId = locationRequestDto.getCountryId();
		final Integer provinceId = locationRequestDto.getProvinceId();
		this.validateCountryAndProvince(countryId, provinceId);
	}

	public void validateCanBeDeleted(final Integer locationId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		this.validateLocation(this.errors, locationId);
		this.validateLocationNotUsedInGermplasm(locationId);
		this.validateLocationNotUsedInLot(locationId);
		this.validateLocationNotUsedInAttribute(locationId);
		this.validateLocationNotUsedInName(locationId);
		this.validateLocationNotUsedInStudy(locationId);
	}

	private void validateLocationNotUsedInLot(final Integer locationId) {
		final boolean isLocationUsedInLots = this.lotService.isLocationUsedInLot(locationId);
		if (isLocationUsedInLots) {
			this.errors.reject("location.is.used.in.lots", new String[] {locationId.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationNotUsedInName(final Integer locationId) {
		final boolean isLocationUsedInNames = this.germplasmNameService.isLocationUsedInGermplasmName(locationId);
		if (isLocationUsedInNames) {
			this.errors.reject("location.is.used.in.names", new String[] {locationId.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationNotUsedInAttribute(final Integer locationId) {
		final boolean isLocationUsedInAttributes = this.germplasmAttributeService.isLocationUsedInAttribute(locationId);
		if (isLocationUsedInAttributes) {
			this.errors.reject("location.is.used.in.attributes", new String[] {locationId.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationNotUsedInGermplasm(final Integer locationId) {
		final boolean isLocationUsedInGermplasm = this.germplasmService.isLocationUsedInGermplasm(locationId);
		if (isLocationUsedInGermplasm) {
			this.errors.reject("location.is.used.in.germplasm", new String[] {locationId.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationNotUsedInStudy(final Integer locationId) {
		final boolean isLocationUsedInStudies = this.studyService.isLocationUsedInStudy(locationId);
		if (isLocationUsedInStudies) {
			this.errors.reject("location.is.used.in.studies", new String[] {locationId.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationAbbrNotExists(final Integer locationId, final String locationAbbr) {
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationAbbreviations(Arrays.asList(locationAbbr));
		final List<org.generationcp.middleware.api.location.Location> locationList =
			this.locationService.getLocations(locationSearchRequest, null);

		if (!locationList.isEmpty()) {
			final List<String> locationAbbrIds = locationList.stream() //
				.map(org.generationcp.middleware.api.location.Location::getLocationDbId) //
				.collect(Collectors.toList());
			locationAbbrIds.removeAll(Arrays.asList(locationId.toString()));
			if (!locationAbbrIds.isEmpty()) {
				this.errors.reject("location.abbr.is.in.used", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

	private void validateLocationAbbrNotExists(final String locationAbbr) {
		final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
		locationSearchRequest.setLocationAbbreviations(Arrays.asList(locationAbbr));
		final List<org.generationcp.middleware.api.location.Location> locationList =
			this.locationService.getLocations(locationSearchRequest, null);

		if (!locationList.isEmpty()) {
			this.errors.reject("location.abbr.is.in.used", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationType(final Integer locationTypeId) {
		if (locationTypeId == null) {
			this.errors.reject("location.type.is.required", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<LocationTypeDTO> locationTypeDTOS = this.locationService.getLocationTypes();
		final List<LocationTypeDTO> locationTypeDTO =
			locationTypeDTOS.stream().filter(locType -> locType.getId().equals(locationTypeId)).collect(Collectors.toList());
		if (Collections.isEmpty(locationTypeDTO)) {
			this.errors.reject("location.type.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
