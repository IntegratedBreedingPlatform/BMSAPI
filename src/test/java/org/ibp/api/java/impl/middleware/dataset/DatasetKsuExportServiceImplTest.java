package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.data.initializer.DatasetTypeTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.MethodTestDataInitializer;
import org.generationcp.middleware.data.initializer.ValueReferenceTestDataInitializer;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.MethodService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetFileGenerator;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetKsuExportServiceImplTest {

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private DatasetService studyDatasetService;

	@Mock
	private DatasetCollectionOrderService datasetCollectionOrderService;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private ZipUtil zipUtil;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private MethodService methodService;

	@Mock
	private DatasetKsuCSVGenerator datasetKSUCSVGenerator;

	@Mock
	private DatasetKsuExcelGenerator datasetKSUExcelGenerator;

	@Mock
	private DatasetTypeService datasetTypeService;

	@InjectMocks
	private DatasetKsuCSVExportServiceImpl datasetKSUCSVExportService;

	@InjectMocks
	private DatasetKsuExcelExportServiceImpl datasetKSUExcelExportService;

	private final MeasurementVariable measurementVariable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GENERAL_TRAIT_CLASS.getId(), "GW100", "2");

	@Before
	public void setUp() {
		final Property property = new Property(new Term());
		property.setName("METHOD");
		when(this.ontologyDataManager.getProperty(TermId.BREEDING_METHOD_PROP.getId())).thenReturn(property);
		when(this.datasetTypeService.getAllDatasetTypesMap()).thenReturn(DatasetTypeTestDataInitializer.createDatasetTypes());
		final DatasetTypeDTO datasetType = new DatasetTypeDTO(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId(), "PLANT_SUBOBSERVATIONS");
		when(this.datasetTypeService.getDatasetTypeById(datasetType.getDatasetTypeId())).thenReturn(datasetType);
	}

	@Test
	public void testGenerateForKSUExcel() throws IOException {
		this.testGenerateDatasetKSUExportService(this.datasetKSUExcelExportService, AbstractDatasetExportService.XLS,
			this.datasetKSUExcelGenerator);
	}

	@Test
	public void testGenerateForKSUCSV() throws IOException {
		this.testGenerateDatasetKSUExportService(this.datasetKSUCSVExportService, AbstractDatasetExportService.CSV,
			this.datasetKSUCSVGenerator);
	}

	private void testGenerateDatasetKSUExportService(
		final AbstractDatasetExportService datasetKSUExportService, final String fileExtension, final
		DatasetFileGenerator generator) throws IOException {
		final Study study = new Study();
		final DataSet trialDataSet = new DataSet();
		final DatasetDTO dataSetDTO = new DatasetDTO();
		final int instanceId1 = 1;
		final int instanceId2 = 2;

		study.setId(1);
		study.setName("STUDY");
		trialDataSet.setId(1);
		dataSetDTO.setDatasetId(1);
		dataSetDTO.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		dataSetDTO.setName("PLANT");
		dataSetDTO.setInstances(this.createStudyInstances());

		when(this.studyDataManager.getStudy(study.getId())).thenReturn(study);
		when(this.studyDataManager.getDataSetsByType(anyInt(), eq(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(Arrays.asList(trialDataSet));

		datasetKSUExportService.setZipUtil(this.zipUtil);

		when(this.datasetService.getDataset(anyInt())).thenReturn(dataSetDTO);
		dataSetDTO.setParentDatasetId(1);

		final File zipFile = new File("");
		final Set<Integer> instanceIds = new HashSet<>(Arrays.asList(instanceId1, instanceId2));
		when(this.zipUtil.zipFiles(eq(FileNameGenerator.generateFileName(study.getName())), anyListOf(File.class))).thenReturn(zipFile);
		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(1, new ArrayList<>());
		instanceObservationUnitRowsMap.put(2, new ArrayList<>());
		when(this.studyDatasetService.getInstanceObservationUnitRowsMap(eq(study.getId()), eq(dataSetDTO.getDatasetId()), any(
			ArrayList.class))).thenReturn(instanceObservationUnitRowsMap);

		final File result = datasetKSUExportService.generate(study.getId(), dataSetDTO.getDatasetId(), instanceIds,
			DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER.getId(), generator, false, fileExtension);

		verify(this.studyDatasetService)
			.getInstanceObservationUnitRowsMap(eq(study.getId()), eq(dataSetDTO.getDatasetId()), any(ArrayList.class));
		verify(this.datasetCollectionOrderService)
			.reorder(eq(DatasetCollectionOrderServiceImpl.CollectionOrder.PLOT_ORDER), eq(trialDataSet.getId()),
				any(HashMap.class), eq(instanceObservationUnitRowsMap));
		verify(generator).generateTraitAndSelectionVariablesFile(any(ArrayList.class), anyString());
		verify(this.ontologyDataManager).getProperty(TermId.BREEDING_METHOD_PROP.getId());
		verify(this.methodService).getAllBreedingMethods();
		assertSame(result, zipFile);
	}

	@Test
	public void testGetTraitAndSelectionVariables() {
		this.datasetKSUCSVExportService.getTraitAndSelectionVariables(1);
		verify(this.datasetService).getObservationSetVariables(1, Lists.newArrayList(VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId()));
	}

	@Test
	public void testconvertTraitAndSelectionVariablesData() {
		final List<MeasurementVariable> measurementVariables = Arrays.asList(this.measurementVariable);
		final List<String[]> results = this.datasetKSUCSVExportService.convertTraitAndSelectionVariablesData(measurementVariables);
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(BaseDatasetKsuExportService.TRAIT_FILE_HEADERS, results.get(0));
	}

	@Test
	public void testGetDataTypeDescription() {
		String dataTypeDescription = this.datasetKSUCSVExportService.getDataTypeDescription(this.measurementVariable);
		Assert.assertEquals("numeric", dataTypeDescription);

		this.measurementVariable.setDataTypeId(null);
		dataTypeDescription = this.datasetKSUCSVExportService.getDataTypeDescription(this.measurementVariable);
		Assert.assertEquals("unrecognized", dataTypeDescription);
	}

	@Test
	public void testGePossibleValuesString() {
		final List<Method> methods = new ArrayList<>();
		methods.add(MethodTestDataInitializer.createMethod(1, "GEN", "AGB1"));
		methods.add(MethodTestDataInitializer.createMethod(2, "GEN", "AGB2"));
		this.measurementVariable.setProperty("Property");
		String possibleValuesString = this.datasetKSUCSVExportService.getPossibleValuesString("Property", methods, this.measurementVariable);
		Assert.assertEquals("AGB1/AGB2", possibleValuesString);

		this.measurementVariable.setProperty("");
		possibleValuesString = this.datasetKSUCSVExportService.getPossibleValuesString("Property", methods, this.measurementVariable);
		Assert.assertEquals("", possibleValuesString);

		this.measurementVariable.setPossibleValues(ValueReferenceTestDataInitializer.createPossibleValues());
		possibleValuesString = this.datasetKSUCSVExportService.getPossibleValuesString("Property", methods, this.measurementVariable);
		Assert.assertEquals("1/2/3/4/5", possibleValuesString);
	}

	private List<StudyInstance> createStudyInstances() {
		final StudyInstance studyInstance1 = this.createStudyInstance(1);
		final StudyInstance studyInstance2 = this.createStudyInstance(2);
		return new ArrayList<>(Arrays.asList(studyInstance1, studyInstance2));
	}

	private StudyInstance createStudyInstance(final Integer instanceId) {
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceId(instanceId);
		studyInstance.setInstanceNumber(instanceId);
		studyInstance.setLocationName("IRRI");
		return studyInstance;
	}
}
