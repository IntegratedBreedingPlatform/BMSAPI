package org.ibp.api.rest.sample;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Date;

@AutoProperty
public class SampleDTO {

	private String sampleName;
	private String sampleBusinessKey;
	private String takenBy;
	private Date samplingDate;
	private String sampleList;
	private Integer plantNumber;
	private String plantBusinessKey;

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(final String sampleName) {
		this.sampleName = sampleName;
	}

	public String getSampleBusinessKey() {
		return sampleBusinessKey;
	}

	public void setSampleBusinessKey(final String sampleBusinessKey) {
		this.sampleBusinessKey = sampleBusinessKey;
	}

	public String getTakenBy() {
		return takenBy;
	}

	public void setTakenBy(final String takenBy) {
		this.takenBy = takenBy;
	}

	public Date getSamplingDate() {
		return samplingDate;
	}

	public void setSamplingDate(final Date samplingDate) {
		this.samplingDate = samplingDate;
	}

	public String getSampleList() {
		return sampleList;
	}

	public void setSampleList(final String sampleList) {
		this.sampleList = sampleList;
	}

	public Integer getPlantNumber() {
		return plantNumber;
	}

	public void setPlantNumber(final Integer plantNumber) {
		this.plantNumber = plantNumber;
	}

	public String getPlantBusinessKey() {
		return plantBusinessKey;
	}

	public void setPlantBusinessKey(final String plantBusinessKey) {
		this.plantBusinessKey = plantBusinessKey;
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
