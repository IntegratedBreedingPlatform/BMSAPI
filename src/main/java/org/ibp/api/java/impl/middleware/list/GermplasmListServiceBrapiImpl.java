package org.ibp.api.java.impl.middleware.list;

import org.generationcp.middleware.api.brapi.v2.list.GermplasmListImportRequestDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmListSearchRequestDTO;
import org.generationcp.middleware.service.api.GermplasmListDTO;
import org.ibp.api.brapi.GermplasmListServiceBrapi;
import org.ibp.api.brapi.v2.list.GermplasmListImportResponse;
import org.ibp.api.java.impl.middleware.list.validator.GermplasmListImportRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@Transactional
public class GermplasmListServiceBrapiImpl implements GermplasmListServiceBrapi {

	@Autowired
	private org.generationcp.middleware.api.brapi.GermplasmListServiceBrapi middlewareGermplasmListServiceBrapi;

	@Autowired
	private GermplasmListImportRequestValidator germplasmListImportRequestValidator;

	@Override
	public List<GermplasmListDTO> searchGermplasmListDTOs(final GermplasmListSearchRequestDTO searchRequestDTO, final Pageable pageable) {
		return this.middlewareGermplasmListServiceBrapi.searchGermplasmListDTOs(searchRequestDTO, pageable);
	}

	@Override
	public long countGermplasmListDTOs(final GermplasmListSearchRequestDTO searchRequestDTO) {
		return this.middlewareGermplasmListServiceBrapi.countGermplasmListDTOs(searchRequestDTO);
	}

	@Override
	public GermplasmListImportResponse createGermplasmLists(final String crop, List<GermplasmListImportRequestDTO> importRequestDTOS) {
		final GermplasmListImportResponse response = new GermplasmListImportResponse();
		final int originalListSize = importRequestDTOS.size();
		int noOfCreatedLists = 0;

		// Remove lists that fails any validation. They will be excluded from creation
		final BindingResult bindingResult = this.germplasmListImportRequestValidator.pruneListsInvalidForImport(importRequestDTOS);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}
		if (!CollectionUtils.isEmpty(importRequestDTOS)) {
			final List<GermplasmListDTO> savedLists = this.middlewareGermplasmListServiceBrapi.saveGermplasmListDTOs(importRequestDTOS);
			if (!CollectionUtils.isEmpty(savedLists)) {
				noOfCreatedLists = savedLists.size();
			}
			response.setEntityList(savedLists);
		}
		response.setCreatedSize(noOfCreatedLists);
		response.setImportListSize(originalListSize);
		return response;
	}


}
