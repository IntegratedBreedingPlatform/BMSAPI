package org.ibp.api.java.study;

import org.generationcp.middleware.domain.dms.DescriptorData;
import org.generationcp.middleware.domain.dms.ObservationData;
import org.ibp.api.domain.study.StudyInstance;

import java.util.List;
import java.util.Optional;

public interface StudyInstanceService {

	List<StudyInstance> createStudyInstances(final String cropName, final int studyId, final Integer numberOfInstancesToGenerate);

	List<StudyInstance> getStudyInstances(int studyId);

	void deleteStudyInstances(final Integer studyId, final List<Integer> instanceIds);

	Optional<StudyInstance> getStudyInstance(int studyId, final Integer instanceId);

	ObservationData addInstanceObservation(Integer studyId, Integer instanceId, ObservationData observationData);

	ObservationData updateInstanceObservation(Integer studyId, Integer instanceId, Integer observationDataId, ObservationData observationData);

	DescriptorData addInstanceDescriptor(Integer studyId, Integer instanceId, DescriptorData descriptorData);

	DescriptorData updateInstanceDescriptor(Integer studyId, Integer instanceId, Integer descriptorDataId, DescriptorData descriptorData);

}
