
package org.ibp.api.java.study;

import java.util.List;
import java.util.Map;

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

	Observation updateObsevation(final Integer studyIdentifier, Observation observation);

	List<StudyGermplasm> getStudyGermplasmList(final Integer studyIdentifer);

	StudyDetails getStudyDetails(String studyId);

	Map<Integer, FieldMap> getFieldMap(final String studyIdentifier);

	Integer importStudy(final StudyImportDTO studyImportDTO, String programUUID);

	List<StudyFolder> getAllStudyFolders();
}
