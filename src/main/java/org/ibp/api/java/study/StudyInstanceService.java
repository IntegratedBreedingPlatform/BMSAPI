package org.ibp.api.java.study;

import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.domain.dms.InstanceDescriptorData;
import org.generationcp.middleware.domain.dms.InstanceObservationData;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.brapi.v2.study.StudyImportResponse;
import org.ibp.api.domain.study.StudyInstance;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StudyInstanceService {

	List<StudyInstance> createStudyInstances(final String cropName, final int studyId, final Integer numberOfInstancesToGenerate);

	List<StudyInstance> getStudyInstances(int studyId);

	void deleteStudyInstances(final Integer studyId, final List<Integer> instanceIds);

	Optional<StudyInstance> getStudyInstance(int studyId, final Integer instanceId);

	InstanceObservationData addInstanceObservation(Integer studyId, Integer instanceId, InstanceObservationData instanceObservationData);

	InstanceObservationData updateInstanceObservation(Integer studyId, Integer instanceId, Integer observationDataId,
		InstanceObservationData instanceObservationData);

	InstanceDescriptorData addInstanceDescriptorData(Integer studyId, Integer instanceId, InstanceDescriptorData instanceDescriptorData);

	InstanceDescriptorData updateInstanceDescriptorData(Integer studyId, Integer instanceId, Integer descriptorDataId,
		InstanceDescriptorData instanceDescriptorData);

	StudyDetailsDto getStudyDetailsByGeolocation(Integer geolocationId);

	long countStudyInstances(StudySearchFilter studySearchFilter);

	List<StudyInstanceDto> getStudyInstances(StudySearchFilter studySearchFilter, Pageable pageable);

	List<StudyInstanceDto> getStudyInstancesWithMetadata(StudySearchFilter studySearchFilter, Pageable pageable);

	StudyImportResponse createStudies(String cropName, List<StudyImportRequestDTO> studyImportRequestDTOS);

}
