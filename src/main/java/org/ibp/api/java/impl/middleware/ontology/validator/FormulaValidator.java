package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.service.impl.derived_variables.DerivedVariableServiceImpl;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FormulaValidator implements Validator {

	@Autowired
	private DerivedVariableProcessor processor;

	@Autowired
	protected TermValidator termValidator;

	@Autowired
	protected TermDataManager termDataManager;

	@Autowired
	protected OntologyVariableDataManager ontologyVariableDataManager;

	@Override
	public boolean supports(final Class<?> aClass) {
		return FormulaDto.class.equals(aClass);
	}

	@Override
	public void validate(final Object target, final Errors errors) {

		final FormulaDto formulaDto = (FormulaDto) target;

		if (formulaDto == null) {
			errors.reject("variable.formula.required", "");
			return;
		}

		// Validate target variable

		if (formulaDto.getTarget() == null) {
			errors.reject("variable.formula.targetid.required", "");
			return;
		}

		final Integer targetTermId = formulaDto.getTarget().getId();

		final TermRequest targetTerm = new TermRequest(String.valueOf(targetTermId), "target variable", CvId.VARIABLES.getId());
		this.termValidator.validate(targetTerm, errors);

		if (errors.hasErrors()) {
			return;
		}

		if (!this.isValidVariableTypeForFormula(targetTermId)) {
			errors.reject("variable.formula.target.not.valid", new String[] {String.valueOf(targetTermId)}, "");
		}

		// Validate inputs and set ids

		final Set<Term> nonTraitInputs = new LinkedHashSet<>();
		final Set<Term> obsoleteInputs = new LinkedHashSet<>();
		final Map<String, DataType> inputVariablesDataTypeMap = new HashMap<>();

		for (final FormulaVariable input : formulaDto.getInputs()) {
			final Term inputTerm = this.termDataManager.getTermByNameAndCvId(input.getName(), CvId.VARIABLES.getId());
			if (inputTerm == null) {
				errors.reject("variable.input.not.exists", new Object[] {input.getName()}, "");
			} else {
				if (inputTerm.isObsolete()) {
					obsoleteInputs.add(inputTerm);
				} else {
					final int id = inputTerm.getId();

					// We need the datatype info of each variable so that we can properly create mock data later.
					final Optional<DataType> dataTypeOptional = this.ontologyVariableDataManager.getDataType(id);
					if (dataTypeOptional.isPresent()) {
						inputVariablesDataTypeMap.put(DerivedVariableUtils.wrapTerm(inputTerm.getName()), dataTypeOptional.get());
					}

					input.setId(id); // it will be used to save the input
					if (!this.isValidVariableTypeForFormula(id)) {
						nonTraitInputs.add(inputTerm);
					}
				}

			}
		}

		if (!obsoleteInputs.isEmpty()) {
			this.addMultipleTermError(errors, "variable.formula.inputs.obsolete", obsoleteInputs);
		}

		if (!nonTraitInputs.isEmpty()) {
			this.addMultipleTermError(errors, "variable.formula.inputs.not.trait", nonTraitInputs);
		}

		// Validate formula definition

		if (StringUtils.isBlank(formulaDto.getDefinition())) {
			errors.reject("variable.formula.definition.required", "");
			return;
		}

		// Validate syntax

		try {
			String formula = formulaDto.getDefinition();
			final List<String> aggregateFunctionInputVariables =
				this.getAggregateFunctionInputVariables(formula, inputVariablesDataTypeMap, errors);
			if (errors.hasErrors()) {
				return;
			}
			final Map<String, Object> parameters = DerivedVariableUtils.extractParameters(formula);

			// Create mock data for each variable.
			for (final Map.Entry<String, Object> termEntry : parameters.entrySet()) {
				if (aggregateFunctionInputVariables.contains(termEntry.getKey())) {
					termEntry.setValue(new ArrayList<>());
				} else if (inputVariablesDataTypeMap.get(termEntry.getKey()) == DataType.DATE_TIME_VARIABLE) {
					termEntry.setValue(new Date());
				} else {
					termEntry.setValue(BigDecimal.ONE);
				}
			}
			formula = DerivedVariableUtils.replaceDelimiters(formula);
			this.processor.evaluateFormula(formula, parameters);
		} catch (final Exception e) {
			// Inform the ontology manager admin about the exception
			// Mapping engine errors to UI errors would be impractical
			errors.reject("variable.formula.invalid", new Object[] {e.getMessage(), e.getCause()}, "");
		}
	}

	private void addMultipleTermError(final Errors errors, final String message, final Set<Term> invalidInputs) {
		errors.reject(message, new String[] {
			StringUtils.join(Iterables.transform(
				invalidInputs, new Function<Term, String>() {

					@Nullable
					@Override
					public String apply(@Nullable final Term term) {
						if (term == null){
							return "";
						}
						return term.getName() + "(" + term.getId() + ")";
					}
				}), ", ")}, "");
	}

	List<String> getAggregateFunctionInputVariables(final String formula, final Map<String, DataType> inputVariablesDataTypeMap,
		final Errors errors) {
		final List<String> inputVariables = new ArrayList<>();
		final Map<String, List<String>> aggregateFunctionInputVariablesMap =
			DerivedVariableUtils.getAggregateFunctionInputVariablesMap(formula);
		for (final String aggregateFunction : DerivedVariableUtils.AGGREGATE_FUNCTIONS) {
			inputVariables.addAll(this.validateAggregateInputVariable(inputVariablesDataTypeMap, errors, aggregateFunction,
				aggregateFunctionInputVariablesMap.get(aggregateFunction)));
		}
		return inputVariables;
	}

	List<String> validateAggregateInputVariable(final Map<String, DataType> inputVariablesDataTypeMap, final Errors errors,
		final String aggregateFunction, final List<String> inputVariables) {
		final List<String> aggregateInputVariable = new ArrayList<>();
		for (final String inputVariable : inputVariables) {
			if (DataType.NUMERIC_VARIABLE.getId() != inputVariablesDataTypeMap.get(inputVariable).getId()) {
				errors.reject("variable.formula." + aggregateFunction + ".input.not.numeric", "");
				return inputVariables;
			}
			aggregateInputVariable.add(inputVariable);
		}
		return aggregateInputVariable;
	}

	private boolean isValidVariableTypeForFormula(final int id) {
		return Iterables.any(this.ontologyVariableDataManager.getVariableTypes(id), new Predicate<VariableType>() {

			@Override
			public boolean apply(@Nullable final VariableType variableType) {
				return DerivedVariableServiceImpl.CALCULATED_VARIABLE_VARIABLE_TYPES.contains(variableType.getId());
			}
		});
	}

}
