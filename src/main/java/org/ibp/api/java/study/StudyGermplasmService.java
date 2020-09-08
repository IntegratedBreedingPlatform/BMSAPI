package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.study.StudyGermplasmDto;

import java.util.List;

public interface StudyGermplasmService {

	StudyGermplasmDto replaceStudyGermplasm(Integer studyId, Integer entryId, StudyGermplasmDto studyGermplasmDto);

	List<StudyGermplasmDto> createStudyGermplasmList(Integer studyId, Integer germplasmListId);

	void deleteStudyGermplasm(Integer studyId);
}
