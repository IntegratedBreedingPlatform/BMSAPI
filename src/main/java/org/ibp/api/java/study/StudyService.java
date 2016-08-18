
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

	List<Observation> getObservations(final Integer studyId, final int instanceNumber, final int pageNumber, final int pageSize);

	Observation getSingleObservation(Integer studyId, Integer obeservationId);

	Observation updateObservation(final Integer studyIdentifier, Observation observation);

	List<Observation> updateObservations(final Integer studyIdentifier, List<Observation> observation);

	List<StudyGermplasm> getStudyGermplasmList(final Integer studyIdentifer);

	StudyDetails getStudyDetails(String studyId);

	Map<Integer, FieldMap> getFieldMap(final String studyIdentifier);

	Integer importStudy(final StudyImportDTO studyImportDTO, String programUUID);

	List<StudyFolder> getAllStudyFolders();

	String getProgramUUID(Integer studyIdentifier);
}
