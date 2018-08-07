
package org.ibp.api.domain.study;

import org.generationcp.middleware.pojos.dms.Phenotype;

import java.util.Objects;

/**
 *
 */
public class Measurement {

	/**
	 * The measurement identifier can only be compared within an observation.
	 */
	private MeasurementIdentifier measurementIdentifier;

	private String measurementValue;

	private Phenotype.ValueStatus valueStatus;

	public Measurement() {

	}

	public Measurement(final MeasurementIdentifier measurementIdentifier, final String value, final Phenotype.ValueStatus valueStatus) {
		this.measurementIdentifier = measurementIdentifier;
		this.measurementValue = value;
		this.valueStatus = valueStatus;
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
	public void setMeasurementIdentifier(final MeasurementIdentifier measurementIdentifier) {
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
	public void setMeasurementValue(final String measurementValue) {
		this.measurementValue = measurementValue;
	}

	public Phenotype.ValueStatus getValueStatus() {
		return this.valueStatus;
	}

	public void setValueStatus(final Phenotype.ValueStatus valueStatus) {
		this.valueStatus = valueStatus;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		final Measurement that = (Measurement) o;
		return Objects.equals(this.measurementIdentifier, that.measurementIdentifier) &&
			Objects.equals(this.measurementValue, that.measurementValue) &&
			this.valueStatus == that.valueStatus;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.measurementIdentifier, this.measurementValue, this.valueStatus);
	}

	@Override
	public String toString() {
		return "Measurement{" +
			"measurementIdentifier=" + this.measurementIdentifier +
			", measurementValue='" + this.measurementValue + '\'' +
			", valueStatus=" + this.valueStatus +
			'}';
	}
}
