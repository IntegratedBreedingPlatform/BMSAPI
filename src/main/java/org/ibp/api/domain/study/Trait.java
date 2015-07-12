
package org.ibp.api.domain.study;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * TODO: Replace with Onotology Domain API entity.
 *
 */
public class Trait {

	private Integer traitId;

	private String traitName;

	public Trait() {

	}

	/**
	 * @param traitId
	 * @param traitName
	 */
	public Trait(Integer traitId, String traitName) {
		this.traitId = traitId;
		this.traitName = traitName;
	}

	/**
	 * @return the traitId
	 */
	public Integer getTraitId() {
		return this.traitId;
	}

	/**
	 * @param traitId the traitId to set
	 */
	public void setTraitId(Integer traitId) {
		this.traitId = traitId;
	}

	/**
	 * @return the traitName
	 */
	public String getTraitName() {
		return this.traitName;
	}

	/**
	 * @param traitName the traitName to set
	 */
	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Trait)) {
			return false;
		}
		Trait castOther = (Trait) other;
		return new EqualsBuilder().append(this.traitId, castOther.traitId).append(this.traitName, castOther.traitName).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.traitId).append(this.traitName).toHashCode();
	}

}
