package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmAttributeService {

	List<GermplasmAttributeDto> getGermplasmAttributeDtos(Integer gid, Integer variableTypeId, String programUUID);

	GermplasmAttributeRequestDto createGermplasmAttribute(Integer gid, GermplasmAttributeRequestDto germplasmAttributeRequestDto,
		String programUUID);

	GermplasmAttributeRequestDto updateGermplasmAttribute(Integer gid, Integer attributeId, GermplasmAttributeRequestDto dto, String programUUID);

	void deleteGermplasmAttribute(Integer gid, Integer attributeId);

	List<AttributeDTO> getAttributesByGUID(
		String germplasmUUID, List<String> attributeDbIds, Pageable pageable);

	long countAttributesByGUID(String germplasmUUID, List<String> attributeDbIds);

}
