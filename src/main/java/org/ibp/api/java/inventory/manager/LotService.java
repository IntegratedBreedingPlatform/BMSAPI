package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotSplitRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface LotService {

	List<ExtendedLotDto> searchLots(LotsSearchDto lotsSearchDto, Pageable pageable);

	long countSearchLots(LotsSearchDto lotsSearchDto);

	List<Variable> getGermplasmAttributeVariables(LotsSearchDto searchDto, String programUUID);

	Map<Integer, Map<Integer, String>> getGermplasmAttributeValues(LotsSearchDto searchDto);

	String saveLot(String programUUID, LotGeneratorInputDto lotGeneratorInputDto);

	List<String> createLots(final String programUUID, LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto);

	void updateLots(String programUUID, List<ExtendedLotDto> lotDtos, LotUpdateRequestDto lotRequest);

	void importLotsWithInitialTransaction(String programUUID, LotImportRequestDto lotImportRequestDto);

	LotSearchMetadata getLotsSearchMetadata(LotsSearchDto lotsSearchDto);

	void closeLots(LotsSearchDto lotsSearchDto);

	void mergeLots(String keepLotUUID, LotsSearchDto lotsSearchDto);

	void splitLot(String programUUID, LotSplitRequestDto lotSplitRequestDto);

}
