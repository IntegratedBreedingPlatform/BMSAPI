package org.ibp.api.java.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;

import java.util.List;

public interface GermplasmAttributeService {

	List<GermplasmAttributeDto> getGermplasmAttributeDtos(Integer gid, String attributeType);

	GermplasmAttributeRequestDto createGermplasmAttribute(Integer gid, GermplasmAttributeRequestDto germplasmAttributeRequestDto);
}
