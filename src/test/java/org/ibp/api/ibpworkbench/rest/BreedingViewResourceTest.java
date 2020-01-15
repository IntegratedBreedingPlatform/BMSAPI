
package org.ibp.api.ibpworkbench.rest;

import org.ibp.api.domain.breedingview.BreedingViewResponse;
import org.ibp.api.java.breedingview.BreedingViewService;
import org.ibp.api.rest.breedingview.BreedingViewResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BreedingViewResourceTest {

	private BreedingViewResource breedingView;

	@Mock
	private BreedingViewService breedingViewService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.breedingView = new BreedingViewResource();
		this.breedingView.setBreedingViewService(this.breedingViewService);
	}

	@Test
	public void testSaveSsaResultSummaryWithValidRequiredParams() {
		final String mainOutputFilePath = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSOutput_1_6.csv";
		final String summaryOutputFilePath = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSSummary_1_6.csv";
		final String outlierOutputFilePath = null;
		final String workbenchProjectId = "1";
		final String studyId = "1";
		final String inputDataSetId = "3";
		final String outputDataSetId = "0";
		final String cropName = "maize";

		final BreedingViewResponse response =
			this.breedingView.saveSingleSiteAnalysisData(mainOutputFilePath, summaryOutputFilePath, outlierOutputFilePath,
				workbenchProjectId, studyId, inputDataSetId, outputDataSetId, cropName);
		Assert.assertTrue("Web service should succeed", response.isSuccessful());
		Assert.assertTrue("Web service should return no errors", response.getMessage().indexOf("Successfully invoked service.") != -1);
	}

}
