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

package org.ibp.api.ibpworkbench.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.ibp.api.domain.design.License;
import org.ibp.api.ibpworkbench.constants.WebAPIConstants;
import org.ibp.api.ibpworkbench.model.DataResponse;
import org.ibp.api.ibpworkbench.service.BreedingViewService;
import org.ibp.api.java.design.DesignLicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(value = "Breeding View Resource")
@RestController
@RequestMapping("/breeding_view")
// TODO rename to BreedingViewResource
public class BreedingView {

	private static final Logger LOG = LoggerFactory.getLogger(BreedingView.class);

	@Autowired
	private BreedingViewService breedingViewService;

	@Resource
	private DesignLicenseService designLicenseService;

	@Value("${bv.design.validation.on.login.enabled}")
	private Boolean bvDesignValidationEnabled;

	public static final String SUCCESS = "success";
	public static final String ERRORS = "errors";

	public static final String CONTACT_SUPPORT_MESSAGE = "<br/><br/>Please contact <a "
		+ "href=\"https://ibplatform.atlassian.net/servicedesk/customer/portal/4/group/25/create/51\" target=\"_blank\">"
		+ "IBP support</a>.";

	@ApiOperation(value = "Save the Single-Site Analysis CSV output file with heritability", notes = "", response = DataResponse.class)
	@RequestMapping(value = "{cropName}/ssa/save_result_summary", method = RequestMethod.GET, produces = {MediaType.APPLICATION_XML_VALUE})
	@ResponseBody
	public DataResponse saveSsaResultSummary(
			@ApiParam(value = "Path and filename of the SSA output file", required = true) @RequestParam("mainOutputFilePath") final String mainOutputFilePath,

			@ApiParam(value = "Path and filename of the Summary output file", required = true) @RequestParam("SummaryOutputFilePath")
			final String summaryOutputFilePath,

			@ApiParam(value = "Path and filename of the Outlier output file", required = false) @RequestParam(value = "OutlierFilePath", required = false)
			final String outlierOutputFilePath,

			@ApiParam(value = "Current Project ID", required = true) @RequestParam("WorkbenchProjectId") final String workbenchProjectId,

			@ApiParam(value = "Study ID", required = true) @RequestParam("StudyId") final String studyId,

			@ApiParam(value = "Input Dataset ID", required = true) @RequestParam("InputDataSetId") final String inputDataSetId,

			@ApiParam(value = "Output Dataset ID", required = true) @RequestParam("OutputDataSetId") final String outputDataSetId,

			@PathVariable(value = "cropName") final String cropName
	) {
		DataResponse response;

		try {
			final Map<String, String> params = new HashMap<>();
			final List<String> errors = new ArrayList<>();
			if (mainOutputFilePath == null || mainOutputFilePath.isEmpty()) {
				errors.add("mainOutputFilePath is a required field!");
			}
			if (summaryOutputFilePath == null || summaryOutputFilePath.isEmpty()) {
				errors.add("summaryOutputFilePath is a required field!");
			}
			if (workbenchProjectId == null || workbenchProjectId.isEmpty()) {
				errors.add("WorkbenchProjectId is a required field!");
			}
			if (studyId == null || studyId.isEmpty()) {
				errors.add("StudyId is a required field!");
			}

			if (inputDataSetId == null || inputDataSetId.isEmpty()) {
				errors.add("InputDataSetId is a required field!");
			}
			if (outputDataSetId == null || outputDataSetId.isEmpty()) {
				errors.add("OutputDataSetId is a required field!");
			}

			if (errors.isEmpty()) {
				params.put(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue(), mainOutputFilePath);
				params.put(WebAPIConstants.SUMMARY_OUTPUT_FILE_PATH.getParamValue(), summaryOutputFilePath);
				params.put(WebAPIConstants.OUTLIER_OUTPUT_FILE_PATH.getParamValue(), outlierOutputFilePath);
				params.put(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue(), workbenchProjectId);
				params.put(WebAPIConstants.STUDY_ID.getParamValue(), studyId);
				params.put(WebAPIConstants.INPUT_DATASET_ID.getParamValue(), inputDataSetId);
				params.put(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue(), outputDataSetId);

				this.breedingViewService.execute(params, errors);
				response = new DataResponse(true, "Successfully invoked service.");
			} else {
				response = new DataResponse(false, "Errors invoking web service: " + errors);
			}
		} catch (final Exception e) {
			BreedingView.LOG.debug(e.getMessage(), e);
			response = new DataResponse(false, "Failed to invoke service: " + e.toString());
		}

		return response;
	}

	public void setBreedingViewService(final BreedingViewService breedingViewService) {
		this.breedingViewService = breedingViewService;
	}

	@ApiOperation(value = "Gets list of Breeding View Licenses")
	@RequestMapping(value = "/validate-license", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> validateBVLicense() {
		final Map<String, Object> out = new LinkedHashMap<>();
		HttpStatus isSuccess = HttpStatus.BAD_REQUEST;

		if (this.bvDesignValidationEnabled) {
			try {
				final License designLicenseInfo = this.designLicenseService.getLicenseInfo();
				final String expiry = designLicenseInfo.getExpiry();
				if (!StringUtils.isEmpty(expiry)) {
					final Format formatter = new SimpleDateFormat(DesignLicenseService.LICENSE_DATE_FORMAT);
					final Date expiryDate = (Date) formatter.parseObject(expiry);
					final Date currentDate = DateUtil.getCurrentDateWithZeroTime();

					if (currentDate.compareTo(expiryDate) > 0) {
						out.put(SUCCESS, Boolean.FALSE);
						out.put(ERRORS, "Login unauthorized: No BMS license has been found or has been expired."
							+ CONTACT_SUPPORT_MESSAGE);
						return new ResponseEntity<>(out, isSuccess);
					}

					final long daysDifference = TimeUnit.DAYS.convert(Math.abs(expiryDate.getTime() - currentDate.getTime()),
						TimeUnit.MILLISECONDS);

					if (daysDifference <= 30) {
						out.put("warnings", "Your organization’s BMS licence is going to expire soon (" + expiry + ")."
							+ CONTACT_SUPPORT_MESSAGE);
					}
				}
			} catch (final ParseException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		isSuccess = HttpStatus.OK;
		out.put(SUCCESS, Boolean.TRUE);

		return new ResponseEntity<>(out, isSuccess);
	}

}
