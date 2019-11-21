package org.ibp.api.java.impl.middleware.inventory_new.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory_new.LotDto;
import org.generationcp.middleware.domain.inventory_new.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory_new.LotsSearchDto;
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

@Component
public class LotInputValidator {


	@Autowired
	private LocationValidator locationValidator;

	@Autowired
	private InventoryScaleValidator inventoryScaleValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private LotService lotService;

	private BindingResult errors;


	public LotInputValidator() {
	}

	public void validate(final LotGeneratorInputDto lotGeneratorInputDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());
		locationValidator.validateSeedLocationId(errors, lotGeneratorInputDto.getLocationId());
		inventoryScaleValidator.validateInventoryScaleId(errors, lotGeneratorInputDto.getScaleId());
		germplasmValidator.validateGermplasmId(errors, lotGeneratorInputDto.getGid());
		validateStockId(lotGeneratorInputDto);
		validateAmount(lotGeneratorInputDto.getInitialBalanceAmount());
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateAmount(final Double amount) {
		if (amount == null) {
			errors.reject("lot.initial.amount.required", "");
			return;
		}
		if (amount <=0 ) {
			errors.reject("lot.initial.amount.positive.value", "");
			return;
		}
	}

	private void validateStockId(final LotGeneratorInputDto lotGeneratorInputDto) {
		if (lotGeneratorInputDto.getGenerateStock() == null) {
			errors.reject("lot.generate.stock.mandatory", "");
			return;
		}
		if (!lotGeneratorInputDto.getGenerateStock()) {
			final String stockId = lotGeneratorInputDto.getStockId();
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
			if (!StringUtils.isEmpty(lotGeneratorInputDto.getStockPrefix())){
				errors.reject("lot.stock.prefix.not.empty", "");
			}
		} else {
			if (!StringUtils.isEmpty(lotGeneratorInputDto.getStockPrefix()) && !lotGeneratorInputDto.getStockPrefix()
				.matches("[a-zA-Z]+")) {
				errors.reject("lot.stock.prefix.invalid", "");
				return;
			}
			if (!StringUtils.isEmpty(lotGeneratorInputDto.getStockId())){
				errors.reject("lot.stock.id.not.empty", "");
				return;
			}
		}

	}

}
