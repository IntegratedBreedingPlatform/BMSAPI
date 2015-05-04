package org.ibp.api.java.germplasm;

import java.util.List;

import org.ibp.api.domain.germplasm.GermplasmSummary;


public interface GermplasmService {
	
	List<GermplasmSummary> searchGermplasm(String searchText);
	
	GermplasmSummary getGermplasm(String germplasmId);

}
