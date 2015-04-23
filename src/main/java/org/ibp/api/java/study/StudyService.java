package org.ibp.api.java.study;

import java.util.List;

import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudySummary;

public interface StudyService {

	/**
	 * @param programUniqueId Optional parameter, if provided the results are filtered to only return studies that belong to the program
	 *        identified by this unique id.
	 * @return List of {@link StudySummary}ies. Omits deleted studies.
	 */
	List<StudySummary> listAllStudies(final String programUniqueId);
	
	List<Observation> getObservations(Integer studyId);

}
