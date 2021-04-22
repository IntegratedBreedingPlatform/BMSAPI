package org.ibp.api.java.study;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.study.StudyEntryGeneratorRequestDto;
import org.generationcp.middleware.domain.study.StudyEntryPropertyBatchUpdateRequest;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.java.impl.middleware.study.StudyEntryMetadata;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyEntryService {

	StudyEntryDto replaceStudyEntry(Integer studyId, Integer entryId, StudyEntryDto studyEntryDto);

	List<StudyEntryDto> createStudyEntries(Integer studyId, StudyEntryGeneratorRequestDto studyEntryGeneratorRequestDto);

	List<StudyEntryDto> createStudyEntries(Integer studyId, Integer listId);

	List<StudyEntryDto> getStudyEntries(Integer studyId, StudyEntrySearchDto.Filter filter, Pageable pageable);

	long countFilteredStudyEntries(Integer studyId, StudyEntrySearchDto.Filter filter);

	void deleteStudyEntries(Integer studyId);

	void updateStudyEntriesProperty(Integer studyId, StudyEntryPropertyBatchUpdateRequest studyEntryPropertyBatchUpdateRequest);

	long countAllStudyEntries(Integer studyId);

	long countAllStudyTestEntries(Integer studyId);

	long countAllCheckTestEntries(Integer studyId, String programUuid, Boolean checkOnly);

	long countAllNonReplicatedTestEntries(Integer studyId);

	StudyEntryMetadata getStudyEntriesMetadata(Integer studyId, String programUuid);

	List<MeasurementVariable> getEntryDescriptorColumns(Integer studyId);
}
