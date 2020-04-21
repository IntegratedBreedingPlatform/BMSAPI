package org.ibp.api.java.impl.middleware.inventory.manager;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.SearchCompositeDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.manager.LotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LotServiceImpl implements LotService {

	@Autowired
	private LotInputValidator lotInputValidator;

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
	private SearchRequestService searchRequestService;

	@Autowired
	private ExtendedLotListValidator extendedLotListValidator;

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
	public Integer saveLot(
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

	/**
	 * TODO Extract
	 *  {@link org.ibp.api.java.impl.middleware.inventory.manager.TransactionServiceImpl#lock}
	 *  somewhere and lock update logic
	 */
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
		this.lotService.saveLotsWithInitialTransaction(cropType, loggedInUser.getUserid(), lotImportRequestDto.getLotList());
	}

	@Override
	public LotSearchMetadata getLotsSearchMetadata(final LotsSearchDto lotsSearchDto) {
		return lotService.getLotSearchMetadata(lotsSearchDto);
	}

	/**
	 * TODO Extract
	 * {@link org.ibp.api.java.impl.middleware.inventory.manager.TransactionServiceImpl#lock}
	 * somewhere and lock update logic
	 */
	@Override
	public void closeLots(final SearchCompositeDto searchCompositeDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();

		LotsSearchDto searchDTO;
		if (searchCompositeDto.getSearchRequestId() != null) {
			searchDTO = (LotsSearchDto) this.searchRequestService
				.getSearchRequest(searchCompositeDto.getSearchRequestId(), LotsSearchDto.class);
		} else {
			searchDTO = new LotsSearchDto();
			searchDTO.setLotIds(new ArrayList<>(searchCompositeDto.getItemIds()));
		}
		final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);

		extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));

		lotService.closeLots(loggedInUser.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toList()));

	}
}
