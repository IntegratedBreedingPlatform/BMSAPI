package org.ibp.api.brapi;

import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmListSearchRequestDTO;
import org.generationcp.middleware.service.api.GermplasmListDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GermplasmListServiceBrapi {

	List<GermplasmListDTO> searchGermplasmListDTOs(GermplasmListSearchRequestDTO searchRequestDTO, Pageable pageable);

	long countGermplasmListDTOs(GermplasmListSearchRequestDTO searchRequestDTO);
}
