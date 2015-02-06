package org.generationcp.bms.program.dto;

import org.generationcp.middleware.pojos.workbench.Project;

import java.util.Date;

public class ProjectBasicInfo {

    private Long id;
    private String uniqueID;
    private String projectName;
    private int userId;
    private String cropType;
    private Date startDate;
    private Date lastOpenDate;

    public ProjectBasicInfo(){}

    public ProjectBasicInfo(Project project){
        this.id = project.getProjectId();
        this.uniqueID = project.getUniqueID();
        this.projectName = project.getProjectName();
        this.userId = project.getUserId();
        this.cropType = project.getCropType().getCropName();
        this.startDate = project.getStartDate();
        this.lastOpenDate = project.getLastOpenDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getLastOpenDate() {
        return lastOpenDate;
    }

    public void setLastOpenDate(Date lastOpenDate) {
        this.lastOpenDate = lastOpenDate;
    }
}
