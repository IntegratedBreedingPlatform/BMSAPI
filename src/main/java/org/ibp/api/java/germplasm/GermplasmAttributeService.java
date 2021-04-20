package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.attribute.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;

import java.util.List;
import java.util.Set;

public interface GermplasmAttributeService {

	List<GermplasmAttributeDto> getGermplasmAttributeDtos(Integer gid, String attributeType);

	GermplasmAttributeRequestDto createGermplasmAttribute(Integer gid, GermplasmAttributeRequestDto germplasmAttributeRequestDto, final String programUUID);

	GermplasmAttributeRequestDto updateGermplasmAttribute(Integer gid, Integer attributeId, GermplasmAttributeRequestDto dto, final String programUUID);

	void deleteGermplasmAttribute(Integer gid, Integer attributeId);

	List<AttributeDTO> filterGermplasmAttributes(Set<String> codes, String type);
}
