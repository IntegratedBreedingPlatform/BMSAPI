package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotMergeRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class LotSplitValidator {

	private BindingResult errors;

	@Autowired
	private LotInputValidator lotInputValidator;

	public void validateRequest(final LotSplitRequestDto lotSplitRequestDto) {
		BaseValidator.checkNotNull(lotSplitRequestDto,"lot.split.input.null");
		BaseValidator.checkNotNull(lotSplitRequestDto.getSplitLotUUID(),"lot.split.uuid.null");
		BaseValidator.checkNotEmpty(lotSplitRequestDto.getSplitLotUUID(),"lot.split.uuid.null");

		final LotSplitRequestDto.InitialLotDepositDto initialDeposit = lotSplitRequestDto.getInitialDeposit();
		BaseValidator.checkNotNull(initialDeposit,"lot.split.initial.deposit.null");

		final LotSplitRequestDto.NewLotSplitDto newLot = lotSplitRequestDto.getNewLot();
		BaseValidator.checkNotNull(newLot,"lot.split.new.lot.null");
	}

	public void validateSplitLot(ExtendedLotDto extendedLotDto, double initialDeposit){

		BaseValidator.checkNotNull(extendedLotDto,"lot.split.new.lot.null");
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotMergeRequestDto.class.getName());

		if (!extendedLotDto.getStatus().equals(LotStatus.ACTIVE.name())) {
			errors.reject("lots.closed", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.lotInputValidator.validateLotBalance(extendedLotDto.getActualBalance());

		if (extendedLotDto.getActualBalance() <= initialDeposit) {
			this.errors.reject("lot.split.new.lot.invalid.amount", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

}
