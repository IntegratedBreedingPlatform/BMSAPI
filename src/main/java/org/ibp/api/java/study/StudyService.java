
package org.ibp.api.java.study;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.api.germplasm.GermplasmStudyDto;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v2.trial.TrialImportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyService {

	TrialObservationTable getTrialObservationTable(int studyIdentifier);

	/**
	 * @param studyIdentifier id for the study (Nursery / Trial)
	 * @param instanceDbId    id for a Trial instance of a Trial (Nursery has 1 instance). If present studyIdentifier will not be used
	 * @return
	 */
	TrialObservationTable getTrialObservationTable(int studyIdentifier, Integer instanceDbId);

	String getProgramUUID(Integer studyIdentifier);

	List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(StudySearchFilter studySearchFilter, Pageable pageable);

	long countStudies(StudySearchFilter studySearchFilter);

	Boolean isSampled(Integer studyId);

	List<StudyTypeDto> getStudyTypes();

	StudyReference getStudyReference(Integer studyId);

	void updateStudy(Study study);

	List<TreeNode> getStudyTree(String parentKey, String programUUID);

	Integer getEnvironmentDatasetId(Integer studyId);

	List<GermplasmStudyDto> getGermplasmStudies(Integer gid);

	TrialImportResponse createTrials(String cropName, List<TrialImportRequestDTO> trialImportRequestDTOs);
}
