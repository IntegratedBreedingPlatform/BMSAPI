
package org.ibp.api.domain.study;

public class FieldMapStudySummary {

	private Integer order;
	private String studyName;
	private String type;
	private String dataset;
	private Long totalNumberOfPlots;
	private Integer environment;
	private Long numberOfEntries;
	private Long numbeOfReps;
	private Long plotsNeeded;

	public FieldMapStudySummary() {

	}


	public Integer getOrder() {
		return this.order;
	}


	public void setOrder(Integer order) {
		this.order = order;
	}


	public String getStudyName() {
		return this.studyName;
	}


	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}


	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataset() {
		return this.dataset;
	}


	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public Long getTotalNumberOfPlots() {
		return this.totalNumberOfPlots;
	}

	public void setTotalNumberOfPlots(Long totalNumberOfPlots) {
		this.totalNumberOfPlots = totalNumberOfPlots;
	}

	public Integer getEnvironment() {
		return this.environment;
	}


	public void setEnvironment(Integer environment) {
		this.environment = environment;
	}

	public Long getNumberOfEntries() {
		return this.numberOfEntries;
	}


	public void setNumberOfEntries(Long numberOfEntries) {
		this.numberOfEntries = numberOfEntries;
	}


	public Long getNumbeOfReps() {
		return this.numbeOfReps;
	}


	public void setNumbeOfReps(Long numbeOfReps) {
		this.numbeOfReps = numbeOfReps;
	}


	public Long getPlotsNeeded() {
		return this.plotsNeeded;
	}

	
	public void setPlotsNeeded(Long plotsNeeded) {
		this.plotsNeeded = plotsNeeded;
	}

}
