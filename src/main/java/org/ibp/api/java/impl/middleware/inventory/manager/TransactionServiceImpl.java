package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotAdjustmentRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotWithdrawalInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.brapi.v2.inventory.TransactionMapper;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotDepositDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotDepositRequestDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotWithdrawalInputDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.TransactionUpdateRequestDtoValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
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
	private StudyValidator studyValidator;

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
	private LotDepositDtoValidator lotDepositDtoValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SearchRequestDtoResolver searchRequestDtoResolver;

	@Autowired
	private TransactionUpdateRequestDtoValidator transactionUpdateRequestDtoValidator;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private LotInputValidator lotInputValidator;

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
	public List<TransactionType> getAllTransactionTypes() {
		return TransactionType.getAll();
	}

	@Override
	public void saveWithdrawals(final LotWithdrawalInputDto lotWithdrawalInputDto,
		final TransactionStatus transactionStatus) {

		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		this.lotWithdrawalInputDtoValidator.validate(lotWithdrawalInputDto);

		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(lotWithdrawalInputDto.getSelectedLots());
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

		this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(lotDtos, lotWithdrawalInputDto.getSelectedLots().getItemIds());
		this.extendedLotListValidator.validateEmptyList(lotDtos);
		this.extendedLotListValidator.validateEmptyUnits(lotDtos);
		this.extendedLotListValidator.validateClosedLots(lotDtos);
		this.lotWithdrawalInputDtoValidator.validateWithdrawalInstructionsUnits(lotWithdrawalInputDto, lotDtos);

		this.transactionService
			.withdrawLots(user.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()),
				lotWithdrawalInputDto,
				transactionStatus);
	}

	@Override
	public void confirmPendingTransactions(final SearchCompositeDto<Integer, Integer> searchCompositeDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), TransactionService.class.getName());

		this.inventoryCommonValidator.validateSearchCompositeDto(searchCompositeDto, errors);

		final TransactionsSearchDto transactionsSearchDto = this.searchRequestDtoResolver.getTransactionsSearchDto(searchCompositeDto);

		final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);
		final Set<ExtendedLotDto> lotDtos = transactionDtos.stream().map(TransactionDto::getLot).collect(
			Collectors.toSet());

		this.transactionInputValidator.validateEmptyList(transactionDtos);
		this.transactionInputValidator.validateAllProvidedTransactionsExists(transactionDtos, searchCompositeDto.getItemIds());
		this.transactionInputValidator.validatePendingStatus(transactionDtos);
		this.extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));

		this.transactionService.confirmPendingTransactions(transactionDtos);
	}

	@Override
	public List<TransactionDto> getAvailableBalanceTransactions(final String lotUUID) {
		final LotsSearchDto searchDTO = new LotsSearchDto();
		final Set<String> lotUUIDs = new HashSet<>(Arrays.asList(lotUUID));
		searchDTO.setLotUUIDs(Arrays.asList(lotUUID));

		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);
		this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(lotDtos, lotUUIDs);
		return this.transactionService.getAvailableBalanceTransactions(lotDtos.get(0).getLotId());
	}

	@Override
	public void updatePendingTransactions(final List<TransactionUpdateRequestDto> transactionUpdateInputDtos) {
		this.transactionUpdateRequestDtoValidator.validate(transactionUpdateInputDtos);
		this.transactionService.updatePendingTransactions(transactionUpdateInputDtos);
	}

	@Override
	public void saveDeposits(final LotDepositRequestDto lotDepositRequestDto, final TransactionStatus transactionStatus) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		if (lotDepositRequestDto.getSourceStudyId() != null) {
			this.studyValidator.validate(lotDepositRequestDto.getSourceStudyId(), true);
		}
		this.lotDepositRequestDtoValidator.validate(lotDepositRequestDto);

		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(lotDepositRequestDto.getSelectedLots());
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

		this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(lotDtos, lotDepositRequestDto.getSelectedLots().getItemIds());
		this.extendedLotListValidator.validateEmptyList(lotDtos);
		this.extendedLotListValidator.validateEmptyUnits(lotDtos);
		this.extendedLotListValidator.validateClosedLots(lotDtos);
		this.lotDepositRequestDtoValidator.validateDepositInstructionsUnits(lotDepositRequestDto, lotDtos);

		this.transactionService
			.depositLots(user.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()),
				lotDepositRequestDto,
				transactionStatus,
				null,
				null);
	}

	@Override
	public void saveDeposits(final List<LotDepositDto> lotDepositDtos, final TransactionStatus transactionStatus) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		this.lotDepositDtoValidator.validate(lotDepositDtos);

		final SearchCompositeDto<Integer, String> searchCompositeDto = new SearchCompositeDto<>();
		searchCompositeDto.setItemIds(lotDepositDtos.stream().map(LotDepositDto::getLotUID).collect(Collectors.toSet()));

		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(searchCompositeDto);
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

		this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(lotDtos, searchCompositeDto.getItemIds());
		this.extendedLotListValidator.validateEmptyList(lotDtos);
		this.extendedLotListValidator.validateEmptyUnits(lotDtos);
		this.extendedLotListValidator.validateClosedLots(lotDtos);

		this.transactionService
			.depositLots(user.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()),
				lotDepositDtos,
				transactionStatus);

	}

	@Override
	public void cancelPendingTransactions(final SearchCompositeDto<Integer, Integer> searchCompositeDto) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), TransactionService.class.getName());

		this.inventoryCommonValidator.validateSearchCompositeDto(searchCompositeDto, errors);

		final TransactionsSearchDto transactionsSearchDto = this.searchRequestDtoResolver.getTransactionsSearchDto(searchCompositeDto);

		final List<TransactionDto> transactionDtos = this.transactionService.searchTransactions(transactionsSearchDto, null);
		final Set<ExtendedLotDto> lotDtos = transactionDtos.stream().map(TransactionDto::getLot).collect(
			Collectors.toSet());

		this.transactionInputValidator.validateEmptyList(transactionDtos);
		this.transactionInputValidator.validateAllProvidedTransactionsExists(transactionDtos, searchCompositeDto.getItemIds());
		this.transactionInputValidator.validatePendingStatus(transactionDtos);
		this.extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));

		this.transactionService.cancelPendingTransactions(transactionDtos);

	}

	@Override
	public List<org.ibp.api.brapi.v2.inventory.TransactionDto> getTransactions(final TransactionsSearchDto transactionsSearchDto,
		final Pageable pageable) {
		final List<TransactionDto> transactions = this.transactionService.searchTransactions(transactionsSearchDto, pageable);
		final List<org.ibp.api.brapi.v2.inventory.TransactionDto> transactionList = new ArrayList<>();
		final ModelMapper transactionMapper = TransactionMapper.getInstance();
		for (final TransactionDto transactionDto : transactions) {
			transactionList.add(transactionMapper.map(transactionDto, org.ibp.api.brapi.v2.inventory.TransactionDto.class));
		}
		return transactionList;
	}

	@Override
	public void saveLotBalanceAdjustment(final LotAdjustmentRequestDto lotAdjustmentRequestDto) {
		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), TransactionService.class.getName());

		this.inventoryCommonValidator.validateSearchCompositeDto(lotAdjustmentRequestDto.getSelectedLots(), errors);

		final LotsSearchDto searchDTO = this.searchRequestDtoResolver.getLotsSearchDto(lotAdjustmentRequestDto.getSelectedLots());
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

		this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(lotDtos, lotAdjustmentRequestDto.getSelectedLots().getItemIds());
		this.extendedLotListValidator.validateEmptyList(lotDtos);
		this.extendedLotListValidator.validateEmptyUnits(lotDtos);
		this.extendedLotListValidator.validateClosedLots(lotDtos);

		this.lotInputValidator.validateLotBalance(lotAdjustmentRequestDto.getBalance());
		this.inventoryCommonValidator.validateTransactionNotes(lotAdjustmentRequestDto.getNotes(), errors);

		this.transactionService
			.saveAdjustmentTransactions(user.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toSet()),
				lotAdjustmentRequestDto.getBalance(), lotAdjustmentRequestDto.getNotes());
	}
}
