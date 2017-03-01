
package org.ibp.api.java.germplasm;

import java.util.List;

import org.ibp.api.domain.germplasm.GermplasmListDetails;
import org.ibp.api.domain.germplasm.GermplasmListSummary;

public interface GermplasmListService {

	List<GermplasmListSummary> searchGermplasmLists(String searchText, String cropname);

	List<GermplasmListSummary> list(String cropname);

	GermplasmListDetails getGermplasmListDetails(Integer listId);
}
