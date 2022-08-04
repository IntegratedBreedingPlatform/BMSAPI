package org.ibp.api.java.study;

import org.generationcp.middleware.domain.dms.InstanceDescriptorData;
import org.generationcp.middleware.domain.dms.InstanceObservationData;
import org.ibp.api.domain.study.StudyInstance;

import java.util.List;
import java.util.Optional;

public interface StudyInstanceService {

	List<StudyInstance> createStudyInstances(final String cropName, final int studyId, String programUUID,
		final Integer numberOfInstancesToGenerate);

	List<StudyInstance> getStudyInstances(int studyId);

	void deleteStudyInstances(final Integer studyId, final List<Integer> instanceIds);

	Optional<StudyInstance> getStudyInstance(int studyId, final Integer instanceId);

	InstanceObservationData addInstanceObservation(Integer studyId, Integer instanceId, InstanceObservationData instanceObservationData);

	InstanceObservationData updateInstanceObservation(Integer studyId, Integer instanceId, Integer observationDataId,
		InstanceObservationData instanceObservationData);

	InstanceDescriptorData addInstanceDescriptorData(Integer studyId, Integer instanceId, InstanceDescriptorData instanceDescriptorData);

	InstanceDescriptorData updateInstanceDescriptorData(Integer studyId, Integer instanceId, Integer descriptorDataId,
		InstanceDescriptorData instanceDescriptorData);

	void deleteInstanceGeoreferences(Integer studyId, Integer instanceId);
}
