package org.ibp.api.java.impl.middleware.inventory.manager;

import org.generationcp.commons.service.StockService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotListValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.manager.LotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class LotServiceImpl implements LotService {

	@Autowired
	private LotInputValidator lotInputValidator;

	@Autowired
	private LotListValidator lotListValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotService lotService;

	@Autowired
	private StockService stockService;

	@Resource
	private ContextUtil contextUtil;

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

	@Override
	public void importLotsWithInitialTransaction(final List<LotItemDto> lotItemDtoList) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		final CropType cropType = this.contextUtil.getProjectInContext().getCropType();
		this.lotListValidator.validate(lotItemDtoList);
		this.lotService.saveLotsWithInitialTransaction(cropType, loggedInUser.getUserid(), lotItemDtoList);
	}
}
