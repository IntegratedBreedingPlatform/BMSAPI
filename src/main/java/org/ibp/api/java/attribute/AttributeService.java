package org.ibp.api.java.attribute;

import org.generationcp.middleware.api.attribute.GermplasmAttributeDto;
import org.generationcp.middleware.api.attribute.GermplasmAttributeRequestDto;

import java.util.List;

public interface AttributeService {

	List<GermplasmAttributeDto> getGermplasmAttributeDtos(Integer gid, String attributeType);

	GermplasmAttributeRequestDto createGermplasmAttribute(Integer gid, GermplasmAttributeRequestDto germplasmAttributeRequestDto);
}
