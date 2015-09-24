
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * A "light" version of {@link Observation} dedicated for use in study import scenario.
 */
public class ObservationImportDTO {

	@NotNull
	private Integer gid;

	@Valid
	private List<MeasurementImportDTO> measurements = new ArrayList<>();

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public List<MeasurementImportDTO> getMeasurements() {
		return this.measurements;
	}

	public void setMeasurements(final List<MeasurementImportDTO> measurements) {
		this.measurements = measurements;
	}
}
