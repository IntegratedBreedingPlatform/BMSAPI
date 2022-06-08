package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.germplasm.AttributeRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;

import java.util.List;

public interface LotAttributeService {

	List<GermplasmAttributeDto> getLotAttributeDtos(Integer gid, String programUUID);

	AttributeRequestDto createLotAttribute(Integer gid, AttributeRequestDto dto);

	AttributeRequestDto updateLotAttribute(Integer attributeId, AttributeRequestDto dto);

	void deleteLotAttribute(Integer attributeId);
}
