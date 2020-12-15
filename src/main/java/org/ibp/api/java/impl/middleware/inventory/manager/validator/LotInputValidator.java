package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.apache.commons.lang3.StringUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotMultiUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.InventoryUnitValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LotInputValidator {

	public static final int NEW_LOT_UID_MAX_LENGTH = 36;

	private static final Integer STOCK_ID_MAX_LENGTH = 35;

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

	public LotInputValidator() {
	}

	public void validate(final String programUUID, final LotGeneratorInputDto lotGeneratorInputDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		this.locationValidator.validateSeedLocationId(this.errors, programUUID, lotGeneratorInputDto.getLocationId());
		this.inventoryUnitValidator.validateInventoryUnitId(this.errors, lotGeneratorInputDto.getUnitId());
		this.germplasmValidator.validateGermplasmId(this.errors, lotGeneratorInputDto.getGid());
		this.validateStockId(lotGeneratorInputDto);
		this.inventoryCommonValidator.validateLotNotes(lotGeneratorInputDto.getNotes(), this.errors);
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validate(final String programUUID, final LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());
		final LotGeneratorInputDto lotGeneratorInputDto = lotGeneratorBatchRequestDto.getLotGeneratorInput();
		BaseValidator.checkNotNull(lotGeneratorInputDto, "param.null", new String[] {"lotGeneratorInputDto"});

		this.locationValidator.validateSeedLocationId(this.errors, programUUID, lotGeneratorInputDto.getLocationId());
		this.inventoryUnitValidator.validateInventoryUnitId(this.errors, lotGeneratorInputDto.getUnitId());
		this.validateStockId(lotGeneratorInputDto);
		this.inventoryCommonValidator.validateLotNotes(lotGeneratorInputDto.getNotes(), this.errors);
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validate(final String programUUID, final List<ExtendedLotDto> lotDtos, final LotUpdateRequestDto lotUpdateRequestDto) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		this.extendedLotListValidator.validateClosedLots(lotDtos);

		if (lotUpdateRequestDto.getSingleInput() != null) {
			final Integer locationId = lotUpdateRequestDto.getSingleInput().getLocationId();
			if (locationId != null) {
				this.locationValidator.validateSeedLocationId(this.errors, programUUID, locationId);
			}

			final Integer unitId = lotUpdateRequestDto.getSingleInput().getUnitId();
			if (unitId != null) {
				this.inventoryUnitValidator.validateInventoryUnitId(this.errors, unitId);
			}

			final Integer gid = lotUpdateRequestDto.getSingleInput().getGid();
			if (gid != null) {
				this.germplasmValidator.validateGermplasmId(this.errors, gid);
			}

			this.inventoryCommonValidator.validateLotNotes(lotUpdateRequestDto.getSingleInput().getNotes(), this.errors);
			if (lotUpdateRequestDto.getSingleInput().getUnitId() != null) {
				final List<String> lotUUids = lotDtos.stream().map(extendedLotDto -> extendedLotDto.getLotUUID()).collect(Collectors.toList());
				this.validateNoConfirmedTransactions(lotUUids);
			}

		} else if (lotUpdateRequestDto.getMultiInput() != null) {
			final List<String> filteredLocationAbbrs =
				lotUpdateRequestDto.getMultiInput().getLotList().stream().map(LotMultiUpdateRequestDto.LotUpdateDto::getStorageLocationAbbr).distinct().collect(Collectors.toList());

			if (filteredLocationAbbrs.stream().anyMatch(s -> !StringUtils.isBlank(s))) {
				this.locationValidator.validateSeedLocationAbbr(this.errors, programUUID, filteredLocationAbbrs);
			}

			final List<String> unitNames =
				lotUpdateRequestDto.getMultiInput().getLotList().stream().map(LotMultiUpdateRequestDto.LotUpdateDto::getUnitName).distinct().collect(Collectors.toList());
			if (unitNames.stream().anyMatch(s -> !StringUtils.isBlank(s))) {
				if (unitNames.stream().anyMatch(s -> StringUtils.isBlank(s))) {
					this.errors.reject("lot.input.list.units.null.or.empty", "");
				} else {
					this.inventoryCommonValidator.validateUnitNames(unitNames, this.errors);
				}
			}

			final List<String> notesList = lotUpdateRequestDto.getMultiInput().getLotList().stream().map(LotMultiUpdateRequestDto.LotUpdateDto::getNotes).distinct().collect(Collectors.toList());
			if (notesList.stream().anyMatch(s -> !StringUtils.isBlank(s))) {
				this.inventoryCommonValidator.validateLotNotes(notesList, this.errors);
			}

			final List<String> lotUUids =
				lotUpdateRequestDto.getMultiInput().getLotList().stream().filter(lotUpdateDto -> !StringUtils.isBlank(lotUpdateDto.getUnitName()))
					.map(LotMultiUpdateRequestDto.LotUpdateDto::getLotUID).collect(Collectors.toList());

			if (!Collections.isEmpty(lotUUids)) {
				this.validateNoConfirmedTransactions(lotUUids);
			}

			this.validateNewLotUIDs(lotUpdateRequestDto.getMultiInput().getLotList());
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateLotBalance(final Double balance) {
		if (balance == null || balance < 0) {
			this.errors = new MapBindingResult(new HashMap<String, String>(), Double.class.getName());
			this.errors.reject("lot.balance.invalid");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateNoConfirmedTransactions(final List<String> lotUUids) {
		final TransactionsSearchDto transactionsSearchDto = new TransactionsSearchDto();
		transactionsSearchDto.setLotUUIDs(lotUUids);
		final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);

		if (Collections.isEmpty(transactionDtos)) {
			return;
		}

		if (transactionDtos.stream().filter(transactionDto ->  !StringUtil.isEmpty(transactionDto.getLot().getUnitName())).map(TransactionDto::getTransactionStatus)
			.anyMatch(s -> s.equals(TransactionStatus.CONFIRMED.getValue()))
		) {

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
			this.inventoryCommonValidator.validateStockIdPrefix(lotGeneratorInputDto.getStockPrefix(), this.errors);
			if (!StringUtils.isEmpty(lotGeneratorInputDto.getStockId())){
				this.errors.reject("lot.stock.id.not.empty", "");
				return;
			}
		}

	}

	private void validateNewLotUIDs(final List<LotMultiUpdateRequestDto.LotUpdateDto> lotList) {
		Set<String> newLotUIDs = new HashSet<>();
		Set<String> duplicatedNewLotUIDs = new HashSet<>();
		Set<String> invalidNewLotUIDs = new HashSet<>();
		lotList
			.stream()
			.map(LotMultiUpdateRequestDto.LotUpdateDto::getNewLotUID)
			.forEach(newLotUID -> {
				if (!StringUtils.isBlank(newLotUID) && newLotUID.length() > NEW_LOT_UID_MAX_LENGTH) {
					invalidNewLotUIDs.add(newLotUID);
				}

				if (!StringUtils.isBlank(newLotUID) && !newLotUIDs.add(newLotUID)) {
					duplicatedNewLotUIDs.add(newLotUID);
				}
			});

		if (!Collections.isEmpty(duplicatedNewLotUIDs)) {
			this.errors.reject("lot.update.duplicated.new.lot.uids", new String[] {Util.buildErrorMessageFromList(Arrays.asList(duplicatedNewLotUIDs), 3)}, "");
		}
		if (!Collections.isEmpty(invalidNewLotUIDs)) {
			this.errors.reject("lot.update.invalid.new.lot.uids", new String[] {Util.buildErrorMessageFromList(Arrays.asList(invalidNewLotUIDs), 3), String.valueOf(NEW_LOT_UID_MAX_LENGTH)}, "");
		}

		if (!CollectionUtils.isEmpty(newLotUIDs)) {
			final LotsSearchDto lotsSearchDto = new LotsSearchDto();
			lotsSearchDto.setLotUUIDs(new ArrayList<>(newLotUIDs));
			final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(lotsSearchDto, null);
			if (!CollectionUtils.isEmpty(extendedLotDtos)) {
				final List<String> existingLotUIDs = extendedLotDtos
					.stream()
					.map(LotDto::getLotUUID)
					.collect(Collectors.toList());
				this.errors.reject("lot.update.existing.new.lot.uids", new String[] {Util.buildErrorMessageFromList(existingLotUIDs, 3)}, "");
			}
		}

	}

}
