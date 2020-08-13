package org.ibp.api.java.impl.middleware.inventory.manager;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.common.SearchRequestDtoResolver;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.manager.LotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
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
	private SearchRequestService searchRequestService;

	@Autowired
	private SearchRequestDtoResolver searchRequestDtoResolver;

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
	public String saveLot(
		final LotGeneratorInputDto lotGeneratorInputDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		lotInputValidator.validate(lotGeneratorInputDto);
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
	public List<String> createLots(final LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto) {
		// validations
		final SearchCompositeDto<Integer, Integer> searchComposite = lotGeneratorBatchRequestDto.getSearchComposite();
		final BindingResult errors = new MapBindingResult(new HashMap<>(), LotGeneratorBatchRequestDto.class.getName());
		this.inventoryCommonValidator.validateSearchCompositeDto(searchComposite, errors);
		this.lotInputValidator.validate(lotGeneratorBatchRequestDto);
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
		return this.lotService.saveLots(cropType, loggedInUser.getUserid(), lotList);
	}

	@Override
	public void updateLots(final List<ExtendedLotDto> lotDtos, final LotUpdateRequestDto lotRequest) {
		this.lotInputValidator.validate(lotDtos, lotRequest);
		this.lotService.updateLots(lotDtos, lotRequest);
	}

	@Override
	public void importLotsWithInitialTransaction(final LotImportRequestDto lotImportRequestDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		final CropType cropType = this.contextUtil.getProjectInContext().getCropType();
		this.lotImportRequestDtoValidator.validate(lotImportRequestDto);
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
		return lotService.getLotSearchMetadata(lotsSearchDto);
	}

	@Override
	public void closeLots(final LotsSearchDto searchDTO) {
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);
		extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		lotService.closeLots(loggedInUser.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toList()));
	}

}
