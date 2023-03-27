package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.genotype.SampleGenotypeService;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetExcelGeneratorTest {

	private static final int INSTANCE_ID = 1;
	private static final Integer STUDY_ID = 1;
	private static final Integer ENVIRONMENT_DATASET_ID = 1;
	private static final Integer PLOT_DATASET_ID = 1;
	private static final String STUDY_DETAIL_TEST = "StudyDetailTest";
	private static final String EXPERIMENTAL_DESIGN_TEST = "ExperimentalDesignTest";
	private static final String ENVIRONMENTAL_DETAILS_TEST = "EnvironmentalDesignTest";
	private static final String ENVIRONMENTAL_CONDITIONS_TEST = "EnvironmentalConditionsTest";
	private static final String GERMPLASM_DESCRIPTORS_TEST = "GermplasmDescriptorsTest";
	private static final String GERMPLASM_PASSPORTS_TEST = "GermplasmPassportsTest";
	private static final String GERMPLASM_ATTRIBUTES_TEST = "GermplasmAttributesTest";
	private static final String OBSERVATION_UNIT_TEST = "ObservationUnitTest";
	private static final String TRAITS_TEST = "TraitsTest";
	private static final String SELECTION_TEST = "SelectionTest";
	private static final String GENOTYPE_MARKER_TEST = "GenotypeMarkerTest";
	private static final String VARIABLE_NAME_1 = "VARIABLE_NAME_1";
	private static final String VARIABLE_NAME_2 = "VARIABLE_NAME_2";
	private static final String VARIABLE_VALUE_1 = "VARIABLE_VALUE_1";
	private static final String VARIABLE_VALUE_2 = "VARIABLE_VALUE_2";
	private static final String VARIABLE_ALIAS_1 = "VARIABLE_ALIAS_1";
	private static final String VARIABLE_ALIAS_2 = "VARIABLE_ALIAS_2";
	private static final int valueIndex = 7;
	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(10);

	private List<ObservationUnitRow> observationUnitRows;

	private List<MeasurementVariable> measurementVariables;

	private ResourceBundleMessageSource messageSource;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetService datasetService;

	@Mock
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetServiceMiddleService;

	@Mock
	private DatasetTypeService datasetTypeService;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private SampleGenotypeService sampleGenotypeService;

	@InjectMocks
	private DatasetExcelGenerator datasetExcelGenerator;

	private final Random random = new Random();

	@Before
	public void setUp() {
		final MeasurementVariable studyDetailVariable = new MeasurementVariable();
		studyDetailVariable.setDataTypeId(VariableType.STUDY_DETAIL.getId());
		studyDetailVariable.setValue(STUDY_DETAIL_TEST);
		studyDetailVariable.setVariableType(VariableType.STUDY_DETAIL);

		final MeasurementVariable experimentalDesignVariable = new MeasurementVariable();
		experimentalDesignVariable.setDataTypeId(VariableType.EXPERIMENTAL_DESIGN.getId());
		experimentalDesignVariable.setValue(EXPERIMENTAL_DESIGN_TEST);
		experimentalDesignVariable.setVariableType(VariableType.EXPERIMENTAL_DESIGN);

		final MeasurementVariable environmentDetailsVariable = new MeasurementVariable();
		environmentDetailsVariable.setDataTypeId(VariableType.ENVIRONMENT_DETAIL.getId());
		environmentDetailsVariable.setValue(ENVIRONMENTAL_DETAILS_TEST);
		environmentDetailsVariable.setVariableType(VariableType.ENVIRONMENT_DETAIL);

		final MeasurementVariable environmentConditionsVariable = new MeasurementVariable();
		environmentConditionsVariable.setDataTypeId(VariableType.ENVIRONMENT_CONDITION.getId());
		environmentConditionsVariable.setValue(ENVIRONMENTAL_CONDITIONS_TEST);
		environmentConditionsVariable.setVariableType(VariableType.ENVIRONMENT_CONDITION);
		environmentConditionsVariable.setTermId(1234);
		final Map<Integer, String> environmentConditionMap = new HashMap<>();
		environmentConditionMap.put(1234, ENVIRONMENTAL_CONDITIONS_TEST);

		final MeasurementVariable germplasmDescriptorVariable = new MeasurementVariable();
		germplasmDescriptorVariable.setDataTypeId(VariableType.GERMPLASM_DESCRIPTOR.getId());
		germplasmDescriptorVariable.setValue(GERMPLASM_DESCRIPTORS_TEST);
		germplasmDescriptorVariable.setVariableType(VariableType.GERMPLASM_DESCRIPTOR);

		final MeasurementVariable germplasmPassportsVariable = new MeasurementVariable();
		germplasmPassportsVariable.setDataTypeId(VariableType.GERMPLASM_PASSPORT.getId());
		germplasmPassportsVariable.setValue(GERMPLASM_PASSPORTS_TEST);
		germplasmPassportsVariable.setVariableType(VariableType.GERMPLASM_PASSPORT);

		final MeasurementVariable germplasmAttributesVariable = new MeasurementVariable();
		germplasmAttributesVariable.setDataTypeId(VariableType.GERMPLASM_ATTRIBUTE.getId());
		germplasmAttributesVariable.setValue(GERMPLASM_ATTRIBUTES_TEST);
		germplasmAttributesVariable.setVariableType(VariableType.GERMPLASM_ATTRIBUTE);

		final MeasurementVariable observationUnitVariable = new MeasurementVariable();
		observationUnitVariable.setDataTypeId(VariableType.OBSERVATION_UNIT.getId());
		observationUnitVariable.setValue(OBSERVATION_UNIT_TEST);
		observationUnitVariable.setVariableType(VariableType.OBSERVATION_UNIT);

		final MeasurementVariable traitsVariable = new MeasurementVariable();
		traitsVariable.setDataTypeId(VariableType.TRAIT.getId());
		traitsVariable.setValue(TRAITS_TEST);
		traitsVariable.setVariableType(VariableType.TRAIT);

		final MeasurementVariable selectionVariable = new MeasurementVariable();
		selectionVariable.setDataTypeId(VariableType.SELECTION_METHOD.getId());
		selectionVariable.setValue(SELECTION_TEST);
		selectionVariable.setVariableType(VariableType.SELECTION_METHOD);

		final MeasurementVariable genotypeMarkerVariable = new MeasurementVariable();
		genotypeMarkerVariable.setTermId(1);
		genotypeMarkerVariable.setDataTypeId(VariableType.GENOTYPE_MARKER.getId());
		genotypeMarkerVariable.setValue(GENOTYPE_MARKER_TEST);
		genotypeMarkerVariable.setVariableType(VariableType.GENOTYPE_MARKER);

		final List<MeasurementVariable> studyDetailVariables = Lists.newArrayList(studyDetailVariable);
		final List<MeasurementVariable> environmentVariables =
			Lists.newArrayList(environmentDetailsVariable, experimentalDesignVariable, environmentConditionsVariable);
		final List<MeasurementVariable> plotVariables = Lists.newArrayList(germplasmDescriptorVariable, germplasmPassportsVariable,
			germplasmAttributesVariable);
		final List<MeasurementVariable> datasetVariables = Lists.newArrayList(observationUnitVariable, traitsVariable, selectionVariable);

		this.messageSource = new ResourceBundleMessageSource();
		this.messageSource.setUseCodeAsDefaultMessage(true);
		this.datasetExcelGenerator.setMessageSource(this.messageSource);
		final DataSet dataSet = new DataSet();
		dataSet.setId(DatasetExcelGeneratorTest.STUDY_ID);
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyTypeDto.getTrialDto());
		final ObservationUnitData observationUnitData1 = new ObservationUnitData();
		observationUnitData1.setValue(DatasetExcelGeneratorTest.VARIABLE_VALUE_1);
		final ObservationUnitData observationUnitData2 = new ObservationUnitData();
		observationUnitData2.setValue(DatasetExcelGeneratorTest.VARIABLE_VALUE_2);
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> variables = new HashMap<>();
		variables.put(DatasetExcelGeneratorTest.VARIABLE_NAME_1, observationUnitData1);
		variables.put(DatasetExcelGeneratorTest.VARIABLE_NAME_2, observationUnitData2);
		observationUnitRow.setVariables(variables);
		this.observationUnitRows = Arrays.asList(observationUnitRow);

		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		measurementVariable1.setAlias(DatasetExcelGeneratorTest.VARIABLE_ALIAS_1);
		measurementVariable1.setName(DatasetExcelGeneratorTest.VARIABLE_NAME_1);
		measurementVariable1.setVariableType(VariableType.TRAIT);
		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		measurementVariable2.setAlias(DatasetExcelGeneratorTest.VARIABLE_ALIAS_2);
		measurementVariable2.setName(DatasetExcelGeneratorTest.VARIABLE_NAME_2);
		measurementVariable2.setVariableType(VariableType.TRAIT);
		this.measurementVariables = Arrays.asList(measurementVariable1, measurementVariable2);

		this.measurementVariables = Arrays.asList(measurementVariable1, measurementVariable2);
		when(this.studyDataManager.getStudyDetails(DatasetExcelGeneratorTest.STUDY_ID)).thenReturn(studyDetails);
		when(this.studyDataManager.getDataSetsByType(DatasetExcelGeneratorTest.STUDY_ID, DatasetTypeEnum.SUMMARY_DATA.getId()))
			.thenReturn(Arrays.asList(dataSet));
		when(this.datasetService
			.getMeasurementVariables(DatasetExcelGeneratorTest.ENVIRONMENT_DATASET_ID, Lists
				.newArrayList(VariableType.STUDY_DETAIL.getId()))).thenReturn(studyDetailVariables);
		when(this.datasetService
			.getMeasurementVariables(DatasetExcelGeneratorTest.ENVIRONMENT_DATASET_ID, Lists
				.newArrayList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
					VariableType.ENVIRONMENT_CONDITION.getId()))).thenReturn(environmentVariables);
		when(this.studyDataManager.getPhenotypeByVariableId(DatasetExcelGeneratorTest.ENVIRONMENT_DATASET_ID, INSTANCE_ID))
			.thenReturn(environmentConditionMap);

		when(this.datasetService.getMeasurementVariables(PLOT_DATASET_ID, Lists
			.newArrayList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.TREATMENT_FACTOR.getId(),
				VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.ENTRY_DETAIL.getId(), VariableType.GERMPLASM_ATTRIBUTE.getId(),
				VariableType.GERMPLASM_PASSPORT.getId()))).thenReturn(plotVariables);

		when(this.datasetService
			.getMeasurementVariables(dataSet.getId(), Lists
				.newArrayList(VariableType.OBSERVATION_UNIT.getId(), VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId())))
			.thenReturn(datasetVariables);

		final Map<Integer, MeasurementVariable> sampleGenotypeVariablesMap = new HashedMap();
		sampleGenotypeVariablesMap.put(genotypeMarkerVariable.getTermId(), genotypeMarkerVariable);
		when(this.sampleGenotypeService.getSampleGenotypeVariables(any())).thenReturn(sampleGenotypeVariablesMap);

		final DatasetTypeDTO datasetType = new DatasetTypeDTO(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId(), "PLANT_SUBOBSERVATIONS");
		when(this.datasetTypeService.getDatasetTypeById(datasetType.getDatasetTypeId())).thenReturn(datasetType);

		ContextHolder.setCurrentProgram(PROGRAM_UUID);
		ContextHolder.setCurrentCrop("maize");
	}

	@Test
	public void testGenerateSingleInstanceFile() throws IOException {
		final String filename = "filename";
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceId(INSTANCE_ID);
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		datasetDTO.setDatasetId(INSTANCE_ID);
		datasetDTO.setParentDatasetId(INSTANCE_ID);

		final File
			file = this.datasetExcelGenerator
			.generateSingleInstanceFile(DatasetExcelGeneratorTest.STUDY_ID, datasetDTO, new ArrayList<>(),
				new ArrayList<>(), new HashMap<>(), filename, studyInstance);
		assertEquals(filename, file.getName());
		Mockito.verify(this.studyDataManager).getStudyDetails(INSTANCE_ID);
		Mockito.verify(this.datasetService)
			.getMeasurementVariables(DatasetExcelGeneratorTest.STUDY_ID, Lists.newArrayList(VariableType.STUDY_DETAIL.getId()));
		Mockito.verify(this.studyDataManager).getDataSetsByType(DatasetExcelGeneratorTest.STUDY_ID, DatasetTypeEnum.SUMMARY_DATA.getId());
		Mockito.verify(this.datasetService)
			.getMeasurementVariables(
				INSTANCE_ID, Lists
					.newArrayList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.ENVIRONMENT_CONDITION.getId()));
		Mockito.verify(this.datasetService).getMeasurementVariables(INSTANCE_ID, Lists
			.newArrayList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.TREATMENT_FACTOR.getId(),
				VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.ENTRY_DETAIL.getId(), VariableType.GERMPLASM_ATTRIBUTE.getId(),
				VariableType.GERMPLASM_PASSPORT.getId()));
		Mockito.verify(this.datasetService)
			.getMeasurementVariables(INSTANCE_ID, Lists
				.newArrayList(VariableType.OBSERVATION_UNIT.getId(), VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId()));
		Mockito.verify(this.studyDataManager).getPhenotypeByVariableId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGenerateMultiInstanceFile() {
		final String filename = "filename";
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceId(INSTANCE_ID);
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		datasetDTO.setDatasetId(INSTANCE_ID);
		datasetDTO.setParentDatasetId(INSTANCE_ID);
		this.datasetExcelGenerator
			.generateMultiInstanceFile(new HashMap<>(), new HashMap<>(), new ArrayList<>(), filename);
	}

	@Test
	public void testDescriptionSheet() throws IOException {
		final String filename = "filename";
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceId(INSTANCE_ID);
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		datasetDTO.setDatasetId(INSTANCE_ID);
		datasetDTO.setParentDatasetId(INSTANCE_ID);

		this.datasetExcelGenerator.setIncludeSampleGenotypeValues(true);
		final File
			file = this.datasetExcelGenerator
			.generateSingleInstanceFile(DatasetExcelGeneratorTest.STUDY_ID, datasetDTO, this.measurementVariables,
				this.observationUnitRows, new HashMap<>(), filename, studyInstance);

		final FileInputStream inputStream = new FileInputStream(file);
		final Workbook workbook = new HSSFWorkbook(inputStream);
		final Sheet descriptionSheet = workbook.getSheetAt(0);
		final Sheet observationSheet = workbook.getSheetAt(1);
		assertEquals(STUDY_DETAIL_TEST, descriptionSheet.getRow(8).getCell(valueIndex).getStringCellValue());
		assertEquals(EXPERIMENTAL_DESIGN_TEST, descriptionSheet.getRow(11).getCell(valueIndex).getStringCellValue());
		assertEquals(ENVIRONMENTAL_DETAILS_TEST, descriptionSheet.getRow(14).getCell(valueIndex).getStringCellValue());
		assertEquals(ENVIRONMENTAL_CONDITIONS_TEST, descriptionSheet.getRow(17).getCell(valueIndex).getStringCellValue());
		assertEquals(GERMPLASM_DESCRIPTORS_TEST, descriptionSheet.getRow(20).getCell(valueIndex).getStringCellValue());
		assertEquals(GERMPLASM_PASSPORTS_TEST, descriptionSheet.getRow(25).getCell(valueIndex).getStringCellValue());
		assertEquals(GERMPLASM_ATTRIBUTES_TEST, descriptionSheet.getRow(28).getCell(valueIndex).getStringCellValue());
		assertEquals(OBSERVATION_UNIT_TEST, descriptionSheet.getRow(33).getCell(valueIndex).getStringCellValue());
		assertEquals(TRAITS_TEST, descriptionSheet.getRow(36).getCell(valueIndex).getStringCellValue());
		assertEquals(SELECTION_TEST, descriptionSheet.getRow(39).getCell(valueIndex).getStringCellValue());
		assertEquals(GENOTYPE_MARKER_TEST, descriptionSheet.getRow(42).getCell(valueIndex).getStringCellValue());
		assertEquals(VARIABLE_ALIAS_1, observationSheet.getRow(0).getCell(0).getStringCellValue());
		assertEquals(VARIABLE_ALIAS_2, observationSheet.getRow(0).getCell(1).getStringCellValue());
		assertEquals(VARIABLE_VALUE_1, observationSheet.getRow(1).getCell(0).getStringCellValue());
		assertEquals(VARIABLE_VALUE_2, observationSheet.getRow(1).getCell(1).getStringCellValue());
	}

	@Test
	public void testGetEnvironmentalConditions() {

		final Random random = new Random();
		final int environmentDatasetId = random.nextInt(10);
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceId(random.nextInt(10));
		final int studyConditionTermid = 100;
		final String studyConditionValue = "99";

		final MeasurementVariable studyConditionVariable = new MeasurementVariable();
		final String variableName = "VariableName";
		studyConditionVariable.setTermId(studyConditionTermid);
		studyConditionVariable.setVariableType(VariableType.ENVIRONMENT_CONDITION);
		studyConditionVariable.setName(variableName);

		final MeasurementVariable environmentDetailVariable = new MeasurementVariable();
		environmentDetailVariable.setTermId(TermId.LOCATION_ID.getId());
		environmentDetailVariable.setVariableType(VariableType.ENVIRONMENT_DETAIL);
		environmentDetailVariable.setName("LOCATION_ID");

		final Map<Integer, String> environmentConditionMap = new HashMap<>();
		environmentConditionMap.put(studyConditionVariable.getTermId(), studyConditionValue);

		when(this.studyDataManager.getPhenotypeByVariableId(environmentDatasetId, studyInstance.getInstanceId()))
			.thenReturn(environmentConditionMap);
		final List<MeasurementVariable> environmentVariables = Arrays.asList(studyConditionVariable, environmentDetailVariable);

		final List<MeasurementVariable> result =
			this.datasetExcelGenerator.getEnvironmentalConditions(environmentDatasetId, environmentVariables, studyInstance);
		assertEquals(1, result.size());
		assertEquals(studyConditionTermid, result.get(0).getTermId());
		assertEquals(studyConditionValue, result.get(0).getValue());

	}

	@Test
	public void testGetEnvironmentalDetails() {

		final int environmentDatasetId = this.random.nextInt();
		final int instanceNumber = this.random.nextInt();
		final int instanceDbId = this.random.nextInt();
		final int locationId = this.random.nextInt();
		final String locationName = "Some Location";
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setLocationId(locationId);
		studyInstance.setLocationName(locationName);
		studyInstance.setInstanceNumber(instanceNumber);
		studyInstance.setInstanceId(instanceDbId);

		final MeasurementVariable trialInstanceVariable = new MeasurementVariable();
		trialInstanceVariable.setTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
		trialInstanceVariable.setVariableType(VariableType.ENVIRONMENT_DETAIL);
		trialInstanceVariable.setName("TRIAL_INSTANCE");

		final MeasurementVariable locationVariable = new MeasurementVariable();
		locationVariable.setTermId(TermId.LOCATION_ID.getId());
		locationVariable.setVariableType(VariableType.ENVIRONMENT_DETAIL);
		locationVariable.setName(TermId.LOCATION_ID.name());
		locationVariable.setAlias("LOCATION_NAME");

		final int someVariableTermId = this.random.nextInt();
		final String someVariableValue = "Value";
		final MeasurementVariable someVariable = new MeasurementVariable();
		someVariable.setTermId(someVariableTermId);
		someVariable.setVariableType(VariableType.ENVIRONMENT_DETAIL);
		someVariable.setName("SomeVariable");

		final StandardVariable standardVariable =
			StandardVariableTestDataInitializer.createStandardVariable(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME");
		when(this.ontologyDataManager.getStandardVariable(TermId.TRIAL_LOCATION.getId(), PROGRAM_UUID))
			.thenReturn(standardVariable);
		final Map<Integer, String> geoLocationMap = new HashMap<>();
		geoLocationMap.put(someVariableTermId, someVariableValue);
		when(this.studyDataManager.getGeolocationByInstanceId(environmentDatasetId, studyInstance.getInstanceId()))
			.thenReturn(geoLocationMap);

		final List<MeasurementVariable> result =
			this.datasetExcelGenerator
				.getEnvironmentalDetails(environmentDatasetId, Arrays.asList(trialInstanceVariable, locationVariable, someVariable),
					studyInstance);

		assertEquals(TermId.TRIAL_INSTANCE_FACTOR.getId(), result.get(0).getTermId());
		assertEquals(instanceNumber, Integer.valueOf(result.get(0).getValue()).intValue());
		assertEquals(TermId.LOCATION_ID.getId(), result.get(1).getTermId());
		assertEquals(locationId, Integer.valueOf(result.get(1).getValue()).intValue());
		assertEquals(TermId.TRIAL_LOCATION.getId(), result.get(2).getTermId());
		assertEquals(locationName, result.get(2).getValue());
		assertEquals(someVariableTermId, result.get(3).getTermId());
		assertEquals(someVariableValue, result.get(3).getValue());

	}

	@Test
	public void testCreateLocationNameVariable() {

		final StandardVariable standardVariable =
			StandardVariableTestDataInitializer.createStandardVariable(TermId.TRIAL_LOCATION.getId(), "LOCATION_NAME");
		when(this.ontologyDataManager.getStandardVariable(TermId.TRIAL_LOCATION.getId(), PROGRAM_UUID))
			.thenReturn(standardVariable);

		final MeasurementVariable result = this.datasetExcelGenerator.createLocationNameVariable("Alias", "Philippines");
		assertEquals("Alias", result.getAlias());
		assertEquals("LOCATION_NAME", result.getName());
		assertEquals("Philippines", result.getValue());
		assertEquals(standardVariable.getDescription(), result.getDescription());
		assertEquals(standardVariable.getProperty().getName(), result.getProperty());
		assertEquals(standardVariable.getScale().getName(), result.getScale());
		assertEquals(standardVariable.getMethod().getName(), result.getMethod());
		assertEquals(standardVariable.getDataType().getName(), result.getDataType());
		assertEquals(standardVariable.getDataType().getId(), result.getDataTypeId().intValue());
		assertEquals(PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0), result.getLabel());
		assertEquals(TermId.TRIAL_LOCATION.getId(), result.getTermId());
		assertEquals(PhenotypicType.TRIAL_ENVIRONMENT, result.getRole());
		assertEquals(VariableType.ENVIRONMENT_DETAIL, result.getVariableType());

	}

	@Test
	public void testOrderColumns() {
		final List<MeasurementVariable> columns = new ArrayList<>();
		final MeasurementVariable selectionVariable = new MeasurementVariable();
		selectionVariable.setTermId(1003);
		selectionVariable.setVariableType(VariableType.SELECTION_METHOD);
		selectionVariable.setName("SELECTION");
		columns.add(selectionVariable);

		final MeasurementVariable traitVariable = new MeasurementVariable();
		traitVariable.setTermId(1002);
		traitVariable.setVariableType(VariableType.TRAIT);
		traitVariable.setName("TRAIT");
		columns.add(traitVariable);

		final MeasurementVariable studyConditionVariable = new MeasurementVariable();
		studyConditionVariable.setTermId(1001);
		studyConditionVariable.setVariableType(VariableType.ENVIRONMENT_CONDITION);
		studyConditionVariable.setName("CONDITION");
		columns.add(studyConditionVariable);

		final MeasurementVariable observationUnitVariable = new MeasurementVariable();
		observationUnitVariable.setTermId(TermId.OBS_UNIT_ID.getId());
		observationUnitVariable.setVariableType(VariableType.EXPERIMENTAL_DESIGN);
		observationUnitVariable.setName("OBS_UNIT");
		columns.add(observationUnitVariable);

		final List<MeasurementVariable> orderedColumns = this.datasetExcelGenerator.orderColumns(columns);
		assertEquals(observationUnitVariable, orderedColumns.get(0));
		assertEquals(studyConditionVariable, orderedColumns.get(1));
		assertEquals(traitVariable, orderedColumns.get(2));
		assertEquals(selectionVariable, orderedColumns.get(3));

	}
}
