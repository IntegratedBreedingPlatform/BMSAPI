package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.dataset.DatasetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DerivedVariableValidatorTest {

	@Mock
	private FormulaService formulaService;

	@Mock
	private DatasetService datasetService;

	@InjectMocks
	private final DerivedVariableValidator variableValidator = new DerivedVariableValidator();

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

		final Integer variableId = RandomUtils.nextInt();
		final Integer datasetId = RandomUtils.nextInt();
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

		// Only the variable with formula is loaded in the dataset.
		final MeasurementVariable targetMeasurementVariable = new MeasurementVariable();
		targetMeasurementVariable.setTermId(variableId);

		when(this.formulaService.getByTargetId(variableId)).thenReturn(Optional.of(formulaDto));
		when(this.datasetService.getMeasurementVariables(datasetId, Arrays.asList(VariableType.TRAIT.getId())))
			.thenReturn(Arrays.asList(targetMeasurementVariable));

		try {
			this.variableValidator.verifyMissingInputVariables(variableId, datasetId);
			fail("Method should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals(DerivedVariableValidator.STUDY_EXECUTE_CALCULATION_MISSING_VARIABLES, e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testVerifyMissingInputVariablesVariablesArePresentInADataset() {

		final Integer variableId = RandomUtils.nextInt();
		final Integer datasetId = RandomUtils.nextInt();
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
		inputMeasurementVariable1.setTermId(variableId);
		final MeasurementVariable inputMeasurementVariable2 = new MeasurementVariable();
		inputMeasurementVariable2.setTermId(variableId);

		when(this.formulaService.getByTargetId(variableId)).thenReturn(Optional.of(formulaDto));
		when(this.datasetService.getMeasurementVariables(datasetId, Arrays.asList(VariableType.TRAIT.getId())))
			.thenReturn(Arrays.asList(targetMeasurementVariable, inputMeasurementVariable1, inputMeasurementVariable2));

		try {
			this.variableValidator.verifyMissingInputVariables(variableId, datasetId);
		} catch (final ApiRequestValidationException e) {
			fail("Method should not throw an exception");
		}

	}

}
