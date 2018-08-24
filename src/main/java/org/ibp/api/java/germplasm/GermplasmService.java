
package org.ibp.api.java.germplasm;

import java.util.List;

import org.generationcp.middleware.domain.gms.GermplasmDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;

public interface GermplasmService {

	int searchGermplasmCount(String searchText);

	List<GermplasmSummary> searchGermplasm(String searchText, int pageNumber, int pageSize);

	GermplasmSummary getGermplasm(String germplasmId);

	Integer DEFAULT_PEDIGREE_LEVELS = 20;

	PedigreeDTO getPedigree(String germplasmId, String notation);

	PedigreeTree getPedigreeTree(String germplasmId, Integer levels);

	DescendantTree getDescendantTree(String germplasmId);

	GermplasmDTO getGermplasmDTObyGID (final Integer germplasmId);
}
