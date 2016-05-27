
package org.ibp.api.domain.study;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 */
public class Measurement {

	/**
	 * The measurement identifier can only be compared within an observation.
	 */
	private MeasurementIdentifier measurementIdentifier;

	private String measurementValue;

	public Measurement() {

	}

	public Measurement(MeasurementIdentifier measurementIdentifier, final String value) {
		this.measurementIdentifier = measurementIdentifier;
		this.measurementValue = value;
	}

	/**
	 * @return the measurementIdentifier
	 */
	public MeasurementIdentifier getMeasurementIdentifier() {
		return this.measurementIdentifier;
	}

	/**
	 * @param measurementIdentifier the measurementIdentifier to set
	 */
	public void setMeasurementIdentifier(MeasurementIdentifier measurementIdentifier) {
		this.measurementIdentifier = measurementIdentifier;
	}

	/**
	 * @return the measurementValue
	 */
	public String getMeasurementValue() {
		return this.measurementValue;
	}

	/**
	 * @param measurementValue the measurementValue to set
	 */
	public void setMeasurementValue(String measurementValue) {
		this.measurementValue = measurementValue;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Measurement)) {
			return false;
		}
		Measurement castOther = (Measurement) other;
		return new EqualsBuilder().append(this.measurementIdentifier, castOther.measurementIdentifier)
				.append(this.measurementValue, castOther.measurementValue).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.measurementIdentifier).append(this.measurementValue).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("measurementIdentifier", this.measurementIdentifier)
				.append("measurementValue", this.measurementValue).toString();
	}

}
