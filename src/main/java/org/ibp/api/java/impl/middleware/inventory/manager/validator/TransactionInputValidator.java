package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class TransactionInputValidator {

	private BindingResult errors;

	public TransactionInputValidator() {
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
