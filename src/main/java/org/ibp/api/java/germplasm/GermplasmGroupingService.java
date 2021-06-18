package org.ibp.api.java.germplasm;

import org.generationcp.middleware.service.api.GermplasmGroup;
import org.ibp.api.domain.germplasm.GermplasmUngroupingResponse;

import java.util.List;

public interface GermplasmGroupingService {

	List<GermplasmGroup> markFixed(GermplasmGroupingRequest germplasmGroupingRequest);

	GermplasmUngroupingResponse unfixLines(List<Integer> gids);

}
