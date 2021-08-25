package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.brapi.v2.study.StudyImportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StudyServiceBrapi {

	Optional<StudyDetailsDto> getStudyDetailsByInstance(Integer instanceId);

	long countStudyInstances(StudySearchFilter studySearchFilter);

	List<StudyInstanceDto> getStudyInstances(StudySearchFilter studySearchFilter, Pageable pageable);

	List<StudyInstanceDto> getStudyInstancesWithMetadata(StudySearchFilter studySearchFilter, Pageable pageable);

	StudyImportResponse createStudies(String cropName, List<StudyImportRequestDTO> studyImportRequestDTOS);

}