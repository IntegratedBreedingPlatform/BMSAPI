package org.ibp.api.java.impl.middleware.dataset;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.api.genotype.SampleGenotypeService;
import org.generationcp.middleware.data.initializer.DatasetTypeTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetExcelExportServiceImplTest {

	private static final int RANDOM_STRING_LENGTH = 10;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetExcelGenerator datasetExcelGenerator;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Mock
	private ZipUtil zipUtil;

	@Mock
	private DatasetService studyDatasetService;

	@Mock
	private DatasetTypeService datasetTypeService;

	@Mock
	private DatasetCollectionOrderService datasetCollectionOrderService;

	@Mock
	private SampleGenotypeService sampleGenotypeService;

	@InjectMocks
	private DatasetExcelExportServiceImpl datasetExportService;

	private final List<MeasurementVariable> measurementVariables = new ArrayList<>();

	private final Random random = new Random();
	private final Study study = new Study();
	private final DataSet trialDataSet = new DataSet();
	private final DatasetDTO dataSetDTO = new DatasetDTO();
	private final int instanceId1 = this.random.nextInt();
	private final int instanceId2 = this.random.nextInt();

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
		this.createColumnHeaders();

		when(this.datasetTypeService.getAllDatasetTypesMap()).thenReturn(DatasetTypeTestDataInitializer.createDatasetTypes());
	}

	@Test
	public void testExport_IncludeSampleGenotypeValues() throws IOException {

		final File zipFile = new File("");
		final Set<Integer> instanceIds = new HashSet<>(Arrays.asList(this.instanceId1, this.instanceId2));
		when(this.zipUtil.zipFiles(contains(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
		Mockito.when(this.sampleGenotypeService.getSampleGenotypeVariables(any()))
			.thenReturn(new HashMap<>());
		final File result = this.datasetExportService.export(this.study.getId(), this.dataSetDTO.getDatasetId(), instanceIds,
			DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId(), false, true);

		verify(this.studyValidator).validate(this.study.getId(), false);
		verify(this.datasetValidator).validateDataset(this.study.getId(), this.dataSetDTO.getDatasetId());
		verify(this.instanceValidator).validate(this.dataSetDTO.getDatasetId(), instanceIds);
		verify(this.sampleGenotypeService).getSampleGenotypeVariables(any());
		assertSame(result, zipFile);
	}

	@Test
	public void testExport_ExcludeSampleGenotypeValues() throws IOException {

		final File zipFile = new File("");
		final Set<Integer> instanceIds = new HashSet<>(Arrays.asList(this.instanceId1, this.instanceId2));
		when(this.zipUtil.zipFiles(contains(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
		final File result = this.datasetExportService.export(this.study.getId(), this.dataSetDTO.getDatasetId(), instanceIds,
			DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId(), false, false);

		verify(this.studyValidator).validate(this.study.getId(), false);
		verify(this.datasetValidator).validateDataset(this.study.getId(), this.dataSetDTO.getDatasetId());
		verify(this.instanceValidator).validate(this.dataSetDTO.getDatasetId(), instanceIds);
		verify(this.sampleGenotypeService, times(0)).getSampleGenotypeVariables(any());
		assertSame(result, zipFile);
	}

	@Test
	public void testGenerateExcelFilesMoreThanOneInstance() throws IOException {

		final List<MeasurementVariable> measurementVariables = new ArrayList<>();

		final File zipFile = new File("");

		when(this.datasetExcelGenerator.generateSingleInstanceFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables),
			ArgumentMatchers.<ObservationUnitRow>anyList(), anyMap(), anyString(), any(StudyInstance.class)))
			.thenReturn(new File(""));
		when(this.zipUtil.zipFiles(contains(this.study.getName()), anyListOf(File.class))).thenReturn(zipFile);
		this.datasetExportService.setZipUtil(this.zipUtil);

		final Map<Integer, StudyInstance> studyInstanceMap = this.datasetExportService
			.getSelectedDatasetInstancesMap(this.createStudyInstances(), new HashSet<>(Arrays.asList(this.instanceId1, this.instanceId2)));
		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(this.instanceId1, new ArrayList<>());
		instanceObservationUnitRowsMap.put(this.instanceId2, new ArrayList<>());

		final File result = this.datasetExportService
			.generateFiles(
				this.study, this.dataSetDTO, studyInstanceMap, instanceObservationUnitRowsMap, new HashMap<>(), new ArrayList<>(),
				this.datasetExcelGenerator, AbstractDatasetExportService.XLS);
		verify(this.datasetExcelGenerator, Mockito.times(studyInstanceMap.size()))
			.generateSingleInstanceFile(
				anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.<ObservationUnitRow>anyList(), anyMap(),
				anyString(),
				any(StudyInstance.class));

		verify(this.zipUtil).zipFiles(contains(this.study.getName()), anyListOf(File.class));
		assertSame(result, zipFile);
	}

	@Test
	public void testGenerateExcelFileInSingleFile() throws IOException {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final File excelFile = new File("");
		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(1, new ArrayList<>());
		instanceObservationUnitRowsMap.put(2, new ArrayList<>());

		when(
			this.datasetExcelGenerator.generateMultiInstanceFile(eq(study.getId()), eq(this.dataSetDTO), eq(instanceObservationUnitRowsMap), anyMap(),
				eq(measurementVariables), anyString()))
			.thenReturn(excelFile);

		final File result = this.datasetExportService
			.generateInSingleFile(
				this.study, this.dataSetDTO, instanceObservationUnitRowsMap,
				new HashMap<>(), measurementVariables, this.datasetExcelGenerator, AbstractDatasetExportService.XLS);

		verify(this.datasetExcelGenerator)
			.generateMultiInstanceFile(eq(study.getId()), eq(this.dataSetDTO), eq(instanceObservationUnitRowsMap), anyMap(), eq(measurementVariables), anyString());
		assertSame(result, excelFile);
	}

	@Test
	public void testGenerateExcelFilesOnlyOneInstance() throws IOException {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();

		final File excelFile = new File("");

		when(this.datasetExcelGenerator.generateSingleInstanceFile(anyInt(), eq(this.dataSetDTO), eq(measurementVariables),
			ArgumentMatchers.<ObservationUnitRow>anyList(), anyMap(), anyString(), any(StudyInstance.class)))
			.thenReturn(excelFile);

		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(1, new ArrayList<>());

		final StudyInstance studyInstance = this.createStudyInstance(1);
		final Map<Integer, StudyInstance> studyInstanceMap = new HashMap<>();
		studyInstanceMap.put(1, studyInstance);
		final File result = this.datasetExportService.generateFiles(
			this.study, this.dataSetDTO, studyInstanceMap, instanceObservationUnitRowsMap, new HashMap<>(), new ArrayList<>(),
			this.datasetExcelGenerator, AbstractDatasetExportService.XLS);

		verify(this.datasetExcelGenerator)
			.generateSingleInstanceFile(
				anyInt(), eq(this.dataSetDTO), eq(measurementVariables), ArgumentMatchers.<ObservationUnitRow>anyList(), anyMap(),
				anyString(),
				any(StudyInstance.class));

		verify(this.zipUtil, times(0)).zipFiles(anyString(), anyListOf(File.class));
		assertSame(result, excelFile);

	}

	@Test
	public void testGetSelectedDatasetInstancesMap() {
		final List<StudyInstance> studyInstances = this.createStudyInstances();
		Map<Integer, StudyInstance> selectedDatasetInstanceMap =
			this.datasetExportService.getSelectedDatasetInstancesMap(studyInstances, new HashSet<>(Arrays.asList(this.instanceId2)));
		Assert.assertEquals(1, selectedDatasetInstanceMap.size());

		selectedDatasetInstanceMap = this.datasetExportService
			.getSelectedDatasetInstancesMap(studyInstances, new HashSet<>(Arrays.asList(this.instanceId1, this.instanceId2)));
		Assert.assertEquals(2, selectedDatasetInstanceMap.size());
	}

	@Test
	public void testMoveTrialInstanceInTheFirstColumn() {
		final List<MeasurementVariable> reorderedColumns = this.datasetExportService
			.moveSelectedVariableInTheFirstColumn(this.createColumnHeaders(), TermId.TRIAL_INSTANCE_FACTOR.getId());
		Assert.assertEquals(TermId.TRIAL_INSTANCE_FACTOR.getId(), reorderedColumns.get(0).getTermId());
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
		return studyInstance;
	}

	private List<MeasurementVariable> createColumnHeaders() {

		final MeasurementVariable mvar1 =
			MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GID.getId(), TermId.GID.name());
		mvar1.setAlias("DIG");
		this.measurementVariables.add(mvar1);

		final MeasurementVariable mvar2 =
			MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(), TermId.DESIG.name());
		mvar2.setAlias("DESIGNATION");
		this.measurementVariables.add(mvar2);

		final MeasurementVariable mvar3 = MeasurementVariableTestDataInitializer
			.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), TermId.TRIAL_INSTANCE_FACTOR.name());
		mvar3.setAlias("TRIAL_INSTANCE");
		this.measurementVariables.add(mvar3);
		return this.measurementVariables;
	}

}
