package org.ibp.api.java.impl.middleware.germplasm.brapi;

import org.apache.commons.collections.CollectionUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportRequestValidator;
import org.ibp.api.brapi.v2.germplasm.GermplasmImportResponse;
import org.ibp.api.brapi.v2.germplasm.GermplasmUpdateRequestValidator;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class GermplasmServiceBrapiImpl implements GermplasmServiceBrapi {
	
	@Autowired
	private  org.generationcp.middleware.api.brapi.GermplasmServiceBrapi germplasmServiceBrapi;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private GermplasmImportRequestValidator germplasmImportRequestValidator;

	@Autowired
	private GermplasmUpdateRequestValidator germplasmUpdateRequestValidator;

	@Autowired
	private InstanceValidator instanceValidator;

	private BindingResult errors;

	@Override
	public GermplasmImportResponse createGermplasm(final String cropName, final List<GermplasmImportRequest> germplasmImportRequestList) {
		final GermplasmImportResponse response = new GermplasmImportResponse();
		final int originalListSize = germplasmImportRequestList.size();
		int noOfCreatedGermplasm = 0;
		// Remove germplasm that fails any validation. They will be excluded from creation
		final BindingResult bindingResult = this.germplasmImportRequestValidator.pruneGermplasmInvalidForImport(germplasmImportRequestList);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}
		if (!CollectionUtils.isEmpty(germplasmImportRequestList)) {
			final List<GermplasmDTO> germplasmDTOList = this.germplasmServiceBrapi.createGermplasm(cropName, germplasmImportRequestList);
			if (!CollectionUtils.isEmpty(germplasmDTOList)) {
				this.populateGermplasmPedigree(germplasmDTOList);
				noOfCreatedGermplasm = germplasmDTOList.size();
			}
			response.setEntityList(germplasmDTOList);
		}
		response.setCreatedSize(noOfCreatedGermplasm);
		response.setImportListSize(originalListSize);
		return response;
	}

	@Override
	public GermplasmDTO updateGermplasm(final String germplasmUUID, final GermplasmUpdateRequest germplasmUpdateRequest) {
		this.validateGermplasmUUID(germplasmUUID);
		this.germplasmUpdateRequestValidator.validate(germplasmUpdateRequest);
		return this.germplasmServiceBrapi.updateGermplasm(germplasmUUID, germplasmUpdateRequest);
	}



	@Override
	public PedigreeDTO getPedigree(final String germplasmUUID, final String notation, final Boolean includeSiblings) {
		this.validateGermplasmUUID(germplasmUUID);
		final Optional<GermplasmDTO> germplasmDTO = this.germplasmServiceBrapi.getGermplasmDTOByGUID(germplasmUUID);
		final PedigreeDTO pedigreeDTO = this.germplasmServiceBrapi.getPedigree(Integer.valueOf(germplasmDTO.get().getGid()), notation, includeSiblings);
		if (pedigreeDTO != null) {
			pedigreeDTO.setPedigree(this.pedigreeService.getCrossExpansion(Integer.valueOf(germplasmDTO.get().getGid()), this.crossExpansionProperties));
		}
		return pedigreeDTO;
	}

	@Override
	public ProgenyDTO getProgeny(final String germplasmUUID) {
		this.validateGermplasmUUID(germplasmUUID);
		final Optional<GermplasmDTO> germplasmDTO = this.germplasmServiceBrapi.getGermplasmDTOByGUID(germplasmUUID);
		return this.germplasmServiceBrapi.getProgeny(Integer.valueOf(germplasmDTO.get().getGid()));
	}

	@Override
	public GermplasmDTO getGermplasmDTObyGUID(final String germplasmUUID) {
		this.validateGermplasmUUID(germplasmUUID);
		final GermplasmDTO germplasmDTO = this.germplasmServiceBrapi.getGermplasmDTOByGUID(germplasmUUID).get();
		germplasmDTO.setPedigree(this.pedigreeService.getCrossExpansion(Integer.valueOf(germplasmDTO.getGid()), this.crossExpansionProperties));
		return germplasmDTO;
	}

	@Override
	public List<GermplasmDTO> searchGermplasmDTO(
		final GermplasmSearchRequestDto germplasmSearchRequestDTO, final Pageable pageable) {
		try {

			final List<GermplasmDTO> germplasmDTOList = this.germplasmServiceBrapi.searchGermplasmDTO(germplasmSearchRequestDTO, pageable);
			if (germplasmDTOList != null) {
				this.populateGermplasmPedigree(germplasmDTOList);
			}
			return germplasmDTOList;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to search germplasm", e);
		}
	}

	@Override
	public long countGermplasmDTOs(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		try {
			return this.germplasmServiceBrapi.countGermplasmDTOs(germplasmSearchRequestDTO);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to count germplasm", e);
		}
	}

	@Override
	public long countGermplasmByStudy(final Integer studyDbId) {
		try {
			return this.germplasmServiceBrapi.countGermplasmByStudy(studyDbId);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to count germplasm", e);
		}
	}

	@Override
	public List<GermplasmDTO> getGermplasmByStudy(final int studyDbId, final Pageable pageable) {
		try {

			this.instanceValidator.validateStudyDbId(studyDbId);

			final List<GermplasmDTO> germplasmDTOList = this.germplasmServiceBrapi.getGermplasmByStudy(studyDbId, pageable);
			if (germplasmDTOList != null) {
				this.populateGermplasmPedigree(germplasmDTOList);
			}
			return germplasmDTOList;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to search germplasm", e);
		}
	}

	private void populateGermplasmPedigree(final List<GermplasmDTO> germplasmDTOList) {
		final Set<Integer> gids = germplasmDTOList.stream().map(germplasmDTO -> Integer.valueOf(germplasmDTO.getGid()))
			.collect(Collectors.toSet());
		final Map<Integer, String> crossExpansionsMap =
			this.pedigreeService.getCrossExpansions(gids, null, this.crossExpansionProperties);
		for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
			final Integer gid = Integer.valueOf(germplasmDTO.getGid());
			germplasmDTO.setPedigree(crossExpansionsMap.get(gid));
		}
	}

	private void validateGermplasmUUID(final String germplasmUUID) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGermplasmUUID(this.errors, germplasmUUID);
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}

}
