package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceSearchRequest;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.GermplasmStudySourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class GermplasmStudySourceServiceImpl implements GermplasmStudySourceService {

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private PedigreeService pedigreeService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceService
		germplasmStudySourceMiddlewareService;

	@Override
	public List<GermplasmStudySourceDto> getGermplasmStudySources(
		final GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest) {

		this.studyValidator.validate(germplasmStudySourceSearchRequest.getStudyId(), false);

		final List<GermplasmStudySourceDto> germplasmStudySourceDtoList = this.germplasmStudySourceMiddlewareService
			.getGermplasmStudySourceList(germplasmStudySourceRequest);

		for (final GermplasmStudySourceDto germplasmStudySourceDto : germplasmStudySourceDtoList) {
			germplasmStudySourceDto.setCross(this.pedigreeService.getCrossExpansion(germplasmStudySourceDto.getGid(), this.crossExpansionProperties));
		}
		return germplasmStudySourceDtoList;
	}

	@Override
	public long countGermplasmStudySources(final GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest) {
		return this.germplasmStudySourceMiddlewareService.countGermplasmStudySources(germplasmStudySourceSearchRequest);
	}

	@Override
	public long countFilteredGermplasmStudySources(final GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest) {
		return this.germplasmStudySourceMiddlewareService.countFilteredGermplasmStudySources(germplasmStudySourceSearchRequest);
	}
}
