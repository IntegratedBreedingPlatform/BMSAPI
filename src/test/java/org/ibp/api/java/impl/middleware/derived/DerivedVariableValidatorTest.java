package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang.math.RandomUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.derived_variables.DerivedVariableService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DerivedVariableValidatorTest {

	private static final Integer VARIABLE_ID = 29001;
	private static final Integer STUDY_ID = 1001;
	private static final Integer DATASET_ID = 1002;
	private static final Integer SUBOBS_ID = 1003;
	private static final List<Integer> SUBOBS_DATASET_TYPE_IDS = Arrays.asList(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId(),
		DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId(), DatasetTypeEnum.TIME_SERIES_SUBOBSERVATIONS.getId(),
		DatasetTypeEnum.CUSTOM_SUBOBSERVATIONS.getId());


	@Mock
	private FormulaService formulaService;

	@Mock
	private DatasetService datasetService;

	@Mock
	private DatasetTypeService datasetTypeService;

	@Mock
	private DerivedVariableService middlewareDerivedVariableService;

	@InjectMocks
	private final DerivedVariableValidator variableValidator = new DerivedVariableValidator();

	@Before
	public void setUo() {
		Mockito.when(this.datasetTypeService.getSubObservationDatasetTypeIds()).
			thenReturn(SUBOBS_DATASET_TYPE_IDS);
		Mockito.when(this.formulaService.getByTargetId(VARIABLE_ID)).thenReturn(Optional.of(this.createFormulaDTO()));
	}

	@Test
	public void testValidateInvalidRequest() {
		try {
			this.variableValidator.validate(null, null);
			fail("Method should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals(DerivedVariableValidator.STUDY_EXECUTE_CALCULATION_INVALID_REQUEST, e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateFormulaNotFound() {
		final Integer variableId = RandomUtils.nextInt();
		final List<Integer> geoLocationIds = Arrays.asList(RandomUtils.nextInt());
		when(this.formulaService.getByTargetId(variableId)).thenReturn(Optional.<FormulaDto>absent());

		try {
			this.variableValidator.validate(variableId, geoLocationIds);
			fail("Method should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals(DerivedVariableValidator.STUDY_EXECUTE_CALCULATION_FORMULA_NOT_FOUND, e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testValidateSuccess() {
		final Integer variableId = RandomUtils.nextInt();
		final List<Integer> geoLocationIds = Arrays.asList(RandomUtils.nextInt());
		when(this.formulaService.getByTargetId(variableId)).thenReturn(Optional.of(new FormulaDto()));

		try {
			this.variableValidator.validate(variableId, geoLocationIds);
		} catch (final ApiRequestValidationException e) {
			fail("Method should not throw an exception");
		}
	}

	@Test
	public void testVerifyMissingInputVariablesVariablesAreNotPresentInADataset() {

		final int studyId = RandomUtils.nextInt();
		final int variableId = RandomUtils.nextInt();
		final int datasetId = RandomUtils.nextInt();
		final FormulaDto formulaDto = new FormulaDto();

		// Create two input variables for the target variable.
		final FormulaVariable targetVariable = new FormulaVariable();
		targetVariable.setId(variableId);
		final FormulaVariable formulaVariable1 = new FormulaVariable();
		formulaVariable1.setId(RandomUtils.nextInt());
		final FormulaVariable formulaVariable2 = new FormulaVariable();
		formulaVariable2.setId(RandomUtils.nextInt());
		formulaDto.setInputs(Arrays.asList(formulaVariable1, formulaVariable2));
		formulaDto.setTarget(targetVariable);

		when(this.formulaService.getByTargetId(variableId)).thenReturn(Optional.of(formulaDto));
		when(this.middlewareDerivedVariableService.getMissingFormulaVariablesInStudy(studyId, datasetId, variableId))
			.thenReturn(Collections.set(formulaVariable1, formulaVariable2));

		try {
			this.variableValidator.verifyInputVariablesArePresentInStudy(variableId, datasetId, studyId);
			fail("Method should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals(DerivedVariableValidator.STUDY_EXECUTE_CALCULATION_MISSING_VARIABLES, e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testVerifyMissingInputVariablesVariablesArePresentInADataset() {

		final int studyId = RandomUtils.nextInt();
		final int variableId = RandomUtils.nextInt();
		final int datasetId = RandomUtils.nextInt();
		final FormulaDto formulaDto = new FormulaDto();

		// Create two input variables for the target variable.
		final FormulaVariable targetVariable = new FormulaVariable();
		targetVariable.setId(variableId);
		final FormulaVariable formulaVariable1 = new FormulaVariable();
		formulaVariable1.setId(RandomUtils.nextInt());
		final FormulaVariable formulaVariable2 = new FormulaVariable();
		formulaVariable2.setId(RandomUtils.nextInt());
		formulaDto.setInputs(Arrays.asList(formulaVariable1, formulaVariable2));
		formulaDto.setTarget(targetVariable);

		// Variable with formula including the input variables are added to the dataset.
		final MeasurementVariable targetMeasurementVariable = new MeasurementVariable();
		targetMeasurementVariable.setTermId(variableId);
		final MeasurementVariable inputMeasurementVariable1 = new MeasurementVariable();
		inputMeasurementVariable1.setTermId(formulaVariable1.getId());
		final MeasurementVariable inputMeasurementVariable2 = new MeasurementVariable();
		inputMeasurementVariable2.setTermId(formulaVariable2.getId());

		when(this.formulaService.getByTargetId(variableId)).thenReturn(Optional.of(formulaDto));
		when(this.middlewareDerivedVariableService.getMissingFormulaVariablesInStudy(studyId, datasetId, variableId))
			.thenReturn(new HashSet<>());

		try {
			this.variableValidator.verifyInputVariablesArePresentInStudy(variableId, datasetId, studyId);
		} catch (final ApiRequestValidationException e) {
			fail("Method should not throw an exception");
		}

	}

	@Test
	public void testValidateForAggregateFunctionsSuccess() {
		final Map<Integer, Integer> inputVariableDatasetMap = new HashMap<>();
		inputVariableDatasetMap.put(VARIABLE_ID, SUBOBS_ID);
		try {
			final DatasetDTO plotDataset = new DatasetDTO();
			plotDataset.setDatasetId(DATASET_ID);
			Mockito.when(this.datasetService.getDatasets(STUDY_ID, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))))
				.thenReturn(Arrays.asList(plotDataset));

			final DatasetDTO subobsDataset = new DatasetDTO();
			subobsDataset.setDatasetId(SUBOBS_ID);
			Mockito.when(this.datasetService.getDatasets(STUDY_ID, new HashSet<>(SUBOBS_DATASET_TYPE_IDS))).thenReturn(Arrays.asList(subobsDataset));
			this.variableValidator.validateForAggregateFunctions(VARIABLE_ID, STUDY_ID, DATASET_ID, inputVariableDatasetMap);
			Mockito.verify(this.datasetTypeService).getSubObservationDatasetTypeIds();
			Mockito.verify(this.datasetService).getDatasets(STUDY_ID, new HashSet<>(SUBOBS_DATASET_TYPE_IDS));
		} catch (final ApiRequestValidationException e) {
			fail("Method should not throw an exception");
		}
	}

	@Test
	public void testValidateForAggregateFunctionsWithError() {
		final Map<Integer, Integer> inputVariableDatasetMap = new HashMap<>();
		inputVariableDatasetMap.put(VARIABLE_ID, SUBOBS_ID);

		try {
			final DatasetDTO plotDataset = new DatasetDTO();
			plotDataset.setDatasetId(SUBOBS_ID);
			Mockito.when(this.datasetService.getDatasets(STUDY_ID, new HashSet<>(Arrays.asList(DatasetTypeEnum.PLOT_DATA.getId()))))
				.thenReturn(Arrays.asList(plotDataset));
			this.variableValidator.validateForAggregateFunctions(VARIABLE_ID, STUDY_ID, DATASET_ID, inputVariableDatasetMap);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals(DerivedVariableValidator.STUDY_EXECUTE_CALCULATION_INPUT_NOT_IN_SUBLEVEL, e.getErrors().get(0).getCode());
			Mockito.verify(this.datasetTypeService).getSubObservationDatasetTypeIds();
			Mockito.verify(this.datasetService).getDatasets(STUDY_ID, new HashSet<>(SUBOBS_DATASET_TYPE_IDS));
		}
	}

	@Test
	public void testVerifySubObservationsInputVariablesInAggregateFunction() {
		final List<Integer> subobsIds = Arrays.asList(SUBOBS_ID);
		final Map<Integer, Integer> inputVariableDatasetMap = new HashMap<>();
		inputVariableDatasetMap.put(VARIABLE_ID, SUBOBS_ID);
		final List<String> aggregateInputVariables = Arrays.asList(VARIABLE_ID.toString());
		final Optional<FormulaDto> formula = Optional.of(this.createFormulaDTO());
		try{
			this.variableValidator.verifySubObservationsInputVariablesInAggregateFunction(subobsIds, inputVariableDatasetMap, formula, aggregateInputVariables);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Should not throw an exception.");
		}

		try{
			this.variableValidator.verifySubObservationsInputVariablesInAggregateFunction(subobsIds, inputVariableDatasetMap, formula, new ArrayList<>());
			Assert.fail("Should throw an exception.");
		} catch (final ApiRequestValidationException e) {
			assertEquals(DerivedVariableValidator.STUDY_EXECUTE_CALCULATION_NOT_AGGREGATE_FUNCTION, e.getErrors().get(0).getCode());
		}
	}

	@Test
	public void testVerifyAggregateInputVariablesInSubObsLevel() {
		final List<Integer> subobsIds = Arrays.asList(SUBOBS_ID);
		final Map<Integer, Integer> inputVariableDatasetMap = new HashMap<>();
		inputVariableDatasetMap.put(VARIABLE_ID, SUBOBS_ID);
		final List<String> aggregateInputVariables = Arrays.asList(VARIABLE_ID.toString());

		try {
			this.variableValidator.verifyAggregateInputVariablesInSubObsLevel(subobsIds, inputVariableDatasetMap, aggregateInputVariables);
		}  catch (final ApiRequestValidationException e) {
			Assert.fail("Should not throw an exception.");
		}

		try {
			this.variableValidator.verifyAggregateInputVariablesInSubObsLevel(new ArrayList<>(), inputVariableDatasetMap, aggregateInputVariables);
			Assert.fail("Should throw an exception.");
		}  catch (final ApiRequestValidationException e) {
			assertEquals(DerivedVariableValidator.STUDY_EXECUTE_CALCULATION_INPUT_NOT_IN_SUBLEVEL, e.getErrors().get(0).getCode());
		}
	}

	private FormulaDto createFormulaDTO() {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setDefinition("avg({{29001}})");
		final FormulaVariable formulaVariable = new FormulaVariable();
		formulaVariable.setId(VARIABLE_ID);
		formulaDto.setInputs(Arrays.asList(formulaVariable));
		return formulaDto;
	}
}
