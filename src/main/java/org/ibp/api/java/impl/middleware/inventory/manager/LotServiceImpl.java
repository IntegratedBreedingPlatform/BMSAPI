package org.ibp.api.java.impl.middleware.inventory.manager;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchService;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.common.SearchOriginCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotAttributeColumnDto;
import org.generationcp.middleware.domain.inventory.manager.LotDepositRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.ims.TransactionSourceType;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.inventory.TransactionService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceSearchRequest;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotMergeValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotSplitValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.inventory.manager.LotService;
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
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.springframework.util.CollectionUtils.isEmpty;

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
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Autowired
	private SearchRequestDtoResolver searchRequestDtoResolver;

	@Autowired
	private LotMergeValidator lotMergeValidator;

	@Autowired
	private LotSplitValidator lotSplitValidator;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private GermplasmStudySourceService germplasmStudySourceService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private GermplasmSearchService germplasmSearchService;

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	private static final String DEFAULT_STOCKID_PREFIX = "SID";

	@Override
	public List<ExtendedLotDto> searchLots(final LotsSearchDto lotsSearchDto, final Pageable pageable) {
		return this.lotService.searchLots(lotsSearchDto, pageable);
	}

	@Override
	public List<ExtendedLotDto> searchLotsApplyExportResultsLimit(final LotsSearchDto lotsSearchDto, final Pageable pageable) {
		return this.lotService.searchLotsApplyExportResultsLimit(lotsSearchDto, pageable);
	}

	@Override
	public long countSearchLots(final LotsSearchDto lotsSearchDto) {
		return this.lotService.countSearchLots(lotsSearchDto);
	}

	@Override
	public Map<Integer, Map<Integer, String>> getGermplasmAttributeValues(final LotsSearchDto searchDto) {
		return this.lotService.getGermplasmAttributeValues(searchDto);
	}

	@Override
	public String saveLot(final LotGeneratorInputDto lotGeneratorInputDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		this.lotInputValidator.validate(lotGeneratorInputDto);
		if (lotGeneratorInputDto.getGenerateStock()) {
			final String nextStockIDPrefix = this.resolveStockIdPrefix(lotGeneratorInputDto.getStockPrefix());
			lotGeneratorInputDto.setStockId(nextStockIDPrefix + "1");
		}

		return this.lotService.saveLot(this.contextUtil.getProjectInContext().getCropType(), loggedInUser.getUserid(), lotGeneratorInputDto);
	}

	@Override
	public List<String> createLots(final String programUUID, final LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto) {
		// validations

		this.lotInputValidator.validate(programUUID, lotGeneratorBatchRequestDto);
		final BindingResult errors = new MapBindingResult(new HashMap<>(), LotGeneratorBatchRequestDto.class.getName());
		this.searchCompositeDtoValidator.validateSearchCompositeDto(lotGeneratorBatchRequestDto.getSearchComposite(), errors);

		final SearchCompositeDto<SearchOriginCompositeDto, Integer> searchComposite = lotGeneratorBatchRequestDto.getSearchComposite();
		checkArgument(!isEmpty(searchComposite.getItemIds()) || searchComposite != null && searchComposite.isValid(),
			"either.provide.a.list.of.items.or.a.composite");

		List<Integer> gids = null;

		if (searchComposite.getSearchRequest() != null) {
			final SearchOriginCompositeDto.SearchOrigin searchOrigin = searchComposite.getSearchRequest().getSearchOrigin();
			if (searchOrigin == null) {
				errors.reject("search.origin.no.defined", searchComposite.getSearchRequest().getSearchOrigin().values(), "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}

			switch (searchOrigin) {
				case GERMPLASM_SEARCH:
					final GermplasmSearchRequest germplasmSearchRequest = (GermplasmSearchRequest) this.searchRequestService
						.getSearchRequest(searchComposite.getSearchRequest().getSearchRequestId(), GermplasmSearchRequest.class);

					final List<GermplasmSearchResponse> germplasmSearchResponses =
						this.germplasmSearchService.searchGermplasm(germplasmSearchRequest, null, programUUID);
					gids = germplasmSearchResponses.stream().map(GermplasmSearchResponse::getGid).collect(Collectors.toList());
					checkArgument(!gids.isEmpty(), "searchrequestid.no.results");
					break;

				case MANAGE_STUDY_SOURCE:
					final GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest =
						(GermplasmStudySourceSearchRequest) this.searchRequestService
							.getSearchRequest(searchComposite.getSearchRequest().getSearchRequestId(),
								GermplasmStudySourceSearchRequest.class);
					this.studyValidator.validate(germplasmStudySourceSearchRequest.getStudyId(), false);
					gids = this.germplasmStudySourceService.getGermplasmStudySources(germplasmStudySourceSearchRequest, null).stream().map(
						GermplasmStudySourceDto::getGid).collect(Collectors.toList());
					checkArgument(!gids.isEmpty(), "searchrequestid.no.results");
					break;

				case MANAGE_STUDY_PLOT:
					final ObservationUnitsSearchDTO observationUnitsSearchDTO =
						(ObservationUnitsSearchDTO) this.searchRequestService
							.getSearchRequest(searchComposite.getSearchRequest().getSearchRequestId(),
								ObservationUnitsSearchDTO.class);
					final DatasetDTO datasetDTO = this.studyDatasetService.getDataset(Integer.valueOf(observationUnitsSearchDTO.getDatasetId()));
					this.studyValidator.validate(datasetDTO.getParentDatasetId(), false);
					this.datasetValidator.validateDataset(datasetDTO.getParentDatasetId(), observationUnitsSearchDTO.getDatasetId());

					// We only need the GID column here
					// Adding only the GID to the visible columns list so that other columns will not be processed/returned
					// thus improving the execution performance.
					observationUnitsSearchDTO.setVisibleColumns(Sets.newHashSet(TermId.GID.name()));

					gids = this.studyDatasetService.getObservationUnitRows(datasetDTO.getParentDatasetId(),
						observationUnitsSearchDTO.getDatasetId(), observationUnitsSearchDTO, null).stream().map(
						ObservationUnitRow::getGid).collect(Collectors.toList());
					checkArgument(!gids.isEmpty(), "searchrequestid.no.results");
					break;
			}
		} else {
			gids = new ArrayList<>(searchComposite.getItemIds());
			this.germplasmValidator.validateGids(errors, gids);
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final LotGeneratorInputDto lotGeneratorInput = lotGeneratorBatchRequestDto.getLotGeneratorInput();

		// resolve stock id prefix
		final String nextStockIdPrefix = lotGeneratorInput.getGenerateStock() ?
			this.resolveStockIdPrefix(lotGeneratorInput.getStockPrefix()) :
			null;

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
		return this.lotService.saveLots(cropType, loggedInUser.getUserid(), lotList);
	}

	@Override
	public void updateLots(final List<ExtendedLotDto> lotDtos, final LotUpdateRequestDto lotRequest, final String programUUID) {
		this.lotInputValidator.validate(lotDtos, lotRequest, programUUID);
		this.lotService.updateLots(lotDtos, lotRequest, programUUID);
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
		this.lotService.saveLots(cropType, loggedInUser.getUserid(), lotImportRequestDto.getLotList());
	}

	@Override
	public LotSearchMetadata getLotsSearchMetadata(final LotsSearchDto lotsSearchDto) {
		return this.lotService.getLotSearchMetadata(lotsSearchDto);
	}

	@Override
	public void closeLots(final LotsSearchDto searchDTO) {
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);
		this.extendedLotListValidator.validateClosedLots(lotDtos);
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		this.lotService.closeLots(loggedInUser.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toList()));
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
	public void splitLot(final String programUUID, final LotSplitRequestDto lotSplitRequestDto) {

		final List<String> lotUUIDs = Arrays.asList(lotSplitRequestDto.getSplitLotUUID());
		final LotsSearchDto splitLotSearchDto = new LotsSearchDto();
		splitLotSearchDto.setLotUUIDs(lotUUIDs);
		final List<ExtendedLotDto> splitLotDtosSearchResult = this.searchLots(splitLotSearchDto, null);
		this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(splitLotDtosSearchResult, new HashSet<>(lotUUIDs));

		final ExtendedLotDto splitLotDto = splitLotDtosSearchResult.get(0);
		final LotSplitRequestDto.InitialLotDepositDto initialDeposit = lotSplitRequestDto.getInitialDeposit();
		this.lotSplitValidator.validateSplitLot(programUUID, splitLotDto, lotSplitRequestDto.getNewLot(), lotSplitRequestDto.getInitialDeposit());

		//Creates the new lot
		final LotSplitRequestDto.NewLotSplitDto newLot = lotSplitRequestDto.getNewLot();
		final LotGeneratorInputDto lotGeneratorInputDto = new LotGeneratorInputDto(splitLotDto.getGid(), splitLotDto.getUnitId(), newLot);
		this.lotInputValidator.validate(lotGeneratorInputDto);
		if (lotGeneratorInputDto.getGenerateStock()) {
			final String nextStockIDPrefix = this.resolveStockIdPrefix(lotGeneratorInputDto.getStockPrefix());
			lotGeneratorInputDto.setStockId(nextStockIDPrefix + "1");
		}

		final Integer loggedInUserId = this.securityService.getCurrentlyLoggedInUser().getUserid();
		final String newLotUUID = this.lotService.saveLot(this.contextUtil.getProjectInContext().getCropType(), loggedInUserId,
			lotGeneratorInputDto);

		//Create an adjustment transaction for the split lot
		this.transactionService.saveAdjustmentTransactions(loggedInUserId,
			Arrays.asList(splitLotDto.getLotId()).stream().collect(Collectors.toSet()),
			splitLotDto.getAvailableBalance() - initialDeposit.getAmount(),
			null);

		//Create deposit transaction for the new lot
		final LotsSearchDto newLotSearchDto = new LotsSearchDto();
		newLotSearchDto.setLotUUIDs(Arrays.asList(newLotUUID));
		final List<ExtendedLotDto> newLotDtosSearchResult = this.searchLots(newLotSearchDto, null);
		if (newLotDtosSearchResult.isEmpty()) {
			final BindingResult errors = new MapBindingResult(new HashMap<>(), LotGeneratorBatchRequestDto.class.getName());
			errors.reject("lot.split.new.lot.null", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		final ExtendedLotDto newLotDto = newLotDtosSearchResult.get(0);

		final LotDepositRequestDto lotDepositRequestDto = new LotDepositRequestDto();
		final Map<String, Double> depositsPerUnit = new HashMap() {{
			this.put(splitLotDto.getUnitName(), initialDeposit.getAmount());
		}};
		lotDepositRequestDto.setDepositsPerUnit(depositsPerUnit);
		lotDepositRequestDto.setNotes(initialDeposit.getNotes());

		this.transactionService.depositLots(loggedInUserId,
			Arrays.asList(newLotDto.getLotId()).stream().collect(Collectors.toSet()),
			lotDepositRequestDto, TransactionStatus.CONFIRMED, TransactionSourceType.SPLIT_LOT,
			splitLotDto.getLotId());
	}

	@Override
	public List<LotAttributeColumnDto> getLotAttributeColumnDtos(final String programUUID) {
		return this.lotService.getLotAttributeColumnDtos(programUUID);
	}

	private String resolveStockIdPrefix(final String stockPrefix) {
		if (Objects.isNull(stockPrefix) || stockPrefix.isEmpty()) {
			return this.stockService.calculateNextStockIDPrefix(DEFAULT_STOCKID_PREFIX, "-");
		}
		return this.stockService.calculateNextStockIDPrefix(stockPrefix, "-");
	}

}
