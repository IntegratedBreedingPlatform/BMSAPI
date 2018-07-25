package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.FormulaVariable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

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
			errors.rejectValue("variable.formula.required", "");
			return;
		}

		// Validate target variable

		if (formulaDto.getTargetTermId() == null) {
			errors.reject("variable.formula.targetid.required", "");
			return;
		}

		final TermRequest term = new TermRequest(String.valueOf(formulaDto.getTargetTermId()), "target variable", CvId.VARIABLES.getId());
		this.termValidator.validate(term, errors);

		// Validate inputs

		for (final FormulaVariable formulaVariable : formulaDto.getInputs()) {
			final Term termByName = this.termDataManager.getTermByName(formulaVariable.getName());
			if (termByName == null) {
				errors.reject("variable.input.not.exists", new Object[] {formulaVariable.getName()}, "");
			} else {
				formulaVariable.setId(termByName.getId());
			}
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

	public void validateDelete(final FormulaDto formula, final Errors errors) {
		final TermRequest term = new TermRequest(String.valueOf(formula.getTargetTermId()), "formula", CvId.VARIABLES.getId());

		final boolean isVariableUsedInStudy =
			this.ontologyVariableDataManager.isVariableUsedInStudy(Integer.valueOf(formula.getTargetTermId()));

		if (isVariableUsedInStudy) {
			errors.reject("variable.formula.invalid.is.not.deletable", "");
		}
	}

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
