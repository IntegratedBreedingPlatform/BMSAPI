
package org.ibp.api.java.study;

import org.generationcp.middleware.api.germplasm.GermplasmStudyDto;
import org.generationcp.middleware.api.study.StudyDTO;
import org.generationcp.middleware.api.study.StudyDetailsDTO;
import org.generationcp.middleware.api.study.StudySearchRequest;
import org.generationcp.middleware.api.study.StudySearchResponse;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyService {

	String getProgramUUID(Integer studyIdentifier);

	Boolean isSampled(Integer studyId);

	List<StudyTypeDto> getStudyTypes();

	StudyReference getStudyReference(Integer studyId);

	void updateStudy(Study study);

	Integer getEnvironmentDatasetId(Integer studyId);

	List<GermplasmStudyDto> getGermplasmStudies(Integer gid);

	void deleteStudy(Integer studyId);

	@Deprecated
	List<StudyDTO> getFilteredStudies(String programUUID, StudySearchRequest studySearchRequest, Pageable pageable);

	@Deprecated
	long countFilteredStudies(String programUUID, StudySearchRequest studySearchRequest);

	void deleteNameTypeFromStudies(Integer nameTypeId);

	List<StudySearchResponse> searchStudies(String programUUID, StudySearchRequest studySearchRequest, Pageable pageable);

	long countSearchStudies(String programUUID, StudySearchRequest studySearchRequest);

	StudyDetailsDTO getStudyDetails(final String programUUID, Integer studyId);

}
