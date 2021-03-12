package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class GermplasmNameValidator {

	private static final Integer LOT_NOTES_MAX_LENGTH = 255;

	@Autowired
	GermplasmValidator germplasmValidator;

	@Autowired
	GermplasmNameService germplasmNameService;

	@Autowired
	LocationValidator locationValidator;

	public void validate(final GermplasmNameRequestDto germplasmNameRequestDto) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmNameRequestDto.class.getName());
		germplasmValidator.validateGermplasmId(errors, germplasmNameRequestDto.getGid());

		final Name name = germplasmNameService.getNameByNameId(germplasmNameRequestDto.getId());
		this.ValidateGermplasmName(errors, germplasmNameRequestDto, name);
		this.ValidatepreferredName(errors, name);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	protected void ValidateGermplasmName(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto, final Name name) {
		if (name == null || !name.getGermplasmId().equals(germplasmNameRequestDto.getGid())) {
			errors.reject("germplasm.name.invalid", "");
		}
	}

	protected void ValidatepreferredName(final BindingResult errors, final Name name) {
		if (name.getNstat().equals(1)) {
			errors.reject("germplasm.name.preferred.invalid", "");
		}
	}

	public void validateUpdateName(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) {
		germplasmValidator.validateGermplasmId(errors, germplasmNameRequestDto.getGid());

		final Name name = germplasmNameService.getNameByNameId(germplasmNameRequestDto.getId());
		this.ValidateGermplasmName(errors, germplasmNameRequestDto, name);

		if (germplasmNameRequestDto.getTypeId() != null) {
			this.validateNameType(errors, germplasmNameRequestDto);
		}
		if (germplasmNameRequestDto != null && germplasmNameRequestDto.getName() != null) {
			this.validateNameLength(errors, germplasmNameRequestDto);
		}

		if (germplasmNameRequestDto != null && germplasmNameRequestDto.getDate() != null) {
			this.validateDate(errors, germplasmNameRequestDto);
		}

		if (germplasmNameRequestDto != null && germplasmNameRequestDto.getLocationId() != null) {
			locationValidator.validateLocation(errors, germplasmNameRequestDto.getLocationId());
		}
	}

	public void validateCreateName(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) {
		germplasmValidator.validateGermplasmId(errors, germplasmNameRequestDto.getGid());
		this.validateNameType(errors, germplasmNameRequestDto);
		this.validateNameLength(errors, germplasmNameRequestDto);
		this.validateDate(errors, germplasmNameRequestDto);
		locationValidator.validateLocation(errors, germplasmNameRequestDto.getLocationId());
	}

	public void validateNameLength(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) {
		if (StringUtils.isBlank(germplasmNameRequestDto.getName())){
			errors.reject("germplasm.name.required", "");
		}
		if (germplasmNameRequestDto.getName().length() > LOT_NOTES_MAX_LENGTH) {
			errors.reject("germplasm.name.length", "");
		}
	}

	public void validateDate(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) {
		Util.getSimpleDateFormat(Util.DATE_AS_NUMBER_FORMAT).format(germplasmNameRequestDto.getDate()); // verificar esto.
	}

	public void validateNameType(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) { // Verify to use
		final UserDefinedField userDefinedField = this.germplasmNameService.getNameType(germplasmNameRequestDto.getTypeId());
		if (userDefinedField != null || !userDefinedField.getFtable().equals("NAMES") || !userDefinedField.getFtype().equals("NAME")) {
			errors.reject("germplasm.name.type.invalid", "");

		}
	}
}
