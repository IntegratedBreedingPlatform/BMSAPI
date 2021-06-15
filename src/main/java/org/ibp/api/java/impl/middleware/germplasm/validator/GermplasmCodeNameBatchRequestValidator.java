package org.ibp.api.java.impl.middleware.germplasm.validator;

import liquibase.util.StringUtils;
import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class GermplasmCodeNameBatchRequestValidator {

	protected static final String CODE_1 = "CODE1";
	protected static final String CODE_2 = "CODE2";
	protected static final String CODE_3 = "CODE3";

	private BindingResult errors;

	private final List<String> validNameTypes = Arrays.asList(CODE_1, CODE_2, CODE_3);

	@Autowired
	private GermplasmValidator germplasmValidator;

	public void validate(final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmCodeNameBatchRequestDto.class.getName());
		this.germplasmValidator.validateGids(this.errors, germplasmCodeNameBatchRequestDto.getGids());
		this.validateNameType(germplasmCodeNameBatchRequestDto.getNameType());
		this.validateGermplasmNameSetting(this.errors, germplasmCodeNameBatchRequestDto.getGermplasmCodeNameSetting());
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateGermplasmNameSetting(final BindingResult errors, final GermplasmNameSetting germplasmNameSetting) {
		if (germplasmNameSetting != null) {
			if (StringUtils.isEmpty(germplasmNameSetting.getPrefix())) {
				errors.reject("germplasm.code.name.prefix.required");
			} else if (germplasmNameSetting.getPrefix().length() > 49) {
				errors.reject("germplasm.code.name.prefix.max.length.exceeded");
			}
			if (StringUtils.isNotEmpty(germplasmNameSetting.getSuffix()) && germplasmNameSetting.getSuffix().length() > 49) {
				errors.reject("germplasm.code.name.suffix.max.length.exceeded");
			}
		}
	}

	private void validateNameType(final String nameType) {
		if (!this.validNameTypes.contains(nameType)) {
			this.errors.reject("germplasm.code.name.invalid.name.type");
		}
	}
}
