package org.generationcp.ibpworkbench.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.model.DataResponse;
import org.generationcp.ibpworkbench.model.TraitsAndMeans;
import org.generationcp.ibpworkbench.service.BreedingViewService;
import org.generationcp.ibpworkbench.util.TraitsAndMeansCSVUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

@Component
@Path("/breeding_view")
public class BreedingView {
    private final static Logger log = LoggerFactory.getLogger(BreedingView.class);

    @Autowired
    private BreedingViewService breedingViewService;

    @GET
    @Path("/ssa/save_result")
    @Produces(MediaType.TEXT_XML)
    public DataResponse saveSsaResult(@QueryParam("Filename") String fileName,
                                      @QueryParam("WorkbenchProjectId") String workbenchProjectId,
                                      @QueryParam("StudyId") String studyId,
                                      @QueryParam("InputDataSetId") String inputDataSetId,
                                      @QueryParam("OutputDataSetId") String outputDataSetId) {
        DataResponse response;
        try {
            Map<String, String> params = new HashMap<String, String>();
            List<String> errors = new ArrayList<String>();
            if(fileName == null || fileName.isEmpty()) {
                errors.add("Filename is a required field!");
            }
            if(workbenchProjectId == null || workbenchProjectId.isEmpty()) {
                errors.add("WorkbenchProjectId is a required field!");
            }
            if(studyId == null || studyId.isEmpty()) {
                errors.add("StudyId is a required field!");
            }
            if(inputDataSetId == null || inputDataSetId.isEmpty()) {
                errors.add("InputDataSetId is a required field!");
            }
            if(outputDataSetId == null || outputDataSetId.isEmpty()) {
                errors.add("OutputDataSetId is a required field!");
            }

            if(errors.size() == 0) {
                params.put(WebAPIConstants.FILENAME.getParamValue(), fileName);
                params.put(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue(), workbenchProjectId);
                params.put(WebAPIConstants.STUDY_ID.getParamValue(), studyId);
                params.put(WebAPIConstants.INPUT_DATASET_ID.getParamValue(), inputDataSetId);
                params.put(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue(), outputDataSetId);

                breedingViewService.execute(params, errors);
                response = new DataResponse(true, "Successfully invoked service.");
            } else {
                response = new DataResponse(true, "Errors invoking we service: " + errors);
            }
        } catch (Exception e) {
            response = new DataResponse(false, "Failed to invoke service: " + e.toString());
        }
        return response;
    }

    @GET
    @Path("/test")
    @Produces("text/plain")
    public String test() {
        return "WebService for BreedingView has been setup properly.";
    }
}
