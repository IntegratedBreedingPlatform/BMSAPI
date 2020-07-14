package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface LotService {

	List<ExtendedLotDto> searchLots(LotsSearchDto lotsSearchDto, Pageable pageable);

	long countSearchLots(LotsSearchDto lotsSearchDto);

	List<UserDefinedField> getGermplasmAttributeTypes(LotsSearchDto searchDto);

	Map<Integer, Map<Integer, String>> getGermplasmAttributeValues(LotsSearchDto searchDto);

	String saveLot(LotGeneratorInputDto lotGeneratorInputDto);

	List<String> createLots(LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto);

	void updateLots(List<ExtendedLotDto> lotDtos, LotUpdateRequestDto lotRequest);

	void importLotsWithInitialTransaction(LotImportRequestDto lotImportRequestDto);

	LotSearchMetadata getLotsSearchMetadata(LotsSearchDto lotsSearchDto);

	void closeLots(LotsSearchDto lotsSearchDto);
}
