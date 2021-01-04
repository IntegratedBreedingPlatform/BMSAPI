package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceSearchRequest;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.GermplasmStudySourceService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		final GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest, final Pageable pageable) {

		this.studyValidator.validate(germplasmStudySourceSearchRequest.getStudyId(), false);

		final List<GermplasmStudySourceDto> germplasmStudySourceDtoList = this.germplasmStudySourceMiddlewareService
			.getGermplasmStudySources(germplasmStudySourceSearchRequest, pageable);

		final Map<Integer, String> crossExpansionsMap = this.pedigreeService
			.getCrossExpansions(germplasmStudySourceDtoList.stream().map(GermplasmStudySourceDto::getGid).collect(Collectors.toSet()), null,
				this.crossExpansionProperties);

		for (final GermplasmStudySourceDto germplasmStudySourceDto : germplasmStudySourceDtoList) {
			germplasmStudySourceDto.setCross(crossExpansionsMap.get(germplasmStudySourceDto.getGid()));
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
