
package org.ibp.api.domain.study;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class StudyGermplasm {

	@NotEmpty
	private String entryType;

	@NotEmpty
	private String entryNo;

	@NotEmpty
	private String position;

	@JsonUnwrapped
	private GermplasmListEntrySummary germplasmListEntrySummary;

	/**
	 * @return the entryType
	 */
	public String getEntryType() {
		return this.entryType;
	}

	/**
	 * @param entryType the entryType to set
	 */
	public void setEntryType(String entryType) {
		this.entryType = entryType;
	}

	/**
	 * @return the entryNo
	 */
	public String getEntryNo() {
		return this.entryNo;
	}

	/**
	 * @param entryNo the entryNo to set
	 */
	public void setEntryNo(String entryNo) {
		this.entryNo = entryNo;
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
	public void setPosition(String position) {
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
	public void setGermplasmListEntrySummary(GermplasmListEntrySummary germplasmListEntrySummary) {
		this.germplasmListEntrySummary = germplasmListEntrySummary;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof StudyGermplasm)) {
			return false;
		}
		StudyGermplasm castOther = (StudyGermplasm) other;
		return new EqualsBuilder().append(this.entryType, castOther.entryType).append(this.entryNo, castOther.entryNo)
				.append(this.position, castOther.position).append(this.germplasmListEntrySummary, castOther.germplasmListEntrySummary)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.entryType).append(this.entryNo).append(this.position)
				.append(this.germplasmListEntrySummary).toHashCode();
	}

}
