package org.generationcp.ibpworkbench.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.generationcp.ibpworkbench.constants.WebAPIConstants;
import org.generationcp.ibpworkbench.exceptions.IBPWebServiceException;
import org.generationcp.ibpworkbench.model.DataResponse;
import org.generationcp.ibpworkbench.service.BreedingViewService;
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
		breedingView = Mockito.spy(new BreedingView());
		breedingView.setBreedingViewService(breedingViewService);
		doNothing().when(breedingView).closeManagerFactory();
	}
	
	@Test
	public void test_saveSsaResultSummary_nullRequiredParams() {
		mainOutputFilePath = null;
		summaryOutputFilePath = null;
		workbenchProjectId = null;
		studyId = null;
		inputDataSetId = null;
		outputDataSetId = null;
		DataResponse response = breedingView.saveSsaResultSummary(mainOutputFilePath,
				summaryOutputFilePath,outlierOutputFilePath,
				workbenchProjectId,studyId,inputDataSetId,outputDataSetId,info);
		assertFalse("Web service should fail",
				response.isSuccessful());
		assertTrue("Web service should return errors",
				response.getMessage().indexOf("Errors invoking web service")!=-1);
		assertTrue("Web service should flag a null/empty mainOutputFilePath",
				response.getMessage().indexOf("mainOutputFilePath is a required field!")!=-1);
		assertTrue("Web service should flag a null/empty summaryOutputFilePath",
				response.getMessage().indexOf("summaryOutputFilePath is a required field!")!=-1);
		assertTrue("Web service should flag a null/empty WorkbenchProjectId",
				response.getMessage().indexOf("WorkbenchProjectId is a required field!")!=-1);
		assertTrue("Web service should flag a null/empty StudyId",
				response.getMessage().indexOf("StudyId is a required field!")!=-1);
		assertTrue("Web service should flag a null/empty InputDataSetId",
				response.getMessage().indexOf("InputDataSetId is a required field!")!=-1);
		assertTrue("Web service should flag a null/empty OutputDataSetId",
				response.getMessage().indexOf("OutputDataSetId is a required field!")!=-1);
	}
	
	@Test
	public void test_saveSsaResultSummary_ValidRequiredParams() {
		mainOutputFilePath = 
				"C:\\Breeding Management System\\workspace\\maize_test\\breeding_view\\output\\BMSOutput_1_6.csv";
		summaryOutputFilePath = 
				"C:\\Breeding Management System\\workspace\\maize_test\\breeding_view\\output\\BMSSummary_1_6.csv";
		outlierOutputFilePath = null;
		workbenchProjectId = "1";
		studyId = "1";
		inputDataSetId = "3";
		outputDataSetId = "0";
		Map<String, String> params = createBVParams(mainOutputFilePath,
				summaryOutputFilePath,outlierOutputFilePath,
				workbenchProjectId,studyId,inputDataSetId,outputDataSetId);
		try {
			doNothing().when(breedingViewService).execute(
					params, new ArrayList<String>());
		} catch (IBPWebServiceException e) {
			fail("BreedingViewService cannot be mocked");
		}
		DataResponse response = breedingView.saveSsaResultSummary(mainOutputFilePath,
				summaryOutputFilePath,outlierOutputFilePath,
				workbenchProjectId,studyId,inputDataSetId,outputDataSetId,info);
		assertTrue("Web service should succeed",
				response.isSuccessful());
		assertTrue("Web service should return no errors",
				response.getMessage().indexOf("Successfully invoked service.")!=-1);
		
	}

	private Map<String, String> createBVParams(String mainOutputFilePath2,
			String summaryOutputFilePath2, String outlierOutputFilePath2,
			String workbenchProjectId2, String studyId2,
			String inputDataSetId2, String outputDataSetId2) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue(), mainOutputFilePath);
        params.put(WebAPIConstants.SUMMARY_OUTPUT_FILE_PATH.getParamValue(), summaryOutputFilePath);
        params.put(WebAPIConstants.OUTLIER_OUTPUT_FILE_PATH.getParamValue(), outlierOutputFilePath);
        params.put(WebAPIConstants.WORKBENCH_PROJECT_ID.getParamValue(), workbenchProjectId);
        params.put(WebAPIConstants.STUDY_ID.getParamValue(), studyId);
        params.put(WebAPIConstants.INPUT_DATASET_ID.getParamValue(), inputDataSetId);
        params.put(WebAPIConstants.OUTPUT_DATASET_ID.getParamValue(), outputDataSetId);
        return params;
	}
}
