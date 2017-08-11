
package org.ibp.api.ibpworkbench.rest;

import org.ibp.api.ibpworkbench.model.DataResponse;
import org.ibp.api.ibpworkbench.service.BreedingViewService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
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
	private String cropName;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.breedingView = new BreedingView();
		this.breedingView.setBreedingViewService(this.breedingViewService);
	}

	@Test
	public void testSaveSsaResultSummaryWhenRequiredParamsAreNull() {
		this.mainOutputFilePath = null;
		this.summaryOutputFilePath = null;
		this.workbenchProjectId = null;
		this.studyId = null;
		this.inputDataSetId = null;
		this.outputDataSetId = null;
		this.cropName = null;

		final DataResponse response =
				this.breedingView.saveSsaResultSummary(this.mainOutputFilePath, this.summaryOutputFilePath, this.outlierOutputFilePath,
						this.workbenchProjectId, this.studyId, this.inputDataSetId, this.outputDataSetId, this.cropName);
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
	public void testSaveSsaResultSummaryWithValidRequiredParams() {
		this.mainOutputFilePath = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSOutput_1_6.csv";
		this.summaryOutputFilePath = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSSummary_1_6.csv";
		this.outlierOutputFilePath = null;
		this.workbenchProjectId = "1";
		this.studyId = "1";
		this.inputDataSetId = "3";
		this.outputDataSetId = "0";

		final DataResponse response =
				this.breedingView.saveSsaResultSummary(this.mainOutputFilePath, this.summaryOutputFilePath, this.outlierOutputFilePath,
						this.workbenchProjectId, this.studyId, this.inputDataSetId, this.outputDataSetId, this.cropName);
		Assert.assertTrue("Web service should succeed", response.isSuccessful());
		Assert.assertTrue("Web service should return no errors", response.getMessage().indexOf("Successfully invoked service.") != -1);
	}
	
}
