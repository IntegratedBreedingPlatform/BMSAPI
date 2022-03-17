package org.ibp.api.java.impl.middleware.list;

import org.generationcp.middleware.api.brapi.v2.list.GermplasmListImportRequestDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmListSearchRequestDTO;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.GermplasmListDTO;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.ibp.api.brapi.GermplasmListServiceBrapi;
import org.ibp.api.brapi.v2.list.GermplasmListImportResponse;
import org.ibp.api.java.impl.middleware.list.validator.GermplasmListImportValidator;
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
	private GermplasmListImportValidator germplasmListImportValidator;

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

		// Remove studies that fails any validation. They will be excluded from creation
		final BindingResult bindingResult = this.germplasmListImportValidator.pruneListsInvalidForImport(importRequestDTOS);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}
		if (!CollectionUtils.isEmpty(importRequestDTOS)) {

			/*final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
			final List<StudyInstanceDto> instances =
				this.middlewareStudyServiceBrapi.saveStudyInstances(cropName, studyImportRequestDTOS, user.getUserid());
			if (!CollectionUtils.isEmpty(instances)) {
				noOfCreatedStudies = instances.size();
			}
			response.setEntityList(instances);*/
		}
		response.setCreatedSize(noOfCreatedLists);
		response.setImportListSize(originalListSize);
		return response;


		return response;
	}


}
