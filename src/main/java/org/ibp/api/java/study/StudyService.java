
package org.ibp.api.java.study;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.service.api.study.StudyDetailDto;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyDetails;
import org.ibp.api.domain.study.StudyFolder;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.StudySummary;

public interface StudyService {

	List<StudySummary> search(final String programUniqueId, String principalInvestigator, String location, String season);

	List<Observation> getObservations(Integer studyId);

	Observation getSingleObservation(Integer studyId, Integer obeservationId);

	Observation updateObservation(final Integer studyIdentifier, Observation observation);

	List<Observation> updateObservations(final Integer studyIdentifier, List<Observation> observation);

	List<StudyGermplasm> getStudyGermplasmList(final Integer studyIdentifer);

	StudyDetails getStudyDetails(String studyId);

	StudyDetailDto getStudyDetails(final int studyIdentifier);

	/**
	 *
	 * @param studyIdentifier id for the study (Nursery / Trial)
	 * @param instanceDbId id for a Trial instance of a Trial (Nursery has 1 instance). If present studyIdentifier will not be used
	 * @return
	 */
	StudyDetailDto getStudyDetails(int studyIdentifier, Integer instanceDbId);

	Map<Integer, FieldMap> getFieldMap(final String studyIdentifier);

	Integer importStudy(final StudyImportDTO studyImportDTO, String programUUID);

	List<StudyFolder> getAllStudyFolders();

	String getProgramUUID(Integer studyIdentifier);

}
