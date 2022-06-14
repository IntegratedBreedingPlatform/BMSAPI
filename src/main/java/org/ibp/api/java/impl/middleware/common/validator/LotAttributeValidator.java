package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections4.CollectionUtils;
import org.generationcp.middleware.domain.germplasm.AttributeRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.service.api.inventory.LotAttributeService;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LotAttributeValidator extends AttributeValidator{

	@Autowired
	public LotService lotService;

	@Autowired
	public LotAttributeService lotAttributeService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	public void validateAttribute(final BindingResult errors, final Integer lotId, final AttributeRequestDto dto,
		final Integer attributeId) {
		BaseValidator.checkNotNull(dto, "param.null", new String[] {"request body"});
		BaseValidator.checkNotNull(dto.getVariableId(), "param.null", new String[] {"variableId"});
		final Variable variable = this.ontologyVariableDataManager.getVariable(null, dto.getVariableId(), false);
		this.validateLot(errors, lotId);
		if(attributeId == null) {
			this.validateAttributeVariable(errors, variable, Collections.singletonList(VariableType.INVENTORY_ATTRIBUTE));
			this.validateLotAttributeShouldNotExist(errors, lotId, dto);
		} else {
			this.validateLotAttributeForUpdate(errors, lotId, dto, attributeId);
		}
		this.validateAttributeValue(errors, dto.getValue());
		this.validateVariableDataTypeValue(errors, variable, dto.getValue());
		this.validateAttributeDate(errors, dto.getDate());
	}

	public void validateLot(final BindingResult errors, final Integer lotId) {
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		lotsSearchDto.setLotIds(Collections.singletonList(lotId));
		final List<ExtendedLotDto> lots = this.lotService.searchLots(lotsSearchDto, null);
		if(CollectionUtils.isEmpty(lots)) {
			errors.reject("lot.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateLotAttributeShouldNotExist(final BindingResult errors, final Integer lotId, final AttributeRequestDto dto) {
		final List<GermplasmAttributeDto> lotAttributeDtos =
			this.lotAttributeService.getLotAttributeDtos(lotId, null);
		final List<GermplasmAttributeDto> existingLotAttributes = lotAttributeDtos.stream()
			.filter(existing -> existing.getVariableId().equals(dto.getVariableId())).collect(Collectors.toList());
		if (!existingLotAttributes.isEmpty()) {
			errors.reject("attribute.name.invalid.existing", new String[] {"Lot", existingLotAttributes.get(0).getVariableName()}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateLotAttributeForUpdate(final BindingResult errors, final Integer lotId, final AttributeRequestDto dto,
		final Integer attributeId) {
		final List<GermplasmAttributeDto> lotAttributeDtos =
			this.lotAttributeService.getLotAttributeDtos(lotId, null);

		List<GermplasmAttributeDto> existingLotAttributes = lotAttributeDtos.stream()
			.filter(existing -> existing.getId().equals(attributeId)).collect(Collectors.toList());
		if(existingLotAttributes.isEmpty()) {
			errors.reject("attribute.id.invalid.not.existing", new String[] {"Lot", String.valueOf(attributeId)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		existingLotAttributes = existingLotAttributes.stream()
			.filter(existing -> existing.getVariableId().equals(dto.getVariableId())).collect(Collectors.toList());
		if (existingLotAttributes.isEmpty()) {
			errors.reject("attribute.variable.id.invalid.not.existing", new String[] {String.valueOf(dto.getVariableId())}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateLotAttributeExists(final BindingResult errors, final Integer lotId, final Integer attributeId) {
		this.validateLot(errors, lotId);

		final List<GermplasmAttributeDto> lotAttributeDtos =
			this.lotAttributeService.getLotAttributeDtos(lotId, null);
		final List<GermplasmAttributeDto> existingLotAttributes = lotAttributeDtos.stream()
			.filter(existing -> existing.getId().equals(attributeId)).collect(Collectors.toList());
		if(existingLotAttributes.isEmpty()) {
			errors.reject("attribute.id.invalid.not.existing", new String[] {"Lot", String.valueOf(attributeId)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
