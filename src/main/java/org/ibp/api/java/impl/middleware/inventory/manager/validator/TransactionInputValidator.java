package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.impl.middleware.common.validator.InventoryUnitValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class TransactionInputValidator {

	@Autowired
	private InventoryUnitValidator inventoryUnitValidator;

	@Autowired
	private LotService lotService;

	private BindingResult errors;

	public TransactionInputValidator() {
	}

	public void validate(final TransactionDto transactionDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), TransactionDto.class.getName());
		this.validateTransactionType(transactionDto.getTransactionType());
		this.validateAmount(transactionDto);
		this.validateLotAndScale(transactionDto);
		this.validateNotes(transactionDto.getNotes());
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNotes(final String notes) {
		if (notes != null) {
			if (notes.length() > 255) {
				this.errors.reject("transaction.notes.length");
			}
		}
	}

	private void validateLotAndScale(final TransactionDto transactionDto) {
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		lotsSearchDto.setLotIds(Lists.newArrayList(transactionDto.getLot().getLotId()));
		final List<ExtendedLotDto> result = this.lotService.searchLots(lotsSearchDto, null);
		if (result.size() == 1) {
			final ExtendedLotDto lot = result.get(0);
			final Integer unitId = lot.getUnitId();
			this.inventoryUnitValidator.validateNotNullInventoryScaleId(this.errors, unitId);
			if (lot.getStatus().equalsIgnoreCase(LotStatus.CLOSED.name())) {
				this.errors.reject("transaction.closed.lot", "");
			}
		} else {
			this.errors.reject("transaction.wrong.lot", "");
		}
	}

	private void validateTransactionType(final String transactionType) {
		if (!TransactionType.DEPOSIT.getValue().equalsIgnoreCase(transactionType)) {
			this.errors.reject("transaction.wrong.transaction.type", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}
	}

	private void validateAmount(final TransactionDto transactionDto) {
		final Double amount = transactionDto.getAmount();
		final String transactionType = transactionDto.getTransactionType();
		if (amount == null) {
			this.errors.reject("transaction.initial.amount.required", "");
			return;
		}
		if (TransactionType.DEPOSIT.getValue().equalsIgnoreCase(transactionType) && amount <= 0) {
			this.errors.reject("transaction.initial.amount.positive.value", "");
			return;
		}
	}

	public void validatePendingStatus(final List<TransactionDto> transactionDtos) {
		errors = new MapBindingResult(new HashMap<String, String>(), TransactionDto.class.getName());
		final Predicate<TransactionDto> isPendingStatus = transactionDto -> transactionDto.getTransactionStatus().equals(TransactionStatus.PENDING.getValue());

		if (transactionDtos.stream().filter(isPendingStatus.negate()).count() > 0) {
			errors.reject("transaction.wrong.transaction.status", new String[] {
				Util.buildErrorMessageFromList(
					transactionDtos.stream().filter(isPendingStatus.negate()).map(TransactionDto::getTransactionId).collect(
						Collectors.toList()), 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
