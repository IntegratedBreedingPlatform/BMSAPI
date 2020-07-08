package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceRequest;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.study.GermplasmStudySourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class GermplasmStudySourceServiceImpl implements GermplasmStudySourceService {

	@Resource
	private PedigreeService pedigreeService;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Resource
	private org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceService
		germplasmStudySourceMiddlewareService;

	@Override
	public List<GermplasmStudySourceDto> getGermplasmStudySourceList(final GermplasmStudySourceRequest germplasmStudySourceRequest) {

		final List<GermplasmStudySourceDto> germplasmStudySourceDtoList = this.germplasmStudySourceMiddlewareService
			.getGermplasmStudySourceList(germplasmStudySourceRequest);

		for (final GermplasmStudySourceDto germplasmStudySourceDto : germplasmStudySourceDtoList) {
			germplasmStudySourceDto.setCross(this.pedigreeService.getCrossExpansion(germplasmStudySourceDto.getGid(), this.crossExpansionProperties));
		}
		return germplasmStudySourceDtoList;
	}

	@Override
	public long countGermplasmStudySourceList(final GermplasmStudySourceRequest germplasmStudySourceRequest) {
		return this.germplasmStudySourceMiddlewareService.countGermplasmStudySourceList(germplasmStudySourceRequest);
	}

	@Override
	public long countFilteredGermplasmStudySourceList(final GermplasmStudySourceRequest germplasmStudySourceRequest) {
		return this.germplasmStudySourceMiddlewareService.countFilteredGermplasmStudySourceList(germplasmStudySourceRequest);
	}
}
