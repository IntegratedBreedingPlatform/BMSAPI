package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.springframework.data.domain.Pageable;

import javax.print.DocFlavor;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface LotService {

	List<ExtendedLotDto> searchLots(LotsSearchDto lotsSearchDto, Pageable pageable);

	long countSearchLots(LotsSearchDto lotsSearchDto);

	Integer saveLot(LotGeneratorInputDto lotGeneratorInputDto);

	void importLotsWithInitialTransaction(List<LotItemDto> lotList);

	Map<String, BigInteger> getLotsSearchMetadata(LotsSearchDto lotsSearchDto);

}
