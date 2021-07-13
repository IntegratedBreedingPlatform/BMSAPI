package org.ibp.api.domain.sample;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class SampleObservationDto implements Serializable {

	private static final long serialVersionUID = 2340381705850740790L;

	private Integer studyDbId;
	private Integer locationDbId;
	private String observationUnitDbId;
	private String plantDbId;
	private String sampleDbId;
	private String takenBy;
	private String sampleDate;
	private String sampleType;
	private String tissueType;
	private String notes;
	private String studyName;
	private String season;
	private String locationName;
	private Integer entryNumber;
	private Integer plotNumber;
	private String germplasmDbId;
	private String plantingDate;
	private String harvestDate;

	public SampleObservationDto() {

	}

	public SampleObservationDto(final Integer studyDbId, final String obsUnitId, final String plantId, final String sampleDbId) {
		this.studyDbId = studyDbId;
		this.observationUnitDbId = obsUnitId;
		this.plantDbId = plantId;
		this.sampleDbId = sampleDbId;
	}

	public Integer getStudyDbId() {
		return this.studyDbId;
	}

	public void setStudyDbId(final Integer studyDbId) {
		this.studyDbId = studyDbId;
	}

	public Integer getLocationDbId() {
		return this.locationDbId;
	}

	public void setLocationDbId(final Integer locationDbId) {
		this.locationDbId = locationDbId;
	}

	public String getObservationUnitDbId() {
		return this.observationUnitDbId;
	}

	public void setObservationUnitDbId(final String observationUnitDbId) {
		this.observationUnitDbId = observationUnitDbId;
	}

	public String getPlantDbId() {
		return this.plantDbId;
	}

	public void setPlantDbId(final String plantDbId) {
		this.plantDbId = plantDbId;
	}

	public String getSampleDbId() {
		return this.sampleDbId;
	}

	public void setSampleDbId(final String sampleDbId) {
		this.sampleDbId = sampleDbId;
	}

	public String getTakenBy() {
		return this.takenBy;
	}

	public void setTakenBy(final String takenBy) {
		this.takenBy = takenBy;
	}

	public String getSampleDate() {
		return this.sampleDate;
	}

	public void setSampleDate(final String sampleDate) {
		this.sampleDate = sampleDate;
	}

	public String getSampleType() {
		return this.sampleType;
	}

	public void setSampleType(final String sampleType) {
		this.sampleType = sampleType;
	}

	public String getTissueType() {
		return this.tissueType;
	}

	public void setTissueType(final String tissueType) {
		this.tissueType = tissueType;
	}

	public String getNotes() {
		return this.notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public String getStudyName() {
		return this.studyName;
	}

	public void setStudyName(final String studyName) {
		this.studyName = studyName;
	}

	public String getSeason() {
		return this.season;
	}

	public void setSeason(final String season) {
		this.season = season;
	}

	public String getLocationName() {
		return this.locationName;
	}

	public void setLocationName(final String locationName) {
		this.locationName = locationName;
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

	public String getGermplasmDbId() {
		return this.germplasmDbId;
	}

	public void setGermplasmDbId(final String germplasmDbId) {
		this.germplasmDbId = germplasmDbId;
	}

	public String getPlantingDate() {
		return this.plantingDate;
	}

	public void setPlantingDate(final String plantingDate) {
		this.plantingDate = plantingDate;
	}

	public String getHarvestDate() {
		return this.harvestDate;
	}

	public void setHarvestDate(final String harvestDate) {
		this.harvestDate = harvestDate;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof SampleObservationDto)) {
			return false;
		}
		final SampleObservationDto sampleObservationDto = (SampleObservationDto) other;
		return new EqualsBuilder()
			.append(this.studyDbId, sampleObservationDto.studyDbId)
			.append(this.locationDbId, sampleObservationDto.locationDbId)
			.append(this.observationUnitDbId, sampleObservationDto.observationUnitDbId)
			.append(this.plantDbId, sampleObservationDto.plantDbId)
			.append(this.sampleDbId, sampleObservationDto.sampleDbId)
			.append(this.germplasmDbId, sampleObservationDto.germplasmDbId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(this.studyDbId)
			.append(this.locationDbId)
			.append(this.observationUnitDbId)
			.append(this.plantDbId)
			.append(this.sampleDbId)
			.append(this.germplasmDbId).hashCode();
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}

