package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.text.ParseException;
import java.util.HashMap;

@Component
public class GermplasmNameValidator {

	private static final Integer LOT_NOTES_MAX_LENGTH = 255;

	private BindingResult errors;

	@Autowired
	GermplasmValidator germplasmValidator;

	@Autowired
	GermplasmNameService germplasmNameService;

	public void validate(final GermplasmNameRequestDto germplasmNameRequestDto, final String programUUID) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		germplasmValidator.validateGermplasmId(errors, germplasmNameRequestDto.getGid());

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (germplasmNameRequestDto.getId() != null) {
			final Name name = germplasmNameService.getNameByNameId(germplasmNameRequestDto.getId());
			this.validateGermplasmName(errors, germplasmNameRequestDto, name);

			if (!StringUtils.isBlank(germplasmNameRequestDto.getNameTypeCode())) {
				this.validateNameTypeCode(errors, germplasmNameRequestDto);
			}
			if (germplasmNameRequestDto.getName() != null) {
				this.validateNameLength(errors, germplasmNameRequestDto);
			}

			if (germplasmNameRequestDto.getDate() != null) {
				this.validateNameDate(errors, germplasmNameRequestDto);
			}

			if (germplasmNameRequestDto.isPreferredName() != null) {
				this.validatePreferredNameUpdatable(errors, germplasmNameRequestDto, name);
			}
		} else {
			this.validateNameTypeCode(errors, germplasmNameRequestDto);
			this.validateNameLength(errors, germplasmNameRequestDto);
			this.validateNameDate(errors, germplasmNameRequestDto);
			this.validatePreferredName(errors,germplasmNameRequestDto);
		}
	}

	public void validatePreferredName(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) {
		if(germplasmNameRequestDto.isPreferredName() != null){
			errors.reject("germplasm.name.preferred.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateDeleteName(final GermplasmNameRequestDto germplasmNameRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), GermplasmNameRequestDto.class.getName());
		germplasmValidator.validateGermplasmId(errors, germplasmNameRequestDto.getGid());

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Name name = germplasmNameService.getNameByNameId(germplasmNameRequestDto.getId());
		this.validateGermplasmName(errors, germplasmNameRequestDto, name);
		this.validateDeletepreferredName(errors, name);

	}

	protected void validateGermplasmName(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto,
		final Name name) {
		if (name == null || !name.getGermplasmId().equals(germplasmNameRequestDto.getGid())) {
			errors.reject("germplasm.name.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());

		}
	}

	protected void validateDeletepreferredName(final BindingResult errors, final Name name) {
		if (name.getNstat().equals(1)) {
			errors.reject("germplasm.name.preferred.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());

		}
	}

	protected void validatePreferredNameUpdatable(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto,
		final Name name) {
		if (!germplasmNameRequestDto.isPreferredName() && name.getNstat().equals(1)) {
			errors.reject("germplasm.name.preferred.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	protected void validateNameLength(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) {
		if (StringUtils.isBlank(germplasmNameRequestDto.getName())) {
			errors.reject("germplasm.name.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());

		}

		if (germplasmNameRequestDto.getName().length() > LOT_NOTES_MAX_LENGTH) {
			errors.reject("germplasm.name.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());

		}
	}

	protected void validateNameDate(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) {
		if (germplasmNameRequestDto.getDate() == null) {
			errors.reject("germplasm.name.date.required", "");
		}

		try {
			Util.getSimpleDateFormat(Util.DATE_AS_NUMBER_FORMAT).parse(germplasmNameRequestDto.getDate().toString()); // verificar esto.
		} catch (ParseException e) {
			errors.reject("germplasm.name.date.invalid", new Object[] {
					germplasmNameRequestDto.getDate().toString()},
				"Invalid date value found.");
		}
	}

	protected void validateNameTypeCode(final BindingResult errors, final GermplasmNameRequestDto germplasmNameRequestDto) {
		final UserDefinedField userDefinedField = this.germplasmNameService.getNameType(germplasmNameRequestDto.getNameTypeCode());
		if (userDefinedField == null || !userDefinedField.getFtable().equals("NAMES") || !userDefinedField.getFtype().equals("NAME")) {
			errors.reject("germplasm.name.type.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}


	public void setGermplasmNameService(final GermplasmNameService germplasmNameService) {
		this.germplasmNameService = germplasmNameService;
	}


	public  GermplasmNameService getGermplasmNameService() {
		return this.germplasmNameService;
	}
}
