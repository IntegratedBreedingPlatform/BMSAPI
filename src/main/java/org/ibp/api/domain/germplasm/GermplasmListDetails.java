
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

public class GermplasmListDetails extends GermplasmListSummary {

	private final List<GermplasmSummary> germplasm = new ArrayList<GermplasmSummary>();

	public void addGermplasmEntry(GermplasmSummary gps) {
		if (gps != null) {
			this.germplasm.add(gps);
		}
	}

	public List<GermplasmSummary> getGermplasm() {
		return germplasm;
	}

}
