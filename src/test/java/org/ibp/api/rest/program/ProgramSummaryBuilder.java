package org.ibp.api.rest.program;

import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.domain.program.ProgramSummary;

public class ProgramSummaryBuilder {

	private final ProgramSummary model;

	public ProgramSummaryBuilder() {
		this.model = new ProgramSummary();
	}

	public ProgramSummaryBuilder projectId(String projectId) {
		this.model.setId(projectId);
		return this;
	}

	public ProgramSummaryBuilder projectName(String projectName) {
		this.model.setProjectName(projectName);
		return this;
	}

	public ProgramSummaryBuilder uniqueID(String uniqueID) {
		this.model.setUniqueID(uniqueID);
		return this;
	}

	public ProgramSummaryBuilder userId(String userId) {
		this.model.setUserId(userId);
		return this;
	}

	public ProgramSummaryBuilder cropType(CropType cropType) {
		this.model.setCropType(cropType.getCropName());
		return this;
	}

	public ProgramSummaryBuilder startDate(String startDate) {
		this.model.setStartDate(startDate);
		return this;
	}

	public ProgramSummary build() {
		return this.model;
	}
}
