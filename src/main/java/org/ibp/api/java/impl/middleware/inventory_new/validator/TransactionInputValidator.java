package org.ibp.api.java.impl.middleware.inventory_new.validator;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.inventory_new.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory_new.LotsSearchDto;
import org.generationcp.middleware.domain.inventory_new.TransactionDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.impl.middleware.common.validator.InventoryScaleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Component
public class TransactionInputValidator {

	@Autowired
	private InventoryScaleValidator inventoryScaleValidator;

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

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateLotAndScale(final TransactionDto transactionDto) {
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		lotsSearchDto.setLotIds(Lists.newArrayList(transactionDto.getLot().getLotId()));
		final List<ExtendedLotDto> result = this.lotService.searchLots(lotsSearchDto, null);
		if (result.size() == 1) {
			final ExtendedLotDto lot = result.get(0);
			final Integer scaleId = lot.getScaleId();
			this.inventoryScaleValidator.validateNotNullInventoryScaleId(this.errors, scaleId);
			if (lot.getStatus().equals(LotStatus.CLOSED.getIntValue())) {
				this.errors.rejectValue("transaction.closed.lot", "");
			}
		} else {
			this.errors.rejectValue("transaction.wrong.lot", "");
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
}
