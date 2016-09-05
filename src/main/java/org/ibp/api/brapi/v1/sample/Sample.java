
package org.ibp.api.brapi.v1.sample;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sample {

	private Integer studyId;
	private Integer locationId;
	private Integer plotId;
	private String plantId;

	private String sampleId;
	private String takenBy;
	private String sampleDate;
	private String notes;

	private String studyName;
	private Integer year;
	private String season;
	private String locationName;

	private Integer entryNumber;
	private Integer plotNumber;
	private Integer fieldId;
	private String fieldName;

	private Integer germplasmId;
	private String seedSource;
	private String pedigree;

	private String plantingDate;
	private String harvestDate;

	public Integer getStudyId() {
		return this.studyId;
	}

	public void setStudyId(final Integer studyId) {
		this.studyId = studyId;
	}

	public Integer getLocationId() {
		return this.locationId;
	}

	public void setLocationId(final Integer locationId) {
		this.locationId = locationId;
	}

	public Integer getPlotId() {
		return this.plotId;
	}

	public void setPlotId(final Integer plotId) {
		this.plotId = plotId;
	}

	public String getPlantId() {
		return this.plantId;
	}

	public void setPlantId(final String plantId) {
		this.plantId = plantId;
	}

	public String getSampleId() {
		return this.sampleId;
	}

	public void setSampleId(final String sampleId) {
		this.sampleId = sampleId;
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

	public Integer getYear() {
		return this.year;
	}

	public void setYear(final Integer year) {
		this.year = year;
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

	public Integer getFieldId() {
		return this.fieldId;
	}

	public void setFieldId(final Integer fieldId) {
		this.fieldId = fieldId;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(final String fieldName) {
		this.fieldName = fieldName;
	}

	public Integer getGermplasmId() {
		return this.germplasmId;
	}

	public void setGermplasmId(final Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	public String getSeedSource() {
		return this.seedSource;
	}

	public void setSeedSource(final String seedSource) {
		this.seedSource = seedSource;
	}

	public String getPedigree() {
		return this.pedigree;
	}

	public void setPedigree(final String pedigree) {
		this.pedigree = pedigree;
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

}
