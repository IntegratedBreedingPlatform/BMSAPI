
package org.ibp.api.domain.study;


/**
 * A "light" version of {@link Measurement} dedicated for use in study import scenario.
 */
public class MeasurementImportDTO {

	private Integer traitId;

	private String traitValue;

	public Integer getTraitId() {
		return this.traitId;
	}

	public void setTraitId(final Integer traitId) {
		this.traitId = traitId;
	}

	public String getTraitValue() {
		return this.traitValue;
	}

	public void setTraitValue(final String traitValue) {
		this.traitValue = traitValue;
	}

}
