package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotMergeRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.Objects;

@Component
public class LotSplitValidator {

	private BindingResult errors;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private LocationValidator locationValidator;

	public void validateRequest(final LotSplitRequestDto lotSplitRequestDto) {
		BaseValidator.checkNotNull(lotSplitRequestDto,"lot.split.input.null");
		BaseValidator.checkNotNull(lotSplitRequestDto.getSplitLotUUID(),"lot.split.uuid.null");
		BaseValidator.checkNotEmpty(lotSplitRequestDto.getSplitLotUUID(),"lot.split.uuid.null");

		final LotSplitRequestDto.InitialLotDepositDto initialDeposit = lotSplitRequestDto.getInitialDeposit();
		BaseValidator.checkNotNull(initialDeposit,"lot.split.initial.deposit.null");

		final LotSplitRequestDto.NewLotSplitDto newLot = lotSplitRequestDto.getNewLot();
		BaseValidator.checkNotNull(newLot,"lot.split.new.lot.null");
	}

	public void validateSplitLot(final String programUUID, final ExtendedLotDto splitLotDto,
		final LotSplitRequestDto.NewLotSplitDto newLotSplitDto,
		final LotSplitRequestDto.InitialLotDepositDto initialLotDepositDto){

		BaseValidator.checkNotNull(splitLotDto,"lot.split.lot.null");
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotMergeRequestDto.class.getName());

		if (!splitLotDto.getStatus().equals(LotStatus.ACTIVE.name())) {
			errors.reject("lots.closed", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (Objects.isNull(splitLotDto.getAvailableBalance()) || splitLotDto.getAvailableBalance() <= 0) {
			this.errors = new MapBindingResult(new HashMap<String, String>(), Double.class.getName());
			this.errors.reject("lot.split.new.lot.invalid.available.balance");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (Objects.isNull(initialLotDepositDto) || initialLotDepositDto.getAmount() <= 0) {
			this.errors = new MapBindingResult(new HashMap<String, String>(), Double.class.getName());
			this.errors.reject("lot.split.initial.deposit.invalid");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (splitLotDto.getAvailableBalance() <= initialLotDepositDto.getAmount()) {
			this.errors.reject("lot.split.new.lot.invalid.amount", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.inventoryCommonValidator.validateLotNotes(newLotSplitDto.getNotes(), errors);
		this.inventoryCommonValidator.validateTransactionNotes(initialLotDepositDto.getNotes(), errors);
		this.locationValidator.validateSeedLocationId(errors, programUUID, newLotSplitDto.getLocationId());
	}

}
