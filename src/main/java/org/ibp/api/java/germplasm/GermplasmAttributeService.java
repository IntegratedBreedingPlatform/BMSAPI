package org.ibp.api.java.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;

import java.util.List;

public interface GermplasmAttributeService {

	List<GermplasmAttributeDto> getGermplasmAttributeDtos(Integer gid, Integer variableTypeId);

	GermplasmAttributeRequestDto createGermplasmAttribute(Integer gid, GermplasmAttributeRequestDto germplasmAttributeRequestDto,
		String programUUID);

	GermplasmAttributeRequestDto updateGermplasmAttribute(Integer gid, Integer attributeId, GermplasmAttributeRequestDto dto, String programUUID);

	void deleteGermplasmAttribute(Integer gid, Integer attributeId);

}
