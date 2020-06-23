package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.study.StudyGermplasmDto;

public interface StudyGermplasmService {

    StudyGermplasmDto replaceStudyGermplasm(Integer studyId, Integer entryId, StudyGermplasmDto studyGermplasmDto);


}
