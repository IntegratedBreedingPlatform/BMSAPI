package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;

import java.util.List;

public interface StudyGermplasmService {

	StudyGermplasmDto replaceStudyEntry(Integer studyId, Integer entryId, StudyGermplasmDto studyGermplasmDto);

	List<StudyGermplasmDto> createStudyEntries(Integer studyId, Integer germplasmListId);

	void deleteStudyEntries(Integer studyId);

	void updateStudyEntryProperty(Integer studyId, Integer entryId, StudyEntryPropertyData studyEntryPropertyData);
}
