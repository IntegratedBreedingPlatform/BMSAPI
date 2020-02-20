package org.ibp.api.java.study;

import org.ibp.api.domain.study.StudyInstance;

import java.util.List;
import java.util.Optional;

public interface StudyEnvironmentService {

	List<StudyInstance> createStudyEnvironments(final String cropName, final int studyId, final Integer numberOfEnvironmentsToGenerate);

	List<StudyInstance> getStudyEnvironments(int studyId);

	void deleteStudyEnvironments(final Integer studyId, final List<Integer> environmentIds);

	Optional<StudyInstance> getStudyEnvironment(int studyId, final Integer environmentId);

}
