
package org.ibp.api.java.study;

import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.domain.study.Observation;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyService {

	List<Observation> getObservations(final Integer studyId, final int instanceId, final int pageNumber, final int pageSize,
		final String sortBy, final String sortOrder);

	Observation getSingleObservation(Integer studyId, Integer obeservationId);

	Observation updateObservation(final Integer studyIdentifier, Observation observation);

	List<Observation> updateObservations(final Integer studyIdentifier, List<Observation> observation);

	TrialObservationTable getTrialObservationTable(final int studyIdentifier);

	/**
	 * @param studyIdentifier id for the study (Nursery / Trial)
	 * @param instanceDbId    id for a Trial instance of a Trial (Nursery has 1 instance). If present studyIdentifier will not be used
	 * @return
	 */
	TrialObservationTable getTrialObservationTable(int studyIdentifier, Integer instanceDbId);

	String getProgramUUID(Integer studyIdentifier);

	StudyDetailsDto getStudyDetailsByGeolocation(final Integer geolocationId);

	List<StudyDetailsDto> getStudyDetails(StudySearchFilter studySearchFilter, Pageable pageable);

	List<org.generationcp.middleware.domain.dms.StudySummary> getStudies(StudySearchFilter studySearchFilter, Pageable pageable);

	long countStudies(StudySearchFilter studySearchFilter);

	List<PhenotypeSearchDTO> searchPhenotypes(final Integer pageSize, final Integer pageNumber, final PhenotypeSearchRequestDTO requestDTO);

	long countPhenotypes(final PhenotypeSearchRequestDTO requestDTO);

	Boolean isSampled(final Integer studyId);

	List<StudyTypeDto> getStudyTypes();

	StudyReference getStudyReference(final Integer studyId);

	void updateStudy(final Study study);

	long countStudyInstances(StudySearchFilter studySearchFilter);

	List<StudyInstanceDto> getStudyInstances(StudySearchFilter studySearchFilter, Pageable pageable);

	List<TreeNode> getStudyTree(String parentKey, String programUUID);

	Integer getEnvironmentDatasetId(Integer studyId);

}
