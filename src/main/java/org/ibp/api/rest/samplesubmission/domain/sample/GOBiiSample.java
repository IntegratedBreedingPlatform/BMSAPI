package org.ibp.api.rest.samplesubmission.domain.sample;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class GOBiiSample {

	private String name;

	private String sampleUuid;

	private String wellRow;

	private String wellColumn;

	private String plateName;

	private String sampleId;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getSampleUuid() {
		return sampleUuid;
	}

	public void setSampleUuid(final String sampleUuid) {
		this.sampleUuid = sampleUuid;
	}

	public String getWellRow() {
		return wellRow;
	}

	public void setWellRow(final String wellRow) {
		this.wellRow = wellRow;
	}

	public String getWellColumn() {
		return wellColumn;
	}

	public void setWellColumn(final String wellColumn) {
		this.wellColumn = wellColumn;
	}

	public String getPlateName() {
		return plateName;
	}

	public void setPlateName(final String plateName) {
		this.plateName = plateName;
	}

	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(final String sampleId) {
		this.sampleId = sampleId;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
