package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.germplasm.AttributeRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;

import java.util.List;

public interface LotAttributeService {

	List<GermplasmAttributeDto> getLotAttributeDtos(Integer lotId, String programUUID);

	AttributeRequestDto createLotAttribute(Integer lotId, AttributeRequestDto dto);

	AttributeRequestDto updateLotAttribute(Integer lotId, Integer attributeId, AttributeRequestDto dto);

	void deleteLotAttribute(Integer lotId, Integer attributeId);
}
