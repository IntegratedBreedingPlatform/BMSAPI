package org.ibp.api.java.impl.middleware.name.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeRequestDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GermplasmNameTypeValidator {

	public static final Integer CODE_OR_NAME_MAX_LENGTH = 50;
	public static final Integer DESCRIPTION_MAX_LENGTH = 255;
	private BindingResult errors;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	public void validate(final GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmNameTypeRequestDTO.class.getName());

		if (StringUtils.isBlank(germplasmNameTypeRequestDTO.getCode())) {
			this.errors.reject("germplasm.name.type.code.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());

		}

		if (germplasmNameTypeRequestDTO.getCode().length() > GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH) {
			this.errors.reject("germplasm.name.type.code.length.invalid",
				new String[] {GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateIfCodeAlreadyExists(germplasmNameTypeRequestDTO.getCode());

		if (StringUtils.isBlank(germplasmNameTypeRequestDTO.getName())) {
			this.errors.reject("germplasm.name.type.name.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (germplasmNameTypeRequestDTO.getName().length() > GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH) {
			this.errors.reject("germplasm.name.type.name.length.invalid",
				new String[] {GermplasmNameTypeValidator.CODE_OR_NAME_MAX_LENGTH.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateIfNameAlreadyExists(germplasmNameTypeRequestDTO.getName());

		if (StringUtils.isNotBlank(germplasmNameTypeRequestDTO.getDescription()) && //
			germplasmNameTypeRequestDTO.getDescription().length() > GermplasmNameTypeValidator.DESCRIPTION_MAX_LENGTH) {
			this.errors.reject("germplasm.name.type.description.length.invalid",
				new String[] {GermplasmNameTypeValidator.DESCRIPTION_MAX_LENGTH.toString()}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	protected void validateIfCodeAlreadyExists(final String code) {
		final Set<String> codes = new HashSet<>(Arrays.asList(code));
		final List<GermplasmNameTypeDTO> nameTypeDTOS = this.germplasmNameTypeService.filterGermplasmNameTypes(codes);
		if (nameTypeDTOS != null && !nameTypeDTOS.isEmpty()) {
			this.errors.reject("germplasm.name.type.code.invalid", new String[] {code}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	protected void validateIfNameAlreadyExists(final String name) {
		final List<GermplasmNameTypeDTO> nameTypeDTOS = this.germplasmNameTypeService.filterGermplasmNameTypesByName(name);
		if (!nameTypeDTOS.isEmpty()) {
			this.errors.reject("germplasm.name.type.name.invalid", new String[] {name}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}
}
