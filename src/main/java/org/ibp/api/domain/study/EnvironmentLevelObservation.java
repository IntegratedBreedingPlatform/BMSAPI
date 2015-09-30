
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class EnvironmentLevelObservation {

	@NotNull
	private Integer environmentNumber;

	@Valid
	private List<EnvironmentLevelMeasurement> measurements = new ArrayList<>();

	public Integer getEnvironmentNumber() {
		return this.environmentNumber;
	}

	public void setEnvironmentNumber(final Integer environmentNumber) {
		this.environmentNumber = environmentNumber;
	}

	public List<EnvironmentLevelMeasurement> getMeasurements() {
		return this.measurements;
	}

	public void setMeasurements(final List<EnvironmentLevelMeasurement> measurements) {
		this.measurements = measurements;
	}

}
