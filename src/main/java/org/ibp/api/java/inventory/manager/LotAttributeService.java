package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.shared.AttributeDto;
import org.generationcp.middleware.domain.shared.AttributeRequestDto;

import java.util.List;
import java.util.Map;

public interface LotAttributeService {

	List<AttributeDto> getLotAttributeDtos(Integer lotId, String programUUID);

	AttributeRequestDto createLotAttribute(Integer lotId, AttributeRequestDto dto);

	AttributeRequestDto updateLotAttribute(Integer lotId, Integer attributeId, AttributeRequestDto dto);

	void deleteLotAttribute(Integer lotId, Integer attributeId);

	Map<Integer, Map<Integer, String>> getAttributesByLotIdsMap(List<Integer> lotIds);
}
