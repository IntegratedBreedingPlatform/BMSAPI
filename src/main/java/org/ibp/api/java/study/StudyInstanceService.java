package org.ibp.api.java.study;

import org.ibp.api.domain.study.StudyInstance;

public interface StudyInstanceService {

	StudyInstance createStudyInstance(final String cropName, final Integer studyId, final String instanceNumber);

	void removeStudyInstance(final String cropName, final Integer studyId, final String instanceNumber);

}
