
package org.ibp.api.rest.sample;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonInclude;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleListDto {

	private String listName;
	private String description;
	private String notes;
	private String createdBy;
	private String takenBy;
	private String samplingDate;
	private String cropName;
	private Integer parentId;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private String createdDate;
	private String programUUID;

	// Creation from study
	private Integer datasetId;
	private Integer selectionVariableId;
	private List<Integer> instanceIds;

	// List builder
	private List<SampleDTO> entries;

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getNotes() {
		return this.notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getSelectionVariableId() {
		return this.selectionVariableId;
	}

	public void setSelectionVariableId(final Integer selectionVariableId) {
		this.selectionVariableId = selectionVariableId;
	}

	public List<Integer> getInstanceIds() {
		return this.instanceIds;
	}

	public void setInstanceIds(final List<Integer> instanceIds) {
		this.instanceIds = instanceIds;
	}

	public String getTakenBy() {
		return this.takenBy;
	}

	public void setTakenBy(final String takenBy) {
		this.takenBy = takenBy;
	}

	public String getSamplingDate() {
		return this.samplingDate;
	}

	public void setSamplingDate(final String samplingDate) {
		this.samplingDate = samplingDate;
	}

	public String getCropName() {
		return this.cropName;
	}

	public void setCropName(final String cropName) {
		this.cropName = cropName;
	}

	public Integer getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(final Integer datasetId) {
		this.datasetId = datasetId;
	}
	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(final Integer parentId) {
		this.parentId = parentId;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(final String listName) {
		this.listName = listName;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(final String createdDate) {
		this.createdDate = createdDate;
	}

	public String getProgramUUID() {
		return programUUID;
	}

	public void setProgramUUID(final String programUUID) {
		this.programUUID = programUUID;
	}

	public List<SampleDTO> getEntries() {
		if (this.entries == null) {
			this.entries = new ArrayList<>();
		}
		return this.entries;
	}

	public void setEntries(final List<SampleDTO> entries) {
		this.entries = entries;
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
