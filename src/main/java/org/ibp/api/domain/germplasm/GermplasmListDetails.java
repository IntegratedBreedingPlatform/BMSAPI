
package org.ibp.api.domain.germplasm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class GermplasmListDetails extends GermplasmListSummary {

	private final List<GermplasmListEntrySummary> germplasmEntries = new ArrayList<GermplasmListEntrySummary>();

	private transient int hashCode;

	public void addGermplasmEntry(GermplasmListEntrySummary germplasmEntry) {
		if (germplasmEntry != null) {
			this.germplasmEntries.add(germplasmEntry);
		}
	}

	public List<GermplasmListEntrySummary> getGermplasmEntries() {
		return this.germplasmEntries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof GermplasmListDetails)) {
			return false;
		}
		GermplasmListDetails castOther = (GermplasmListDetails) other;
		return new EqualsBuilder().append(this.germplasmEntries, castOther.germplasmEntries).isEquals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.hashCode == 0) {
			this.hashCode = new HashCodeBuilder().append(this.germplasmEntries).toHashCode();
		}
		return this.hashCode;
	}

}
