package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LotService {

	List<ExtendedLotDto> searchLots(LotsSearchDto lotsSearchDto, Pageable pageable);

	long countSearchLots(LotsSearchDto lotsSearchDto);

	Integer saveLot(LotGeneratorInputDto lotGeneratorInputDto);

	void updateLots(List<ExtendedLotDto> lotDtos, LotUpdateRequestDto lotRequest);

	void importLotsWithInitialTransaction(LotImportRequestDto lotImportRequestDto);

	LotSearchMetadata getLotsSearchMetadata(LotsSearchDto lotsSearchDto);

}
