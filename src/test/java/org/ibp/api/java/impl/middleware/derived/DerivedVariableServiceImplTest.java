package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.OverwriteDataException;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DerivedVariableServiceImplTest {

	public static final String TARGET_VARIABLE_NAME = "TARGET_VARIABLE";
	public static final String VARIABLE1_NAME = "VARIABLE1";
	public static final String VARIABLE2_NAME = "VARIABLE2";
	public static final String VARIABLE3_NAME = "VARIABLE3";
	public static final String VARIABLE4_NAME = "VARIABLE4";
	public static final String VARIABLE5_NAME = "VARIABLE5";
	public static final String VARIABLE6_NAME = "VARIABLE6";

	public static final int TARGET_VARIABLE_TERMID = 321;
	public static final int VARIABLE1_TERMID = 123;
	public static final int VARIABLE2_TERMID = 456;
	public static final int VARIABLE3_TERMID = 789;
	public static final int VARIABLE4_TERMID = 20439;
	public static final int VARIABLE5_TERMID = 8630;
	public static final int VARIABLE6_TERMID = 8830;

	private static final String TERM_VALUE_1 = "1000";
	private static final String TERM_VALUE_2 = "12.5";
	private static final String TERM_VALUE_3 = "10";
	private static final String DATE_TERM1_VALUE = "20180101";
	private static final String DATE_TERM2_VALUE = "20180101";

	private static final String FORMULA = "({{" + VARIABLE1_TERMID + "}}/100)*((100-{{" + VARIABLE2_TERMID + "}})/(100-12.5))*(10/{{"
		+ VARIABLE3_TERMID + "}}) + fn:daysdiff({{" + VARIABLE5_TERMID + "}},{{" + VARIABLE6_TERMID + "}})";
	private static final String FORMULA_RESULT = "10";

	public static final int STUDY_ID = RandomUtils.nextInt();
	public static final int DATASET_ID = RandomUtils.nextInt();
	public static final List<Integer> GEO_LOCATION_IDS = new ArrayList<>();

	@Mock
	private DatasetService middlwareDatasetService;

	@Mock
	private org.generationcp.middleware.service.api.derived_variables.DerivedVariableService middlewareDerivedVariableService;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private DerivedVariableValidator derivedVariableValidator;

	@Mock
	private FormulaService formulaService;

	private ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();

	@InjectMocks
	private final DerivedVariableServiceImpl derivedVariableService = new DerivedVariableServiceImpl();

	private final DerivedVariableProcessor processor = new DerivedVariableProcessor();

	@Before
	public void init() {

		// We cannot mock resourceBundleMessageSource.getMessage because the method is marked as final.
		// So as a workaround, use a real class instance and set setUseCodeAsDefaultMessage to true
		resourceBundleMessageSource.setUseCodeAsDefaultMessage(true);
		derivedVariableService.setResourceBundleMessageSource(resourceBundleMessageSource);
		derivedVariableService.setProcessor(processor);

		GEO_LOCATION_IDS.add(RandomUtils.nextInt());

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap = this.createInstanceIdObservationUnitRowsMap();
		final Map<Integer, MeasurementVariable> measurementVariablesMap = this.createMeasurementVariablesMap();
		final FormulaDto formula = this.createFormula(FORMULA);

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);
		when(this.middlewareDerivedVariableService.createVariableIdMeasurementVariableMap(DATASET_ID))
			.thenReturn(measurementVariablesMap);
		when(this.formulaService.getByTargetId(TARGET_VARIABLE_TERMID)).thenReturn(Optional.of(formula));

	}

	@Test
	public void testExecute() {

		final Map<String, Object> result =
			this.derivedVariableService.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, true);

		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID, true);
		verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
		verify(this.derivedVariableValidator).verifyMissingInputVariables(TARGET_VARIABLE_TERMID, DATASET_ID);

		final ArgumentCaptor<String> captureValue = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> captureCategoricalId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> captureObservationUnitId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> captureObservationId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<MeasurementVariable> captureTargetMeasurementVariable = ArgumentCaptor.forClass(MeasurementVariable.class);

		verify(this.middlewareDerivedVariableService).saveCalculatedResult(captureValue.capture(),
			captureCategoricalId.capture(), captureObservationUnitId.capture(), captureObservationId.capture(),
			captureTargetMeasurementVariable.capture());

		assertTrue(result.isEmpty());
		assertEquals(FORMULA_RESULT, captureValue.getValue());
		assertNull(captureCategoricalId.getValue());
		assertNotNull(captureObservationUnitId.getValue());
		assertNotNull(captureObservationId.getValue());
		assertEquals(TARGET_VARIABLE_TERMID, captureTargetMeasurementVariable.getValue().getTermId());

	}

	@Test
	public void testExecuteHasDataToOverwriteButAvoidOverwriting() {

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap = this.createInstanceIdObservationUnitRowsMap();

		// Set a value to the target trait variable so that the system will detect that it needs to be overwritten.
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(TARGET_VARIABLE_NAME).setValue("100");

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);

		try {

			// Set overwriteExistingData to false so that the system will throw a runtime exception that will prevent
			// the calculated data from overwriting the existing data
			this.derivedVariableService.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, false);

			fail("Should throw OverwriteDataException");

		} catch (final OverwriteDataException e) {

			verify(this.studyValidator).validate(STUDY_ID, false);
			verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID, true);
			verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
			verify(this.derivedVariableValidator).verifyMissingInputVariables(TARGET_VARIABLE_TERMID, DATASET_ID);

			final ArgumentCaptor<String> captureValue = ArgumentCaptor.forClass(String.class);
			final ArgumentCaptor<Integer> captureCategoricalId = ArgumentCaptor.forClass(Integer.class);
			final ArgumentCaptor<Integer> captureObservationUnitId = ArgumentCaptor.forClass(Integer.class);
			final ArgumentCaptor<Integer> captureObservationId = ArgumentCaptor.forClass(Integer.class);
			final ArgumentCaptor<MeasurementVariable> captureTargetMeasurementVariable = ArgumentCaptor.forClass(MeasurementVariable.class);

			verify(this.middlewareDerivedVariableService).saveCalculatedResult(captureValue.capture(),
				captureCategoricalId.capture(), captureObservationUnitId.capture(), captureObservationId.capture(),
				captureTargetMeasurementVariable.capture());

		}

	}

	@Test
	public void testExecuteHasDataToOverwrite() {

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap = this.createInstanceIdObservationUnitRowsMap();

		// Set a value to the target trait variable so that the system will detect that it needs to be overwritten.
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(TARGET_VARIABLE_NAME).setValue("100");

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);

		final Map<String, Object> result =
			this.derivedVariableService.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, true);

		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID, true);
		verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
		verify(this.derivedVariableValidator).verifyMissingInputVariables(TARGET_VARIABLE_TERMID, DATASET_ID);

		final ArgumentCaptor<String> captureValue = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> captureCategoricalId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> captureObservationUnitId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> captureObservationId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<MeasurementVariable> captureTargetMeasurementVariable = ArgumentCaptor.forClass(MeasurementVariable.class);

		verify(this.middlewareDerivedVariableService).saveCalculatedResult(captureValue.capture(),
			captureCategoricalId.capture(), captureObservationUnitId.capture(), captureObservationId.capture(),
			captureTargetMeasurementVariable.capture());

		assertTrue((boolean) result.get(DerivedVariableServiceImpl.HAS_DATA_OVERWRITE_RESULT_KEY));
		assertEquals(FORMULA_RESULT, captureValue.getValue());
		assertNull(captureCategoricalId.getValue());
		assertNotNull(captureObservationUnitId.getValue());
		assertNotNull(captureObservationId.getValue());
		assertEquals(TARGET_VARIABLE_TERMID, captureTargetMeasurementVariable.getValue().getTermId());

	}

	@Test
	public void testExecuteParseDateError() {

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap = this.createInstanceIdObservationUnitRowsMap();

		// Set a invalide date format value to the date type input variables
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(VARIABLE5_NAME).setValue("20202020");
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(VARIABLE6_NAME).setValue("20202020");

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);

		try {
			this.derivedVariableService.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, false);
			fail("Should throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {

			assertEquals(DerivedVariableServiceImpl.STUDY_EXECUTE_CALCULATION_PARSING_EXCEPTION, e.getErrors().get(0).getCode());
			verify(this.studyValidator).validate(STUDY_ID, false);
			verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID, true);
			verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
			verify(this.derivedVariableValidator).verifyMissingInputVariables(TARGET_VARIABLE_TERMID, DATASET_ID);
			verify(this.middlewareDerivedVariableService, times(0)).saveCalculatedResult(anyString(),
				anyInt(), anyInt(), anyInt(),
				any(MeasurementVariable.class));

		}

	}

	@Test
	public void testExecuteInvalidFormula() {

		final FormulaDto invalidFormula = this.createFormula(FORMULA + "+++++");
		when(this.formulaService.getByTargetId(TARGET_VARIABLE_TERMID)).thenReturn(Optional.of(invalidFormula));

		try {
			this.derivedVariableService.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, false);
			fail("Should throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {

			assertEquals(DerivedVariableServiceImpl.STUDY_EXECUTE_CALCULATION_ENGINE_EXCEPTION, e.getErrors().get(0).getCode());
			verify(this.studyValidator).validate(STUDY_ID, false);
			verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID, true);
			verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
			verify(this.derivedVariableValidator).verifyMissingInputVariables(TARGET_VARIABLE_TERMID, DATASET_ID);
			verify(this.middlewareDerivedVariableService, times(0)).saveCalculatedResult(anyString(),
				anyInt(), anyInt(), anyInt(),
				any(MeasurementVariable.class));

		}

	}

	@Test
	public void testExecuteHasInputMissingData() {

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap = this.createInstanceIdObservationUnitRowsMap();

		// Set empty values for input variables so that the system will detect it.
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(VARIABLE1_NAME).setValue("");
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(VARIABLE2_NAME).setValue("");

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);

		final Map<String, Object> result =
			this.derivedVariableService.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, true);

		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID, true);
		verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
		verify(this.derivedVariableValidator).verifyMissingInputVariables(TARGET_VARIABLE_TERMID, DATASET_ID);

		assertEquals(
			DerivedVariableServiceImpl.STUDY_EXECUTE_CALCULATION_MISSING_DATA,
			result.get(DerivedVariableServiceImpl.INPUT_MISSING_DATA_RESULT_KEY));

	}

	private FormulaDto createFormula(final String formula) {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setInputs(this.createFormulaVariables());
		formulaDto.setDefinition(formula);

		final FormulaVariable targetVariable = new FormulaVariable();
		targetVariable.setName(TARGET_VARIABLE_NAME);
		targetVariable.setId(TARGET_VARIABLE_TERMID);
		formulaDto.setTarget(targetVariable);
		return formulaDto;
	}

	private List<FormulaVariable> createFormulaVariables() {

		final List<FormulaVariable> formulaVariables = new ArrayList<>();

		final FormulaVariable formulaVariable1 = new FormulaVariable();
		formulaVariable1.setId(VARIABLE3_TERMID);
		formulaVariable1.setName(VARIABLE3_NAME);
		formulaVariable1.setTargetTermId(VARIABLE1_TERMID);

		final FormulaVariable formulaVariable2 = new FormulaVariable();
		formulaVariable2.setId(VARIABLE4_TERMID);
		formulaVariable2.setName(VARIABLE4_NAME);
		formulaVariable2.setTargetTermId(VARIABLE2_TERMID);

		formulaVariables.add(formulaVariable1);
		formulaVariables.add(formulaVariable2);

		return formulaVariables;

	}

	private Map<Integer, MeasurementVariable> createMeasurementVariablesMap() {

		final Map<Integer, MeasurementVariable> measurementVariablesMap = new HashMap<>();
		measurementVariablesMap.put(TARGET_VARIABLE_TERMID, this.createMeasurementVariable(TARGET_VARIABLE_TERMID, TARGET_VARIABLE_NAME,
			DataType.NUMERIC_VARIABLE));
		measurementVariablesMap.put(VARIABLE1_TERMID, this.createMeasurementVariable(VARIABLE1_TERMID, VARIABLE1_NAME,
			DataType.NUMERIC_VARIABLE));
		measurementVariablesMap.put(VARIABLE2_TERMID, this.createMeasurementVariable(VARIABLE2_TERMID, VARIABLE2_NAME,
			DataType.NUMERIC_VARIABLE));
		measurementVariablesMap.put(VARIABLE3_TERMID, this.createMeasurementVariable(VARIABLE3_TERMID, VARIABLE3_NAME,
			DataType.NUMERIC_VARIABLE));
		measurementVariablesMap.put(VARIABLE5_TERMID, this.createMeasurementVariable(VARIABLE5_TERMID, VARIABLE5_NAME,
			DataType.DATE_TIME_VARIABLE));
		measurementVariablesMap.put(VARIABLE6_TERMID, this.createMeasurementVariable(VARIABLE6_TERMID, VARIABLE6_NAME,
			DataType.DATE_TIME_VARIABLE));

		return measurementVariablesMap;

	}

	private MeasurementVariable createMeasurementVariable(final int VARIABLE_ID, final String variableName, final DataType dataType) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(VARIABLE_ID);
		measurementVariable.setName(variableName);
		measurementVariable.setDataTypeId(dataType.getId());
		return measurementVariable;
	}

	private Map<Integer, List<ObservationUnitRow>> createInstanceIdObservationUnitRowsMap() {
		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap = new HashMap<>();
		instanceIdObservationUnitRowsMap.put(1, Arrays.asList(this.createObservationUnitRowTestData(1)));
		return instanceIdObservationUnitRowsMap;
	}

	private ObservationUnitRow createObservationUnitRowTestData(final Integer observationUnitId) {
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(observationUnitId);
		observationUnitRow.setVariables(this.createObservationUnitDataTestData());
		return observationUnitRow;
	}

	private Map<String, ObservationUnitData> createObservationUnitDataTestData() {
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(TARGET_VARIABLE_NAME, this.createObservationUnitDataTestData(TARGET_VARIABLE_TERMID, null, null));
		observationUnitDataMap.put(VARIABLE1_NAME, this.createObservationUnitDataTestData(VARIABLE1_TERMID, TERM_VALUE_1, null));
		observationUnitDataMap.put(VARIABLE2_NAME, this.createObservationUnitDataTestData(VARIABLE2_TERMID, TERM_VALUE_2, null));
		observationUnitDataMap.put(VARIABLE3_NAME, this.createObservationUnitDataTestData(VARIABLE3_TERMID, TERM_VALUE_3, null));
		observationUnitDataMap.put(VARIABLE4_NAME, this.createObservationUnitDataTestData(VARIABLE4_TERMID, "", null));
		observationUnitDataMap.put(VARIABLE5_NAME, this.createObservationUnitDataTestData(VARIABLE5_TERMID, DATE_TERM1_VALUE, null));
		observationUnitDataMap.put(VARIABLE6_NAME, this.createObservationUnitDataTestData(VARIABLE6_TERMID, DATE_TERM2_VALUE, null));
		return observationUnitDataMap;
	}

	private ObservationUnitData createObservationUnitDataTestData(final Integer VARIABLE_ID, final String value, final Integer cValueId) {
		final ObservationUnitData observationUnitData = new ObservationUnitData();
		observationUnitData.setVariableId(VARIABLE_ID);
		observationUnitData.setValue(value);
		observationUnitData.setCategoricalValueId(cValueId);
		observationUnitData.setObservationId(RandomUtils.nextInt());
		return observationUnitData;
	}

}
