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

	private BindingResult errors;

	private final List<String> validNameTypes = Arrays.asList("CODE1", "CODE2", "CODE3");

	@Autowired
	private GermplasmValidator germplasmValidator;

	public void validate(final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmCodeNameBatchRequestDto.class.getName());
		this.germplasmValidator.validateGids(this.errors, germplasmCodeNameBatchRequestDto.getGids());
		this.validateNameType(germplasmCodeNameBatchRequestDto.getNameType());
		this.validateGermplasmNameSetting(germplasmCodeNameBatchRequestDto.getGermplasmCodeNameSetting());
		throw new ApiRequestValidationException(this.errors.getAllErrors());
	}

	private void validateGermplasmNameSetting(final GermplasmNameSetting germplasmNameSetting) {
		if (germplasmNameSetting != null) {
			if (StringUtils.isEmpty(germplasmNameSetting.getPrefix())) {
				this.errors.reject("germplasm.code.name.prefix.required");
			} else if (germplasmNameSetting.getPrefix().length() > 49) {
				this.errors.reject("germplasm.code.name.prefix.max.length.exceeded");
			}
			if (StringUtils.isNotEmpty(germplasmNameSetting.getPrefix()) && germplasmNameSetting.getSuffix().length() > 49) {
				this.errors.reject("germplasm.code.name.suffix.max.length.exceeded");
			}
		}
	}

	private void validateNameType(final String nameType) {
		if (!this.validNameTypes.contains(nameType)) {
			this.errors.reject("germplasm.code.name.invalid.name.type");
		}
	}
}
