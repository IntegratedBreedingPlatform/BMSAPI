/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package org.generationcp.ibpworkbench.constants;


public enum WebAPIConstants {
    
	MAIN_OUTPUT_FILE_PATH("mainOutputFilePath"),
    HERITABILITY_OUTPUT_FILE_PATH("heritabilityOutputFilePath"),
    STUDY_ID("StudyId"),
    WORKBENCH_PROJECT_ID("WorkbenchProjectId"),
    INPUT_DATASET_ID("InputDataSetId"),
    OUTPUT_DATASET_ID("OutputDataSetId"), 
    OUTLIER_OUTPUT_FILE_PATH("OutlierFilePath");

    private String paramValue;

    private WebAPIConstants(String param) {
        paramValue = param;
    }

    public String getParamValue() {
        return paramValue;
    }
}
