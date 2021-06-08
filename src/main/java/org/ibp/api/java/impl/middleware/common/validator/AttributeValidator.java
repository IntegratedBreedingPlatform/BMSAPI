package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttributeValidator {

	private static final List<VariableType> ALLOWED_ATTRIBUTE_TYPES =
		Arrays.asList(VariableType.GERMPLASM_ATTRIBUTE, VariableType.GERMPLASM_PASSPORT);

	private static final List<Integer> ALLOWED_ATTRIBUTE_TYPES_IDS =
		Arrays.asList(VariableType.GERMPLASM_ATTRIBUTE.getId(), VariableType.GERMPLASM_PASSPORT.getId());

	private static final Integer GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH = 255;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private VariableValueValidator variableValueValidator;

	public void validateAttributeType(final BindingResult errors, final Integer attributeType) {
		if (!AttributeValidator.ALLOWED_ATTRIBUTE_TYPES_IDS.contains(attributeType)) {
			errors.reject("attribute.variable.type.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

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

	public void validateAttributeVariable(final BindingResult errors, final Variable variable) {
		if (variable == null) {
			errors.reject("attribute.variable.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (!CollectionUtils.containsAny(variable.getVariableTypes(), ALLOWED_ATTRIBUTE_TYPES)) {
			errors.reject("attribute.variable.type.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateVariableDataTypeValue(final BindingResult errors, final Variable variable, final String value) {
		if (!variableValueValidator.isValidAttributeValue(variable, value)) {
			errors.reject("invalid.variable.value", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateGermplasmAttributeShouldNotExist(final BindingResult errors, final Integer gid, final GermplasmAttributeRequestDto dto) {
		//TODO Implement new service to get by gid and variable
		final List<GermplasmAttributeDto> germplasmAttributeDtos =
			this.germplasmAttributeService.getGermplasmAttributeDtos(gid, null);
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
		//TODO Implement new service to get by attribute id
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

	public void validateAttribute(final BindingResult errors, final Integer gid, final GermplasmAttributeRequestDto dto,
		final Integer attributeId) {
		BaseValidator.checkNotNull(dto, "param.null", new String[] {"request body"});
		BaseValidator.checkNotNull(dto.getVariableId(), "param.null", new String[] {"variableId"});
		BaseValidator.checkNotEmpty(dto.getValue(), "param.null", new String[] {"value"});
		final Variable variable = this.ontologyVariableDataManager.getVariable(null, dto.getVariableId(), false);
		if(attributeId == null) {
			this.validateAttributeVariable(errors, variable);
			this.validateGermplasmAttributeShouldNotExist(errors, gid, dto);
		} else {
			this.validateGermplasmAttributeForUpdate(errors, gid, dto, attributeId);
		}
		this.validateGermplasmAttributeValue(errors, dto.getValue());
		this.validateVariableDataTypeValue(errors, variable, dto.getValue());
		this.validateGermplasmAttributeDate(errors, dto.getDate());
	}

	void validateGermplasmAttributeValue(final BindingResult errors, final String value) {
		if (value.length() > GERMPLASM_ATTRIBUTE_VALUE_MAX_LENGTH) {
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

	public void setOntologyVariableDataManager(
		final OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}
}
