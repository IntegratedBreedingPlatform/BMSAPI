package org.ibp.api.rest.program;

import java.util.Date;

import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.domain.program.ProjectBasicInfo;

public class ProgramBasicInfoBuilder {

	private final ProjectBasicInfo model;

	public ProgramBasicInfoBuilder() {
		this.model = new ProjectBasicInfo();
	}

	public ProgramBasicInfoBuilder projectId(Long projectId) {
		this.model.setId(projectId);
		return this;
	}

	public ProgramBasicInfoBuilder projectName(String projectName) {
		this.model.setProjectName(projectName);
		return this;
	}

	public ProgramBasicInfoBuilder uniqueID(String uniqueID) {
		this.model.setUniqueID(uniqueID);
		return this;
	}

	public ProgramBasicInfoBuilder userId(Integer userId) {
		this.model.setUserId(userId);
		return this;
	}

	public ProgramBasicInfoBuilder cropType(CropType cropType) {
		this.model.setCropType(cropType.getCropName());
		return this;
	}

	public ProgramBasicInfoBuilder startDate(Date startDate) {
		this.model.setStartDate(startDate);
		return this;
	}

	public ProgramBasicInfoBuilder lastOpenDate(Date lastOpenDate) {
		this.model.setLastOpenDate(lastOpenDate);
		return this;
	}

	public ProjectBasicInfo build() {
		return this.model;
	}
}
