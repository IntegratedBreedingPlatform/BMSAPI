
package org.ibp.api.ibpworkbench.rest;

import org.ibp.api.domain.breedingview.BreedingViewResponse;
import org.ibp.api.exception.IBPWebServiceException;
import org.ibp.api.java.breedingview.BreedingViewService;
import org.ibp.api.rest.breedingview.BreedingViewResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;

@RunWith(MockitoJUnitRunner.class)
public class BreedingViewResourceTest {

	private BreedingViewResource breedingViewResource;

	@Mock
	private BreedingViewService breedingViewService;
	public static final String MAIN_OUTPUT_FILE_PATH = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSOutput_1_6.csv";
	public static final String SUMMARY_OUTPUT_FILE_PATH = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSSummary_1_6.csv";
	public static final String OUTLIER_OUTPUT_FILE_PATH = null;
	public static final String WORKBENCH_PROJECT_ID = "1";
	public static final String STUDY_ID = "1";
	public static final String INPUT_DATASET_ID = "3";
	public static final String OUTPUT_DATASET_ID = "0";
	public static final String CROP_NAME = "maize";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.breedingViewResource = new BreedingViewResource();
		this.breedingViewResource.setBreedingViewService(this.breedingViewService);
	}

	@Test
	public void testSaveSsaResultSummaryWithInvalidParameters() {

		try {
			this.breedingViewResource.saveSingleSiteAnalysisData(null, SUMMARY_OUTPUT_FILE_PATH, OUTLIER_OUTPUT_FILE_PATH,
				WORKBENCH_PROJECT_ID, STUDY_ID, INPUT_DATASET_ID, OUTPUT_DATASET_ID, CROP_NAME);
			Assert.fail("Method should throw an error");
		} catch (
			final IllegalArgumentException e) {
			// Do nothing
		}
		try {
			this.breedingViewResource.saveSingleSiteAnalysisData(SUMMARY_OUTPUT_FILE_PATH, null, OUTLIER_OUTPUT_FILE_PATH,
				WORKBENCH_PROJECT_ID, STUDY_ID, INPUT_DATASET_ID, OUTPUT_DATASET_ID, CROP_NAME);
			Assert.fail("Method should throw an error");
		} catch (
			final IllegalArgumentException e) {
			// Do nothing
		}
		try {
			this.breedingViewResource.saveSingleSiteAnalysisData(SUMMARY_OUTPUT_FILE_PATH, SUMMARY_OUTPUT_FILE_PATH, null,
				WORKBENCH_PROJECT_ID, STUDY_ID, INPUT_DATASET_ID, OUTPUT_DATASET_ID, CROP_NAME);
		} catch (
			final IllegalArgumentException e) {
			Assert.fail("Method should throw an error");
		}
		try {
			this.breedingViewResource
				.saveSingleSiteAnalysisData(SUMMARY_OUTPUT_FILE_PATH, SUMMARY_OUTPUT_FILE_PATH, OUTLIER_OUTPUT_FILE_PATH,
					null, STUDY_ID, INPUT_DATASET_ID, OUTPUT_DATASET_ID, CROP_NAME);
			Assert.fail("Method should throw an error");
		} catch (
			final IllegalArgumentException e) {
			// Do nothing
		}
		try {
			this.breedingViewResource
				.saveSingleSiteAnalysisData(SUMMARY_OUTPUT_FILE_PATH, SUMMARY_OUTPUT_FILE_PATH, OUTLIER_OUTPUT_FILE_PATH,
					WORKBENCH_PROJECT_ID, null, INPUT_DATASET_ID, OUTPUT_DATASET_ID, CROP_NAME);
			Assert.fail("Method should throw an error");
		} catch (
			final IllegalArgumentException e) {
			// Do nothing
		}
		try {
			this.breedingViewResource
				.saveSingleSiteAnalysisData(SUMMARY_OUTPUT_FILE_PATH, SUMMARY_OUTPUT_FILE_PATH, OUTLIER_OUTPUT_FILE_PATH,
					WORKBENCH_PROJECT_ID, STUDY_ID, null, OUTPUT_DATASET_ID, CROP_NAME);
			Assert.fail("Method should throw an error");
		} catch (
			final IllegalArgumentException e) {
			// Do nothing
		}
		try {
			this.breedingViewResource
				.saveSingleSiteAnalysisData(SUMMARY_OUTPUT_FILE_PATH, SUMMARY_OUTPUT_FILE_PATH, OUTLIER_OUTPUT_FILE_PATH,
					WORKBENCH_PROJECT_ID, STUDY_ID, INPUT_DATASET_ID, null, CROP_NAME);
			Assert.fail("Method should throw an error");
		} catch (
			final IllegalArgumentException e) {
			// Do nothing
		}
		try {
			this.breedingViewResource
				.saveSingleSiteAnalysisData(SUMMARY_OUTPUT_FILE_PATH, SUMMARY_OUTPUT_FILE_PATH, OUTLIER_OUTPUT_FILE_PATH,
					WORKBENCH_PROJECT_ID, STUDY_ID, INPUT_DATASET_ID, OUTPUT_DATASET_ID, null);
			Assert.fail("Method should throw an error");
		} catch (
			final IllegalArgumentException e) {
			// Do nothing
		}

	}

	@Test
	public void testSaveSsaResultSummaryWithValidRequiredParams() throws IBPWebServiceException {
		final BreedingViewResponse response =
			this.breedingViewResource.saveSingleSiteAnalysisData(MAIN_OUTPUT_FILE_PATH, SUMMARY_OUTPUT_FILE_PATH, OUTLIER_OUTPUT_FILE_PATH,
				WORKBENCH_PROJECT_ID, STUDY_ID, INPUT_DATASET_ID, OUTPUT_DATASET_ID, CROP_NAME);

		Mockito.verify(this.breedingViewService).execute(anyMap(), anyList());
		Assert.assertTrue("Web service should succeed", response.isSuccessful());
		Assert.assertTrue("Web service should return no errors", response.getMessage().indexOf("Successfully invoked service.") != -1);
	}

}
