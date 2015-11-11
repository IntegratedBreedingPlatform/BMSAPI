
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * A "light" version of {@link Observation} dedicated for use in study import scenario.
 */
public class ObservationImportDTO {

	@NotNull
	private Integer gid;

	@NotNull
	private Integer entryNumber;

	@NotNull
	private Integer plotNumber;

	private Integer environmentNumber;

	private Integer replicationNumber;

	@Valid
	@NotEmpty
	private List<MeasurementImportDTO> measurements = new ArrayList<>();

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public Integer getEntryNumber() {
		return this.entryNumber;
	}

	public void setEntryNumber(final Integer entryNumber) {
		this.entryNumber = entryNumber;
	}

	public Integer getPlotNumber() {
		return this.plotNumber;
	}

	public void setPlotNumber(final Integer plotNumber) {
		this.plotNumber = plotNumber;
	}

	public Integer getEnvironmentNumber() {
		return this.environmentNumber;
	}

	public void setEnvironmentNumber(final Integer environmentNumber) {
		this.environmentNumber = environmentNumber;
	}

	public Integer getReplicationNumber() {
		return this.replicationNumber;
	}

	public void setReplicationNumber(final Integer replicationNumber) {
		this.replicationNumber = replicationNumber;
	}

	public List<MeasurementImportDTO> getMeasurements() {
		return this.measurements;
	}

	public void setMeasurements(final List<MeasurementImportDTO> measurements) {
		this.measurements = measurements;
	}
}
