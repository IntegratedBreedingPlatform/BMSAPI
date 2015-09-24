
package org.ibp.api.domain.study;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A "light" version of {@link Measurement} dedicated for use in study import scenario.
 */
public class MeasurementImportDTO {

	@NotNull
	private Integer traitId;

	@NotNull
	@Size(min = 1)
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
