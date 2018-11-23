package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetExportServiceImplTest {

	public static final int RANDOM_STRING_LENGTH = 10;
	@Mock
	private StudyValidator studyValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private DatasetService studyDatasetService;

	@Mock
	private DatasetCollectionOrderService datasetCollectionOrderService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetCSVGenerator datasetCSVGenerator;

	@Mock
	private ZipUtil zipUtil;

	@InjectMocks
	private DatasetExportServiceImpl datasetExportService;

	final Random random = new Random();
	final Study study = new Study();
	final DataSet trialDataSet = new DataSet();
	final DataSet dataSet = new DataSet();
	final int instanceId1 = random.nextInt();
	final int instanceId2 = random.nextInt();

	@Before
	public void setUp() {

		this.study.setId(random.nextInt());
		this.study.setName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		this.trialDataSet.setId(random.nextInt());
		this.dataSet.setId(random.nextInt());
		this.dataSet.setDataSetType(DataSetType.PLANT_SUBOBSERVATIONS);
		this.dataSet.setName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));

		final List<StudyInstance> studyInstances = this.createStudyInstances();

		when(this.studyDataManager.getStudy(this.study.getId())).thenReturn(this.study);
		when(this.studyDataManager.getDataSet(this.dataSet.getId())).thenReturn(this.dataSet);
		when(this.studyDataManager.getDataSetsByType(this.study.getId(), DataSetType.SUMMARY_DATA))
			.thenReturn(Arrays.asList(this.trialDataSet));
		when(this.studyDatasetService.getDatasetInstances(this.study.getId(), this.dataSet.getId())).thenReturn(studyInstances);

		this.datasetExportService.setZipUtil(this.zipUtil);
	}

	@Test
	public void testExportAsCSV() throws IOException {

		final File zipFile = new File("");
		final Set<Integer> instanceIds = new HashSet<>(Arrays.asList(instanceId1, instanceId2));

		when(this.datasetCSVGenerator.getHeaderNames(anyList())).thenReturn(new ArrayList());
		when(this.datasetCSVGenerator.generateCSVFile(anyList(), anyList(), anyString(), any(CSVWriter.class)))
			.thenReturn(new File(""));
		when(this.zipUtil.zipFiles(eq(this.study.getName()), anyList())).thenReturn(zipFile);

		final File result = datasetExportService.exportAsCSV(this.study.getId(), this.dataSet.getId(), instanceIds,
			DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId());

		verify(this.studyValidator).validate(study.getId(), false);
		verify(this.datasetValidator).validateDataset(study.getId(), dataSet.getId(), false);
		assertSame(result, zipFile);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testExportAsCSVException() throws IOException {

		when(this.datasetCSVGenerator.getHeaderNames(anyList())).thenReturn(new ArrayList<>());
		when(this.datasetCSVGenerator.generateCSVFile(anyList(), anyList(), anyString(), any(CSVWriter.class)))
			.thenThrow(IOException.class);
		final Set<Integer> instanceIds = new HashSet<>(Arrays.asList(instanceId1, instanceId2));

		datasetExportService.exportAsCSV(this.study.getId(), this.dataSet.getId(), instanceIds,
			DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId());

	}

	@Test
	public void testGenerateCSVFilesMoreThanOneInstance() throws IOException {

		final List<StudyInstance> studyInstances = this.createStudyInstances();
		final List<String> headerNames = new ArrayList<>();
		final File zipFile = new File("");

		when(this.datasetCSVGenerator.getHeaderNames(anyList())).thenReturn(headerNames);
		when(this.datasetCSVGenerator.generateCSVFile(eq(headerNames), anyList(), anyString(), any(CSVWriter.class)))
			.thenReturn(new File(""));
		when(this.zipUtil.zipFiles(eq(this.study.getName()), anyList())).thenReturn(zipFile);

		final File result = datasetExportService
			.generateCSVFiles(
				this.study, this.dataSet, studyInstances, DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId());

		for (final StudyInstance studyInstance : studyInstances) {
			verify(this.studyDatasetService)
				.getObservationUnitRows(this.study.getId(), this.dataSet.getId(), studyInstance.getInstanceDbId(), Integer.MAX_VALUE,
					Integer.MAX_VALUE, null, "");

			verify(this.datasetCollectionOrderService)
				.reorder(eq(DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER), eq(this.trialDataSet.getId()),
					eq(String.valueOf(studyInstance.getInstanceNumber())), anyList());

			verify(this.datasetCSVGenerator, times(studyInstances.size()))
				.generateCSVFile(eq(headerNames), anyList(), anyString(), any(CSVWriter.class));
		}

		verify(zipUtil).zipFiles(eq(this.study.getName()), anyList());
		assertSame(result, zipFile);

	}

	@Test
	public void testGenerateCSVFilesOnlyOneInstance() throws IOException {

		final StudyInstance studyInstance = this.createStudyInstance(instanceId1);
		final List<String> headerNames = new ArrayList<>();
		final File csvFile = new File("");

		when(this.datasetCSVGenerator.getHeaderNames(anyList())).thenReturn(headerNames);
		when(this.datasetCSVGenerator.generateCSVFile(eq(headerNames), anyList(), anyString(), any(CSVWriter.class)))
			.thenReturn(csvFile);

		final File result = datasetExportService
			.generateCSVFiles(
				this.study, this.dataSet, Arrays.asList(studyInstance),
				DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId());

		verify(this.studyDatasetService)
			.getObservationUnitRows(this.study.getId(), this.dataSet.getId(), studyInstance.getInstanceDbId(), Integer.MAX_VALUE,
				Integer.MAX_VALUE, null, "");

		verify(this.datasetCollectionOrderService)
			.reorder(eq(DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER), eq(this.trialDataSet.getId()),
				eq(String.valueOf(studyInstance.getInstanceNumber())), anyList());

		verify(this.datasetCSVGenerator)
			.generateCSVFile(eq(headerNames), anyList(), anyString(), any(CSVWriter.class));

		verify(zipUtil, times(0)).zipFiles(anyString(), anyList());
		assertSame(result, csvFile);

	}

	@Test
	public void testGetSelectedDatasetInstances() {

		final List<StudyInstance> result1 =
			datasetExportService
				.getSelectedDatasetInstances(study.getId(), dataSet.getId(), new HashSet<>(Arrays.asList(instanceId1, instanceId2)));

		assertEquals(2, result1.size());

		final List<StudyInstance> result2 =
			datasetExportService.getSelectedDatasetInstances(study.getId(), dataSet.getId(), new HashSet<>(Arrays.asList(instanceId1)));

		assertEquals(1, result2.size());

	}

	private List<StudyInstance> createStudyInstances() {
		final StudyInstance studyInstance1 = createStudyInstance(instanceId1);
		final StudyInstance studyInstance2 = createStudyInstance(instanceId2);
		return new ArrayList<>(Arrays.asList(studyInstance1, studyInstance2));
	}

	private StudyInstance createStudyInstance(final Integer instanceId) {
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceDbId(instanceId);
		studyInstance.setInstanceNumber(random.nextInt());
		studyInstance.setLocationName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		return studyInstance;
	}

}
