package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.commons.derivedvariable.DerivedVariableUtils;
import org.generationcp.middleware.domain.ontology.FormulaDto;
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
public class FormulaValidator extends OntologyValidator implements Validator {

	@Autowired
	private DerivedVariableProcessor processor;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@Override
	public boolean supports(final Class<?> aClass) {
		return FormulaDto.class.equals(aClass);
	}

	@Override
	public void validate(final Object target, final Errors errors) {

		final FormulaDto formulaDto = (FormulaDto) target;

		if (formulaDto == null) {
			this.addCustomError(errors, "variable.formula.required", null);
			return;
		}

		if (formulaDto.getTargetTermId() == null) {
			this.addCustomError(errors, "variable.formula.targetid.required", null);
		}

		if (StringUtils.isBlank(formulaDto.getDefinition())) {
			this.addCustomError(errors, "variable.formula.definition.required", null);
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

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}
}
