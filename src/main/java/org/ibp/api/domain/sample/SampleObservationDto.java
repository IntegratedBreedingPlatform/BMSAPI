package org.ibp.api.domain.sample;

import org.pojomatic.Pojomatic;

import java.io.Serializable;

public class SampleObservationDto implements Serializable {

	private static final long serialVersionUID = 2340381705850740790L;

	private Integer germplasmDbId;
	private String notes;
	private String observationUnitDbId;
	private String plantDbId;
	private String plateDbId;
	private Integer plateIndex;
	private Integer plotDbId;
	private String sampleDbId;
	private String sampleTimestamp;
	private String sampleType;
	private Integer studyDbId;
	private String takenBy;
	private String tissueType;

	public SampleObservationDto(){

	}

	public SampleObservationDto(
		final String observationUnitDbId, final String plantDbId, final String sampleDbId, final Integer studyDbId) {
		this.observationUnitDbId = observationUnitDbId;
		this.plantDbId = plantDbId;
		this.sampleDbId = sampleDbId;
		this.studyDbId = studyDbId;
	}

	public Integer getGermplasmDbId() {
		return germplasmDbId;
	}

	public void setGermplasmDbId(final Integer germplasmDbId) {
		this.germplasmDbId = germplasmDbId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public String getObservationUnitDbId() {
		return observationUnitDbId;
	}

	public void setObservationUnitDbId(final String observationUnitDbId) {
		this.observationUnitDbId = observationUnitDbId;
	}

	public String getPlantDbId() {
		return plantDbId;
	}

	public void setPlantDbId(final String plantDbId) {
		this.plantDbId = plantDbId;
	}

	public String getPlateDbId() {
		return plateDbId;
	}

	public void setPlateDbId(final String plateDbId) {
		this.plateDbId = plateDbId;
	}

	public Integer getPlateIndex() {
		return plateIndex;
	}

	public void setPlateIndex(final Integer plateIndex) {
		this.plateIndex = plateIndex;
	}

	public Integer getPlotDbId() {
		return plotDbId;
	}

	public void setPlotDbId(final Integer plotDbId) {
		this.plotDbId = plotDbId;
	}

	public String getSampleDbId() {
		return sampleDbId;
	}

	public void setSampleDbId(final String sampleDbId) {
		this.sampleDbId = sampleDbId;
	}

	public String getSampleTimestamp() {
		return sampleTimestamp;
	}

	public void setSampleTimestamp(final String sampleTimestamp) {
		this.sampleTimestamp = sampleTimestamp;
	}

	public String getSampleType() {
		return sampleType;
	}

	public void setSampleType(final String sampleType) {
		this.sampleType = sampleType;
	}

	public Integer getStudyDbId() {
		return studyDbId;
	}

	public void setStudyDbId(final Integer studyDbId) {
		this.studyDbId = studyDbId;
	}

	public String getTakenBy() {
		return takenBy;
	}

	public void setTakenBy(final String takenBy) {
		this.takenBy = takenBy;
	}

	public String getTissueType() {
		return tissueType;
	}

	public void setTissueType(final String tissueType) {
		this.tissueType = tissueType;
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

