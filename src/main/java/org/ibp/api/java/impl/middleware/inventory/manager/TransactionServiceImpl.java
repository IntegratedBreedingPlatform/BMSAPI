package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionInputValidator;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

	final Lock lock = new ReentrantLock();

	@Autowired
	private TransactionInputValidator transactionInputValidator;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.TransactionService transactionService;

	@Autowired
	private VariableService variableService;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotService lotService;

	@Override
	public List<TransactionDto> searchTransactions(
		final TransactionsSearchDto transactionsSearchDto, final Pageable pageable) {
		return this.transactionService.searchTransactions(transactionsSearchDto, pageable);
	}

	@Override
	public long countSearchTransactions(final TransactionsSearchDto transactionsSearchDto) {
		return this.transactionService.countSearchTransactions(transactionsSearchDto);
	}

	@Override
	public Integer saveTransaction(final TransactionDto transactionDto) {
		lock.lock();
		try {
			this.transactionInputValidator.validate(transactionDto);
			return this.transactionService.saveTransaction(transactionDto);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TransactionStatus> getAllTransactionStatus() {
		return TransactionStatus.getAll();
	}

	@Override
	public List<TransactionType>  getAllTransactionTypes() {
		return TransactionType.getAll();
	}

	@Override
	public void saveWithdrawals(final Integer userId, final LotsSearchDto lotsSearchDto, final LotWithdrawalInputDto lotWithdrawalInputDto,
		final TransactionStatus transactionStatus) {
		try {
			//FIXME we should only locking the affected lots
			lock.lock();

			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), LotGeneratorInputDto.class.getName());

			final List<ExtendedLotDto> lotDtos = lotService.searchLots(lotsSearchDto, null);

			//Validate that none of them has null unit id
			final long lotsWithoutUnitCount = lotDtos.stream().filter(lot -> lot.getUnitId() == null).count();
			if (lotsWithoutUnitCount != 0) {
				errors.reject("selected.lots.with.no.unit", new String[] {String.valueOf(lotsWithoutUnitCount)}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			//Validate that none of them are closed
			final long closedLotsCount = lotDtos.stream().filter(lot -> lot.getStatus().equals(LotStatus.CLOSED.toString())).count();
			if (closedLotsCount != 0) {
				errors.reject("selected.lots.closed", new String[] {String.valueOf(closedLotsCount)}, "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			//Validate notes length
			if (lotWithdrawalInputDto == null) {
				errors.reject("lot.notes.length", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			if (lotWithdrawalInputDto != null && lotWithdrawalInputDto.getNotes() != null
				&& lotWithdrawalInputDto.getNotes().length() >= 255) {
				errors.reject("lot.withdrawal.input.null", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			if (lotWithdrawalInputDto != null && (lotWithdrawalInputDto.getWithdrawalsPerUnit() == null || lotWithdrawalInputDto
				.getWithdrawalsPerUnit().isEmpty())) {
				errors.reject("lot.withdrawal.input.null", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			// validate units
			if (lotWithdrawalInputDto != null) {
				final Set<String> specifiedUnits = lotWithdrawalInputDto.getWithdrawalsPerUnit().keySet();
				if (!specifiedUnits.isEmpty()) {
					final VariableFilter variableFilter = new VariableFilter();
					variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
					final List<VariableDetails> existingInventoryScales = this.variableService.getVariablesByFilter(variableFilter);
					final List<String> existingScaleNames =
						existingInventoryScales.stream().map(VariableDetails::getName).collect(Collectors.toList());

					if (!existingScaleNames.containsAll(specifiedUnits)) {
						final List<String> invalidScaleNames = new ArrayList<>(specifiedUnits);
						invalidScaleNames.removeAll(existingScaleNames);
						errors.reject("lot.input.invalid.units", new String[] {this.buildErrorMessageFromList(invalidScaleNames)}, "");
						throw new ApiRequestValidationException(errors.getAllErrors());

					}
				}
			}

			//Validate input format
			//lot.withdraw.amount.invalid
			lotWithdrawalInputDto.getWithdrawalsPerUnit().forEach((k,v) -> {
				if (v.isReserveAllAvailableBalance() && v.getWithdrawalAmount()!=null && !v.getWithdrawalAmount().equals(0D) ) {
					errors.reject("lot.withdraw.amount.invalid", "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
				if (!v.isReserveAllAvailableBalance() && (v.getWithdrawalAmount() == null || v.getWithdrawalAmount() <= 0)){
					errors.reject("lot.withdraw.amount.invalid", "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
			});

			this.lotService
				.withdrawLots(userId, lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()), lotWithdrawalInputDto,
					transactionStatus);

		} finally {
			lock.unlock();
		}
	}

	private <T> String buildErrorMessageFromList(final List<T> elements) {
		final StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(elements.stream().limit(3).map(Object::toString).collect(Collectors.joining(" , ")));

		if (elements.size() > 3) {
			stringBuilder.append(" and ").append(elements.size() - 3).append(" more");
		}

		return stringBuilder.toString();
	}

}
