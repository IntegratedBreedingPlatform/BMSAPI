package org.ibp.api.domain.program;

import java.util.Date;

import org.generationcp.middleware.pojos.workbench.Project;

public class ProjectBasicInfo {

	private Long id;
	private String uniqueID;
	private String projectName;
	private int userId;
	private String cropType;
	private Date startDate;
	private Date lastOpenDate;

	public ProjectBasicInfo() {
	}

	public ProjectBasicInfo(Project project) {
		this.id = project.getProjectId();
		this.uniqueID = project.getUniqueID();
		this.projectName = project.getProjectName();
		this.userId = project.getUserId();
		this.cropType = project.getCropType().getCropName();
		this.startDate = project.getStartDate();
		this.lastOpenDate = project.getLastOpenDate();
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUniqueID() {
		return this.uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public int getUserId() {
		return this.userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getCropType() {
		return this.cropType;
	}

	public void setCropType(String cropType) {
		this.cropType = cropType;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getLastOpenDate() {
		return this.lastOpenDate;
	}

	public void setLastOpenDate(Date lastOpenDate) {
		this.lastOpenDate = lastOpenDate;
	}
}
