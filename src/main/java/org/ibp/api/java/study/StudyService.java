
package org.ibp.api.java.study;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.api.germplasm.GermplasmStudyDto;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v2.study.StudyImportResponse;
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

	StudyDetailsDto getStudyDetailsByGeolocation(Integer geolocationId);

	List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(StudySearchFilter studySearchFilter, Pageable pageable);

	long countStudies(StudySearchFilter studySearchFilter);

	List<PhenotypeSearchDTO> searchPhenotypes(Integer pageSize, Integer pageNumber, PhenotypeSearchRequestDTO requestDTO);

	long countPhenotypes(PhenotypeSearchRequestDTO requestDTO);

	Boolean isSampled(Integer studyId);

	List<StudyTypeDto> getStudyTypes();

	StudyReference getStudyReference(Integer studyId);

	void updateStudy(Study study);

	long countStudyInstances(StudySearchFilter studySearchFilter);

	List<StudyInstanceDto> getStudyInstances(StudySearchFilter studySearchFilter, Pageable pageable);

	List<StudyInstanceDto> getStudyInstancesWithMetadata(StudySearchFilter studySearchFilter, Pageable pageable);

	List<TreeNode> getStudyTree(String parentKey, String programUUID);

	Integer getEnvironmentDatasetId(Integer studyId);

	List<GermplasmStudyDto> getGermplasmStudies(Integer gid);

	TrialImportResponse createTrials(String cropName, List<TrialImportRequestDTO> trialImportRequestDTOs);

	StudyImportResponse createStudies(String cropName, List<StudyImportRequestDTO> studyImportRequestDTOS);
}
