package org.ibp.api.java.inventory.manager;

import org.generationcp.middleware.domain.germplasm.AttributeRequestDto;

public interface LotAttributeService {

	AttributeRequestDto createLotAttribute(Integer gid, AttributeRequestDto dto);

	AttributeRequestDto updateLotAttribute(Integer attributeId, AttributeRequestDto dto);

	void deleteLotAttribute(Integer attributeId);
}
