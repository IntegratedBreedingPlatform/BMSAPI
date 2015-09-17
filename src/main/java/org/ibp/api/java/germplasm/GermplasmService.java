
package org.ibp.api.java.germplasm;

import java.util.List;

import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;

public interface GermplasmService {

	List<GermplasmSummary> searchGermplasm(String searchText);

	GermplasmSummary getGermplasm(String germplasmId);

	Integer DEFAULT_PEDIGREE_LEVELS = 20;

	PedigreeTree getPedigreeTree(String germplasmId, Integer levels);

}
