package org.ibp.api.java.impl.middleware.inventory_new;

import org.generationcp.middleware.domain.inventory_new.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory_new.LotDto;
import org.generationcp.middleware.domain.inventory_new.LotsSearchDto;
import org.ibp.api.java.impl.middleware.inventory_new.validator.LotValidator;
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
	private LotValidator lotValidator;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotService lotService;

	@Override
	public List<ExtendedLotDto> searchLots(final LotsSearchDto lotsSearchDto, final Pageable pageable) {
		return lotService.searchLots(lotsSearchDto, pageable);
	}

	@Override
	public long countSearchLots(final LotsSearchDto lotsSearchDto) {
		return lotService.countSearchLots(lotsSearchDto);
	}

	@Override
	public Integer saveLot(final LotDto lotDto) {
		lotValidator.validate(lotDto);
		return lotService.saveLot(lotDto);
	}
}
