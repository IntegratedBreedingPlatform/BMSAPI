package org.generationcp.bms.domain;

import org.generationcp.middleware.domain.dms.StandardVariableSummary;


public class OntologyUsage  {
	
	private StandardVariableSummary standardVariable;
	
	private Long projectCount;
	
	private Long experimentCount;
	
	public StandardVariableSummary getStandardVariable() {
		return standardVariable;
	}
	
	public void setStandardVariable(StandardVariableSummary standardVariable) {
		this.standardVariable = standardVariable;
	}

	public Long getProjectCount() {
		return projectCount;
	}
	
	public void setProjectCount(Long projectCount) {
		this.projectCount = projectCount;
	}
	
	public Long getExperimentCount() {
		return experimentCount;
	}
	
	public void setExperimentCount(Long experimentCount) {
		this.experimentCount = experimentCount;
	}
	
	public String getFlatView() {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(standardVariable.getIsA().getName());
		resultBuilder.append(":");
		resultBuilder.append(standardVariable.getId());
		resultBuilder.append(":");
		resultBuilder.append(standardVariable.getName());
		resultBuilder.append(":");
		resultBuilder.append(standardVariable.getDescription());
		resultBuilder.append(":");
		resultBuilder.append(projectCount);
		resultBuilder.append(":");
		resultBuilder.append(experimentCount);
		return resultBuilder.toString();
	}

}
