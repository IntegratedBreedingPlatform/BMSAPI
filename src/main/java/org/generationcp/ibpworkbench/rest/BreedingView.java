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
package org.generationcp.ibpworkbench.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.generationcp.commons.hibernate.DynamicManagerFactoryProvider;
import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.model.DataResponse;
import org.generationcp.ibpworkbench.service.BreedingViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;


@Component
@Api(value = "/breeding_view", description = "Web Services to process the Breeding View output")
@Path("/breeding_view")
public class BreedingView {

	private static final Logger LOG = LoggerFactory.getLogger(BreedingView.class);
	
    @Autowired
    private BreedingViewService breedingViewService;
    
    @Autowired
    private DynamicManagerFactoryProvider managerFactoryProvider;

    @GET
    @Path("/ssa/save_result")
    @ApiOperation(value = "Save the Single-Site Analysis CSV output file", notes = "", response = DataResponse.class)
    @Produces(MediaType.TEXT_XML)
    public DataResponse saveSsaResult(
 						  @ApiParam(value = "Path and filename of the SSA output file", required = true)
						  @QueryParam("mainOutputFilePath") String fileName,
						  @ApiParam(value = "Current Project ID", required = true)
						  @QueryParam("WorkbenchProjectId") String workbenchProjectId,
						  @ApiParam(value = "Study ID", required = true)
                          @QueryParam("StudyId") String studyId,
                          @ApiParam(value = "Input Dataset ID", required = true)
                          @QueryParam("InputDataSetId") String inputDataSetId,
                          @ApiParam(value = "Output Dataset ID", required = true)
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
            } else {
                if(Integer.parseInt(studyId) > 0) {
                    errors.add("StudyId must a be a negative value!");
                }
            }

            if(inputDataSetId == null || inputDataSetId.isEmpty()) {
                errors.add("InputDataSetId is a required field!");
            }
            if(outputDataSetId == null || outputDataSetId.isEmpty()) {
                errors.add("OutputDataSetId is a required field!");
            }

            if(errors.isEmpty()) {
                params.put(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue(), fileName);
                params.put(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue(), workbenchProjectId);
                params.put(WebAPIConstants.STUDY_ID.getParamValue(), studyId);
                params.put(WebAPIConstants.INPUT_DATASET_ID.getParamValue(), inputDataSetId);
                params.put(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue(), outputDataSetId);

                breedingViewService.execute(params, errors);
                response = new DataResponse(true, "Successfully invoked service.");
            } else {
                response = new DataResponse(true, "Errors invoking web service: " + errors);
            }
        } catch (Exception e) {
        	LOG.debug(e.getMessage(), e);
            response = new DataResponse(false, "Failed to invoke service: " + e.toString());
        }
        
        try{
        	managerFactoryProvider.close();
        }catch(Exception e){
        	LOG.debug(e.getMessage(), e);
        }
        
        return response;
    }
    
    @GET
    @Path("/ssa/save_result_summary")
    @ApiOperation(value = "Save the Single-Site Analysis CSV output file with heritability", notes = "", response = DataResponse.class)
    @Produces(MediaType.TEXT_XML)
    public DataResponse saveSsaResultSummary(
		  @ApiParam(value = "Path and filename of the SSA output file", required = true)
		  @QueryParam("mainOutputFilePath") String mainOutputFilePath,
		  
		  @ApiParam(value = "Path and filename of the Summary output file", required = true)
		  @QueryParam("SummaryOutputFilePath") String summaryOutputFilePath,
		  
		  @ApiParam(value = "Path and filename of the Outlier output file", required = false)
		  @QueryParam("OutlierFilePath") String outlierOutputFilePath,
		  
		  @ApiParam(value = "Current Project ID", required = true)
		  @QueryParam("WorkbenchProjectId") String workbenchProjectId,
		  
		  @ApiParam(value = "Study ID", required = true)
	      @QueryParam("StudyId") String studyId,
	      
	      @ApiParam(value = "Input Dataset ID", required = true)
	      @QueryParam("InputDataSetId") String inputDataSetId,
	      
	      @ApiParam(value = "Output Dataset ID", required = true)
	      @QueryParam("OutputDataSetId") String outputDataSetId, @Context UriInfo info) {
        DataResponse response;

        
        try {
            Map<String, String> params = new HashMap<String, String>();
            List<String> errors = new ArrayList<String>();
            if(mainOutputFilePath == null || mainOutputFilePath.isEmpty()) {
                errors.add("mainOutputFilePath is a required field!");
            }
            if(summaryOutputFilePath == null || summaryOutputFilePath.isEmpty()) {
                errors.add("summaryOutputFilePath is a required field!");
            }
            if(workbenchProjectId == null || workbenchProjectId.isEmpty()) {
                errors.add("WorkbenchProjectId is a required field!");
            }
            if(studyId == null || studyId.isEmpty()) {
                errors.add("StudyId is a required field!");
            } else {
                if(Integer.parseInt(studyId) > 0) {
                    errors.add("StudyId must a be a negative value!");
                }
            }

            if(inputDataSetId == null || inputDataSetId.isEmpty()) {
                errors.add("InputDataSetId is a required field!");
            }
            if(outputDataSetId == null || outputDataSetId.isEmpty()) {
                errors.add("OutputDataSetId is a required field!");
            }

            if(errors.isEmpty()) {
            	params.put(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue(), mainOutputFilePath);
                params.put(WebAPIConstants.SUMMARY_OUTPUT_FILE_PATH.getParamValue(), summaryOutputFilePath);
                params.put(WebAPIConstants.OUTLIER_OUTPUT_FILE_PATH.getParamValue(), outlierOutputFilePath);
                params.put(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue(), workbenchProjectId);
                params.put(WebAPIConstants.STUDY_ID.getParamValue(), studyId);
                params.put(WebAPIConstants.INPUT_DATASET_ID.getParamValue(), inputDataSetId);
                params.put(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue(), outputDataSetId);

                breedingViewService.execute(params, errors);
                response = new DataResponse(true, "Successfully invoked service.");
            } else {
                response = new DataResponse(false, "Errors invoking web service: " + errors);
            }
        } catch (Exception e) {
        	LOG.debug(e.getMessage(), e);
            response = new DataResponse(false, "Failed to invoke service: " + e.toString());
        }
        
        try{
        	managerFactoryProvider.close();
        }catch(Exception e){
        	LOG.debug(e.getMessage(), e);
        }
        return response;
    }
    
}
