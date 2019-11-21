package org.ibp.api.java.impl.middleware.inventory_new;


import org.generationcp.commons.service.StockService;
import org.generationcp.middleware.domain.inventory_new.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory_new.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory_new.LotsSearchDto;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.inventory_new.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory_new.LotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LotServiceImpl implements LotService {

	@Autowired
	private LotInputValidator lotInputValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotService lotService;

	@Autowired
	private StockService stockService;

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
	public Integer saveLot(final LotGeneratorInputDto lotGeneratorInputDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		lotGeneratorInputDto.setUserId(loggedInUser.getUserid());
		lotInputValidator.validate(lotGeneratorInputDto);
		final String nextStockIDPrefix;
		if (lotGeneratorInputDto.getGenerateStock()) {
			if (lotGeneratorInputDto.getStockPrefix() == null || lotGeneratorInputDto.getStockPrefix().isEmpty()) {
				nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(DEFAULT_STOCKID_PREFIX, "-");
			} else {
				nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(lotGeneratorInputDto.getStockPrefix(), "-");
			}
			lotGeneratorInputDto.setStockId(nextStockIDPrefix + "1");
		}

		return lotService.saveLot(lotGeneratorInputDto);
	}
}
