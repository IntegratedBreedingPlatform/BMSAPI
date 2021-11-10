package org.ibp.api.java.impl.middleware.common.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.api.location.LocationService;
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
		final Location location = this.locationDataManager.getLocationByID(locationId);
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

		final List<Location> existingLocations =
			this.locationService.getFilteredLocations(
				new LocationSearchRequest(null, STORAGE_LOCATION_TYPE, null, locationAbbreviations, null), null);
		if (existingLocations.size() != locationAbbreviations.size()) {

			final List<String> existingAbbreviations = existingLocations.stream().map(Location::getLabbr).collect(Collectors.toList());
			final List<String> invalidAbbreviations = new ArrayList<>(locationAbbreviations);
			invalidAbbreviations.removeAll(existingAbbreviations);
			errors.reject("lot.input.invalid.abbreviations", new String[] {Util.buildErrorMessageFromList(invalidAbbreviations, 3)}, "");
		}
	}

	public void validateLocation(final BindingResult errors, final Integer locationId) {
		if (locationId == null) {
			errors.reject("location.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Location location = locationDataManager.getLocationByID(locationId);

		if (location == null) {
			errors.reject("location.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateCanBeDeleted(final Integer locationId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		this.validateExistingLocationId(locationId);
		this.validateLocationBelongsToGermplasms(locationId);
		this.validateLocationBelongsToLots(locationId);
		this.validateLocationBelongsToAttributes(locationId);
		this.validateLocationBelongsToNames(locationId);
		this.validateLocationBelongsToStudies(locationId);
	}

	private void validateLocationBelongsToLots(final Integer locationId) {
		boolean isLocationUsedInLots = this.lotService.isLocationIdUsedInLots(locationId);
		if (isLocationUsedInLots) {
			this.errors.reject("location.is.used.in.lots", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationBelongsToNames(final Integer locationId) {
		boolean isLocationUsedInNames = this.germplasmNameService.isLocationIdUsedInGermplasmNames(locationId);
		if (isLocationUsedInNames) {
			this.errors.reject("location.is.used.in.names", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationBelongsToAttributes(final Integer locationId) {
		boolean isLocationUsedInAttributes = this.germplasmAttributeService.isLocationIdUsedInAttributes(locationId);
		if (isLocationUsedInAttributes) {
			this.errors.reject("location.is.used.in.attributes", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationBelongsToGermplasms(final Integer locationId) {
		final boolean isLocationUsedInGermplasm = this.germplasmService.isLocationIdUsedInGermplasms(locationId);
		if (isLocationUsedInGermplasm) {
			this.errors.reject("location.is.used.in.germplasms", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLocationBelongsToStudies(final Integer locationId) {
		final boolean isLocationUsedInStudies = this.studyService.isLocationIdUsedInStudies(locationId);
		if (isLocationUsedInStudies) {
			this.errors.reject("location.is.used.in.studies", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateExistingLocationId(final Integer locationId) {
		this.validateLocation(this.errors, locationId);
	}

}
