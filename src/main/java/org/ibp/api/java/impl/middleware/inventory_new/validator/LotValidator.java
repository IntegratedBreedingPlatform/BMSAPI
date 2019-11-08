package org.ibp.api.java.impl.middleware.inventory_new.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory_new.LotDto;
import org.generationcp.middleware.domain.inventory_new.LotsSearchDto;
import org.generationcp.middleware.domain.inventory_new.TransactionDto;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.InventoryScaleValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Component
public class LotValidator {


	@Autowired
	private LocationValidator locationValidator;

	@Autowired
	private InventoryScaleValidator inventoryScaleValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private LotService lotService;

	private BindingResult errors;


	public LotValidator() {
	}

	public void validate(final LotDto lotDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());
		locationValidator.validateLocationId(errors, lotDto.getLocationId());
		inventoryScaleValidator.validateInventoryScaleId(errors, lotDto.getScaleId());
		germplasmValidator.validateGermplasmId(errors, lotDto.getGid());
		validateStockId(lotDto.getStockId());
		validateTransactions(lotDto.getTransactions());
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateTransactions(final List<TransactionDto> transactionDtos) {
		if (transactionDtos == null || transactionDtos.isEmpty()) {
			errors.reject("lot.transaction.required", "");
			return;
		}
		if (transactionDtos.size() != 1) {
			errors.reject("lot.transaction.required", "");
			return;
		}
		final Double amount = transactionDtos.get(0).getAmount();
		if (amount == null) {
			errors.reject("lot.transaction.required", "");
			return;
		}
		if (amount <=0 ) {
			errors.reject("transaction.amount.positive.value", "");
			return;
		}
	}

	private void validateStockId(final String stockId) {
		if (StringUtils.isEmpty(stockId)) {
			errors.reject("lot.stock.id.required", "");
			return;
		}
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		lotsSearchDto.setStockId(stockId);
		final long lotsCount = lotService.countSearchLots(lotsSearchDto);
		if (lotsCount != 0) {
			errors.reject("lot.stock.id.invalid", "");
		}

	}

}
