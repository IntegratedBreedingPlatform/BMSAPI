package org.ibp.api.java.impl.middleware.inventory_new;

import org.generationcp.middleware.domain.inventory_new.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory_new.TransactionDto;
import org.generationcp.middleware.domain.inventory_new.TransactionsSearchDto;
import org.ibp.api.java.impl.middleware.inventory_new.validator.TransactionInputValidator;
import org.ibp.api.java.inventory_new.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private TransactionInputValidator transactionInputValidator;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.TransactionService transactionService;

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
	public Integer saveTransaction(final TransactionDto transactionDto, final String lotId) {
		if (transactionDto.getLot() == null) {
			transactionDto.setLot(new ExtendedLotDto());
		}
		transactionDto.getLot().setLotId(Integer.valueOf(lotId));
		this.transactionInputValidator.validate(transactionDto);
		return this.transactionService.saveTransaction(transactionDto);
	}
}
