package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.InventoryUnitValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LotInputValidator {


	@Autowired
	private LocationValidator locationValidator;

	@Autowired
	private InventoryUnitValidator inventoryUnitValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private ExtendedLotListValidator extendedLotListValidator;

	@Autowired
	private LotService lotService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	private BindingResult errors;

	private static final Integer STOCK_ID_MAX_LENGTH = 35;


	public LotInputValidator() {
	}

	public void validate(final LotGeneratorInputDto lotGeneratorInputDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.locationValidator.validateSeedLocationId(this.errors, lotGeneratorInputDto.getLocationId());
		this.inventoryUnitValidator.validateInventoryUnitId(this.errors, lotGeneratorInputDto.getUnitId());
		this.germplasmValidator.validateGermplasmId(this.errors, lotGeneratorInputDto.getGid());
		this.validateStockId(lotGeneratorInputDto);
		this.inventoryCommonValidator.validateLotNotes(lotGeneratorInputDto.getNotes(), errors);
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validate(final LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final LotGeneratorInputDto lotGeneratorInputDto = lotGeneratorBatchRequestDto.getLotGeneratorInput();
		BaseValidator.checkNotNull(lotGeneratorInputDto, "param.null", new String[] {"lotGeneratorInputDto"});

		this.locationValidator.validateSeedLocationId(this.errors, lotGeneratorInputDto.getLocationId());
		this.inventoryUnitValidator.validateInventoryUnitId(this.errors, lotGeneratorInputDto.getUnitId());
		this.validateStockId(lotGeneratorInputDto);
		this.inventoryCommonValidator.validateLotNotes(lotGeneratorInputDto.getNotes(), errors);
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validate(final List<ExtendedLotDto> lotDtos, final LotUpdateRequestDto updateRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		this.extendedLotListValidator.validateClosedLots(lotDtos);

		final Integer locationId = updateRequestDto.getLocationId();
		if (locationId != null) {
			this.locationValidator.validateSeedLocationId(this.errors, locationId);
		}

		final Integer unitId = updateRequestDto.getUnitId();
		if (unitId != null) {
			this.inventoryUnitValidator.validateInventoryUnitId(this.errors, unitId);
		}

		final Integer gid = updateRequestDto.getGid();
		if (gid != null) {
			this.germplasmValidator.validateGermplasmId(this.errors, gid);
		}

		this.inventoryCommonValidator.validateLotNotes(updateRequestDto.getNotes(), errors);
		this.validateTransactionStatus(lotDtos, updateRequestDto);

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateTransactionStatus(final List<ExtendedLotDto> lotDtos, final LotUpdateRequestDto updateRequestDto) {
		final TransactionsSearchDto transactionsSearchDto = new TransactionsSearchDto();
		transactionsSearchDto.setLotIds(lotDtos.stream().map(LotDto::getLotId).collect(Collectors.toList()));
		final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);

		if (transactionDtos == null || transactionDtos.isEmpty()) {
			return;
		}

		if (transactionDtos.stream().map(TransactionDto::getTransactionStatus)
			.anyMatch(s -> s.equals(TransactionStatus.CONFIRMED.getValue()))
			&& (updateRequestDto.getUnitId() != null)) {

			this.errors.reject("lots.transactions.status.confirmed.cannot.change.unit");
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
			if (stockId.length() > STOCK_ID_MAX_LENGTH) {
				this.errors.reject("lot.stock.id.length.higher.than.maximum", new String[] {String.valueOf(STOCK_ID_MAX_LENGTH)}, "");
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
			inventoryCommonValidator.validateStockIdPrefix(lotGeneratorInputDto.getStockPrefix(), errors);
			if (!StringUtils.isEmpty(lotGeneratorInputDto.getStockId())){
				this.errors.reject("lot.stock.id.not.empty", "");
				return;
			}
		}

	}
}
