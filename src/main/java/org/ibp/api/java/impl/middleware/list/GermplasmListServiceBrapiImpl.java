package org.ibp.api.java.impl.middleware.list;

import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmListSearchRequestDTO;
import org.generationcp.middleware.service.api.GermplasmListDTO;
import org.ibp.api.brapi.GermplasmListServiceBrapi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GermplasmListServiceBrapiImpl implements GermplasmListServiceBrapi {

	@Autowired
	private org.generationcp.middleware.api.brapi.GermplasmListServiceBrapi middlewareGermplasmListServiceBrapi;

	@Override
	public List<GermplasmListDTO> searchGermplasmListDTOs(GermplasmListSearchRequestDTO searchRequestDTO, Pageable pageable) {
		return this.middlewareGermplasmListServiceBrapi.searchGermplasmListDTOs(searchRequestDTO, pageable);
	}

	@Override
	public long countGermplasmListDTOs(GermplasmListSearchRequestDTO searchRequestDTO) {
		return this.middlewareGermplasmListServiceBrapi.countGermplasmListDTOs(searchRequestDTO);
	}

}
