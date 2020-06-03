package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class TransactionUpdateRequestDtoValidator {

	private BindingResult errors;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private TransactionService transactionService;

	public void validate(final List<TransactionUpdateRequestDto> transactionUpdateRequestDtos) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), TransactionUpdateRequestDto.class.getName());

		if (transactionUpdateRequestDtos == null) {
			errors.reject("transaction.update.invalid.object", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (transactionUpdateRequestDtos.isEmpty()) {
			errors.reject("transaction.update.invalid.size", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (Util.countNullElements(transactionUpdateRequestDtos) > 0) {
			errors.reject("transaction.update.invalid.transaction.update", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<Integer> transactionIds =
			transactionUpdateRequestDtos.stream().map(TransactionUpdateRequestDto::getTransactionId).collect(
				Collectors.toList());
		if (!Util.areAllUnique(transactionIds)) {
			errors.reject("transaction.update.duplicated.ids", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final TransactionsSearchDto transactionsSearchDto = new TransactionsSearchDto();
		transactionsSearchDto.setTransactionIds(transactionIds);

		final List<TransactionDto> transactionDtos = transactionService.searchTransactions(transactionsSearchDto, null);

		if (transactionDtos.size() != transactionIds.size()) {
			final List<Integer> existentTransactionIds =
				transactionDtos.stream().map(TransactionDto::getTransactionId).collect(Collectors.toList());
			final List<Integer> invalidTransactionIds = new ArrayList<>(transactionIds);
			invalidTransactionIds.removeAll(existentTransactionIds);
			errors.reject("transactions.not.found",
				new String[] {Util.buildErrorMessageFromList(invalidTransactionIds, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final List<TransactionDto> transactionsWithClosedLots =
			transactionDtos.stream().filter(t -> t.getLot().getStatus().equalsIgnoreCase(LotStatus.CLOSED.name())).collect(
				Collectors.toList());
		if (!transactionsWithClosedLots.isEmpty()) {
			errors.reject("transaction.update.closed.lots", new String[] {
				Util.buildErrorMessageFromList(transactionsWithClosedLots.stream().map(TransactionDto::getTransactionId).collect(
					Collectors.toList()), 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Predicate<TransactionDto> isPendingStatus = i -> i.getTransactionStatus().equals(TransactionStatus.PENDING.getValue());
		if (transactionDtos.stream().filter(isPendingStatus.negate()).count() > 0) {
			errors.reject("transaction.update.not.pending.status", new String[] {
				Util.buildErrorMessageFromList(
					transactionDtos.stream().filter(isPendingStatus.negate()).map(TransactionDto::getTransactionId).collect(
						Collectors.toList()), 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Predicate<TransactionDto> isWithdrawalOrDepositType =
			i -> i.getTransactionType().equals(TransactionType.WITHDRAWAL.getValue()) || i.getTransactionType()
				.equals(TransactionType.DEPOSIT.getValue());
		if (transactionDtos.stream().filter(isWithdrawalOrDepositType.negate()).count() > 0) {
			errors.reject("transaction.update.wrong.type", new String[] {
				Util.buildErrorMessageFromList(
					transactionDtos.stream().filter(isWithdrawalOrDepositType.negate()).map(TransactionDto::getTransactionId).collect(
						Collectors.toList()), 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final Predicate<TransactionUpdateRequestDto> isValid = i -> i.isValid();
		if (transactionUpdateRequestDtos.stream().filter(isValid.negate()).count() > 0) {
			errors.reject("transaction.update.invalid.data", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final long invalidAmountCount =
			transactionUpdateRequestDtos.stream().filter(i -> i.getAmount() != null && i.getAmount() <= 0).count();
		if (invalidAmountCount > 0) {
			errors.reject("transaction.update.new.amount.error", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final long invalidNewBalanceCount =
			transactionUpdateRequestDtos.stream().filter(i -> i.getAvailableBalance() != null && i.getAvailableBalance() < 0).count();
		if (invalidNewBalanceCount > 0) {
			errors.reject("transaction.update.new.balance.error", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<Integer> transactionsToModifyNewBalance =
			transactionUpdateRequestDtos.stream().filter(i -> i.getAvailableBalance() != null)
				.map(TransactionUpdateRequestDto::getTransactionId).collect(
				Collectors.toList());
		if (transactionsToModifyNewBalance != null && !transactionsToModifyNewBalance.isEmpty()) {
			final List<Integer> lots = transactionDtos.stream().filter(i -> transactionsToModifyNewBalance.contains(i.getTransactionId()))
				.map(i -> i.getLot().getLotId()).collect(
					Collectors.toList());
			if (!Util.areAllUnique(lots)) {
				errors.reject("transaction.update.new.balance.for.same.lot", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}

		transactionUpdateRequestDtos.stream().forEach(i -> inventoryCommonValidator.validateTransactionNotes(i.getNotes(), errors));

	}

}
