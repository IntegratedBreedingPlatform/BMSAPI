package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
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

/**
 * Created by clarysabel on 2/20/20.
 */
@Component
public class LotWithdrawalInputDtoValidator {

	private BindingResult errors;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	public void validate(final LotWithdrawalInputDto lotWithdrawalInputDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		if (lotWithdrawalInputDto == null) {
			this.errors.reject("lot.withdrawal.input.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.searchCompositeDtoValidator.validateSearchCompositeDto(lotWithdrawalInputDto.getSelectedLots(), this.errors);

		this.inventoryCommonValidator.validateTransactionNotes(lotWithdrawalInputDto.getNotes(), this.errors);

		if (lotWithdrawalInputDto.getWithdrawalsPerUnit() == null || lotWithdrawalInputDto.getWithdrawalsPerUnit().isEmpty()) {
			this.errors.reject("lot.withdrawal.input.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.inventoryCommonValidator.validateUnitNames(new ArrayList<>(lotWithdrawalInputDto.getWithdrawalsPerUnit().keySet()),
			this.errors);

		lotWithdrawalInputDto.getWithdrawalsPerUnit().forEach((k, v) -> {
			if (v.isReserveAllAvailableBalance() && v.getWithdrawalAmount() != null && !v.getWithdrawalAmount().equals(0D)) {
				this.errors.reject("lot.amount.invalid", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
			if (!v.isReserveAllAvailableBalance() && (v.getWithdrawalAmount() == null || v.getWithdrawalAmount() <= 0)) {
				this.errors.reject("lot.amount.invalid", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		});
	}

	public void validateWithdrawalInstructionsUnits(final LotWithdrawalInputDto lotWithdrawalInputDto,
		final List<ExtendedLotDto> extendedLotDtos) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		//All units resulted the search request, must be indicated in the map
		final Set<String> specifiedUnits = lotWithdrawalInputDto.getWithdrawalsPerUnit().keySet();
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

	public void setInventoryCommonValidator(
		final InventoryCommonValidator inventoryCommonValidator) {
		this.inventoryCommonValidator = inventoryCommonValidator;
	}
}
