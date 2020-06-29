package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceRequest;
import org.ibp.api.java.study.StudyGermplasmSourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class StudyGermplasmSourceServiceImpl implements StudyGermplasmSourceService {

	@Resource
	private org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceService studyGermplasmSourceMiddlewareService;

	@Override
	public List<StudyGermplasmSourceDto> getStudyGermplasmSourceList(final StudyGermplasmSourceRequest studyGermplasmSourceRequest) {
		return this.studyGermplasmSourceMiddlewareService.getStudyGermplasmSourceList(studyGermplasmSourceRequest);
	}

	@Override
	public long countStudyGermplasmSourceList(final StudyGermplasmSourceRequest studyGermplasmSourceRequest) {
		return this.studyGermplasmSourceMiddlewareService.countStudyGermplasmSourceList(studyGermplasmSourceRequest);
	}

	@Override
	public long countFilteredStudyGermplasmSourceList(final StudyGermplasmSourceRequest studyGermplasmSourceRequest) {
		return this.studyGermplasmSourceMiddlewareService.countFilteredStudyGermplasmSourceList(studyGermplasmSourceRequest);
	}
}
