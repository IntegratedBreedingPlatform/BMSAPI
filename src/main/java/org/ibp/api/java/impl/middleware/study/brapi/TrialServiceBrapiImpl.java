package org.ibp.api.java.impl.middleware.study.brapi;

import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.TrialSearchRequestDTO;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.TrialServiceBrapi;
import org.ibp.api.brapi.v1.trial.TrialSummary;
import org.ibp.api.brapi.v1.trial.TrialSummaryMapper;
import org.ibp.api.brapi.v2.trial.TrialImportResponse;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.TrialImportRequestValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TrialServiceBrapiImpl implements TrialServiceBrapi {

	@Autowired
	private org.generationcp.middleware.api.brapi.TrialServiceBrapi middlewareTrialServiceBrapi;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private TrialImportRequestValidator trialImportRequestDtoValidator;

	@Override
	public List<TrialSummary> searchTrials(final String cropName, final TrialSearchRequestDTO trialSearchRequestDTO,
		final Pageable pageable) {
		return this.translateResults(this.middlewareTrialServiceBrapi.searchTrials(trialSearchRequestDTO, pageable), cropName);
	}

	@Override
	public long countSearchTrialsResult(final TrialSearchRequestDTO trialSearchRequestDTO) {
		return this.middlewareTrialServiceBrapi.countSearchTrials(trialSearchRequestDTO);
	}

	@Override
	public TrialImportResponse createTrials(final String cropName, final List<TrialImportRequestDTO> trialImportRequestDTOs) {
		final TrialImportResponse response = new TrialImportResponse();
		final int originalListSize = trialImportRequestDTOs.size();
		int noOfCreatedTrials = 0;

		// Remove trials that fails any validation. They will be excluded from creation
		final BindingResult bindingResult =
			this.trialImportRequestDtoValidator.pruneTrialsInvalidForImport(trialImportRequestDTOs, cropName);
		if (bindingResult.hasErrors()) {
			response.setErrors(bindingResult.getAllErrors());
		}
		if (!CollectionUtils.isEmpty(trialImportRequestDTOs)) {

			final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
			final List<org.generationcp.middleware.domain.dms.TrialSummary> studySummaries =
				this.middlewareTrialServiceBrapi.saveTrials(cropName, trialImportRequestDTOs, user.getUserid());
			if (!CollectionUtils.isEmpty(studySummaries)) {
				noOfCreatedTrials = studySummaries.size();
			}
			response.setEntityList(this.translateResults(studySummaries, cropName));
		}
		response.setImportListSize(originalListSize);
		response.setCreatedSize(noOfCreatedTrials);
		return response;
	}

	@Override
	public TrialObservationTable getTrialObservationTable(final int studyIdentifier, final Integer studyDbId) {
		return this.middlewareTrialServiceBrapi.getTrialObservationTable(studyIdentifier, studyDbId);
	}

	@Override
	public TrialObservationTable getTrialObservationTable(final int studyIdentifier) {
		return this.middlewareTrialServiceBrapi.getTrialObservationTable(studyIdentifier);
	}

	private List<TrialSummary> translateResults(final List<org.generationcp.middleware.domain.dms.TrialSummary> trialSummaries,
		final String crop) {
		final ModelMapper modelMapper = TrialSummaryMapper.getInstance();
		final List<org.ibp.api.brapi.v1.trial.TrialSummary> trialSummaryList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(trialSummaries)) {
			for (final org.generationcp.middleware.domain.dms.TrialSummary trialSummary : trialSummaries) {
				final org.ibp.api.brapi.v1.trial.TrialSummary
					trialSummaryDto = modelMapper.map(trialSummary, org.ibp.api.brapi.v1.trial.TrialSummary.class);
				trialSummaryDto.setCommonCropName(crop);
				trialSummaryList.add(trialSummaryDto);
			}
		}
		return trialSummaryList;
	}
}
