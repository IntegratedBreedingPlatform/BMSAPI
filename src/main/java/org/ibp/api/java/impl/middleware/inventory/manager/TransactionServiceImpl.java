package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotWithdrawalInputDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionInputValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.manager.TransactionService;
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
	private org.generationcp.middleware.service.api.inventory.LotService lotService;

	@Autowired
	private LotWithdrawalInputDtoValidator lotWithdrawalInputDtoValidator;

	@Autowired
	private ExtendedLotListValidator extendedLotListValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SearchRequestService searchRequestService;


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
	public void saveWithdrawals(final LotWithdrawalInputDto lotWithdrawalInputDto,
		final TransactionStatus transactionStatus) {
		try {
			//FIXME we should only locking the affected lots
			lock.lock();

			final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

			lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);

			LotsSearchDto searchDTO;
			if (lotWithdrawalInputDto.getLotsSearchId() != null) {
				searchDTO = (LotsSearchDto) this.searchRequestService
					.getSearchRequest(lotWithdrawalInputDto.getLotsSearchId(), LotsSearchDto.class);
			} else {
				searchDTO = new LotsSearchDto();
				searchDTO.setLotIds(new ArrayList<>(lotWithdrawalInputDto.getLotIds()));
			}
			final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

			extendedLotListValidator.validateAllProvidedLotIdsExist(lotDtos, lotWithdrawalInputDto.getLotIds());
			extendedLotListValidator.validateEmptyList(lotDtos);
			extendedLotListValidator.validateEmptyUnits(lotDtos);
			extendedLotListValidator.validateClosedLots(lotDtos);
			lotWithdrawalInputDtoValidator.validateWithdrawalInstructionsUnits(lotWithdrawalInputDto, lotDtos);

			this.transactionService
				.withdrawLots(user.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()), lotWithdrawalInputDto,
					transactionStatus);

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void confirmPendingTransaction(final SearchCompositeDto searchCompositeDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), TransactionService.class.getName());

		try{
			lock.lock();

			//Validate that searchId or list of lots are provided
			if (searchCompositeDto.getSearchId() == null && (searchCompositeDto.getListIds() == null || searchCompositeDto
				.getListIds().isEmpty()) ||
				searchCompositeDto.getSearchId() != null && (searchCompositeDto.getListIds() != null)) {
				errors.reject("transaction.selection.invalid", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			TransactionsSearchDto transactionsSearchDto;
			if (searchCompositeDto.getSearchId() != null) {
				transactionsSearchDto =
					(TransactionsSearchDto) this.searchRequestService.getSearchRequest(searchCompositeDto.getSearchId(), null);
			} else {
				transactionsSearchDto = new TransactionsSearchDto();
				transactionsSearchDto.setTransactionIds(new ArrayList<>(searchCompositeDto.getListIds()));
			}

			final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);
			final Set<ExtendedLotDto> lotDtos = transactionDtos.stream().map(TransactionDto::getLot).collect(
				Collectors.toSet());

			transactionInputValidator.validatePendingStatus(transactionDtos);
			extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));

			this.transactionService.confirmPendingTransaction(transactionDtos);
		} finally {
			lock.unlock();
		}
	}

}
