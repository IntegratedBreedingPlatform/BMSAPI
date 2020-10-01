package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.manager.*;
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

	void mergeLots(String keepLotUUID, LotsSearchDto lotsSearchDto);
}
