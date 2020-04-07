package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotDepositRequestDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotWithdrawalInputDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionUpdateRequestDtoValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
	private LotDepositRequestDtoValidator lotDepositRequestDtoValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private TransactionUpdateRequestDtoValidator transactionUpdateRequestDtoValidator;


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
			if (lotWithdrawalInputDto.getSelectedLots().getSearchRequestId() != null) {
				searchDTO = (LotsSearchDto) this.searchRequestService
					.getSearchRequest(lotWithdrawalInputDto.getSelectedLots().getSearchRequestId(), LotsSearchDto.class);
			} else {
				searchDTO = new LotsSearchDto();
				searchDTO.setLotIds(new ArrayList<>(lotWithdrawalInputDto.getSelectedLots().getItemIds()));
			}
			final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

			extendedLotListValidator.validateAllProvidedLotIdsExist(lotDtos, lotWithdrawalInputDto.getSelectedLots().getItemIds());
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
	public void confirmPendingTransactions(final SearchCompositeDto searchCompositeDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), TransactionService.class.getName());

		try{
			lock.lock();

			//Validate that searchId or list of lots are provided
			if (searchCompositeDto.getSearchRequestId() == null && (searchCompositeDto.getItemIds() == null || searchCompositeDto
				.getItemIds().isEmpty()) ||
				(searchCompositeDto.getSearchRequestId() != null && searchCompositeDto.getItemIds() != null)) {
				errors.reject("transaction.selection.invalid", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			TransactionsSearchDto transactionsSearchDto;
			if (searchCompositeDto.getSearchRequestId() != null) {
				transactionsSearchDto =
					(TransactionsSearchDto) this.searchRequestService
						.getSearchRequest(searchCompositeDto.getSearchRequestId(), TransactionsSearchDto.class);
			} else {
				transactionsSearchDto = new TransactionsSearchDto();
				transactionsSearchDto.setTransactionIds(new ArrayList<>(searchCompositeDto.getItemIds()));
			}

			final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);
			final Set<ExtendedLotDto> lotDtos = transactionDtos.stream().map(TransactionDto::getLot).collect(
				Collectors.toSet());

			transactionInputValidator.validateEmptyList(transactionDtos);
			transactionInputValidator.validateAllProvidedTransactionsExists(transactionDtos, searchCompositeDto.getItemIds());
			transactionInputValidator.validatePendingStatus(transactionDtos);
			extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));

			this.transactionService.confirmPendingTransactions(transactionDtos);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TransactionDto> getAvailableBalanceTransactions(final Integer lotId) {
		final LotsSearchDto searchDTO = new LotsSearchDto();
		final Set<Integer> lotIds = new HashSet<>(Arrays.asList(lotId));
		searchDTO.setLotIds(Arrays.asList(lotId));

		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);
		extendedLotListValidator.validateAllProvidedLotIdsExist(lotDtos, lotIds);
		return this.transactionService.getAvailableBalanceTransactions(lotId);
	}

	@Override
	public void updatePendingTransactions(final List<TransactionUpdateRequestDto> transactionUpdateInputDtos) {
		try {
			lock.lock();
			transactionUpdateRequestDtoValidator.validate(transactionUpdateInputDtos);
			this.transactionService.updatePendingTransactions(transactionUpdateInputDtos);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void saveDeposits(final LotDepositRequestDto lotDepositRequestDto, final TransactionStatus transactionStatus) {
		try {
			//FIXME we should only locking the affected lots
			lock.lock();

			final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

			lotDepositRequestDtoValidator.validate(lotDepositRequestDto);

			LotsSearchDto searchDTO;
			if (lotDepositRequestDto.getSelectedLots().getSearchRequestId() != null) {
				searchDTO = (LotsSearchDto) this.searchRequestService
					.getSearchRequest(lotDepositRequestDto.getSelectedLots().getSearchRequestId(), LotsSearchDto.class);
			} else {
				searchDTO = new LotsSearchDto();
				searchDTO.setLotIds(new ArrayList<>(lotDepositRequestDto.getSelectedLots().getItemIds()));
			}
			final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

			extendedLotListValidator.validateAllProvidedLotIdsExist(lotDtos, lotDepositRequestDto.getSelectedLots().getItemIds());
			extendedLotListValidator.validateEmptyList(lotDtos);
			extendedLotListValidator.validateEmptyUnits(lotDtos);
			extendedLotListValidator.validateClosedLots(lotDtos);
			lotDepositRequestDtoValidator.validateDepositInstructionsUnits(lotDepositRequestDto, lotDtos);

			this.transactionService
				.depositLots(user.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()),
					lotDepositRequestDto,
					transactionStatus);

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void cancelPendingTransactions(final SearchCompositeDto searchCompositeDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), TransactionService.class.getName());

		try {
			lock.lock();

			//Validate that searchId or list of lots are provided
			if (searchCompositeDto.getSearchRequestId() == null && (searchCompositeDto.getItemIds() == null || searchCompositeDto
				.getItemIds().isEmpty()) ||
				(searchCompositeDto.getSearchRequestId() != null && searchCompositeDto.getItemIds() != null)) {
				errors.reject("transaction.selection.invalid", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			TransactionsSearchDto transactionsSearchDto;
			if (searchCompositeDto.getSearchRequestId() != null) {
				transactionsSearchDto =
					(TransactionsSearchDto) this.searchRequestService
						.getSearchRequest(searchCompositeDto.getSearchRequestId(), TransactionsSearchDto.class);
			} else {
				transactionsSearchDto = new TransactionsSearchDto();
				transactionsSearchDto.setTransactionIds(new ArrayList<>(searchCompositeDto.getItemIds()));
			}

			final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);
			final Set<ExtendedLotDto> lotDtos = transactionDtos.stream().map(TransactionDto::getLot).collect(
				Collectors.toSet());

			transactionInputValidator.validateEmptyList(transactionDtos);
			transactionInputValidator.validateAllProvidedTransactionsExists(transactionDtos, searchCompositeDto.getItemIds());
			transactionInputValidator.validatePendingStatus(transactionDtos);
			extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));

			this.transactionService.cancelPendingTransactions(transactionDtos);
		} finally {
			lock.unlock();
		}
	}
}
