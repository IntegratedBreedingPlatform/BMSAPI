package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.AttributeRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmAttributeService {

	List<GermplasmAttributeDto> getGermplasmAttributeDtos(Integer gid, Integer variableTypeId, String programUUID);

	AttributeRequestDto createGermplasmAttribute(Integer gid, AttributeRequestDto germplasmAttributeRequestDto,
		String programUUID);

	AttributeRequestDto updateGermplasmAttribute(Integer gid, Integer attributeId, AttributeRequestDto dto, String programUUID);

	void deleteGermplasmAttribute(Integer gid, Integer attributeId);

	List<AttributeDTO> getAttributesByGUID(
		String germplasmUUID, List<String> attributeDbIds, Pageable pageable);

	long countAttributesByGUID(String germplasmUUID, List<String> attributeDbIds);

}
