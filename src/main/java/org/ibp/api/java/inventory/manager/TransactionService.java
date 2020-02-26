package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {

	List<TransactionDto> searchTransactions(TransactionsSearchDto lotsSearchDto, Pageable pageable);

	long countSearchTransactions(TransactionsSearchDto lotsSearchDto);

	Integer saveTransaction(TransactionDto transactionDto);

	List<TransactionStatus> getAllTransactionStatus();

	List<TransactionType>  getAllTransactionTypes();

	void saveWithdrawals(Integer userId, LotsSearchDto lotsSearchDto, LotWithdrawalInputDto lotWithdrawalInputDto, TransactionStatus transactionStatus);
}
