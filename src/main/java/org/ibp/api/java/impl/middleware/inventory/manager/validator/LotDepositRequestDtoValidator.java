package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LotDepositRequestDtoValidator {

	private BindingResult errors;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	public void validate(final LotDepositRequestDto lotDepositRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		if (lotDepositRequestDto == null) {
			this.errors.reject("lot.deposit.input.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.searchCompositeDtoValidator.validateSearchCompositeDto(lotDepositRequestDto.getSelectedLots(), this.errors);

		this.inventoryCommonValidator.validateTransactionNotes(lotDepositRequestDto.getNotes(), this.errors);

		if (lotDepositRequestDto.getDepositsPerUnit() == null || lotDepositRequestDto.getDepositsPerUnit().isEmpty()) {
			this.errors.reject("lot.deposit.instruction.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.inventoryCommonValidator.validateUnitNames(new ArrayList<>(lotDepositRequestDto.getDepositsPerUnit().keySet()), this.errors);

		lotDepositRequestDto.getDepositsPerUnit().forEach((k, v) -> {
			if (v == null || v <= 0) {
				this.errors.reject("lot.amount.invalid", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		});
	}

	public void validateDepositInstructionsUnits(final LotDepositRequestDto lotDepositRequestDto,
		final List<ExtendedLotDto> extendedLotDtos) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		//All units resulted the search request, must be indicated in the map
		final Set<String> specifiedUnits = lotDepositRequestDto.getDepositsPerUnit().keySet();
		final Set<String> lotsUnits = extendedLotDtos.stream().map(ExtendedLotDto::getUnitName).collect(Collectors.toSet());
		if (!specifiedUnits.containsAll(lotsUnits)) {
			final List<String> missingUnits = new ArrayList<>(lotsUnits);
			missingUnits.removeAll(specifiedUnits);
			this.errors
				.reject("lot.input.instructions.missing.for.units", new String[] {Util.buildErrorMessageFromList(missingUnits, 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (!lotsUnits.containsAll(specifiedUnits)) {
			final List<String> extraUnits = new ArrayList<>(specifiedUnits);
			extraUnits.removeAll(lotsUnits);
			this.errors
				.reject("lot.input.instructions.for.non.present.units", new String[] {Util.buildErrorMessageFromList(extraUnits, 3)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
