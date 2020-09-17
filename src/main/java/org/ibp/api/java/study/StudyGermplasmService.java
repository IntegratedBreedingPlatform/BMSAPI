package org.ibp.api.java.study;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyGermplasmService {

    StudyGermplasmDto replaceStudyGermplasm(Integer studyId, Integer entryId, StudyGermplasmDto studyGermplasmDto);

    List<StudyEntryDto> getStudyEntries(Integer studyId, StudyEntrySearchDto.Filter filter, Pageable pageable);

    long countAllStudyEntries(Integer studyId);

    List<MeasurementVariable> getEntryDescriptorColumns(Integer studyId);
}
