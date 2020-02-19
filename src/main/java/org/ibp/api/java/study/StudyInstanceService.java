package org.ibp.api.java.study;

import org.ibp.api.domain.study.StudyInstance;

import java.util.List;
import java.util.Optional;

public interface StudyInstanceService {

	List<StudyInstance> createStudyInstances(final String cropName, final int studyId, final Integer numberOfInstancesToGenerate);

	List<StudyInstance> getStudyInstances(int studyId);

	void deleteStudyInstances(final Integer studyId, final List<Integer> instanceIds);

	Optional<StudyInstance> getStudyInstance(int studyId, final Integer instanceId);

}
