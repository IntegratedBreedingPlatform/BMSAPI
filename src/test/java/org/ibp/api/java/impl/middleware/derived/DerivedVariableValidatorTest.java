package org.ibp.api.java.impl.middleware.derived;

import com.google.common.base.Optional;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.service.api.derived_variables.DerivedVariableService;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.dataset.DatasetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DerivedVariableValidatorTest {

	@Mock
	private FormulaService formulaService;

	@Mock
	private DatasetService datasetService;

	@Mock
	private DerivedVariableService middlewareDerivedVariableService;

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

		final Integer studyId = RandomUtils.nextInt();
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

		when(this.formulaService.getByTargetId(variableId)).thenReturn(Optional.of(formulaDto));
		try {
			this.variableValidator.verifyInputVariablesArePresentInStudy(variableId, datasetId, studyId);
			fail("Method should throw an exception");
		} catch (final ApiRequestValidationException e) {
			assertEquals(DerivedVariableValidator.STUDY_EXECUTE_CALCULATION_MISSING_VARIABLES, e.getErrors().get(0).getCode());
		}

	}

	@Test
	public void testVerifyMissingInputVariablesVariablesArePresentInADataset() {

		final Integer studyId = RandomUtils.nextInt();
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
		inputMeasurementVariable1.setTermId(formulaVariable1.getId());
		final MeasurementVariable inputMeasurementVariable2 = new MeasurementVariable();
		inputMeasurementVariable2.setTermId(formulaVariable2.getId());

		final Set<Integer> variableIds = new HashSet<>();
		variableIds.add(formulaVariable1.getId());
		variableIds.add(formulaVariable2.getId());

		when(this.formulaService.getByTargetId(variableId)).thenReturn(Optional.of(formulaDto));
		when(this.middlewareDerivedVariableService.extractVariableIdsFromDataset(studyId, datasetId))
			.thenReturn(variableIds);

		try {
			this.variableValidator.verifyInputVariablesArePresentInStudy(variableId, datasetId, studyId);
		} catch (final ApiRequestValidationException e) {
			fail("Method should not throw an exception");
		}

	}

	@Test
	public void testValidateSubobservationInputVariable() {
		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setDefinition("sum({{29001}})");
		FormulaVariable formulaVariable = new FormulaVariable();
		formulaVariable.setId(29001);

		try {
			this.variableValidator.validateSubobservationInputVariable(Optional.of(formulaDto), formulaVariable);
		} catch (final ApiRequestValidationException e) {
			fail("Method should not throw an exception");
		}

		try {
			formulaDto.setDefinition("{{29001}}/{{1001}}");
			this.variableValidator.validateSubobservationInputVariable(Optional.of(formulaDto), formulaVariable);
			fail("Should throw an exception");
		} catch (final ApiRequestValidationException e) {
		}
	}

}
