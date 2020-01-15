/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.ibp.api.rest.breedingview;

import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import liquibase.util.StringUtils;
import org.ibp.api.domain.breedingview.BreedingViewResponse;
import org.ibp.api.exception.IBPWebServiceException;
import org.ibp.api.java.breedingview.BreedingViewService;
import org.ibp.api.java.impl.middleware.breedingview.BreedingViewParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "Breeding View Resource")
@Controller
@RequestMapping("/breeding-view")
public class BreedingViewResource {

	private static final Logger LOG = LoggerFactory.getLogger(BreedingViewResource.class);

	@Autowired
	private BreedingViewService breedingViewService;

	@ApiOperation(value = "Save the Single-Site Analysis CSV output files", notes = "", response = BreedingViewResponse.class)
	@RequestMapping(value = "{cropName}/single-site-analysis/summary-statistics", method = RequestMethod.GET, produces = {
		MediaType.APPLICATION_XML_VALUE})
	@ResponseBody
	public BreedingViewResponse saveSingleSiteAnalysisData(
		@ApiParam(value = "Path and filename of the SSA output file that resides on the server", required = true)
		@RequestParam("mainOutputFilePath") final String mainOutputFilePath,
		@ApiParam(value = "Path and filename of the Summary output file that resides on the server", required = true)
		@RequestParam("SummaryOutputFilePath") final String summaryOutputFilePath,
		@ApiParam(value = "Path and filename of the Outlier output file that resides on the server", required = false)
		@RequestParam(value = "OutlierFilePath", required = false) final String outlierOutputFilePath,
		@ApiParam(value = "Workbench Project ID", required = true) @RequestParam("WorkbenchProjectId") final String workbenchProjectId,
		@ApiParam(value = "Study ID", required = true) @RequestParam("StudyId") final String studyId,
		@ApiParam(value = "Input Dataset ID", required = true) @RequestParam("InputDataSetId") final String inputDataSetId,
		@ApiParam(value = "Output Dataset ID", required = true) @RequestParam("OutputDataSetId") final String outputDataSetId,
		@PathVariable(value = "cropName") final String cropName
	) {
		Preconditions.checkArgument(!StringUtils.isEmpty(mainOutputFilePath), "mainOutputFilePath is required.");
		Preconditions.checkArgument(!StringUtils.isEmpty(summaryOutputFilePath), "summaryOutputFilePath is required.");
		Preconditions.checkArgument(!StringUtils.isEmpty(workbenchProjectId), "workbenchProjectId is required.");
		Preconditions.checkArgument(!StringUtils.isEmpty(studyId), "studyId is required.");
		Preconditions.checkArgument(!StringUtils.isEmpty(inputDataSetId), "inputDataSetId is required.");
		Preconditions.checkArgument(!StringUtils.isEmpty(outputDataSetId), "outputDataSetId is required.");
		Preconditions.checkArgument(!StringUtils.isEmpty(cropName), "cropName is required.");

		final Map<String, String> params = new HashMap<>();
		final List<String> errors = new ArrayList<>();
		params.put(BreedingViewParameter.MAIN_OUTPUT_FILE_PATH.getParamValue(), mainOutputFilePath);
		params.put(BreedingViewParameter.SUMMARY_OUTPUT_FILE_PATH.getParamValue(), summaryOutputFilePath);
		params.put(BreedingViewParameter.OUTLIER_OUTPUT_FILE_PATH.getParamValue(), outlierOutputFilePath);
		params.put(BreedingViewParameter.WORKBENCH_PROJECT_ID.getParamValue(), workbenchProjectId);
		params.put(BreedingViewParameter.STUDY_ID.getParamValue(), studyId);
		params.put(BreedingViewParameter.INPUT_DATASET_ID.getParamValue(), inputDataSetId);
		params.put(BreedingViewParameter.OUTPUT_DATASET_ID.getParamValue(), outputDataSetId);

		try {
			this.breedingViewService.execute(params, errors);
			return new BreedingViewResponse(true, "Successfully invoked service.");
		} catch (IBPWebServiceException e) {
			LOG.error(e.getMessage(), e);
			return new BreedingViewResponse(false, "Errors invoking web service: " + errors);
		}

	}

	public void setBreedingViewService(final BreedingViewService breedingViewService) {
		this.breedingViewService = breedingViewService;
	}

}

