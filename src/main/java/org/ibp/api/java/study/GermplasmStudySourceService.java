package org.ibp.api.java.study;

import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceRequest;

import java.util.List;

public interface GermplasmStudySourceService {

	public List<GermplasmStudySourceDto> getGermplasmStudySourceList(final GermplasmStudySourceRequest germplasmStudySourceRequest);

	public long countGermplasmStudySourceList(final GermplasmStudySourceRequest germplasmStudySourceRequest);

	public long countFilteredGermplasmStudySourceList(final GermplasmStudySourceRequest germplasmStudySourceRequest);

}
