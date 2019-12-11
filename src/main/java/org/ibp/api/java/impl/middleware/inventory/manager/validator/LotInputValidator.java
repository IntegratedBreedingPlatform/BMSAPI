package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
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
		this.locationValidator.validateSeedLocationId(this.errors, lotGeneratorInputDto.getLocationId());
		this.inventoryScaleValidator.validateInventoryScaleId(this.errors, lotGeneratorInputDto.getScaleId());
		this.germplasmValidator.validateGermplasmId(this.errors, lotGeneratorInputDto.getGid());
		this.validateStockId(lotGeneratorInputDto);
		this.validateComments(lotGeneratorInputDto.getComments());
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateComments(final String comments) {
		if (comments != null) {
			if (comments.length() > 255) {
				this.errors.reject("lot.comments.length");
			}
		}
	}

	private void validateStockId(final LotGeneratorInputDto lotGeneratorInputDto) {
		if (lotGeneratorInputDto.getGenerateStock() == null) {
			this.errors.reject("lot.generate.stock.mandatory", "");
			return;
		}
		if (!lotGeneratorInputDto.getGenerateStock()) {
			final String stockId = lotGeneratorInputDto.getStockId();
			if (StringUtils.isEmpty(stockId)) {
				this.errors.reject("lot.stock.id.required", "");
				return;
			}
			final LotsSearchDto lotsSearchDto = new LotsSearchDto();
			lotsSearchDto.setStockId(stockId);
			final long lotsCount = this.lotService.countSearchLots(lotsSearchDto);
			if (lotsCount != 0) {
				this.errors.reject("lot.stock.id.invalid", "");
			}
			if (!StringUtils.isEmpty(lotGeneratorInputDto.getStockPrefix())){
				this.errors.reject("lot.stock.prefix.not.empty", "");
			}
		} else {
			if (!StringUtils.isEmpty(lotGeneratorInputDto.getStockPrefix()) && !lotGeneratorInputDto.getStockPrefix()
				.matches("[a-zA-Z]+")) {
				this.errors.reject("lot.stock.prefix.invalid", "");
				return;
			}
			if (!StringUtils.isEmpty(lotGeneratorInputDto.getStockId())){
				this.errors.reject("lot.stock.id.not.empty", "");
				return;
			}
		}

	}

}
