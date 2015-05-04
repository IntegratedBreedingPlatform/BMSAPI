
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

public class GermplasmListDetails extends GermplasmListSummary {

	private final List<GermplasmListEntrySummary> germplasmEntries = new ArrayList<GermplasmListEntrySummary>();

	public void addGermplasmEntry(GermplasmListEntrySummary germplasmEntry) {
		if (germplasmEntry != null) {
			this.germplasmEntries.add(germplasmEntry);
		}
	}

	public List<GermplasmListEntrySummary> getGermplasmEntries() {
		return germplasmEntries;
	}

}
