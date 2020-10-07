package org.ibp.api.java.impl.middleware.inventory.manager;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotAdjustmentRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.ims.TransactionSourceType;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotMergeValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotSplitValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.manager.LotService;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LotServiceImpl implements LotService {

	@Autowired
	private LotInputValidator lotInputValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private LotImportRequestDtoValidator lotImportRequestDtoValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotService lotService;

	@Autowired
	private StockService stockService;

	@Resource
	private ContextUtil contextUtil;

	@Autowired
	private ExtendedLotListValidator extendedLotListValidator;

	@Autowired
	private InventoryCommonValidator inventoryCommonValidator;

	@Autowired
	private SearchRequestDtoResolver searchRequestDtoResolver;

	@Autowired
	private LotMergeValidator lotMergeValidator;

	@Autowired
	private LotSplitValidator lotSplitValidator;

	@Autowired
	private TransactionService transactionService;

	private static final String DEFAULT_STOCKID_PREFIX = "SID";

	@Override
	public List<ExtendedLotDto> searchLots(final LotsSearchDto lotsSearchDto, final Pageable pageable) {
		return lotService.searchLots(lotsSearchDto, pageable);
	}

	@Override
	public long countSearchLots(final LotsSearchDto lotsSearchDto) {
		return lotService.countSearchLots(lotsSearchDto);
	}

	@Override
	public List<UserDefinedField> getGermplasmAttributeTypes(final LotsSearchDto searchDto) {
		return this.lotService.getGermplasmAttributeTypes(searchDto);
	}

	@Override
	public Map<Integer, Map<Integer, String>> getGermplasmAttributeValues(final LotsSearchDto searchDto) {
		return this.lotService.getGermplasmAttributeValues(searchDto);
	}

	@Override
	public String saveLot(final String programUUID,
		final LotGeneratorInputDto lotGeneratorInputDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		lotInputValidator.validate(programUUID, lotGeneratorInputDto);
		if (lotGeneratorInputDto.getGenerateStock()) {
			final String nextStockIDPrefix;
			if (lotGeneratorInputDto.getStockPrefix() == null || lotGeneratorInputDto.getStockPrefix().isEmpty()) {
				nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(DEFAULT_STOCKID_PREFIX, "-");
			} else {
				nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(lotGeneratorInputDto.getStockPrefix(), "-");
			}
			lotGeneratorInputDto.setStockId(nextStockIDPrefix + "1");
		}

		return lotService.saveLot(this.contextUtil.getProjectInContext().getCropType(), loggedInUser.getUserid(), lotGeneratorInputDto);
	}

	@Override
	public List<String> createLots(final String programUUID, final LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto) {
		// validations
		final SearchCompositeDto<Integer, Integer> searchComposite = lotGeneratorBatchRequestDto.getSearchComposite();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), LotGeneratorBatchRequestDto.class.getName());
		this.inventoryCommonValidator.validateSearchCompositeDto(searchComposite, errors);
		this.lotInputValidator.validate(programUUID, lotGeneratorBatchRequestDto);
		final List<Integer> gids = this.searchRequestDtoResolver.resolveGidSearchDto(searchComposite);
		this.germplasmValidator.validateGids(errors, gids);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final LotGeneratorInputDto lotGeneratorInput = lotGeneratorBatchRequestDto.getLotGeneratorInput();

		// resolve stock id prefix
		String nextStockIdPrefix = null;
		if (lotGeneratorInput.getGenerateStock()) {
			final String stockPrefix = lotGeneratorInput.getStockPrefix();
			if (stockPrefix == null || stockPrefix.isEmpty()) {
				nextStockIdPrefix = this.stockService.calculateNextStockIDPrefix(DEFAULT_STOCKID_PREFIX, "-");
			} else {
				nextStockIdPrefix = this.stockService.calculateNextStockIDPrefix(stockPrefix, "-");
			}
		}

		// generate lot lists
		final List<LotItemDto> lotList = new ArrayList<>();
		int stockIdSuffix = 1;

		for (final Integer gid : gids) {
			final LotItemDto lotItemDto = new LotItemDto();
			lotItemDto.setGid(gid);
			lotItemDto.setUnitId(lotGeneratorInput.getUnitId());
			lotItemDto.setStorageLocationId(lotGeneratorInput.getLocationId());
			lotItemDto.setNotes(lotGeneratorInput.getNotes());
			if (lotGeneratorInput.getGenerateStock()) {
				lotItemDto.setStockId(nextStockIdPrefix + stockIdSuffix++);
			}
			lotList.add(lotItemDto);
		}

		// save to db
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		final CropType cropType = this.contextUtil.getProjectInContext().getCropType();
		return this.lotService.saveLots(cropType, programUUID, loggedInUser.getUserid(), lotList);
	}

	@Override
	public void updateLots(final String programUUID, final List<ExtendedLotDto> lotDtos, final LotUpdateRequestDto lotRequest) {
		this.lotInputValidator.validate(programUUID, lotDtos, lotRequest);
		this.lotService.updateLots(programUUID, lotDtos, lotRequest);
	}

	@Override
	public void importLotsWithInitialTransaction(final String programUUID, final LotImportRequestDto lotImportRequestDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		final CropType cropType = this.contextUtil.getProjectInContext().getCropType();
		this.lotImportRequestDtoValidator.validate(programUUID, lotImportRequestDto);
		final List<LotItemDto> lotsWithNoStockId =
			lotImportRequestDto.getLotList().stream().filter(x -> StringUtils.isEmpty(x.getStockId())).collect(Collectors.toList());
		if (!lotsWithNoStockId.isEmpty()) {
			if (StringUtils.isEmpty(lotImportRequestDto.getStockIdPrefix())) {
				lotImportRequestDto.setStockIdPrefix(DEFAULT_STOCKID_PREFIX);
			}
			final String nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(lotImportRequestDto.getStockIdPrefix(), "-");
			int i = 0;
			for (final LotItemDto lotItemDto : lotsWithNoStockId) {
				lotItemDto.setStockId(nextStockIDPrefix + ++i);
			}
		}
		this.lotService.saveLots(cropType, programUUID, loggedInUser.getUserid(), lotImportRequestDto.getLotList());
	}

	@Override
	public LotSearchMetadata getLotsSearchMetadata(final LotsSearchDto lotsSearchDto) {
		return lotService.getLotSearchMetadata(lotsSearchDto);
	}

	@Override
	public void closeLots(final LotsSearchDto searchDTO) {
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);
		extendedLotListValidator.validateClosedLots(lotDtos);
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		lotService.closeLots(loggedInUser.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toList()));
	}

	@Override
	public void mergeLots(final String keepLotUUID, final LotsSearchDto lotsSearchDto) {
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(lotsSearchDto, null);
		this.lotMergeValidator.validate(keepLotUUID, lotDtos);

		final ExtendedLotDto lotDto = lotDtos.stream()
				.filter(extendedLotDto -> keepLotUUID.equals(extendedLotDto.getLotUUID()))
				.findFirst()
				.get();

		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		this.lotService.mergeLots(loggedInUser.getUserid(), lotDto.getLotId(), lotsSearchDto);
	}

	@Override
	public void splitLot(final LotSplitRequestDto lotSplitRequestDto, final String programUUID) {

		this.lotSplitValidator.validateRequest(lotSplitRequestDto);

		List<String> lotUUIDs = Arrays.asList(lotSplitRequestDto.getSplitLotUUID());
		final LotsSearchDto lotsSearchDto = new LotsSearchDto();
		lotsSearchDto.setLotUUIDs(lotUUIDs);
		final List<ExtendedLotDto> extendedLotDtos = this.searchLots(lotsSearchDto, null);
		this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(extendedLotDtos, new HashSet<>(lotUUIDs));

		ExtendedLotDto splitLotDto = extendedLotDtos.get(0);
		LotSplitRequestDto.InitialLotDepositDto initialDeposit = lotSplitRequestDto.getInitialDeposit();
		this.lotSplitValidator.validate(splitLotDto, initialDeposit.getAmount());

		//Creates the new lot
		final LotSplitRequestDto.NewLotSplitDto newLot = lotSplitRequestDto.getNewLot();
		final LotGeneratorInputDto lotGeneratorInputDto = new LotGeneratorInputDto();
		lotGeneratorInputDto.setGid(splitLotDto.getGid());
		lotGeneratorInputDto.setUnitId(splitLotDto.getUnitId());
		lotGeneratorInputDto.setLocationId(newLot.getLocationId());
		lotGeneratorInputDto.setNotes(newLot.getNotes());
		lotGeneratorInputDto.setGenerateStock(newLot.getGenerateStock());
		lotGeneratorInputDto.setStockPrefix(newLot.getStockPrefix());
		String newLotUUID = this.saveLot(lotGeneratorInputDto);

		//Create an adjustment transaction for the split lot
		LotAdjustmentRequestDto lotAdjustmentRequestDto = new LotAdjustmentRequestDto();
		lotAdjustmentRequestDto.setBalance(splitLotDto.getActualBalance() - initialDeposit.getAmount());

		SearchCompositeDto<Integer, String> balanceAdjustmentSearchCompositeDto = new SearchCompositeDto<>();
		balanceAdjustmentSearchCompositeDto.setItemIds(Arrays.asList(splitLotDto.getLotUUID()).stream().collect(Collectors.toSet()));
		lotAdjustmentRequestDto.setSelectedLots(balanceAdjustmentSearchCompositeDto);
		this.transactionService.saveLotBalanceAdjustment(lotAdjustmentRequestDto);

		//Create deposit transaction for the new lot
		LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
		final Map<String, Double> depositsPerUnit = new HashMap() {{
			put(splitLotDto.getUnitName(), initialDeposit.getAmount());
		}};
		lotDepositRequestDto.setDepositsPerUnit(depositsPerUnit);
		lotDepositRequestDto.setNotes(initialDeposit.getNotes());

		SearchCompositeDto<Integer, String> saveDepositSearchCompositeDto = new SearchCompositeDto<>();
		saveDepositSearchCompositeDto.setItemIds(Arrays.asList(newLotUUID).stream().collect(Collectors.toSet()));
		lotDepositRequestDto.setSelectedLots(saveDepositSearchCompositeDto);
		this.transactionService.saveDeposits(lotDepositRequestDto, TransactionStatus.CONFIRMED, TransactionSourceType.SPLIT_LOT,
			splitLotDto.getLotId());
	}

}
