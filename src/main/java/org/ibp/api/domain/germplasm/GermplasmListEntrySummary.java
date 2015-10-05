
package org.ibp.api.domain.germplasm;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

public class GermplasmListEntrySummary {

	@NotNull
	private Integer gid;

	@NotBlank
	private String designation;

	private String seedSource;

	private String entryCode;

	private String cross;

	public GermplasmListEntrySummary() {

	}

	public GermplasmListEntrySummary(final Integer gid, final String designation, final String seedSource, final String entryCode,
			final String cross) {
		this.gid = gid;
		this.designation = designation;
		this.seedSource = seedSource;
		this.entryCode = entryCode;
		this.cross = cross;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public String getDesignation() {
		return this.designation;
	}

	public void setDesignation(final String designation) {
		this.designation = designation;
	}

	public String getSeedSource() {
		return this.seedSource;
	}

	public void setSeedSource(final String seedSource) {
		this.seedSource = seedSource;
	}

	public String getEntryCode() {
		return this.entryCode;
	}

	public void setEntryCode(final String entryCode) {
		this.entryCode = entryCode;
	}

	public String getCross() {
		return this.cross;
	}

	public void setCross(final String cross) {
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
