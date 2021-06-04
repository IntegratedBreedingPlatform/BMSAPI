package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttributeValidator {

	private static final List<Integer> ALLOWED_ATTRIBUTE_TYPES =
		Arrays.asList(VariableType.GERMPLASM_ATTRIBUTE.getId(), VariableType.GERMPLASM_PASSPORT.getId());

	private static final Integer GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH = 255;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	public void validateAttributeIds(final BindingResult errors, final List<String> attributeIds) {
		if (attributeIds == null || attributeIds.isEmpty()) {
			return;
		}

		final List<Integer> ids =
			attributeIds.stream().map(attributeId -> Integer.valueOf(attributeId)).distinct().collect(Collectors.toList());

		final List<Attribute> attributes = this.germplasmDataManager.getAttributeByIds(ids);
		if (attributes.size() != ids.size()) {
			errors.reject("attribute.invalid", "");
		}
	}

	public void validateAttributeType(final BindingResult errors, final Integer variableTypeId) {
		if (variableTypeId != null && !AttributeValidator.ALLOWED_ATTRIBUTE_TYPES.contains(variableTypeId)) {
			errors.reject("attribute.variable.type.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateGermplasmAttributeShouldNotExist(final BindingResult errors, final Integer gid, final GermplasmAttributeRequestDto dto,
		final Integer variableTypeId) {
		final List<GermplasmAttributeDto> germplasmAttributeDtos =
			this.germplasmAttributeService.getGermplasmAttributeDtos(gid, variableTypeId);

		final List<GermplasmAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getVariableId().equals(dto.getVariableId())).collect(Collectors.toList());
		if (!existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.name.invalid.existing", new String[] {existingGermplasmAttributes.get(0).getVariableName()}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateGermplasmAttributeExists(final BindingResult errors, final Integer gid, final Integer attributeId) {
		final List<GermplasmAttributeDto> germplasmAttributeDtos = this.germplasmAttributeService.getGermplasmAttributeDtos(gid,
			null);
		final List<GermplasmAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getId().equals(attributeId)).collect(Collectors.toList());
		if(existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.id.invalid.not.existing", new Integer[] {attributeId}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateGermplasmAttributeForUpdate(final BindingResult errors, final Integer gid, final GermplasmAttributeRequestDto dto,
		final Integer attributeId) {
		final List<GermplasmAttributeDto> germplasmAttributeDtos = this.germplasmAttributeService.getGermplasmAttributeDtos(gid, null);


		// Filter by germplasm attribute id
		List<GermplasmAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getId().equals(attributeId)).collect(Collectors.toList());
		if(existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.id.invalid.not.existing", new Integer[] {attributeId}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		// Filter by germplasm attribute Variable Id
		existingGermplasmAttributes = existingGermplasmAttributes.stream()
			.filter(existing -> existing.getVariableId().equals(dto.getVariableId())).collect(Collectors.toList());
		if (existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.variable.id.invalid.not.existing", new Integer[] {dto.getVariableId()}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateAttribute(final BindingResult errors, final Integer gid, final GermplasmAttributeRequestDto dto, final Integer variableTypeId,
		final Integer attributeId) {
		if(attributeId == null) {
			this.validateAttributeType(errors, variableTypeId);
			this.validateGermplasmAttributeShouldNotExist(errors, gid, dto, variableTypeId);
		} else {
			this.validateGermplasmAttributeForUpdate(errors, gid, dto, attributeId);
		}
		this.validateGermplasmAttributeValue(errors, dto.getValue());
		this.validateGermplasmAttributeDate(errors, dto.getDate());
	}

	void validateGermplasmAttributeValue(final BindingResult errors, final String value) {
		if (value != null && value.length() > GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH) {
			errors.reject("attribute.value.invalid.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateGermplasmAttributeDate(final BindingResult errors, final String date) {
		if (!DateUtil.isValidDate(date)) {
			errors.reject("attribute.date.invalid.format", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setGermplasmAttributeService(final  GermplasmAttributeService germplasmAttributeService) {
		this.germplasmAttributeService = germplasmAttributeService;
	}
}
