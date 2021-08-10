package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v2.trial.TrialImportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TrialServiceBrapi {

	TrialObservationTable getTrialObservationTable(int studyIdentifier);

	/**
	 * @param studyIdentifier id for the study (Nursery / Trial)
	 * @param instanceDbId    id for a Trial instance of a Trial (Nursery has 1 instance). If present studyIdentifier will not be used
	 * @return
	 */
	TrialObservationTable getTrialObservationTable(int studyIdentifier, Integer instanceDbId);

	List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(StudySearchFilter studySearchFilter, Pageable pageable);

	long countStudies(StudySearchFilter studySearchFilter);

	TrialImportResponse createTrials(String cropName, List<TrialImportRequestDTO> trialImportRequestDTOs);

}
