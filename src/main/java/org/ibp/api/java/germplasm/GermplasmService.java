
package org.ibp.api.java.germplasm;

import java.util.List;

import org.ibp.api.domain.germplasm.GermplasmListDetails;
import org.ibp.api.domain.germplasm.GermplasmListSummary;

public interface GermplasmService {

	List<GermplasmListSummary> searchGermplasmLists(String searchText);

	GermplasmListDetails getGermplasmListDetails(Integer listId);
}
