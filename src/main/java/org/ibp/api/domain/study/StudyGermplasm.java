
package org.ibp.api.domain.study;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;

import javax.validation.constraints.NotNull;

public class StudyGermplasm {

	@JsonUnwrapped
	private GermplasmListEntrySummary germplasmListEntrySummary = new GermplasmListEntrySummary();

	@ApiModelProperty(value = "Type of entry: Check, Test.")
	@NotBlank
	private String entryType;

	@NotNull
	private Integer entryNumber;


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
				.append(this.germplasmListEntrySummary, castOther.germplasmListEntrySummary)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.entryType).append(this.entryNumber)
				.append(this.germplasmListEntrySummary).toHashCode();
	}

}
