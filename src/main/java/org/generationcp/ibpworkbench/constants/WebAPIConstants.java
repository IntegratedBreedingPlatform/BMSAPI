package org.generationcp.ibpworkbench.constants;

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

public enum WebAPIConstants {
    FILENAME("outfile"),
    STUDY_ID("StudyId"),
    WORKBENCH_PROJECT_ID("WorkbenchProjectId"),
    INPUT_DATASET_ID("InputDataSetId"),
    OUTPUT_DATASET_ID("OutputDataSetId");

    private String paramValue;

    private WebAPIConstants(String param) {
        paramValue = param;
    }

    public String getParamValue() {
        return paramValue;
    }
}
