package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceRequest;

import java.util.List;

public interface StudyGermplasmSourceService {

	public List<StudyGermplasmSourceDto> getStudyGermplasmSourceList(final StudyGermplasmSourceRequest studyGermplasmSourceRequest);

	public long countStudyGermplasmSourceList(final StudyGermplasmSourceRequest studyGermplasmSourceRequest);

	public long countFilteredStudyGermplasmSourceList(final StudyGermplasmSourceRequest studyGermplasmSourceRequest);

}
