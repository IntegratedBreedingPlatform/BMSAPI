
package org.ibp.api.java.study;

import java.util.List;
import java.util.Map;

import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyDetails;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.StudyImportDTO;

public interface StudyService {

	/**
	 * @param programUniqueId Optional parameter, if provided the results are filtered to only return studies that belong to the program
	 *        identified by this unique id.
	 * @return List of {@link StudySummary}ies. Omits deleted studies.
	 */
	List<StudySummary> listAllStudies(final String programUniqueId);

	List<Observation> getObservations(Integer studyId);

	Observation getSingleObservation(Integer studyId, Integer obeservationId);

	Observation updateObsevation(final Integer studyIdentifier, Observation observation);

	List<StudyGermplasm> getStudyGermplasmList(final Integer studyIdentifer);

	StudyDetails getStudyDetails(String studyId);

	Map<Integer, FieldMap> getFieldMap(final String studyIdentifier);

	Integer addNewStudy(final StudyImportDTO studyImportDTO, String programUUID);
}
