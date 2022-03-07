package org.ibp.api.brapi.v2;

import org.generationcp.middleware.api.brapi.v2.attribute.AttributeDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeSearchRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AttributeServiceBrapi {

	List<AttributeDTO> getGermplasmAttributes(String crop, AttributeSearchRequestDTO requestDTO, Pageable pageable);

	long countGermplasmAttributes(AttributeSearchRequestDTO requestDTO);

}
