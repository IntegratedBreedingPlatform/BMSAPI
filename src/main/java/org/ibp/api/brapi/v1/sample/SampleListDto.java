
package org.ibp.api.brapi.v1.sample;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;

import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleListDto {

	private String listName;

	private String description;

	private SampleListDTO hierarchy;

	private String type;

	private String notes;

	private String createdBy;

	private List<Integer> gids;

	private Integer selectionVariableId;

	private List<Integer> instanceIds;

	private String takenBy;

	private Date samplingDate;

	private Integer studyId;

	private String cropName;

	private String trialName;

	public String getTrialName() {
		return trialName;
	}

	public void setTrialName(String trialName) {
		this.trialName = trialName;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SampleListDTO getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(SampleListDTO hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public List<Integer> getGids() {
		return gids;
	}

	public void setGids(List<Integer> gids) {
		this.gids = gids;
	}

	public Integer getSelectionVariableId() {
		return selectionVariableId;
	}

	public void setSelectionVariableId(Integer selectionVariableId) {
		this.selectionVariableId = selectionVariableId;
	}

	public List<Integer> getInstanceIds() {
		return instanceIds;
	}

	public void setInstanceIds(List<Integer> instanceIds) {
		this.instanceIds = instanceIds;
	}

	public String getTakenBy() {
		return takenBy;
	}

	public void setTakenBy(String takenBy) {
		this.takenBy = takenBy;
	}

	public Date getSamplingDate() {
		return samplingDate;
	}

	public void setSamplingDate(Date samplingDate) {
		this.samplingDate = samplingDate;
	}

	public Integer getStudyId() {
		return studyId;
	}

	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}

	public String getCropName() {
		return cropName;
	}

	public void setCropName(String cropName) {
		this.cropName = cropName;
	}
}
