package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceSearchRequest;

import java.util.List;

public interface GermplasmStudySourceService {

	List<GermplasmStudySourceDto> getGermplasmStudySources(GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest);

	long countGermplasmStudySources(GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest);

	long countFilteredGermplasmStudySources(GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest);

}
