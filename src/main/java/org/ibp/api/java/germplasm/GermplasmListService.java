
package org.ibp.api.java.germplasm;

import java.util.List;

import org.ibp.api.domain.germplasm.GermplasmListDetails;
import org.ibp.api.domain.germplasm.GermplasmListSummary;

public interface GermplasmListService {

	List<GermplasmListSummary> searchGermplasmLists(String searchText);

	List<GermplasmListSummary> list();

	GermplasmListDetails getGermplasmListDetails(Integer listId);
}
