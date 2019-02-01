package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetCSVExportServiceImplTest {

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
	private InstanceValidator instanceValidator;

	@Mock
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Mock
	private ZipUtil zipUtil;

	@InjectMocks
	private DatasetCSVExportServiceImpl datasetExportService;

	final Random random = new Random();
	final Study study = new Study();
	final DataSet trialDataSet = new DataSet();
	final DatasetDTO dataSetDTO = new DatasetDTO();
	final int instanceId1 = this.random.nextInt();
	final int instanceId2 = this.random.nextInt();

	@Before
	public void setUp() {

		this.study.setId(this.random.nextInt());
		this.study.setName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		this.trialDataSet.setId(this.random.nextInt());
		this.dataSetDTO.setDatasetId(this.random.nextInt());
		this.dataSetDTO.setDatasetTypeId(DataSetType.PLANT_SUBOBSERVATIONS.getId());
		this.dataSetDTO.setName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		this.dataSetDTO.setInstances(this.createStudyInstances());

		when(this.studyDataManager.getStudy(this.study.getId())).thenReturn(this.study);
		when(this.studyDataManager.getDataSetsByType(this.study.getId(), DataSetType.SUMMARY_DATA))
			.thenReturn(Arrays.asList(this.trialDataSet));

		this.datasetExportService.setZipUtil(this.zipUtil);
	}

	@Test
	public void testExport() throws IOException {

		final File zipFile = new File("");
		final Set<Integer> instanceIds = new HashSet<>(Arrays.asList(instanceId1, instanceId2));
		when(this.zipUtil.zipFiles(eq(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
		Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = Mockito.mock(HashMap.class);
		when(this.studyDatasetService.getInstanceObservationUnitRowsMap(eq(this.study.getId()), eq(this.dataSetDTO.getDatasetId()), any(ArrayList.class))).thenReturn(instanceObservationUnitRowsMap);
		when(this.datasetService.getDataset(anyInt())).thenReturn(this.dataSetDTO);
		this.dataSetDTO.setParentDatasetId(1);

		final File result = datasetExportService.export(this.study.getId(), this.dataSetDTO.getDatasetId(), instanceIds,
			DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId(), false);

		verify(this.studyValidator).validate(this.study.getId(), false);
		verify(this.datasetValidator).validateDataset(this.study.getId(), this.dataSetDTO.getDatasetId(), false);
		verify(this.instanceValidator).validate(this.dataSetDTO.getDatasetId(), instanceIds);
		verify(this.studyDatasetService)
			.getInstanceObservationUnitRowsMap(eq(this.study.getId()), eq(this.dataSetDTO.getDatasetId()), any(ArrayList.class));
		verify(this.datasetCollectionOrderService)
			.reorder(eq(DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER), eq(this.trialDataSet.getId()),
				any(HashMap.class), eq(instanceObservationUnitRowsMap));
		assertSame(result, zipFile);
	}

	@Test
	public void testGenerateCSVFilesMoreThanOneInstance() throws IOException {

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();

		final File zipFile = new File("");

		when(this.datasetCSVGenerator.generateFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.<ObservationUnitRow>anyList(), anyString()))
			.thenReturn(new File(""));
		when(this.zipUtil.zipFiles(eq(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
		this.datasetExportService.setZipUtil(this.zipUtil);

		final Map<Integer, StudyInstance> studyInstanceMap = this.datasetExportService.getSelectedDatasetInstancesMap(this.createStudyInstances(), new HashSet<>(Arrays.asList(this.instanceId1, this.instanceId2)));
		Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(this.instanceId1, new ArrayList<ObservationUnitRow>());
		instanceObservationUnitRowsMap.put(this.instanceId2, new ArrayList<ObservationUnitRow>());

		final File result = datasetExportService
			.generateFiles(
				this.study, this.dataSetDTO, studyInstanceMap, instanceObservationUnitRowsMap, new ArrayList<MeasurementVariable>(), datasetCSVGenerator, AbstractDatasetExportService.CSV);
		verify(this.datasetCSVGenerator, Mockito.times(studyInstanceMap.size()))
			.generateFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.<ObservationUnitRow>anyList(), anyString());

		verify(zipUtil).zipFiles(eq(this.study.getName()), anyListOf(File.class));
		assertSame(result, zipFile);
	}

	@Test
	public void testGenerateCSVFileInSingleFile() throws IOException{
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final File csvFile = new File("");

		when(this.datasetCSVGenerator.generateCSVFileWithHeaders(eq(measurementVariables), anyString(), any(CSVWriter.class)))
			.thenReturn(csvFile);

		Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(1, new ArrayList<ObservationUnitRow>());
		instanceObservationUnitRowsMap.put(2, new ArrayList<ObservationUnitRow>());

		final File result = datasetExportService
			.generateInSingleFile(
				this.study, instanceObservationUnitRowsMap,
				measurementVariables, AbstractDatasetExportService.CSV);

		verify( this.datasetCSVGenerator).generateCSVFileWithHeaders(eq(measurementVariables), anyString(), any(CSVWriter.class));
		verify(this.datasetCSVGenerator, times(2)).writeInstanceObservationUnitRowsToCSVFile(eq(measurementVariables), eq(new ArrayList<ObservationUnitRow>()), any(CSVWriter.class));
		assertSame(result, csvFile);
	}

	@Test
	public void testGenerateCSVFilesOnlyOneInstance() throws IOException {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();

		final File csvFile = new File("");

		when(this.datasetCSVGenerator.generateFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.<ObservationUnitRow>anyList(), anyString()))
			.thenReturn(csvFile);

		Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(1, new ArrayList<ObservationUnitRow>());

		final StudyInstance studyInstance = this.createStudyInstance(1);
		final Map<Integer, StudyInstance> studyInstanceMap = new HashMap<>();
		studyInstanceMap.put(1, studyInstance);
		final File result = datasetExportService
			.generateFiles(
				this.study, this.dataSetDTO, studyInstanceMap, instanceObservationUnitRowsMap, new ArrayList<MeasurementVariable>(), datasetCSVGenerator, AbstractDatasetExportService.CSV);

		verify(this.datasetCSVGenerator)
			.generateFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.<ObservationUnitRow>anyList(), anyString());

		verify(this.zipUtil, times(0)).zipFiles(anyString(), anyListOf(File.class));
		assertSame(result, csvFile);

	}

	@Test
	public void testGetSelectedDatasetInstancesMap() {
		final List<StudyInstance> studyInstances = this.createStudyInstances();
		Map<Integer, StudyInstance> selectedDatasetInstanceMap = this.datasetExportService.getSelectedDatasetInstancesMap(studyInstances, new HashSet<Integer>(Arrays.asList(this.instanceId2)));
		Assert.assertEquals(1, selectedDatasetInstanceMap.size());

		selectedDatasetInstanceMap = this.datasetExportService.getSelectedDatasetInstancesMap(studyInstances, new HashSet<Integer>(Arrays.asList(this.instanceId1, this.instanceId2)));
		Assert.assertEquals(2, selectedDatasetInstanceMap.size());
	}

	@Test
	public void testMoveTrialInstanceInTheFirstColumn() {
		final List<MeasurementVariable> reorderedColumns = this.datasetExportService.moveTrialInstanceInTheFirstColumn(this.createColumnHeaders());
		Assert.assertEquals(TermId.TRIAL_INSTANCE_FACTOR.getId(), reorderedColumns.get(0).getTermId());
	}

	private List<StudyInstance> createStudyInstances() {
		final StudyInstance studyInstance1 = this.createStudyInstance(this.instanceId1);
		final StudyInstance studyInstance2 = this.createStudyInstance(this.instanceId2);
		return new ArrayList<>(Arrays.asList(studyInstance1, studyInstance2));
	}

	private StudyInstance createStudyInstance(final Integer instanceId) {
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceDbId(instanceId);
		studyInstance.setInstanceNumber(this.random.nextInt());
		studyInstance.setLocationName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		return studyInstance;
	}

	private List<MeasurementVariable> createColumnHeaders() {
		List<MeasurementVariable> measurementVariables = new ArrayList<>();
		MeasurementVariable mvar1 = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GID.getId(), TermId.GID.name());
		mvar1.setAlias("DIG");
		measurementVariables.add(mvar1);

		MeasurementVariable mvar2 = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(), TermId.DESIG.name());
		mvar2.setAlias("DESIGNATION");
		measurementVariables.add(mvar2);

		MeasurementVariable mvar3 = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), TermId.TRIAL_INSTANCE_FACTOR.name());
		mvar3.setAlias("TRIAL_INSTANCE");
		measurementVariables.add(mvar3);
		return  measurementVariables;
	}

}
