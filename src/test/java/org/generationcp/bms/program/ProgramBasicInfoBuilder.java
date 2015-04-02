package org.generationcp.bms.program;

import org.generationcp.bms.program.dto.ProjectBasicInfo;
import org.generationcp.middleware.pojos.workbench.CropType;

import java.util.Date;

public class ProgramBasicInfoBuilder {

    private ProjectBasicInfo model;

    public ProgramBasicInfoBuilder() {
        model = new ProjectBasicInfo();
    }

    public ProgramBasicInfoBuilder projectId(Long projectId) {
        model.setId(projectId);
        return this;
    }

    public ProgramBasicInfoBuilder projectName(String projectName) {
        model.setProjectName(projectName);
        return this;
    }

    public ProgramBasicInfoBuilder uniqueID(String uniqueID) {
        model.setUniqueID(uniqueID);
        return this;
    }

    public ProgramBasicInfoBuilder userId(Integer userId) {
        model.setUserId(userId);
        return this;
    }

    public ProgramBasicInfoBuilder cropType(CropType cropType) {
        model.setCropType(cropType.getCropName());
        return this;
    }

    public ProgramBasicInfoBuilder startDate(Date startDate) {
        model.setStartDate(startDate);
        return this;
    }

    public ProgramBasicInfoBuilder lastOpenDate(Date lastOpenDate) {
        model.setLastOpenDate(lastOpenDate);
        return this;
    }

    public ProjectBasicInfo build() {
        return model;
    }
}
