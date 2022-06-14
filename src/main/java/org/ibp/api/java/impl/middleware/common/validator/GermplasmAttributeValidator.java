package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.domain.shared.RecordAttributeDto;
import org.generationcp.middleware.domain.shared.AttributeRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GermplasmAttributeValidator extends AttributeValidator{

	private static final List<Integer> ALLOWED_ATTRIBUTE_TYPES_IDS =
		Arrays.asList(VariableType.GERMPLASM_ATTRIBUTE.getId(), VariableType.GERMPLASM_PASSPORT.getId());

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	public void validateAttributeType(final BindingResult errors, final Integer attributeType) {
		if (!GermplasmAttributeValidator.ALLOWED_ATTRIBUTE_TYPES_IDS.contains(attributeType)) {
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

		final VariableFilter variableFilter = new VariableFilter();
		ids.forEach(variableFilter::addVariableId);

		final List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(variableFilter);
		final boolean anyVariableWithNoAllowedType =
			variables.stream().anyMatch(v -> !v.getVariableTypes().contains(VariableType.GERMPLASM_ATTRIBUTE) &&
				!v.getVariableTypes().contains(VariableType.GERMPLASM_PASSPORT));
		if (variables.size() != ids.size() || anyVariableWithNoAllowedType) {
			errors.reject("attribute.invalid", "");
		}
	}

	void validateGermplasmAttributeShouldNotExist(final BindingResult errors, final Integer gid, final AttributeRequestDto dto) {
		//FIXME Search by gid and variableId, to be addressed in IBP-4765
		final List<RecordAttributeDto> germplasmAttributeDtos =
			this.germplasmAttributeService.getGermplasmAttributeDtos(gid, null, null);
		final List<RecordAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getVariableId().equals(dto.getVariableId())).collect(Collectors.toList());
		if (!existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.name.invalid.existing", new String[] {"Germplasm", existingGermplasmAttributes.get(0).getVariableName()}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateGermplasmAttributeExists(final BindingResult errors, final Integer gid, final Integer attributeId) {
		final List<RecordAttributeDto> germplasmAttributeDtos = this.germplasmAttributeService.getGermplasmAttributeDtos(gid,
			null, null);
		final List<RecordAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getId().equals(attributeId)).collect(Collectors.toList());
		if(existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.id.invalid.not.existing", new String[] {"Germplasm", String.valueOf(attributeId)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateGermplasmAttributeForUpdate(final BindingResult errors, final Integer gid, final AttributeRequestDto dto,
		final Integer attributeId) {
		//FIXME Search by attributeId, to be addressed in IBP-4765
		final List<RecordAttributeDto> germplasmAttributeDtos = this.germplasmAttributeService.getGermplasmAttributeDtos(gid, null, null);

		// Filter by germplasm attribute id
		List<RecordAttributeDto> existingGermplasmAttributes = germplasmAttributeDtos.stream()
			.filter(existing -> existing.getId().equals(attributeId)).collect(Collectors.toList());
		if(existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.id.invalid.not.existing", new String[] {"Germplasm", String.valueOf(attributeId)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		// Filter by germplasm attribute Variable Id
		existingGermplasmAttributes = existingGermplasmAttributes.stream()
			.filter(existing -> existing.getVariableId().equals(dto.getVariableId())).collect(Collectors.toList());
		if (existingGermplasmAttributes.isEmpty()) {
			errors.reject("attribute.variable.id.invalid.not.existing", new String[] {String.valueOf(dto.getVariableId())}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateAttribute(final BindingResult errors, final Integer gid, final AttributeRequestDto dto,
		final Integer attributeId) {
		BaseValidator.checkNotNull(dto, "param.null", new String[] {"request body"});
		BaseValidator.checkNotNull(dto.getVariableId(), "param.null", new String[] {"variableId"});
		final Variable variable = this.ontologyVariableDataManager.getVariable(null, dto.getVariableId(), false);
		if(attributeId == null) {
			this.validateAttributeVariable(errors, variable, VariableType.getGermplasmAttributeVariableTypes());
			this.validateGermplasmAttributeShouldNotExist(errors, gid, dto);
		} else {
			this.validateGermplasmAttributeForUpdate(errors, gid, dto, attributeId);
		}
		this.validateAttributeValue(errors, dto.getValue());
		this.validateVariableDataTypeValue(errors, variable, dto.getValue());
		this.validateAttributeDate(errors, dto.getDate());
	}

}
