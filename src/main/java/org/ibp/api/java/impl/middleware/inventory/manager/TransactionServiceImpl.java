package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

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
	private SearchRequestDtoResolver searchRequestDtoResolver;

	@Autowired
	private TransactionUpdateRequestDtoValidator transactionUpdateRequestDtoValidator;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

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

		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);

		final LotsSearchDto searchDTO = searchRequestDtoResolver.getLotsSearchDto(lotWithdrawalInputDto.getSelectedLots());
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

		extendedLotListValidator.validateAllProvidedLotIdsExist(lotDtos, lotWithdrawalInputDto.getSelectedLots().getItemIds());
		extendedLotListValidator.validateEmptyList(lotDtos);
		extendedLotListValidator.validateEmptyUnits(lotDtos);
		extendedLotListValidator.validateClosedLots(lotDtos);
		lotWithdrawalInputDtoValidator.validateWithdrawalInstructionsUnits(lotWithdrawalInputDto, lotDtos);

		this.transactionService
			.withdrawLots(user.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()),
				lotWithdrawalInputDto,
				transactionStatus);
	}

	@Override
	public void confirmPendingTransactions(final SearchCompositeDto<Integer, Integer> searchCompositeDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), TransactionService.class.getName());

		inventoryCommonValidator.validateSearchCompositeDto(searchCompositeDto, errors);

		final TransactionsSearchDto transactionsSearchDto = searchRequestDtoResolver.getTransactionsSearchDto(searchCompositeDto);

		final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);
		final Set<ExtendedLotDto> lotDtos = transactionDtos.stream().map(TransactionDto::getLot).collect(
			Collectors.toSet());

		transactionInputValidator.validateEmptyList(transactionDtos);
		transactionInputValidator.validateAllProvidedTransactionsExists(transactionDtos, searchCompositeDto.getItemIds());
		transactionInputValidator.validatePendingStatus(transactionDtos);
		extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));

		this.transactionService.confirmPendingTransactions(transactionDtos);
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
		transactionUpdateRequestDtoValidator.validate(transactionUpdateInputDtos);
		this.transactionService.updatePendingTransactions(transactionUpdateInputDtos);
	}

	@Override
	public void saveDeposits(final LotDepositRequestDto lotDepositRequestDto, final TransactionStatus transactionStatus) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		lotDepositRequestDtoValidator.validate(lotDepositRequestDto);

		final LotsSearchDto searchDTO = searchRequestDtoResolver.getLotsSearchDto(lotDepositRequestDto.getSelectedLots());
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
	}

	@Override
	public void cancelPendingTransactions(final SearchCompositeDto<Integer, Integer> searchCompositeDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), TransactionService.class.getName());

		inventoryCommonValidator.validateSearchCompositeDto(searchCompositeDto, errors);

		final TransactionsSearchDto transactionsSearchDto = searchRequestDtoResolver.getTransactionsSearchDto(searchCompositeDto);

		final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);
		final Set<ExtendedLotDto> lotDtos = transactionDtos.stream().map(TransactionDto::getLot).collect(
			Collectors.toSet());

		transactionInputValidator.validateEmptyList(transactionDtos);
		transactionInputValidator.validateAllProvidedTransactionsExists(transactionDtos, searchCompositeDto.getItemIds());
		transactionInputValidator.validatePendingStatus(transactionDtos);
		extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));

		this.transactionService.cancelPendingTransactions(transactionDtos);

	}

}
