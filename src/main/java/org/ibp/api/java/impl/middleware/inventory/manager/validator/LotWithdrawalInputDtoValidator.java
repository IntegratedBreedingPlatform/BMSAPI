package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.Util;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by clarysabel on 2/20/20.
 */
@Component
public class LotWithdrawalInputDtoValidator {

	private static Integer NOTES_MAX_LENGTH = 255;

	private BindingResult errors;

	@Autowired
	private VariableService variableService;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	public void validate(final LotWithdrawalInputDto lotWithdrawalInputDto) {
		errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		if (lotWithdrawalInputDto == null) {
			errors.reject("lot.withdrawal.input.null", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		inventoryCommonValidator.validateSearchCompositeDto(lotWithdrawalInputDto.getSelectedLots(), errors);

		if (lotWithdrawalInputDto.getNotes() != null && lotWithdrawalInputDto.getNotes().length() > NOTES_MAX_LENGTH) {
			errors.reject("transaction.notes.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (lotWithdrawalInputDto.getWithdrawalsPerUnit() == null || lotWithdrawalInputDto.getWithdrawalsPerUnit().isEmpty()) {
			errors.reject("lot.withdrawal.input.null", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		// validate units
		final Set<String> specifiedUnits = lotWithdrawalInputDto.getWithdrawalsPerUnit().keySet();
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<VariableDetails> existingInventoryUnits = this.variableService.getVariablesByFilter(variableFilter);
		final List<String> supportedUnitNames = existingInventoryUnits.stream().map(VariableDetails::getName).collect(Collectors.toList());

		if (!supportedUnitNames.containsAll(specifiedUnits)) {
			final List<String> invalidUnitNames = new ArrayList<>(specifiedUnits);
			invalidUnitNames.removeAll(supportedUnitNames);
			errors.reject("lot.input.invalid.units", new String[] {Util.buildErrorMessageFromList(invalidUnitNames, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());

		}

		lotWithdrawalInputDto.getWithdrawalsPerUnit().forEach((k, v) -> {
			if (v.isReserveAllAvailableBalance() && v.getWithdrawalAmount() != null && !v.getWithdrawalAmount().equals(0D)) {
				errors.reject("lot.amount.invalid", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			if (!v.isReserveAllAvailableBalance() && (v.getWithdrawalAmount() == null || v.getWithdrawalAmount() <= 0)) {
				errors.reject("lot.amount.invalid", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		});
	}

	public void validateWithdrawalInstructionsUnits(final LotWithdrawalInputDto lotWithdrawalInputDto,
		final List<ExtendedLotDto> extendedLotDtos) {
		errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		//All units resulted the search request, must be indicated in the map
		final Set<String> specifiedUnits = lotWithdrawalInputDto.getWithdrawalsPerUnit().keySet();
		final Set<String> lotsUnits = extendedLotDtos.stream().map(ExtendedLotDto::getUnitName).collect(Collectors.toSet());
		if (!specifiedUnits.containsAll(lotsUnits)) {
			final List<String> missingUnits = new ArrayList<>(lotsUnits);
			missingUnits.removeAll(specifiedUnits);
			errors.reject("lot.input.instructions.missing.for.units", new String[] {Util.buildErrorMessageFromList(missingUnits, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (!lotsUnits.containsAll(specifiedUnits)) {
			final List<String> extraUnits = new ArrayList<>(specifiedUnits);
			extraUnits.removeAll(lotsUnits);
			errors.reject("lot.input.instructions.for.non.present.units", new String[] {Util.buildErrorMessageFromList(extraUnits, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void setInventoryCommonValidator(
		final InventoryCommonValidator inventoryCommonValidator) {
		this.inventoryCommonValidator = inventoryCommonValidator;
	}
}
