package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceRequest;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.study.StudyGermplasmSourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class StudyGermplasmSourceServiceImpl implements StudyGermplasmSourceService {

	@Resource
	private PedigreeService pedigreeService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private org.generationcp.middleware.service.api.study.germplasm.source.StudyGermplasmSourceService
		studyGermplasmSourceMiddlewareService;

	@Override
	public List<StudyGermplasmSourceDto> getStudyGermplasmSourceList(final StudyGermplasmSourceRequest studyGermplasmSourceRequest) {

		final List<StudyGermplasmSourceDto> studyGermplasmSourceDtoList = this.studyGermplasmSourceMiddlewareService
			.getStudyGermplasmSourceList(studyGermplasmSourceRequest);

		for (final StudyGermplasmSourceDto studyGermplasmSourceDto : studyGermplasmSourceDtoList) {
			studyGermplasmSourceDto.setCross(this.pedigreeService.getCrossExpansion(studyGermplasmSourceDto.getGid(), this.crossExpansionProperties));
		}
		return studyGermplasmSourceDtoList;
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
