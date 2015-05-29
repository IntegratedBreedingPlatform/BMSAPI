
package org.ibp.api.domain.study;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class MeasurementIdentifier {

	private Integer measurementId;

	private Trait trait;

	public MeasurementIdentifier() {

	}

	/**
	 * @param measurementId
	 * @param trait
	 */
	public MeasurementIdentifier(Integer measurementId, Trait trait) {
		super();
		this.measurementId = measurementId;
		this.trait = trait;
	}

	/**
	 * @return the measurementId
	 */
	public Integer getMeasurementId() {
		return this.measurementId;
	}

	/**
	 * @param measurementId the measurementId to set
	 */
	public void setMeasurementId(Integer measurementId) {
		this.measurementId = measurementId;
	}

	/**
	 * @return the trait
	 */
	public Trait getTrait() {
		return this.trait;
	}

	/**
	 * @param trait the trait to set
	 */
	public void setTrait(Trait trait) {
		this.trait = trait;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof MeasurementIdentifier)) {
			return false;
		}
		MeasurementIdentifier castOther = (MeasurementIdentifier) other;
		return new EqualsBuilder().append(this.measurementId, castOther.measurementId).append(this.trait, castOther.trait).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.measurementId).append(this.trait).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("measurementId", this.measurementId).append("trait", this.trait).toString();
	}

}
