
package org.ibp.api.domain.study;

import java.util.List;

public class EnvironmentLevelObservation {

	private Integer environmentNumber;

	private List<EnvironmentLevelMeasurement> measurements;

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
