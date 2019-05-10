package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.data.initializer.DatasetTypeTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DatasetType;
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
	private OntologyDataManager ontologyDataManager;

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
		this.dataSetDTO.setDatasetTypeId(DatasetType.PLANT_SUBOBSERVATIONS);
		this.dataSetDTO.setName(RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH));
		this.dataSetDTO.setInstances(this.createStudyInstances());

		when(this.studyDataManager.getStudy(this.study.getId())).thenReturn(this.study);
		when(this.studyDataManager.getDataSetsByType(anyInt(), eq(DatasetType.SUMMARY_DATA)))
			.thenReturn(Arrays.asList(this.trialDataSet));

		this.datasetExportService.setZipUtil(this.zipUtil);

		when(this.datasetService.getDataset(anyInt())).thenReturn(this.dataSetDTO);
		this.dataSetDTO.setParentDatasetId(1);

		when(this.ontologyDataManager.getAllDatasetTypes()).thenReturn(DatasetTypeTestDataInitializer.createDatasetTypes());

	}

	@Test
	public void testExport() throws IOException {

		final File zipFile = new File("");
		final Set<Integer> instanceIds = new HashSet<>(Arrays.asList(this.instanceId1, this.instanceId2));
		when(this.zipUtil.zipFiles(eq(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = Mockito.mock(HashMap.class);
		when(this.studyDatasetService
			.getInstanceObservationUnitRowsMap(eq(this.study.getId()), eq(this.dataSetDTO.getDatasetId()), any(ArrayList.class)))
			.thenReturn(instanceObservationUnitRowsMap);

		final File result = this.datasetExportService.export(this.study.getId(), this.dataSetDTO.getDatasetId(), instanceIds,
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

		when(this.datasetCSVGenerator.generateSingleInstanceFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables),
			ArgumentMatchers.<ObservationUnitRow>anyList(), anyString(), any(StudyInstance.class)))
			.thenReturn(new File(""));
		when(this.zipUtil.zipFiles(eq(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
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
				anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.<ObservationUnitRow>anyList(), anyString(),
				any(StudyInstance.class));

		verify(this.zipUtil).zipFiles(eq(this.study.getName()), anyListOf(File.class));
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
				this.study, instanceObservationUnitRowsMap,
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
			ArgumentMatchers.<ObservationUnitRow>anyList(), anyString(), any(StudyInstance.class)))
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
				anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.<ObservationUnitRow>anyList(), anyString(),
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
		this.datasetExportService.getColumns(1, 1);
		Mockito.verify(this.datasetService).getDataset(1);
		Mockito.verify(this.studyDataManager).getDataSetsByType(1, DatasetType.SUMMARY_DATA);
		Mockito.verify(this.datasetService).getMeasurementVariables(1, Lists.newArrayList(VariableType.STUDY_DETAIL.getId()));
		Mockito.verify(this.datasetService).getMeasurementVariables(anyInt(), eq(Lists.newArrayList(
			VariableType.ENVIRONMENT_DETAIL.getId(),
			VariableType.STUDY_CONDITION.getId())));
		Mockito.verify(this.datasetService).getMeasurementVariables(
			1,
			Lists.newArrayList(VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
				VariableType.TREATMENT_FACTOR.getId(), VariableType.OBSERVATION_UNIT.getId()));
		Mockito.verify(this.datasetService).getMeasurementVariables(1, Lists.newArrayList(
			VariableType.GERMPLASM_DESCRIPTOR.getId(),
			VariableType.OBSERVATION_UNIT.getId()));
		Mockito.verify(this.datasetService).getMeasurementVariables(1, Lists.newArrayList(TermId.MULTIFACTORIAL_INFO.getId()));
		Mockito.verify(this.datasetService).getMeasurementVariables(1, Lists.newArrayList(VariableType.TRAIT.getId()));
		Mockito.verify(this.datasetService).getMeasurementVariables(1, Lists.newArrayList(VariableType.SELECTION_METHOD.getId()));
	}

	@Test
	public void testGetObservationUnitRowMap() {
		this.datasetExportService.getObservationUnitRowMap(this.study, this.dataSetDTO, new HashMap<Integer, StudyInstance>());
		Mockito.verify(this.studyDatasetService)
			.getInstanceObservationUnitRowsMap(this.study.getId(), this.dataSetDTO.getDatasetId(), new ArrayList<Integer>());
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
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable mvar1 = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GID.getId(), TermId.GID.name());
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
