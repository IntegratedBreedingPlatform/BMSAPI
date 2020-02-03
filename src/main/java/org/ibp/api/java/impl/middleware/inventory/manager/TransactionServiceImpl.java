package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionInputValidator;
import org.ibp.api.java.inventory.manager.TransactionService;
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
	public Integer saveTransaction(final TransactionDto transactionDto) {
		this.transactionInputValidator.validate(transactionDto);
		return this.transactionService.saveTransaction(transactionDto);
	}

	@Override
	public List<TransactionStatus>  getAllTransactionStatuses() {
		return TransactionStatus.getAll();
	}

	@Override
	public List<TransactionType>  getAllTransactionTypes() {
		return TransactionType.getAll();
	}
}
