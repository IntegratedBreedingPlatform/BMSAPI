package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.LotAdjustmentRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {

	List<TransactionDto> searchTransactions(TransactionsSearchDto transactionsSearchDto, Pageable pageable);

	long countSearchTransactions(TransactionsSearchDto transactionsSearchDto);

	List<TransactionStatus> getAllTransactionStatus();

	List<TransactionType> getAllTransactionTypes();

	void saveWithdrawals(LotWithdrawalInputDto lotWithdrawalInputDto, TransactionStatus transactionStatus);

	void confirmPendingTransactions(SearchCompositeDto<Integer, Integer> searchCompositeDto);

	List<TransactionDto> getAvailableBalanceTransactions(String lotUUID);

	void updatePendingTransactions(List<TransactionUpdateRequestDto> transactionUpdateInputDtos);

	void saveDeposits(LotDepositRequestDto lotDepositRequestDto, TransactionStatus transactionStatus);

	void saveDeposits(List<LotDepositDto> lotDepositDtos, TransactionStatus transactionStatus);

	void cancelPendingTransactions(SearchCompositeDto<Integer, Integer> searchCompositeDto);

	List<org.ibp.api.brapi.v2.inventory.TransactionDto> getTransactions(TransactionsSearchDto transactionsSearchDto, Pageable pageable);

	void saveLotBalanceAdjustment(LotAdjustmentRequestDto lotAdjustmentRequestDto);

}
