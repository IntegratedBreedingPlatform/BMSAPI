package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormulaValidatorTest {

	@Mock
	private DerivedVariableProcessor processor;

	@Mock
	protected TermValidator termValidator;

	@Mock
	protected TermDataManager termDataManager;

	@Mock
	protected OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private Errors errors;

	@InjectMocks
	private FormulaValidator formulaValidator;

	private Random random = new Random();

	@Test
	public void testValidateNullFormulaDto() {

		this.formulaValidator.validate(null, errors);

		verify(errors).reject("variable.formula.required", "");

	}

	@Test
	public void testValidateNullFormulaDtoTarget() {

		final FormulaDto formulaDto = new FormulaDto();

		this.formulaValidator.validate(formulaDto, errors);

		verify(errors).reject("variable.formula.targetid.required", "");

	}

	@Test
	public void testValidateErrorsWithTermValidator() {

		final FormulaDto formulaDto = new FormulaDto();
		formulaDto.setTarget(new FormulaVariable());

		when(errors.hasErrors()).thenReturn(true);

		this.formulaValidator.validate(formulaDto, errors);

		verify(termValidator).validate(any(TermRequest.class), any(Errors.class));

	}

	@Test
	public void testValidateVariableIsNotTrait() {

		final FormulaDto formulaDto = createFormulaDto();

		when(errors.hasErrors()).thenReturn(false);
		when(ontologyVariableDataManager.getVariableTypes(formulaDto.getTarget().getId()))
			.thenReturn(Arrays.asList(VariableType.SELECTION_METHOD));

		this.formulaValidator.validate(formulaDto, errors);

		verify(termValidator).validate(any(TermRequest.class), any(Errors.class));
		verify(errors).reject("variable.formula.target.not.valid", new String[] {String.valueOf(formulaDto.getTarget().getId())}, "");

	}

	@Test
	public void testValidateVariableInputNotExists() {

		final FormulaDto formulaDto = createFormulaDto();
		final String inputVariableName = formulaDto.getInputs().get(0).getName();

		when(errors.hasErrors()).thenReturn(false);
		when(ontologyVariableDataManager.getVariableTypes(formulaDto.getTarget().getId())).thenReturn(Arrays.asList(VariableType.TRAIT));
		when(termDataManager.getTermByNameAndCvId(inputVariableName, CvId.VARIABLES.getId())).thenReturn(null);

		this.formulaValidator.validate(formulaDto, errors);

		verify(termValidator).validate(any(TermRequest.class), any(Errors.class));
		verify(errors).reject("variable.input.not.exists", new String[] {inputVariableName}, "");

	}

	@Test
	public void testValidateVariableInputExists() {

		final FormulaDto formulaDto = createFormulaDto();
		final int inputVariableTermId = formulaDto.getInputs().get(0).getId();
		final String inputVariableName = formulaDto.getInputs().get(0).getName();

		final Term inputVariableTerm = new Term();
		inputVariableTerm.setId(inputVariableTermId);
		inputVariableTerm.setName(inputVariableName);

		when(errors.hasErrors()).thenReturn(false);
		when(ontologyVariableDataManager.getVariableTypes(formulaDto.getTarget().getId())).thenReturn(Arrays.asList(VariableType.TRAIT));
		when(termDataManager.getTermByNameAndCvId(inputVariableName, CvId.VARIABLES.getId())).thenReturn(inputVariableTerm);
		when(ontologyVariableDataManager.getDataType(inputVariableTermId)).thenReturn(Optional.of(DataType.NUMERIC_VARIABLE));

		this.formulaValidator.validate(formulaDto, errors);

		verify(termValidator).validate(any(TermRequest.class), any(Errors.class));
		verify(errors, times(0)).reject("variable.input.not.exists", new String[] {inputVariableName}, "");

	}

	@Test
	public void testGetAggregateFunctionInputVariables() {
		final String formula = "FN:avg({{GW_M_g100grn}}, {{GW_M_g200grn}},{{GW_M_g1000grn}})";
		final Map<String, DataType> inputVariablesDataTypeMap = new HashMap<>();
		inputVariablesDataTypeMap.put("__GW_M_g100grn__", DataType.NUMERIC_VARIABLE);
		inputVariablesDataTypeMap.put("__GW_M_g200grn__", DataType.NUMERIC_VARIABLE);
		inputVariablesDataTypeMap.put("__GW_M_g1000grn__", DataType.NUMERIC_VARIABLE);
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), FormulaDto.class.getName());
		final List<String> inputVariables = this.formulaValidator.getAggregateFunctionInputVariables(formula, inputVariablesDataTypeMap, bindingResult);
		Assert.assertEquals(3, inputVariables.size());
		Assert.assertFalse(bindingResult.hasErrors());
	}

	@Test
	public void testGetAggregateFunctionInputVariablesForAverageWithErrors() {
		final String formula = "FN:avg({{GW_M_g100grn}}, {{GW_M_g200grn}},{{Ant_Date_ddmmyyyy}})";
		final Map<String, DataType> inputVariablesDataTypeMap = new HashMap<>();
		inputVariablesDataTypeMap.put("__GW_M_g100grn__", DataType.NUMERIC_VARIABLE);
		inputVariablesDataTypeMap.put("__GW_M_g200grn__", DataType.NUMERIC_VARIABLE);
		inputVariablesDataTypeMap.put("__Ant_Date_ddmmyyyy__", DataType.DATE_TIME_VARIABLE);
		this.formulaValidator.getAggregateFunctionInputVariables(formula, inputVariablesDataTypeMap, this.errors);
		Mockito.verify(this.errors).reject("variable.formula.avg.input.not.numeric", "");
	}

	@Test
	public void testGetAggregateFunctionInputVariablesForSumWithErrors() {
		final String formula = "FN:sum({{GW_M_g100grn}}, {{GW_M_g200grn}},{{Ant_Date_ddmmyyyy}})";
		final Map<String, DataType> inputVariablesDataTypeMap = new HashMap<>();
		inputVariablesDataTypeMap.put("__GW_M_g100grn__", DataType.NUMERIC_VARIABLE);
		inputVariablesDataTypeMap.put("__GW_M_g200grn__", DataType.NUMERIC_VARIABLE);
		inputVariablesDataTypeMap.put("__Ant_Date_ddmmyyyy__", DataType.DATE_TIME_VARIABLE);
		this.formulaValidator.getAggregateFunctionInputVariables(formula, inputVariablesDataTypeMap, this.errors);
		Mockito.verify(this.errors).reject("variable.formula.sum.input.not.numeric", "");
	}

	@Test
	public void testValidateInputVariableIsNotTrait() {

		final FormulaDto formulaDto = createFormulaDto();
		final int inputVariableTermId = formulaDto.getInputs().get(0).getId();
		final String inputVariableName = formulaDto.getInputs().get(0).getName();

		final Term inputVariableTerm = new Term();
		inputVariableTerm.setId(inputVariableTermId);
		inputVariableTerm.setName(inputVariableName);

		when(errors.hasErrors()).thenReturn(false);
		when(ontologyVariableDataManager.getVariableTypes(formulaDto.getTarget().getId())).thenReturn(Arrays.asList(VariableType.TRAIT));
		when(ontologyVariableDataManager.getVariableTypes(inputVariableTermId)).thenReturn(Arrays.asList(VariableType.SELECTION_METHOD));
		when(termDataManager.getTermByNameAndCvId(inputVariableName, CvId.VARIABLES.getId())).thenReturn(inputVariableTerm);
		when(ontologyVariableDataManager.getDataType(inputVariableTermId)).thenReturn(Optional.of(DataType.NUMERIC_VARIABLE));

		this.formulaValidator.validate(formulaDto, errors);

		verify(termValidator).validate(any(TermRequest.class), any(Errors.class));
		verify(errors).reject(eq("variable.formula.inputs.not.trait"), any(new String[0].getClass()), anyString());

	}

	@Test
	public void testValidateFormulaDefinitionIsBlank() {

		final FormulaDto formulaDto = createFormulaDto();
		formulaDto.setDefinition("");
		final int inputVariableTermId = formulaDto.getInputs().get(0).getId();
		final String inputVariableName = formulaDto.getInputs().get(0).getName();

		final Term inputVariableTerm = new Term();
		inputVariableTerm.setId(inputVariableTermId);
		inputVariableTerm.setName(inputVariableName);

		when(errors.hasErrors()).thenReturn(false);
		when(ontologyVariableDataManager.getVariableTypes(formulaDto.getTarget().getId())).thenReturn(Arrays.asList(VariableType.TRAIT));
		when(termDataManager.getTermByNameAndCvId(inputVariableName, CvId.VARIABLES.getId())).thenReturn(inputVariableTerm);
		when(ontologyVariableDataManager.getDataType(inputVariableTermId)).thenReturn(Optional.of(DataType.NUMERIC_VARIABLE));

		this.formulaValidator.validate(formulaDto, errors);

		verify(termValidator).validate(any(TermRequest.class), any(Errors.class));
		verify(errors).reject("variable.formula.definition.required", "");

	}

	@Test
	public void testValidateSuccess() {

		final FormulaDto formulaDto = createFormulaDto();
		final int inputVariableTermId = formulaDto.getInputs().get(0).getId();
		final String inputVariableName = formulaDto.getInputs().get(0).getName();

		final Term inputVariableTerm = new Term();
		inputVariableTerm.setId(inputVariableTermId);
		inputVariableTerm.setName(inputVariableName);

		when(errors.hasErrors()).thenReturn(false);
		when(ontologyVariableDataManager.getVariableTypes(formulaDto.getTarget().getId())).thenReturn(Arrays.asList(VariableType.TRAIT));
		when(termDataManager.getTermByNameAndCvId(inputVariableName, CvId.VARIABLES.getId())).thenReturn(inputVariableTerm);
		when(ontologyVariableDataManager.getDataType(inputVariableTermId)).thenReturn(Optional.of(DataType.NUMERIC_VARIABLE));

		this.formulaValidator.validate(formulaDto, errors);

		verify(termValidator).validate(any(TermRequest.class), any(Errors.class));
		verify(processor).evaluateFormula(anyString(), anyMapOf(String.class, Object.class));

	}

	@Test
	public void testValidateFail() {

		final FormulaDto formulaDto = createFormulaDto();
		final int inputVariableTermId = formulaDto.getInputs().get(0).getId();
		final String inputVariableName = formulaDto.getInputs().get(0).getName();

		final Term inputVariableTerm = new Term();
		inputVariableTerm.setId(inputVariableTermId);
		inputVariableTerm.setName(inputVariableName);

		when(errors.hasErrors()).thenReturn(false);
		when(ontologyVariableDataManager.getVariableTypes(formulaDto.getTarget().getId())).thenReturn(Arrays.asList(VariableType.TRAIT));
		when(termDataManager.getTermByNameAndCvId(inputVariableName, CvId.VARIABLES.getId())).thenReturn(inputVariableTerm);
		when(ontologyVariableDataManager.getDataType(inputVariableTermId)).thenReturn(Optional.of(DataType.NUMERIC_VARIABLE));
		when(processor.evaluateFormula(anyString(), anyMapOf(String.class, Object.class))).thenThrow(RuntimeException.class);

		this.formulaValidator.validate(formulaDto, errors);

		verify(termValidator).validate(any(TermRequest.class), any(Errors.class));
		verify(processor).evaluateFormula(anyString(), anyMapOf(String.class, Object.class));
		verify(errors).reject(eq("variable.formula.invalid"), any(new Object[0].getClass()), anyString());

	}

	private FormulaDto createFormulaDto() {

		final int targetTermId = random.nextInt();
		final int inputVariableTermId = random.nextInt();
		final String inputVariableName = RandomStringUtils.randomAlphabetic(10);

		final FormulaDto formulaDto = new FormulaDto();
		final FormulaVariable formulaVariable = new FormulaVariable();
		formulaVariable.setId(targetTermId);
		final FormulaVariable formulaInput = new FormulaVariable();
		formulaInput.setId(inputVariableTermId);
		formulaInput.setName(inputVariableName);
		formulaDto.setTarget(formulaVariable);
		formulaDto.setInputs(Arrays.asList(formulaInput));
		formulaDto.setDefinition("{{" + inputVariableName + "}}+1");

		return formulaDto;

	}
}
