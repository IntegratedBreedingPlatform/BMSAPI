package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.inventory.manager.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {

	List<TransactionDto> searchTransactions(TransactionsSearchDto lotsSearchDto, Pageable pageable);

	long countSearchTransactions(TransactionsSearchDto lotsSearchDto);

	List<TransactionStatus> getAllTransactionStatus();

	List<TransactionType> getAllTransactionTypes();

	void saveWithdrawals(LotWithdrawalInputDto lotWithdrawalInputDto, TransactionStatus transactionStatus);

	void confirmPendingTransactions(SearchCompositeDto searchCompositeDto);

	List<TransactionDto> getAvailableBalanceTransactions(Integer lotId);

	void updatePendingTransactions(List<TransactionUpdateRequestDto> transactionUpdateInputDtos);

	void saveDeposits(LotDepositRequestDto lotDepositRequestDto, TransactionStatus transactionStatus);

	void cancelPendingTransactions(SearchCompositeDto searchCompositeDto);

}
