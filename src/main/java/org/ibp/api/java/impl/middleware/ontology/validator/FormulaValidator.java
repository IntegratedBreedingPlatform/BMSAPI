package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class FormulaValidator implements Validator {

	@Autowired
	private DerivedVariableProcessor processor;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

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

		if (!this.isTrait(targetTermId)) {
			errors.reject("variable.formula.target.not.trait", new String[] {String.valueOf(targetTermId)}, "");
		}

		// Validate inputs and set ids

		final Set<Term> nonTraitInputs = new LinkedHashSet<>();

		for (final FormulaVariable input : formulaDto.getInputs()) {
			final Term inputTerm = this.termDataManager.getTermByName(input.getName());
			if (inputTerm == null) {
				errors.reject("variable.input.not.exists", new Object[] {input.getName()}, "");
			} else {
				final int id = inputTerm.getId();
				input.setId(id); // it will be used to save the input
				if (!this.isTrait(id)) {
					nonTraitInputs.add(inputTerm);
				}
			}
		}

		if (!nonTraitInputs.isEmpty()) {
			errors.reject("variable.formula.inputs.not.trait", new String[] {StringUtils.join(Iterables.transform(
				nonTraitInputs, new Function<Term, String>() {

					@Nullable
					@Override
					public String apply(@Nullable final Term term) {
						return term.getName() + "(" + term.getId() + ")";
					}
				}), ", ")}, "");
		}

		// Validate formula definition

		if (StringUtils.isBlank(formulaDto.getDefinition())) {
			errors.reject("variable.formula.definition.required", "");
			return;
		}

		// Validate syntax

		try {
			String formula = formulaDto.getDefinition();
			final Map<String, Object> parameters = DerivedVariableUtils.extractParameters(formula);
			for (final Map.Entry<String, Object> termEntry : parameters.entrySet()) {
				termEntry.setValue(BigDecimal.ONE);
			}
			formula = DerivedVariableUtils.replaceDelimiters(formula);
			processor.evaluateFormula(formula, parameters);
		} catch (final Exception e) {
			// Inform the ontology manager admin about the exception
			// Mapping engine errors to UI errors would be impractical
			throw new IllegalArgumentException(
				getMessage("variable.formula.invalid") + e.getMessage() + " - " + e.getCause());
		}
	}

	private boolean isTrait(final int id) {
		return Iterables.any(this.ontologyVariableDataManager.getVariableTypes(id), new Predicate<VariableType>() {

			@Override
			public boolean apply(@Nullable final VariableType variableType) {
				return variableType.equals(VariableType.TRAIT);
			}
		});
	}

	public void validateDelete(final FormulaDto formula, final Errors errors) {
		final TermRequest term = new TermRequest(String.valueOf(formula.getTarget().getId()), "formula", CvId.VARIABLES.getId());

		final boolean isVariableUsedInStudy =
			this.ontologyVariableDataManager.isVariableUsedInStudy(Integer.valueOf(formula.getTarget().getId()));

		if (isVariableUsedInStudy) {
			errors.reject("variable.formula.invalid.is.not.deletable", "");
		}
	}

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
