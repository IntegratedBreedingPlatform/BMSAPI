package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.VariableDatasetsDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.OverwriteDataException;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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

	private static final String TARGET_VARIABLE_NAME = "TARGET_VARIABLE";
	private static final String VARIABLE1_NAME = "VARIABLE1";
	private static final String VARIABLE2_NAME = "VARIABLE2";
	private static final String VARIABLE3_NAME = "VARIABLE3";
	private static final String VARIABLE4_NAME = "VARIABLE4";
	private static final String VARIABLE5_NAME = "VARIABLE5";
	private static final String VARIABLE6_NAME = "VARIABLE6";
	private static final String VARIABLE7_NAME = "VARIABLE7";
	private static final String VARIABLE8_NAME = "VARIABLE8";
	private static final String VARIABLE9_NAME = "VARIABLE9";

	private static final int TARGET_VARIABLE_TERMID = 321;
	private static final int VARIABLE1_TERMID = 123;
	private static final int VARIABLE2_TERMID = 456;
	private static final int VARIABLE3_TERMID = 789;
	private static final int VARIABLE4_TERMID = 20439;
	private static final int VARIABLE5_TERMID = 8630;
	private static final int VARIABLE6_TERMID = 8830;
	private static final int VARIABLE7_TERMID = 1111;
	private static final int VARIABLE8_TERMID = 2222;
	private static final int VARIABLE9_TERMID = 3333;

	private static final String TERM_VALUE_1 = "1000";
	private static final String TERM_VALUE_2 = "12.5";
	private static final String TERM_VALUE_3 = "10";
	private static final String DATE_TERM1_VALUE = "20180101";
	private static final String DATE_TERM2_VALUE = "20180101";
	private static final String TERM_VALUE_9 = "100";

	// TODO: When SUM function is already implemented, verify the aggregate functions and sub-observation values are evaluated properly.
	private static final String FORMULA = "({{" + VARIABLE1_TERMID + "}}/100)*((100-{{" + VARIABLE2_TERMID + "}})/(100-12.5))*(10/{{"
		+ VARIABLE3_TERMID + "}}) + fn:daysdiff({{" + VARIABLE5_TERMID + "}},{{" + VARIABLE6_TERMID + "}}) + fn:avg({{" + VARIABLE7_TERMID
		+ "}}) + {{" + VARIABLE9_TERMID + "}}";
	private static final String FORMULA_RESULT = "112";

	private static final int STUDY_ID = RandomUtils.nextInt();
	private static final int SUMMARY_DATASET_ID = RandomUtils.nextInt();
	private static final int DATASET_ID = RandomUtils.nextInt();
	private static final int OBSERVATION_UNIT_ID = RandomUtils.nextInt();
	private static final List<Integer> GEO_LOCATION_IDS = new ArrayList<>();
	private final Map<Integer, Integer> inputVariableDatasetMap = new HashMap<>();

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

	@Mock
	private DatasetTypeService datasetTypeService;

	private final ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();

	@InjectMocks
	private final DerivedVariableServiceImpl derivedVariableService = new DerivedVariableServiceImpl();

	private final DerivedVariableProcessor processor = new DerivedVariableProcessor();

	@Before
	public void init() {

		// We cannot mock resourceBundleMessageSource.getMessage because the method is marked as final.
		// So as a workaround, use a real class instance and set setUseCodeAsDefaultMessage to true
		this.resourceBundleMessageSource.setUseCodeAsDefaultMessage(true);
		this.derivedVariableService.setResourceBundleMessageSource(this.resourceBundleMessageSource);
		this.derivedVariableService.setProcessor(this.processor);

		GEO_LOCATION_IDS.add(RandomUtils.nextInt());

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap =
			this.createInstanceIdObservationUnitRowsMap(OBSERVATION_UNIT_ID);
		final Map<Integer, MeasurementVariable> measurementVariablesMap = this.createMeasurementVariablesMap();
		final Map<Integer, Map<String, List<Object>>> aggregateValuesFromSubObservation =
			this.createValuesFromSubObservationMap(OBSERVATION_UNIT_ID);
		final FormulaDto formula = this.createFormula(FORMULA);
		final List<Integer> subObservationDatasetTypeIds = Arrays.asList(1, 2, 3);
		final DatasetDTO summaryDataset = new DatasetDTO();
		summaryDataset.setDatasetId(SUMMARY_DATASET_ID);

		this.inputVariableDatasetMap.put(VARIABLE9_TERMID, SUMMARY_DATASET_ID);

		when(this.middlwareDatasetService.getDatasets(STUDY_ID, org.fest.util.Collections.set(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(Arrays.asList(summaryDataset));
		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);
		when(this.middlewareDerivedVariableService.createVariableIdMeasurementVariableMapInStudy(STUDY_ID))
			.thenReturn(measurementVariablesMap);
		when(this.formulaService.getByTargetId(TARGET_VARIABLE_TERMID)).thenReturn(Optional.of(formula));
		when(this.datasetTypeService.getSubObservationDatasetTypeIds()).thenReturn(subObservationDatasetTypeIds);
		when(this.middlewareDerivedVariableService.getValuesFromObservations(STUDY_ID, subObservationDatasetTypeIds,
			this.inputVariableDatasetMap)).thenReturn(aggregateValuesFromSubObservation);

	}

	@Test
	public void testExecute() {

		final Map<String, Object> result =
			this.derivedVariableService
				.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, this.inputVariableDatasetMap, true);

		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);
		verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
		verify(this.derivedVariableValidator).verifyInputVariablesArePresentInStudy(TARGET_VARIABLE_TERMID, DATASET_ID, STUDY_ID);
		verify(this.derivedVariableValidator)
			.validateForAggregateFunctions(TARGET_VARIABLE_TERMID, STUDY_ID, DATASET_ID,
				this.inputVariableDatasetMap);

		final ArgumentCaptor<String> captureValue = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> captureCategoricalId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> captureObservationUnitId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> captureObservationId = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<MeasurementVariable> captureTargetMeasurementVariable = ArgumentCaptor.forClass(MeasurementVariable.class);

		verify(this.middlewareDerivedVariableService).saveCalculatedResult(captureValue.capture(),
			captureCategoricalId.capture(), captureObservationUnitId.capture(), captureObservationId.capture(),
			captureTargetMeasurementVariable.capture());

		verify(this.middlwareDatasetService).updateDependentPhenotypesAsOutOfSync(TARGET_VARIABLE_TERMID, Sets.newHashSet(OBSERVATION_UNIT_ID));

		// TODO: When AVG and SUM functions are already implemented, verify the aggregate functions and sub-observation values are evaluated properly.
		assertTrue(result.isEmpty());
		assertEquals(FORMULA_RESULT, captureValue.getValue());
		assertNull(captureCategoricalId.getValue());
		assertNotNull(captureObservationUnitId.getValue());
		assertNotNull(captureObservationId.getValue());
		assertEquals(TARGET_VARIABLE_TERMID, captureTargetMeasurementVariable.getValue().getTermId());

	}

	@Test
	public void testExecuteHasDataToOverwriteButAvoidOverwriting() {

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap =
			this.createInstanceIdObservationUnitRowsMap(OBSERVATION_UNIT_ID);

		// Set a value to the target trait variable so that the system will detect that it needs to be overwritten.
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(TARGET_VARIABLE_NAME).setValue("100");

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);

		try {

			// Set overwriteExistingData to false so that the system will throw a runtime exception that will prevent
			// the calculated data from overwriting the existing data
			this.derivedVariableService
				.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, this.inputVariableDatasetMap, false);

			fail("Should throw OverwriteDataException");

		} catch (final OverwriteDataException e) {

			verify(this.studyValidator).validate(STUDY_ID, false);
			verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);
			verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
			verify(this.derivedVariableValidator).verifyInputVariablesArePresentInStudy(TARGET_VARIABLE_TERMID, DATASET_ID, STUDY_ID);
			verify(this.derivedVariableValidator)
				.validateForAggregateFunctions(TARGET_VARIABLE_TERMID, STUDY_ID, DATASET_ID,
					this.inputVariableDatasetMap);

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

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap =
			this.createInstanceIdObservationUnitRowsMap(OBSERVATION_UNIT_ID);

		// Set a value to the target trait variable so that the system will detect that it needs to be overwritten.
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(TARGET_VARIABLE_NAME).setValue("100");

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);

		final Map<String, Object> result =
			this.derivedVariableService
				.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, this.inputVariableDatasetMap, true);

		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);
		verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
		verify(this.derivedVariableValidator).verifyInputVariablesArePresentInStudy(TARGET_VARIABLE_TERMID, DATASET_ID, STUDY_ID);
		verify(this.derivedVariableValidator)
			.validateForAggregateFunctions(TARGET_VARIABLE_TERMID, STUDY_ID, DATASET_ID,
				this.inputVariableDatasetMap);

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

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap =
			this.createInstanceIdObservationUnitRowsMap(OBSERVATION_UNIT_ID);

		// Set a invalide date format value to the date type input variables
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(VARIABLE5_NAME).setValue("20202020");
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(VARIABLE6_NAME).setValue("20202020");

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);

		try {
			this.derivedVariableService
				.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, this.inputVariableDatasetMap, false);
			fail("Should throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {

			assertEquals(DerivedVariableServiceImpl.STUDY_EXECUTE_CALCULATION_PARSING_EXCEPTION, e.getErrors().get(0).getCode());
			verify(this.studyValidator).validate(STUDY_ID, false);
			verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);
			verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
			verify(this.derivedVariableValidator).verifyInputVariablesArePresentInStudy(TARGET_VARIABLE_TERMID, DATASET_ID, STUDY_ID);
			verify(this.derivedVariableValidator)
				.validateForAggregateFunctions(TARGET_VARIABLE_TERMID, STUDY_ID, DATASET_ID,
					this.inputVariableDatasetMap);
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
			this.derivedVariableService
				.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, this.inputVariableDatasetMap, false);
			fail("Should throw ApiRequestValidationException");
		} catch (final ApiRequestValidationException e) {

			assertEquals(DerivedVariableServiceImpl.STUDY_EXECUTE_CALCULATION_ENGINE_EXCEPTION, e.getErrors().get(0).getCode());
			verify(this.studyValidator).validate(STUDY_ID, false);
			verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);
			verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
			verify(this.derivedVariableValidator).verifyInputVariablesArePresentInStudy(TARGET_VARIABLE_TERMID, DATASET_ID, STUDY_ID);
			verify(this.derivedVariableValidator)
				.validateForAggregateFunctions(TARGET_VARIABLE_TERMID, STUDY_ID, DATASET_ID,
					this.inputVariableDatasetMap);
			verify(this.middlewareDerivedVariableService, times(0)).saveCalculatedResult(anyString(),
				anyInt(), anyInt(), anyInt(),
				any(MeasurementVariable.class));

		}

	}

	@Test
	public void testExecuteHasInputMissingData() {

		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap =
			this.createInstanceIdObservationUnitRowsMap(OBSERVATION_UNIT_ID);

		// Set empty values for input variables so that the system will detect it.
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(VARIABLE1_NAME).setValue("");
		instanceIdObservationUnitRowsMap.get(1).get(0).getVariables().get(VARIABLE2_NAME).setValue("");

		when(this.middlwareDatasetService.getInstanceIdToObservationUnitRowsMap(STUDY_ID, DATASET_ID, GEO_LOCATION_IDS))
			.thenReturn(instanceIdObservationUnitRowsMap);

		final Map<String, Object> result =
			this.derivedVariableService
				.execute(STUDY_ID, DATASET_ID, TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS, this.inputVariableDatasetMap, true);

		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);
		verify(this.derivedVariableValidator).validate(TARGET_VARIABLE_TERMID, GEO_LOCATION_IDS);
		verify(this.derivedVariableValidator).verifyInputVariablesArePresentInStudy(TARGET_VARIABLE_TERMID, DATASET_ID, STUDY_ID);
		verify(this.derivedVariableValidator)
			.validateForAggregateFunctions(TARGET_VARIABLE_TERMID, STUDY_ID, DATASET_ID,
				this.inputVariableDatasetMap);

		assertEquals(
			DerivedVariableServiceImpl.STUDY_EXECUTE_CALCULATION_MISSING_DATA,
			result.get(DerivedVariableServiceImpl.INPUT_MISSING_DATA_RESULT_KEY));

	}

	@Test
	public void testGetMissingFormulaVariablesInStudy() {

		final Set<FormulaVariable> expectedResult = new HashSet<>();
		expectedResult.add(new FormulaVariable());

		when(this.middlewareDerivedVariableService.getMissingFormulaVariablesInStudy(STUDY_ID, DATASET_ID, VARIABLE1_TERMID))
			.thenReturn(expectedResult);
		final Set<FormulaVariable> result =
			this.derivedVariableService.getMissingFormulaVariablesInStudy(STUDY_ID, DATASET_ID, VARIABLE1_TERMID);

		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);
		assertSame(expectedResult, result);

	}

	@Test
	public void testGetFormulaVariablesInStudy() {

		final Set<FormulaVariable> expectedResult = new HashSet<>();
		expectedResult.add(new FormulaVariable());

		when(this.middlewareDerivedVariableService.getFormulaVariablesInStudy(STUDY_ID, DATASET_ID)).thenReturn(expectedResult);
		final Set<FormulaVariable> result = this.derivedVariableService.getFormulaVariablesInStudy(STUDY_ID, DATASET_ID);
		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);

		assertSame(expectedResult, result);
	}

	@Test
	public void testCountCalculatedVariablesInDatasets() {
		final Set<Integer> datasetIds = new HashSet<>(Arrays.asList(DATASET_ID));
		when(this.middlewareDerivedVariableService.countCalculatedVariablesInDatasets(datasetIds)).thenReturn(1);
		final long result = this.derivedVariableService.countCalculatedVariablesInDatasets(STUDY_ID, datasetIds);
		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);
		assertEquals(1, result);
	}

	@Test
	public void testGetFormulaVariableDatasetMap() {

		final Map<Integer, VariableDatasetsDTO> expectedResult = new HashMap<>();

		when(this.middlewareDerivedVariableService.createVariableDatasetsMap(STUDY_ID, DATASET_ID, VARIABLE1_TERMID))
			.thenReturn(expectedResult);
		final Map<Integer, VariableDatasetsDTO> result =
			this.derivedVariableService.getFormulaVariableDatasetsMap(STUDY_ID, DATASET_ID, VARIABLE1_TERMID);
		verify(this.studyValidator).validate(STUDY_ID, false);
		verify(this.datasetValidator).validateDataset(STUDY_ID, DATASET_ID);

		assertSame(expectedResult, result);
	}

	@Test
	public void testFillWithSubObservationLevelValuesWithNoValues() throws ParseException {
		final DerivedVariableProcessor processor = Mockito.mock(DerivedVariableProcessor.class);
		this.derivedVariableService.setProcessor(processor);
		final int observationUnitId = 1;
		final Map<Integer, Map<String, List<Object>>> valuesFromSubObservation = new HashMap<>();
		final Map<String, List<Object>> valuesMap = new HashMap<>();

		valuesFromSubObservation.put(observationUnitId, valuesMap);
		final Map<Integer, MeasurementVariable> measurementVariablesMap = this.createMeasurementVariablesMap();
		final List<String> inputVariables = Collections.singletonList(DerivedVariableUtils.wrapTerm(String.valueOf(VARIABLE1_TERMID)));
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(inputVariables.get(0), null);

		this.derivedVariableService
			.fillWithSubObservationLevelValues(observationUnitId, valuesFromSubObservation, measurementVariablesMap, new HashSet<>(),
				parameters, inputVariables);
		final ArgumentCaptor<Map<String, List<Object>>> variableAggregateValuesMapCaptor = ArgumentCaptor.forClass(Map.class);
		Mockito.verify(processor).setData(variableAggregateValuesMapCaptor.capture());
		final Map<String, List<Object>> variableAggregateValuesMap = variableAggregateValuesMapCaptor.getValue();
		Assert.assertTrue(variableAggregateValuesMap.get(inputVariables.get(0)).isEmpty());
		Assert.assertFalse(parameters.containsKey(inputVariables.get(0)));
	}

	@Test
	public void testFillWithSubObservationLevelValues() throws ParseException {
		final DerivedVariableProcessor processor = Mockito.mock(DerivedVariableProcessor.class);
		this.derivedVariableService.setProcessor(processor);
		final int observationUnitId = 1;
		final Map<Integer, Map<String, List<Object>>> valuesFromSubObservation = new HashMap<>();
		final Map<String, List<Object>> valuesMap = new HashMap<>();
		valuesMap.put(String.valueOf(VARIABLE1_TERMID), Arrays.asList("1", "2", "3"));
		valuesFromSubObservation.put(observationUnitId, valuesMap);
		final Map<Integer, MeasurementVariable> measurementVariablesMap = this.createMeasurementVariablesMap();
		final List<String> inputVariables = Collections.singletonList(DerivedVariableUtils.wrapTerm(String.valueOf(VARIABLE1_TERMID)));
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(inputVariables.get(0), null);

		this.derivedVariableService
			.fillWithSubObservationLevelValues(observationUnitId, valuesFromSubObservation, measurementVariablesMap, new HashSet<>(),
				parameters, inputVariables);
		final ArgumentCaptor<Map<String, List<Object>>> variableAggregateValuesMapCaptor = ArgumentCaptor.forClass(Map.class);
		Mockito.verify(processor).setData(variableAggregateValuesMapCaptor.capture());
		final Map<String, List<Object>> variableAggregateValuesMap = variableAggregateValuesMapCaptor.getValue();
		Assert.assertEquals(3, variableAggregateValuesMap.get(inputVariables.get(0)).size());
		Assert.assertFalse(parameters.containsKey(inputVariables.get(0)));
	}

	@Test(expected = ParseException.class)
	public void testFillWithSubObservationLevelWithDateValues() throws ParseException {
		final DerivedVariableProcessor processor = Mockito.mock(DerivedVariableProcessor.class);
		this.derivedVariableService.setProcessor(processor);
		final int observationUnitId = 1;
		final Map<Integer, Map<String, List<Object>>> valuesFromSubObservation = new HashMap<>();
		final Map<String, List<Object>> valuesMap = new HashMap<>();
		valuesMap.put(String.valueOf(VARIABLE6_TERMID), Collections.singletonList("03/31/2018"));
		valuesFromSubObservation.put(observationUnitId, valuesMap);
		final Map<Integer, MeasurementVariable> measurementVariablesMap = this.createMeasurementVariablesMap();
		final List<String> inputVariables = Collections.singletonList(DerivedVariableUtils.wrapTerm(String.valueOf(VARIABLE6_TERMID)));
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(inputVariables.get(0), null);

		this.derivedVariableService
			.fillWithSubObservationLevelValues(observationUnitId, valuesFromSubObservation, measurementVariablesMap, new HashSet<>(),
				parameters, inputVariables);
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
		measurementVariablesMap.put(VARIABLE7_TERMID, this.createMeasurementVariable(VARIABLE7_TERMID, VARIABLE7_NAME,
			DataType.NUMERIC_VARIABLE));
		measurementVariablesMap.put(VARIABLE8_TERMID, this.createMeasurementVariable(VARIABLE8_TERMID, VARIABLE8_NAME,
			DataType.NUMERIC_VARIABLE));
		measurementVariablesMap.put(VARIABLE9_TERMID, this.createMeasurementVariable(VARIABLE9_TERMID, VARIABLE9_NAME,
			DataType.NUMERIC_VARIABLE));

		return measurementVariablesMap;

	}

	private MeasurementVariable createMeasurementVariable(final int VARIABLE_ID, final String variableName, final DataType dataType) {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(VARIABLE_ID);
		measurementVariable.setName(variableName);
		measurementVariable.setDataTypeId(dataType.getId());
		return measurementVariable;
	}

	private Map<Integer, List<ObservationUnitRow>> createInstanceIdObservationUnitRowsMap(final Integer observationUnitId) {
		final Map<Integer, List<ObservationUnitRow>> instanceIdObservationUnitRowsMap = new HashMap<>();
		instanceIdObservationUnitRowsMap.put(1, Arrays.asList(this.createObservationUnitRowTestData(observationUnitId)));
		return instanceIdObservationUnitRowsMap;
	}

	private Map<Integer, Map<String, List<Object>>> createValuesFromSubObservationMap(final Integer observationUnitId) {
		final Map<Integer, Map<String, List<Object>>> valuesFromSubObservationMap = new HashMap<>();
		final Map<String, List<Object>> valuesPerVariable = new HashMap<>();
		valuesPerVariable.put(String.valueOf(VARIABLE7_TERMID), Arrays.asList("1", "2", "3"));
		valuesPerVariable.put(String.valueOf(VARIABLE8_TERMID), Arrays.asList("4", "5", "6"));
		valuesFromSubObservationMap.put(observationUnitId, valuesPerVariable);
		return valuesFromSubObservationMap;
	}

	private ObservationUnitRow createObservationUnitRowTestData(final Integer observationUnitId) {
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(observationUnitId);
		observationUnitRow.setVariables(this.createObservationUnitDataTestData());

		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(VARIABLE9_NAME, this.createObservationUnitDataTestData(VARIABLE9_TERMID, TERM_VALUE_9, null));
		observationUnitRow.setEnvironmentVariables(observationUnitDataMap);
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
