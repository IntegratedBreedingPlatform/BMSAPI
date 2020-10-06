package org.ibp.api.java.study;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyEntryService {

	StudyEntryDto replaceStudyEntry(Integer studyId, Integer entryId, StudyEntryDto studyEntryDto);

	List<StudyEntryDto> createStudyEntries(Integer studyId, Integer germplasmListId);

	List<StudyEntryDto> getStudyEntries(Integer studyId, StudyEntrySearchDto.Filter filter, Pageable pageable);

	Boolean hasStudyEntries(Integer studyId);

	void deleteStudyEntries(Integer studyId);

	void updateStudyEntryProperty(Integer studyId, Integer entryId, StudyEntryPropertyData studyEntryPropertyData);

	long countAllStudyEntries(Integer studyId);

	List<MeasurementVariable> getEntryDescriptorColumns(Integer studyId);
}
