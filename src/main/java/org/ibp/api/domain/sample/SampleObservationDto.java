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
	private String plotId;
	private String plantId;
	private String sampleId;
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
	private Integer germplasmDbId;
	private String plantingDate;
	private String harvestDate;

	public SampleObservationDto(){

	}

	public SampleObservationDto(final Integer studyDbId, final String plotId, final String plantId, final String sampleId) {
		this.studyDbId = studyDbId;
		this.plotId = plotId;
		this.plantId = plantId;
		this.sampleId = sampleId;
	}

	public Integer getStudyDbId() {
		return studyDbId;
	}

	public void setStudyDbId(final Integer studyDbId) {
		this.studyDbId = studyDbId;
	}

	public Integer getLocationDbId() {
		return locationDbId;
	}

	public void setLocationDbId(final Integer locationDbId) {
		this.locationDbId = locationDbId;
	}

	public String getPlotId() {
		return plotId;
	}

	public void setPlotId(final String plotId) {
		this.plotId = plotId;
	}

	public String getPlantId() {
		return plantId;
	}

	public void setPlantId(final String plantId) {
		this.plantId = plantId;
	}

	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	public String getTakenBy() {
		return takenBy;
	}

	public void setTakenBy(final String takenBy) {
		this.takenBy = takenBy;
	}

	public String getSampleDate() {
		return sampleDate;
	}

	public void setSampleDate(final String sampleDate) {
		this.sampleDate = sampleDate;
	}

	public String getSampleType() {
		return sampleType;
	}

	public void setSampleType(final String sampleType) {
		this.sampleType = sampleType;
	}

	public String getTissueType() {
		return tissueType;
	}

	public void setTissueType(final String tissueType) {
		this.tissueType = tissueType;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(final String studyName) {
		this.studyName = studyName;
	}

	public String getSeason() {
		return season;
	}

	public void setSeason(final String season) {
		this.season = season;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(final String locationName) {
		this.locationName = locationName;
	}

	public Integer getEntryNumber() {
		return entryNumber;
	}

	public void setEntryNumber(final Integer entryNumber) {
		this.entryNumber = entryNumber;
	}

	public Integer getPlotNumber() {
		return plotNumber;
	}

	public void setPlotNumber(final Integer plotNumber) {
		this.plotNumber = plotNumber;
	}

	public Integer getGermplasmDbId() {
		return germplasmDbId;
	}

	public void setGermplasmDbId(final Integer germplasmDbId) {
		this.germplasmDbId = germplasmDbId;
	}

	public String getPlantingDate() {
		return plantingDate;
	}

	public void setPlantingDate(final String plantingDate) {
		this.plantingDate = plantingDate;
	}

	public String getHarvestDate() {
		return harvestDate;
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
			.append(this.plotId, sampleObservationDto.plotId)
			.append(this.plantId, sampleObservationDto.plantId)
			.append(this.sampleId, sampleObservationDto.sampleId)
			.append(this.germplasmDbId, sampleObservationDto.germplasmDbId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(this.studyDbId)
			.append(this.locationDbId)
			.append(this.plotId)
			.append(this.plantId)
			.append(this.sampleId)
			.append(this.germplasmDbId).hashCode();
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}

