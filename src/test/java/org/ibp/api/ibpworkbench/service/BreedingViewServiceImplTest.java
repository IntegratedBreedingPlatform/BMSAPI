
package org.ibp.api.ibpworkbench.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.generationcp.commons.exceptions.BreedingViewImportException;
import org.generationcp.commons.service.BreedingViewImportService;
import org.ibp.api.java.impl.middleware.breedingview.BreedingViewParameter;
import org.ibp.api.exception.IBPWebServiceException;
import org.ibp.api.java.impl.middleware.breedingview.BreedingViewServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BreedingViewServiceImplTest {

	final static String OUTPUT_FILEPATH = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSOutput_1_6.csv";
	final static String SUMMARY_FILEPATH = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSSummary_1_6.csv";
	final static String OUTLIER_FILEPATH = "C:\\BMS4\\workspace\\maize_test\\breeding_view\\output\\BMSOutlier_1_6.csv";
	final static Integer STUDY_ID = 25347;
	final static Integer WORKBENCH_PROJECT_ID = 2;
	final static Integer DATASET_ID = 25349;
	final static Integer OUTPUT_DATASET_ID = 0;

	@Mock
	private BreedingViewImportService bvImportService;

	private BreedingViewServiceImpl breedingViewService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.breedingViewService = new BreedingViewServiceImpl();
		this.breedingViewService.setBreedingViewImportService(this.bvImportService);
	}
	
	private String getFileName(final String absoluteUrl) {
		return absoluteUrl.substring(absoluteUrl.lastIndexOf("\\")+1);
	}

	@Test
	public void testExecuteWithNoOutlierFile() {
		final Map<String, String> params =
				this.createBVParams(BreedingViewServiceImplTest.OUTPUT_FILEPATH, BreedingViewServiceImplTest.SUMMARY_FILEPATH, "",
						BreedingViewServiceImplTest.WORKBENCH_PROJECT_ID.toString(), BreedingViewServiceImplTest.STUDY_ID.toString(),
						BreedingViewServiceImplTest.DATASET_ID.toString(), BreedingViewServiceImplTest.OUTPUT_DATASET_ID.toString());
		try {
			this.breedingViewService.execute(params, new ArrayList<String>());

			// Verify the service method to import means data was called
			final ArgumentCaptor<File> meansFileCaptor = ArgumentCaptor.forClass(File.class);
			Mockito.verify(this.bvImportService).importMeansData(meansFileCaptor.capture(),
					Matchers.eq(BreedingViewServiceImplTest.STUDY_ID));
			Assert.assertEquals(this.getFileName(BreedingViewServiceImplTest.OUTPUT_FILEPATH), this.getFileName(meansFileCaptor.getValue().getName()));

			// Verify the service method to import means data was called
			final ArgumentCaptor<File> summaryFileCaptor = ArgumentCaptor.forClass(File.class);
			Mockito.verify(this.bvImportService).importSummaryStatsData(summaryFileCaptor.capture(),
					Matchers.eq(BreedingViewServiceImplTest.STUDY_ID));
			Assert.assertEquals(this.getFileName(BreedingViewServiceImplTest.SUMMARY_FILEPATH), this.getFileName(summaryFileCaptor.getValue().getName()));

			// Verify the service method to import outlier was not called
			Mockito.verify(this.bvImportService, Mockito.never()).importOutlierData(Matchers.any(File.class), Matchers.anyInt());

		} catch (IBPWebServiceException | BreedingViewImportException e) {
			Assert.fail("Not expecting IBPWebServiceException to be thrown.");
		}
	}

	

	@Test
	public void testExecuteWithOutlierFile() {
		final Map<String, String> params = this.createBVParams(BreedingViewServiceImplTest.OUTPUT_FILEPATH,
				BreedingViewServiceImplTest.SUMMARY_FILEPATH, BreedingViewServiceImplTest.OUTLIER_FILEPATH,
				BreedingViewServiceImplTest.WORKBENCH_PROJECT_ID.toString(), BreedingViewServiceImplTest.STUDY_ID.toString(),
				BreedingViewServiceImplTest.DATASET_ID.toString(), BreedingViewServiceImplTest.OUTPUT_DATASET_ID.toString());
		try {
			this.breedingViewService.execute(params, new ArrayList<String>());

			// Verify the service method to import means data was called
			final ArgumentCaptor<File> meansFileCaptor = ArgumentCaptor.forClass(File.class);
			Mockito.verify(this.bvImportService).importMeansData(meansFileCaptor.capture(),
					Matchers.eq(BreedingViewServiceImplTest.STUDY_ID));
			Assert.assertEquals(this.getFileName(BreedingViewServiceImplTest.OUTPUT_FILEPATH), this.getFileName(meansFileCaptor.getValue().getName()));

			// Verify the service method to import means data was called
			final ArgumentCaptor<File> summaryFileCaptor = ArgumentCaptor.forClass(File.class);
			Mockito.verify(this.bvImportService).importSummaryStatsData(summaryFileCaptor.capture(),
					Matchers.eq(BreedingViewServiceImplTest.STUDY_ID));
			Assert.assertEquals(this.getFileName(BreedingViewServiceImplTest.SUMMARY_FILEPATH), this.getFileName(summaryFileCaptor.getValue().getName()));

			// Verify the service method to import outlier was called
			final ArgumentCaptor<File> outlierFileCaptor = ArgumentCaptor.forClass(File.class);
			Mockito.verify(this.bvImportService).importOutlierData(outlierFileCaptor.capture(),
					Matchers.eq(BreedingViewServiceImplTest.STUDY_ID));
			Assert.assertEquals(this.getFileName(BreedingViewServiceImplTest.OUTLIER_FILEPATH), this.getFileName(outlierFileCaptor.getValue().getName()));

		} catch (IBPWebServiceException | BreedingViewImportException e) {
			Assert.fail("Not expecting IBPWebServiceException to be thrown.");
		}
	}

	@Test
	public void testExecuteWithBreedingViewImportException() {
		final String parsingError = "ERROR PARSING MEANS FILE";
		final Map<String, String> params = this.createBVParams(BreedingViewServiceImplTest.OUTPUT_FILEPATH,
				BreedingViewServiceImplTest.SUMMARY_FILEPATH, BreedingViewServiceImplTest.OUTLIER_FILEPATH,
				BreedingViewServiceImplTest.WORKBENCH_PROJECT_ID.toString(), BreedingViewServiceImplTest.STUDY_ID.toString(),
				BreedingViewServiceImplTest.DATASET_ID.toString(), BreedingViewServiceImplTest.OUTPUT_DATASET_ID.toString());

		try {
			Mockito.doThrow(new BreedingViewImportException(parsingError)).when(this.bvImportService)
					.importMeansData(Matchers.any(File.class), Matchers.anyInt());

			this.breedingViewService.execute(params, new ArrayList<String>());

			Assert.fail("Expecting IBPWebServiceException to be thrown but was not.");
			
		} catch (final BreedingViewImportException e) {
			Assert.fail("Expecting BreedingViewImportException to be wrapped in IBPWebServiceException but was not.");

		} catch (final IBPWebServiceException e) {
			Assert.assertEquals(parsingError, e.getMessage());
		}
	}

	private Map<String, String> createBVParams(final String mainOutputFilePath, final String summaryOutputFilePath,
			final String outlierOutputFilePath, final String workbenchProjectId, final String studyId, final String inputDataSetId,
			final String outputDataSetId) {
		final Map<String, String> params = new HashMap<String, String>();
		params.put(BreedingViewParameter.MAIN_OUTPUT_FILE_PATH.getParamValue(), mainOutputFilePath);
		params.put(BreedingViewParameter.SUMMARY_OUTPUT_FILE_PATH.getParamValue(), summaryOutputFilePath);
		params.put(BreedingViewParameter.OUTLIER_OUTPUT_FILE_PATH.getParamValue(), outlierOutputFilePath);
		params.put(BreedingViewParameter.WORKBENCH_PROJECT_ID.getParamValue(), workbenchProjectId);
		params.put(BreedingViewParameter.STUDY_ID.getParamValue(), studyId);
		params.put(BreedingViewParameter.INPUT_DATASET_ID.getParamValue(), inputDataSetId);
		params.put(BreedingViewParameter.OUTPUT_DATASET_ID.getParamValue(), outputDataSetId);
		return params;
	}

}
