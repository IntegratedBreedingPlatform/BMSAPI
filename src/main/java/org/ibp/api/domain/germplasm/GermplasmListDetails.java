
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

public class GermplasmListDetails extends GermplasmListSummary {

	private final List<GermplasmListEnrtySummary> germplasmEntries = new ArrayList<GermplasmListEnrtySummary>();

	public void addGermplasmEntry(GermplasmListEnrtySummary germplasmEntry) {
		if (germplasmEntry != null) {
			this.germplasmEntries.add(germplasmEntry);
		}
	}

	public List<GermplasmListEnrtySummary> getGermplasmEntries() {
		return germplasmEntries;
	}

}
