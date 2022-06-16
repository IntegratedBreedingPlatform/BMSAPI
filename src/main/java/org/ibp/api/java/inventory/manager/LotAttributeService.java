package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.shared.AttributeRequestDto;
import org.generationcp.middleware.domain.shared.AttributeDto;

import java.util.List;

public interface LotAttributeService {

	List<AttributeDto> getLotAttributeDtos(Integer lotId, String programUUID);

	AttributeRequestDto createLotAttribute(Integer lotId, AttributeRequestDto dto);

	AttributeRequestDto updateLotAttribute(Integer lotId, Integer attributeId, AttributeRequestDto dto);

	void deleteLotAttribute(Integer lotId, Integer attributeId);
}
