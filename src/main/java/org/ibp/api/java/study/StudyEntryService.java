package org.ibp.api.java.study;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.study.StudyEntryGeneratorBatchRequestDto;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.ibp.api.java.impl.middleware.study.StudyEntryMetadata;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyEntryService {

	StudyEntryDto replaceStudyEntry(Integer studyId, Integer entryId, StudyEntryDto studyEntryDto);

	List<StudyEntryDto> createStudyEntries(Integer studyId, Integer germplasmListId);

	List<StudyEntryDto> createStudyEntries(Integer studyId, StudyEntryGeneratorBatchRequestDto studyEntryGeneratorBatchRequestDto);

	List<StudyEntryDto> getStudyEntries(Integer studyId, StudyEntrySearchDto.Filter filter, Pageable pageable);

	void deleteStudyEntries(Integer studyId);

	void updateStudyEntryProperty(Integer studyId, Integer entryId, StudyEntryPropertyData studyEntryPropertyData);

	long countAllStudyEntries(Integer studyId);

	long countAllStudyTestEntries(Integer studyId);

	long countAllCheckTestEntries(Integer studyId, String programUuid, Boolean checkOnly);

	StudyEntryMetadata getStudyEntriesMetadata(Integer studyId, String programUuid);

	List<MeasurementVariable> getEntryDescriptorColumns(Integer studyId);

	List<Integer> getGidsUsedInStudy(Integer studyId, List<Integer> gids);
}
