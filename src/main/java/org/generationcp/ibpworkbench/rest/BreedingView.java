package org.generationcp.ibpworkbench.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
@Api(value = "/breeding_view", description = "Web Services to process the Breeding View output")
@Path("/breeding_view")
public class BreedingView {
    @SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(BreedingView.class);

    @Autowired
    private BreedingViewService breedingViewService;

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

            if(errors.size() == 0) {
                params.put(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue(), fileName);
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
    @Path("/ssa/save_result_summary")
    @ApiOperation(value = "Save the Single-Site Analysis CSV output file with heritability", notes = "", response = DataResponse.class)
    @Produces(MediaType.TEXT_XML)
    public DataResponse saveSsaResultWithHeritability(
		  @ApiParam(value = "Path and filename of the SSA output file", required = true)
		  @QueryParam("mainOutputFilePath") String mainOutputFilePath,
		  @ApiParam(value = "Path and filename of the heritability output file", required = true)
		  @QueryParam("heritabilityOutputFilePath") String heritabilityOutputFilePath,
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
            if(mainOutputFilePath == null || mainOutputFilePath.isEmpty()) {
                errors.add("mainOutputFilePath is a required field!");
            }
            if(heritabilityOutputFilePath == null || heritabilityOutputFilePath.isEmpty()) {
                errors.add("heritabilityOutputFilePath is a required field!");
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

            if(errors.size() == 0) {
            	params.put(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue(), mainOutputFilePath);
                params.put(WebAPIConstants.HERITABILITY_OUTPUT_FILE_PATH.getParamValue(), heritabilityOutputFilePath);
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
    @Path("/ssa/delete_dataset")
    @ApiOperation(value = "Delete the dataset", notes = "", response = DataResponse.class)
    @Produces(MediaType.TEXT_XML)
    public DataResponse deleteDataSet(@QueryParam("dataSetId") Integer dataSetId) {
    	try {
			breedingViewService.deleteDataSet(dataSetId);
			 return new DataResponse(true, "Successfully deleted the dataset");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new DataResponse(false, "Dataset not deleted: " + e.toString());
		}
       
    }
    
    @GET
    @Path("/test")
    @Produces("text/plain")
    public String test() {
        return "WebService for BreedingView has been setup properly.";
    }
}
