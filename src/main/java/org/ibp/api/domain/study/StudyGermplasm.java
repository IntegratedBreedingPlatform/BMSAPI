
package org.ibp.api.domain.study;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class StudyGermplasm {

	@JsonUnwrapped
	private GermplasmListEntrySummary germplasmListEntrySummary;

	@NotEmpty
	private String entryType;

	private Integer entryNumber;

	@NotEmpty
	private String position;

	/**
	 * @return the entryType
	 */
	public String getEntryType() {
		return this.entryType;
	}

	/**
	 * @param entryType the entryType to set
	 */
	public void setEntryType(final String entryType) {
		this.entryType = entryType;
	}

	public Integer getEntryNumber() {
		return this.entryNumber;
	}

	public void setEntryNumber(final Integer entryNumber) {
		this.entryNumber = entryNumber;
	}

	/**
	 * @return the position
	 */
	public String getPosition() {
		return this.position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(final String position) {
		this.position = position;
	}

	/**
	 * @return the germplasmListEntrySummary
	 */
	@JsonUnwrapped
	public GermplasmListEntrySummary getGermplasmListEntrySummary() {
		return this.germplasmListEntrySummary;
	}

	/**
	 * @param germplasmListEntrySummary the germplasmListEntrySummary to set
	 */
	public void setGermplasmListEntrySummary(final GermplasmListEntrySummary germplasmListEntrySummary) {
		this.germplasmListEntrySummary = germplasmListEntrySummary;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof StudyGermplasm)) {
			return false;
		}
		final StudyGermplasm castOther = (StudyGermplasm) other;
		return new EqualsBuilder().append(this.entryType, castOther.entryType).append(this.entryNumber, castOther.entryNumber)
				.append(this.position, castOther.position).append(this.germplasmListEntrySummary, castOther.germplasmListEntrySummary)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.entryType).append(this.entryNumber).append(this.position)
				.append(this.germplasmListEntrySummary).toHashCode();
	}

}
