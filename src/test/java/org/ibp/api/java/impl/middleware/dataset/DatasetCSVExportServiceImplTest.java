package org.ibp.api.java.impl.middleware.dataset;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.data.initializer.DatasetTypeTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitData;
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
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetCSVExportServiceImplTest {

	private static final int RANDOM_STRING_LENGTH = 10;
	private static final String TEST_ENTRY_DESCRIPTION = "Test Entry";
	private static final String TEST_ENTRY_NAME = "T";
	private static final Integer LOCATION_ID = 1;
	private static final String LOCATION_ABBR = "LOC1";
	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(10);

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
	private DatasetTypeService datasetTypeService;

	@Mock
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Mock
	private ZipUtil zipUtil;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@InjectMocks
	private DatasetCSVExportServiceImpl datasetExportService;

	final private Random random = new Random();
	final private Study study = new Study();
	final private DataSet trialDataSet = new DataSet();
	final private DatasetDTO dataSetDTO = new DatasetDTO();
	final private int instanceId1 = this.random.nextInt();
	final private int instanceId2 = this.random.nextInt();

	@Before
	public void setUp() {

		this.study.setId(this.random.nextInt());
		this.study.setName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		this.trialDataSet.setId(this.random.nextInt());
		this.dataSetDTO.setDatasetId(this.random.nextInt());
		this.dataSetDTO.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		this.dataSetDTO.setName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		this.dataSetDTO.setInstances(this.createStudyInstances());

		when(this.studyDataManager.getStudy(this.study.getId())).thenReturn(this.study);
		when(this.studyDataManager.getDataSetsByType(anyInt(), eq(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(Arrays.asList(this.trialDataSet));

		this.datasetExportService.setZipUtil(this.zipUtil);

		when(this.datasetService.getDataset(anyInt())).thenReturn(this.dataSetDTO);
		this.dataSetDTO.setParentDatasetId(1);

		when(this.datasetTypeService.getAllDatasetTypesMap()).thenReturn(DatasetTypeTestDataInitializer.createDatasetTypes());
		final StandardVariable standardVariable = new StandardVariable();
		final Enumeration enumeration = new Enumeration();
		enumeration.setDescription(TEST_ENTRY_DESCRIPTION);
		enumeration.setName(TEST_ENTRY_NAME);
		standardVariable.setEnumerations(Arrays.asList(enumeration));
		when(this.ontologyDataManager
			.getStandardVariable(TermId.ENTRY_TYPE.getId(), PROGRAM_UUID)).thenReturn(standardVariable);

		ContextHolder.setCurrentProgram(PROGRAM_UUID);
		ContextHolder.setCurrentCrop("maize");
	}

	@Test
	public void testExport() throws IOException {

		final File zipFile = new File("");
		final Set<Integer> instanceIds = new HashSet<>(Arrays.asList(this.instanceId1, this.instanceId2));
		when(this.zipUtil.zipFiles(contains(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = Mockito.mock(HashMap.class);
		when(this.studyDatasetService
			.getInstanceObservationUnitRowsMap(eq(this.study.getId()), eq(this.dataSetDTO.getDatasetId()), any(ArrayList.class)))
			.thenReturn(instanceObservationUnitRowsMap);

		final File result = this.datasetExportService.export(this.study.getId(), this.dataSetDTO.getDatasetId(), instanceIds,
			DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId(), false);

		verify(this.studyValidator).validate(this.study.getId(), false);
		verify(this.datasetValidator).validateDataset(this.study.getId(), this.dataSetDTO.getDatasetId());
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

		when(this.datasetCSVGenerator.generateSingleInstanceFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables),
			ArgumentMatchers.anyList(), anyString(), any(StudyInstance.class)))
			.thenReturn(new File(""));
		when(this.zipUtil.zipFiles(contains(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
		this.datasetExportService.setZipUtil(this.zipUtil);

		final Map<Integer, StudyInstance> studyInstanceMap = this.datasetExportService
			.getSelectedDatasetInstancesMap(this.createStudyInstances(), new HashSet<>(Arrays.asList(this.instanceId1, this.instanceId2)));
		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(this.instanceId1, new ArrayList<ObservationUnitRow>());
		instanceObservationUnitRowsMap.put(this.instanceId2, new ArrayList<ObservationUnitRow>());

		final File result = this.datasetExportService
			.generateFiles(
				this.study, this.dataSetDTO, studyInstanceMap, instanceObservationUnitRowsMap, new ArrayList<MeasurementVariable>(),
				this.datasetCSVGenerator, AbstractDatasetExportService.CSV);
		verify(this.datasetCSVGenerator, Mockito.times(studyInstanceMap.size()))
			.generateSingleInstanceFile(
				anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.anyList(), anyString(),
				any(StudyInstance.class));

		verify(this.zipUtil).zipFiles(eq(FileNameGenerator.generateFileName(this.study.getName())), anyListOf(File.class));
		assertSame(result, zipFile);
	}

	@Test
	public void testGenerateCSVFileInSingleFile() throws IOException {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final File csvFile = new File("");
		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(1, new ArrayList<ObservationUnitRow>());
		instanceObservationUnitRowsMap.put(2, new ArrayList<ObservationUnitRow>());

		when(this.datasetCSVGenerator.generateMultiInstanceFile(eq(instanceObservationUnitRowsMap), eq(measurementVariables), anyString()))
			.thenReturn(csvFile);

		final File result = this.datasetExportService
			.generateInSingleFile(
				this.study, this.dataSetDTO, instanceObservationUnitRowsMap,
				measurementVariables, this.datasetCSVGenerator, AbstractDatasetExportService.CSV);

		verify(this.datasetCSVGenerator)
			.generateMultiInstanceFile(eq(instanceObservationUnitRowsMap), eq(measurementVariables), anyString());
		assertSame(result, csvFile);
	}

	@Test
	public void testGenerateCSVFilesOnlyOneInstance() throws IOException {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();

		final File csvFile = new File("");

		when(this.datasetCSVGenerator.generateSingleInstanceFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables),
			ArgumentMatchers.anyList(), anyString(), any(StudyInstance.class)))
			.thenReturn(csvFile);

		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(1, new ArrayList<ObservationUnitRow>());

		final StudyInstance studyInstance = this.createStudyInstance(1);
		final Map<Integer, StudyInstance> studyInstanceMap = new HashMap<>();
		studyInstanceMap.put(1, studyInstance);
		final File result = this.datasetExportService
			.generateFiles(
				this.study, this.dataSetDTO, studyInstanceMap, instanceObservationUnitRowsMap, new ArrayList<MeasurementVariable>(),
				this.datasetCSVGenerator, AbstractDatasetExportService.CSV);

		verify(this.datasetCSVGenerator)
			.generateSingleInstanceFile(
				anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.anyList(), anyString(),
				any(StudyInstance.class));

		verify(this.zipUtil, times(0)).zipFiles(anyString(), anyListOf(File.class));
		assertSame(result, csvFile);

	}

	@Test
	public void testGetSelectedDatasetInstancesMap() {
		final List<StudyInstance> studyInstances = this.createStudyInstances();
		Map<Integer, StudyInstance> selectedDatasetInstanceMap =
			this.datasetExportService.getSelectedDatasetInstancesMap(studyInstances, new HashSet<Integer>(Arrays.asList(this.instanceId2)));
		Assert.assertEquals(1, selectedDatasetInstanceMap.size());

		selectedDatasetInstanceMap = this.datasetExportService
			.getSelectedDatasetInstancesMap(studyInstances, new HashSet<Integer>(Arrays.asList(this.instanceId1, this.instanceId2)));
		Assert.assertEquals(2, selectedDatasetInstanceMap.size());
	}

	@Test
	public void testMoveTrialInstanceInTheFirstColumn() {
		final List<MeasurementVariable> reorderedColumns = this.datasetExportService
			.moveSelectedVariableInTheFirstColumn(this.createColumnHeaders(), TermId.TRIAL_INSTANCE_FACTOR.getId());
		Assert.assertEquals(TermId.TRIAL_INSTANCE_FACTOR.getId(), reorderedColumns.get(0).getTermId());
	}

	@Test
	public void testGetColumns() {
		final List<Integer> subObsTypeIds = new ArrayList<>();
		subObsTypeIds.add(5);

		this.datasetExportService.getColumns(1, 1);
		Mockito.verify(this.studyDatasetService).getAllDatasetVariables(1, 1);
	}

	@Test
	public void testGetObservationUnitRowMap() {
		this.datasetExportService.getObservationUnitRowMap(this.study, this.dataSetDTO, new HashMap<>());
		Mockito.verify(this.studyDatasetService)
			.getInstanceObservationUnitRowsMap(this.study.getId(), this.dataSetDTO.getDatasetId(), new ArrayList<>());
	}

	@Test
	public void testAddLocationValues() {
		final HashMap<Integer, StudyInstance> studyInstanceHashMap = new HashMap<>();
		studyInstanceHashMap.put(5, this.createStudyInstance(5));

		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap =
			this.createObservationUnitRowMap(TermId.LOCATION_ID.name(), "UNKNOWN");
		this.datasetExportService.addLocationValues(observationUnitRowMap, studyInstanceHashMap);
		final Map<String, ObservationUnitData> variables = observationUnitRowMap.get(5).get(0).getVariables();
		Assert.assertEquals(3, variables.size());
		Assert.assertEquals(LOCATION_ID.toString(), variables.get(DatasetServiceImpl.LOCATION_ID_VARIABLE_NAME).getValue());
		Assert.assertEquals(LOCATION_ABBR, variables.get(DatasetServiceImpl.LOCATION_ABBR_VARIABLE_NAME).getValue());
	}

	private Map<Integer, List<ObservationUnitRow>> createObservationUnitRowMap(final String variableName, final String value) {
		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap = new HashMap<>();
		final ObservationUnitRow row = new ObservationUnitRow();
		final Map<String, ObservationUnitData> variables = new HashMap<>();
		final ObservationUnitData observationUnitData = new ObservationUnitData();
		observationUnitData.setValue(value);
		variables.put(variableName, observationUnitData);
		row.setVariables(variables);
		observationUnitRowMap.put(5, Arrays.asList(row));
		return observationUnitRowMap;
	}

	private List<StudyInstance> createStudyInstances() {
		final StudyInstance studyInstance1 = this.createStudyInstance(this.instanceId1);
		final StudyInstance studyInstance2 = this.createStudyInstance(this.instanceId2);
		return new ArrayList<>(Arrays.asList(studyInstance1, studyInstance2));
	}

	private StudyInstance createStudyInstance(final Integer instanceId) {
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceId(instanceId);
		studyInstance.setInstanceNumber(this.random.nextInt());
		studyInstance.setLocationName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		studyInstance.setLocationId(LOCATION_ID);
		studyInstance.setLocationAbbreviation(LOCATION_ABBR);
		return studyInstance;
	}

	private List<MeasurementVariable> createColumnHeaders() {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable mvar1 =
			MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GID.getId(), TermId.GID.name());
		mvar1.setAlias("DIG");
		measurementVariables.add(mvar1);

		final MeasurementVariable mvar2 =
			MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(), TermId.DESIG.name());
		mvar2.setAlias("DESIGNATION");
		measurementVariables.add(mvar2);

		final MeasurementVariable mvar3 = MeasurementVariableTestDataInitializer
			.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), TermId.TRIAL_INSTANCE_FACTOR.name());
		mvar3.setAlias("TRIAL_INSTANCE");
		measurementVariables.add(mvar3);
		return measurementVariables;
	}

}
