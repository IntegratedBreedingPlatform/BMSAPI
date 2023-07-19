package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.inventory.common.LotGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.inventory.manager.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface LotService {

	List<ExtendedLotDto> searchLots(LotsSearchDto lotsSearchDto, Pageable pageable);

	List<ExtendedLotDto> searchLotsApplyExportResultsLimit(LotsSearchDto lotsSearchDto, Pageable pageable);

	long countSearchLots(LotsSearchDto lotsSearchDto);

	Map<Integer, Map<Integer, String>> getGermplasmAttributeValues(LotsSearchDto searchDto);

	String saveLot(LotGeneratorInputDto lotGeneratorInputDto);

	List<String> createLots(final String programUUID, LotGeneratorBatchRequestDto lotGeneratorBatchRequestDto);

	void updateLots(List<ExtendedLotDto> lotDtos, LotUpdateRequestDto lotRequest, String programUUID);

	void updateLotsBalance(List<LotUpdateBalanceRequestDto> lotUpdateBalanceRequestDtos, String programUUID);

	void importLotsWithInitialTransaction(String programUUID, LotImportRequestDto lotImportRequestDto);

	LotSearchMetadata getLotsSearchMetadata(LotsSearchDto lotsSearchDto);

	void closeLots(LotsSearchDto lotsSearchDto);

	void mergeLots(String keepLotUUID, LotsSearchDto lotsSearchDto);

	void splitLot(String programUUID, LotSplitRequestDto lotSplitRequestDto);

	List<LotAttributeColumnDto> getLotAttributeColumnDtos(String programUUID);

}
