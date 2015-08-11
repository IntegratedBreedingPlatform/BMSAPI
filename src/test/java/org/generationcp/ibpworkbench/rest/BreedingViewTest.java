
package org.generationcp.ibpworkbench.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.exceptions.IBPWebServiceException;
import org.generationcp.ibpworkbench.model.DataResponse;
import org.generationcp.ibpworkbench.service.BreedingViewService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BreedingViewTest {

	private BreedingView breedingView;
	@Mock
	private BreedingViewService breedingViewService;

	private String mainOutputFilePath;
	private String summaryOutputFilePath;
	private String outlierOutputFilePath;
	private String workbenchProjectId;
	private String studyId;
	private String inputDataSetId;
	private String outputDataSetId;
	private UriInfo info;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.breedingView = Mockito.spy(new BreedingView());
		this.breedingView.setBreedingViewService(this.breedingViewService);
		Mockito.doNothing().when(this.breedingView);
	}

	@Test
	public void test_saveSsaResultSummary_nullRequiredParams() {
		this.mainOutputFilePath = null;
		this.summaryOutputFilePath = null;
		this.workbenchProjectId = null;
		this.studyId = null;
		this.inputDataSetId = null;
		this.outputDataSetId = null;
		DataResponse response =
				this.breedingView.saveSsaResultSummary(this.mainOutputFilePath, this.summaryOutputFilePath, this.outlierOutputFilePath,
						this.workbenchProjectId, this.studyId, this.inputDataSetId, this.outputDataSetId, this.info);
		Assert.assertFalse("Web service should fail", response.isSuccessful());
		Assert.assertTrue("Web service should return errors", response.getMessage().indexOf("Errors invoking web service") != -1);
		Assert.assertTrue("Web service should flag a null/empty mainOutputFilePath",
				response.getMessage().indexOf("mainOutputFilePath is a required field!") != -1);
		Assert.assertTrue("Web service should flag a null/empty summaryOutputFilePath",
				response.getMessage().indexOf("summaryOutputFilePath is a required field!") != -1);
		Assert.assertTrue("Web service should flag a null/empty WorkbenchProjectId",
				response.getMessage().indexOf("WorkbenchProjectId is a required field!") != -1);
		Assert.assertTrue("Web service should flag a null/empty StudyId",
				response.getMessage().indexOf("StudyId is a required field!") != -1);
		Assert.assertTrue("Web service should flag a null/empty InputDataSetId",
				response.getMessage().indexOf("InputDataSetId is a required field!") != -1);
		Assert.assertTrue("Web service should flag a null/empty OutputDataSetId",
				response.getMessage().indexOf("OutputDataSetId is a required field!") != -1);
	}

	@Test
	public void test_saveSsaResultSummary_ValidRequiredParams() {
		this.mainOutputFilePath = "C:\\Breeding Management System\\workspace\\maize_test\\breeding_view\\output\\BMSOutput_1_6.csv";
		this.summaryOutputFilePath = "C:\\Breeding Management System\\workspace\\maize_test\\breeding_view\\output\\BMSSummary_1_6.csv";
		this.outlierOutputFilePath = null;
		this.workbenchProjectId = "1";
		this.studyId = "1";
		this.inputDataSetId = "3";
		this.outputDataSetId = "0";
		Map<String, String> params =
				this.createBVParams(this.mainOutputFilePath, this.summaryOutputFilePath, this.outlierOutputFilePath,
						this.workbenchProjectId, this.studyId, this.inputDataSetId, this.outputDataSetId);
		try {
			Mockito.doNothing().when(this.breedingViewService).execute(params, new ArrayList<String>());
		} catch (IBPWebServiceException e) {
			Assert.fail("BreedingViewService cannot be mocked");
		}
		DataResponse response =
				this.breedingView.saveSsaResultSummary(this.mainOutputFilePath, this.summaryOutputFilePath, this.outlierOutputFilePath,
						this.workbenchProjectId, this.studyId, this.inputDataSetId, this.outputDataSetId, this.info);
		Assert.assertTrue("Web service should succeed", response.isSuccessful());
		Assert.assertTrue("Web service should return no errors", response.getMessage().indexOf("Successfully invoked service.") != -1);

	}

	private Map<String, String> createBVParams(String mainOutputFilePath2, String summaryOutputFilePath2, String outlierOutputFilePath2,
			String workbenchProjectId2, String studyId2, String inputDataSetId2, String outputDataSetId2) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue(), this.mainOutputFilePath);
		params.put(WebAPIConstants.SUMMARY_OUTPUT_FILE_PATH.getParamValue(), this.summaryOutputFilePath);
		params.put(WebAPIConstants.OUTLIER_OUTPUT_FILE_PATH.getParamValue(), this.outlierOutputFilePath);
		params.put(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue(), this.workbenchProjectId);
		params.put(WebAPIConstants.STUDY_ID.getParamValue(), this.studyId);
		params.put(WebAPIConstants.INPUT_DATASET_ID.getParamValue(), this.inputDataSetId);
		params.put(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue(), this.outputDataSetId);
		return params;
	}
}
