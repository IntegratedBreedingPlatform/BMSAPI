package org.ibp.api.java.impl.middleware.study.brapi;

import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.TrialServiceBrapi;
import org.ibp.api.brapi.v2.trial.TrialImportResponse;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.TrialImportRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

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
	public List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(final StudySearchFilter studySearchFilter,
		final Pageable pageable) {
		return this.middlewareTrialServiceBrapi.getStudies(studySearchFilter, pageable);
	}

	@Override
	public long countStudies(final StudySearchFilter studySearchFilter) {
		return this.middlewareTrialServiceBrapi.countStudies(studySearchFilter);
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
			final List<StudySummary> studySummaries =
				this.middlewareTrialServiceBrapi.saveStudies(cropName, trialImportRequestDTOs, user.getUserid());
			if (!CollectionUtils.isEmpty(studySummaries)) {
				noOfCreatedTrials = studySummaries.size();
			}
			response.setEntityList(studySummaries);
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
}
