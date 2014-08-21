package org.generationcp.bms.domain;

import java.util.HashSet;
import java.util.Set;


public class StudySummary {

	private final int id;

	private String name;
	private String title;
	private String objective;
	private String type;
	private String startDate;
	private String endDate;
	private String studyDetailsUrl;
	private final Set<DatasetSummary> datasetInfo = new HashSet<DatasetSummary>();

	public StudySummary(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Set<DatasetSummary> getDatasetInfo() {
		return datasetInfo;
	}
	
	public String getStudyDetailsUrl() {
		return studyDetailsUrl;
	}

	public void setStudyDetailsUrl(String studyDetailsUrl) {
		this.studyDetailsUrl = studyDetailsUrl;
	}

	public void addDatasetSummary(DatasetSummary datasetSummary) {
		if(datasetSummary != null) {
			this.datasetInfo.add(datasetSummary);
		}
	}

}
