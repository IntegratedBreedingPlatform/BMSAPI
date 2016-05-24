
package org.ibp.api.java.germplasm;

import java.util.List;

import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;

public interface GermplasmService {

	Integer DEFAULT_PEDIGREE_LEVELS = 20;

	List<GermplasmSummary> searchGermplasm(String searchText);

	GermplasmSummary getGermplasm(String germplasmId);

	PedigreeTree getPedigreeTree(String germplasmId, Integer levels);

	DescendantTree getDescendantTree(String germplasmId);

}
