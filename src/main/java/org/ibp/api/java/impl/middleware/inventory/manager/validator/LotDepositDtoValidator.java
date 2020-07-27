package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.LotDepositDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Component
public class LotDepositDtoValidator {

	private BindingResult errors;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	public void validate(final List<LotDepositDto> lotDepositDtos){
		errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

		if (lotDepositDtos == null) {
			errors.reject("lot.deposit.input.null", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		lotDepositDtos.forEach((LotDepositDto lotDepositDto) ->{

			inventoryCommonValidator.validateTransactionNotes(lotDepositDto.getNotes(), errors);

			if(lotDepositDto.getAmount() == null || lotDepositDto.getAmount() <= 0){
				errors.reject("lot.amount.invalid", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		});
	}

}
