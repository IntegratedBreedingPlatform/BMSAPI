package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.germplasm.GermplasmBasicDetailsDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;

@Component
public class GermplasmBasicDetailsValidator {

	static final Integer REFERENCE_MAX_LENGTH = 255;

	private BindingResult errors;

	@Autowired
	private LocationService locationService;

	public void validate(final GermplasmBasicDetailsDto germplasmBasicDetailsDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmBasicDetailsDto.class.getName());
		BaseValidator.checkNotNull(germplasmBasicDetailsDto, "germplasm.import.request.null");
		if (germplasmBasicDetailsDto.getBreedingLocationId() != null) {
			if (this.locationService
				.searchLocations(
					new LocationSearchRequest(null,
						Collections.singletonList(germplasmBasicDetailsDto.getBreedingLocationId()), null,
						null),
					null, null).isEmpty()) {
				this.errors.reject("germplasm.update.breeding.location.invalid", "");
			}
		}
		if (StringUtils.isNotEmpty(germplasmBasicDetailsDto.getReference())
			&& germplasmBasicDetailsDto.getReference().length() > REFERENCE_MAX_LENGTH) {
			this.errors.reject("germplasm.import.reference.length.error", "");
		}

		if (StringUtils.isNotEmpty(germplasmBasicDetailsDto.getCreationDate()) && !DateUtil
			.isValidDate(germplasmBasicDetailsDto.getCreationDate())) {
			this.errors.reject("germplasm.import.creation.date.invalid", "");
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
