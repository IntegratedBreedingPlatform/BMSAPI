package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetService;
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
import org.springframework.context.support.ResourceBundleMessageSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DatasetExcelGeneratorTest {

	public static final int INSTANCE_DB_ID = 1;
	private static final Integer STUDY_ID = 1;
	private static final Integer ENVIRONMENT_DATASET_ID = 1;
	private static final Integer PLOT_DATASET_ID = 1;
	public static final String STUDY_DETAIL_TEST = "StudyDetailTest";
	public static final String EXPERIMENTAL_DESIGN_TEST = "ExperimentalDesignTest";
	public static final String ENVIRONMENTAL_DETAILS_TEST = "EnvironmentalDesignTest";
	public static final String ENVIRONMENTAL_CONDITIONS_TEST = "EnvironmentalConditionsTest";
	public static final String GERMPLASM_DESCRIPTORS_TEST = "GermplasmDescriptorsTest";
	public static final String OBSERVATION_UNIT_TEST = "ObservationUnitTest";
	public static final String TRAITS_TEST = "TraitsTest";
	public static final String SELECTION_TEST = "SelectionTest";
	private static final String VARIABLE_NAME_1 = "VARIABLE_NAME_1";
	private static final String VARIABLE_NAME_2 = "VARIABLE_NAME_2";
	private static final String VARIABLE_VALUE_1 = "VARIABLE_VALUE_1";
	private static final String VARIABLE_VALUE_2 = "VARIABLE_VALUE_2";
	public static final int valueIndex = 7;

	private List<ObservationUnitRow> observationUnitRows;

	private List<MeasurementVariable> measurementVariables;

	private ResourceBundleMessageSource messageSource;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetService datasetService;

	@InjectMocks
	private DatasetExcelGenerator datasetExcelGenerator;

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
		environmentConditionsVariable.setDataTypeId(VariableType.TRAIT.getId());
		environmentConditionsVariable.setValue(ENVIRONMENTAL_CONDITIONS_TEST);
		environmentConditionsVariable.setVariableType(VariableType.TRAIT);
		environmentConditionsVariable.setTermId(1234);
		final Map<Integer, String> environmentConditionMap = new HashMap<>();
		environmentConditionMap.put(1234, ENVIRONMENTAL_CONDITIONS_TEST);

		final MeasurementVariable germplasmDescriptorVariable = new MeasurementVariable();
		germplasmDescriptorVariable.setDataTypeId(VariableType.GERMPLASM_DESCRIPTOR.getId());
		germplasmDescriptorVariable.setValue(GERMPLASM_DESCRIPTORS_TEST);
		germplasmDescriptorVariable.setVariableType(VariableType.GERMPLASM_DESCRIPTOR);

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

		final List<MeasurementVariable> studyDetailVariables = Lists.newArrayList(studyDetailVariable);
		final List<MeasurementVariable> environmentVariables =
			Lists.newArrayList(environmentDetailsVariable, experimentalDesignVariable, environmentConditionsVariable);
		final List<MeasurementVariable> plotVariables = Lists.newArrayList(germplasmDescriptorVariable);
		final List<MeasurementVariable> datasetVariables = Lists.newArrayList(observationUnitVariable, traitsVariable, selectionVariable);

		this.messageSource = new ResourceBundleMessageSource();
		this.messageSource.setUseCodeAsDefaultMessage(true);
		this.datasetExcelGenerator.setMessageSource(this.messageSource);
		final DataSet dataSet = new DataSet();
		dataSet.setId(DatasetExcelGeneratorTest.STUDY_ID);
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(StudyTypeDto.getTrialDto());
		final ObservationUnitData observationUnitData1 = new ObservationUnitData();
		observationUnitData1.setValue(this.VARIABLE_VALUE_1);
		final ObservationUnitData observationUnitData2 = new ObservationUnitData();
		observationUnitData2.setValue(this.VARIABLE_VALUE_2);
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> variables = new HashMap<>();
		variables.put(this.VARIABLE_NAME_1, observationUnitData1);
		variables.put(this.VARIABLE_NAME_2, observationUnitData2);
		observationUnitRow.setVariables(variables);
		this.observationUnitRows = Arrays.asList(observationUnitRow);

		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		measurementVariable1.setAlias(this.VARIABLE_NAME_1);
		measurementVariable1.setName(this.VARIABLE_NAME_1);
		measurementVariable1.setVariableType(VariableType.TRAIT);
		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		measurementVariable2.setAlias(this.VARIABLE_NAME_2);
		measurementVariable2.setName(this.VARIABLE_NAME_2);
		measurementVariable2.setVariableType(VariableType.TRAIT);
		this.measurementVariables = Arrays.asList(measurementVariable1, measurementVariable2);

		this.measurementVariables = Arrays.asList(measurementVariable1, measurementVariable2);
		Mockito.when(this.studyDataManager.getStudyDetails(DatasetExcelGeneratorTest.STUDY_ID)).thenReturn(studyDetails);
		Mockito.when(this.studyDataManager.getDataSetsByType(DatasetExcelGeneratorTest.STUDY_ID, DataSetType.SUMMARY_DATA))
			.thenReturn(Arrays.asList(dataSet));
		Mockito.when(this.datasetService
			.getMeasurementVariables(DatasetExcelGeneratorTest.ENVIRONMENT_DATASET_ID, Lists
				.newArrayList(VariableType.STUDY_DETAIL.getId()))).thenReturn(studyDetailVariables);
		Mockito.when(this.datasetService
			.getMeasurementVariables(DatasetExcelGeneratorTest.ENVIRONMENT_DATASET_ID, Lists
				.newArrayList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
					VariableType.STUDY_CONDITION.getId(), VariableType.TRAIT.getId()))).thenReturn(environmentVariables);
		Mockito.when(this.studyDataManager.getPhenotypeByVariableId(DatasetExcelGeneratorTest.ENVIRONMENT_DATASET_ID, INSTANCE_DB_ID))
			.thenReturn(environmentConditionMap);

		Mockito.when(this.datasetService.getMeasurementVariables(PLOT_DATASET_ID, Lists
			.newArrayList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.TREATMENT_FACTOR.getId(),
				VariableType.GERMPLASM_DESCRIPTOR.getId()))).thenReturn(plotVariables);

		Mockito.when(this.datasetService
			.getMeasurementVariables(dataSet.getId(), Lists
				.newArrayList(VariableType.OBSERVATION_UNIT.getId(), VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId())))
			.thenReturn(datasetVariables);
	}

	@Test
	public void testGenerateSingleInstanceFile() throws IOException {
		final String filename = "filename";
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceDbId(INSTANCE_DB_ID);
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetTypeId(DataSetType.PLANT_SUBOBSERVATIONS.getId());
		datasetDTO.setDatasetId(INSTANCE_DB_ID);
		datasetDTO.setParentDatasetId(INSTANCE_DB_ID);
		final File
			file = this.datasetExcelGenerator
			.generateSingleInstanceFile(DatasetExcelGeneratorTest.STUDY_ID, datasetDTO, new ArrayList<MeasurementVariable>(),
				new ArrayList<ObservationUnitRow>(), filename, studyInstance);
		Assert.assertEquals(filename, file.getName());
		Mockito.verify(this.studyDataManager).getStudyDetails(INSTANCE_DB_ID);
		Mockito.verify(this.datasetService)
			.getMeasurementVariables(DatasetExcelGeneratorTest.STUDY_ID, Lists.newArrayList(VariableType.STUDY_DETAIL.getId()));
		Mockito.verify(this.studyDataManager).getDataSetsByType(DatasetExcelGeneratorTest.STUDY_ID, DataSetType.SUMMARY_DATA);
		Mockito.verify(this.datasetService)
			.getMeasurementVariables(
				INSTANCE_DB_ID, Lists
					.newArrayList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.STUDY_CONDITION.getId(), VariableType.TRAIT.getId()));
		Mockito.verify(this.datasetService).getMeasurementVariables(INSTANCE_DB_ID, Lists
			.newArrayList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.TREATMENT_FACTOR.getId(),
				VariableType.GERMPLASM_DESCRIPTOR.getId()));
		Mockito.verify(this.datasetService)
			.getMeasurementVariables(INSTANCE_DB_ID, Lists
				.newArrayList(VariableType.OBSERVATION_UNIT.getId(), VariableType.TRAIT.getId(), VariableType.SELECTION_METHOD.getId()));
		Mockito.verify(this.studyDataManager).getPhenotypeByVariableId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
		Mockito.verify(this.studyDataManager).getGeolocationByVariableId(INSTANCE_DB_ID, INSTANCE_DB_ID);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGenerateMultiInstanceFile() throws IOException {
		final String filename = "filename";
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceDbId(INSTANCE_DB_ID);
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetTypeId(DataSetType.PLANT_SUBOBSERVATIONS.getId());
		datasetDTO.setDatasetId(INSTANCE_DB_ID);
		datasetDTO.setParentDatasetId(INSTANCE_DB_ID);
		this.datasetExcelGenerator
			.generateMultiInstanceFile(new HashMap<Integer, List<ObservationUnitRow>>(), new ArrayList<MeasurementVariable>(), filename);
	}

	@Test
	public void testDescriptionSheet() throws IOException {
		final String filename = "filename";
		final StudyInstance studyInstance = new StudyInstance();
		studyInstance.setInstanceDbId(INSTANCE_DB_ID);
		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetTypeId(DataSetType.PLANT_SUBOBSERVATIONS.getId());
		datasetDTO.setDatasetId(INSTANCE_DB_ID);
		datasetDTO.setParentDatasetId(INSTANCE_DB_ID);

		final File
			file = this.datasetExcelGenerator
			.generateSingleInstanceFile(DatasetExcelGeneratorTest.STUDY_ID, datasetDTO, this.measurementVariables,
				this.observationUnitRows, filename, studyInstance);

		final FileInputStream inputStream = new FileInputStream(file);
		final Workbook workbook = new HSSFWorkbook(inputStream);
		final Sheet descriptionSheet = workbook.getSheetAt(0);
		final Sheet observationSheet = workbook.getSheetAt(1);
		Assert.assertEquals(descriptionSheet.getRow(8).getCell(valueIndex).getStringCellValue(), STUDY_DETAIL_TEST);
		Assert.assertEquals(descriptionSheet.getRow(11).getCell(valueIndex).getStringCellValue(), EXPERIMENTAL_DESIGN_TEST);
		Assert.assertEquals(descriptionSheet.getRow(14).getCell(valueIndex).getStringCellValue(), ENVIRONMENTAL_DETAILS_TEST);
		Assert.assertEquals(descriptionSheet.getRow(17).getCell(valueIndex).getStringCellValue(), ENVIRONMENTAL_CONDITIONS_TEST);
		Assert.assertEquals(descriptionSheet.getRow(20).getCell(valueIndex).getStringCellValue(), GERMPLASM_DESCRIPTORS_TEST);
		Assert.assertEquals(descriptionSheet.getRow(23).getCell(valueIndex).getStringCellValue(), OBSERVATION_UNIT_TEST);
		Assert.assertEquals(descriptionSheet.getRow(26).getCell(valueIndex).getStringCellValue(), TRAITS_TEST);
		Assert.assertEquals(descriptionSheet.getRow(29).getCell(valueIndex).getStringCellValue(), SELECTION_TEST);
		Assert.assertEquals(observationSheet.getRow(0).getCell(0).getStringCellValue(), VARIABLE_NAME_1);
		Assert.assertEquals(observationSheet.getRow(0).getCell(1).getStringCellValue(), VARIABLE_NAME_2);
		Assert.assertEquals(observationSheet.getRow(1).getCell(0).getStringCellValue(), VARIABLE_VALUE_1);
		Assert.assertEquals(observationSheet.getRow(1).getCell(1).getStringCellValue(), VARIABLE_VALUE_2);
	}
}
