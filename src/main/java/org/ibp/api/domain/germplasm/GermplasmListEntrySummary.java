
package org.ibp.api.domain.germplasm;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class GermplasmListEntrySummary {

	private Integer gid;

	private String designation;

	private String seedSource;

	private String entryCode;

	private String cross;

	public GermplasmListEntrySummary() {

	}

	public GermplasmListEntrySummary(Integer gid, String designation, String seedSource, String entryCode, String cross) {
		this.gid = gid;
		this.designation = designation;
		this.seedSource = seedSource;
		this.entryCode = entryCode;
		this.cross = cross;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public String getDesignation() {
		return this.designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getSeedSource() {
		return this.seedSource;
	}

	public void setSeedSource(String seedSource) {
		this.seedSource = seedSource;
	}

	public String getEntryCode() {
		return this.entryCode;
	}

	public void setEntryCode(String entryCode) {
		this.entryCode = entryCode;
	}

	public String getCross() {
		return this.cross;
	}

	public void setCross(String cross) {
		this.cross = cross;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof GermplasmListEntrySummary)) {
			return false;
		}
		final GermplasmListEntrySummary castOther = (GermplasmListEntrySummary) other;
		return new EqualsBuilder().append(this.gid, castOther.gid).append(this.designation, castOther.designation)
				.append(this.seedSource, castOther.seedSource).append(this.entryCode, castOther.entryCode)
				.append(this.cross, castOther.cross).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.gid).append(this.designation).append(this.seedSource).append(this.entryCode)
				.append(this.cross).toHashCode();
	}

}
