
package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Component
public class TermDeletableValidator extends OntologyValidator implements org.springframework.validation.Validator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TermDeletableValidator.class);

	@Autowired
	private FormulaService formulaService;

	@Override
	public boolean supports(Class<?> aClass) {
		return TermRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {
		TermRequest request = (TermRequest) target;

		try {
			this.checkTermExist(request.getTermName(), request.getId(), request.getCvId(), errors);

			if (errors.hasErrors()) {
				return;
			}

			boolean hasUsage = false,
					isReferred = false;
			if (Objects.equals(request.getCvId(), CvId.VARIABLES.getId())) {
				hasUsage = this.ontologyVariableDataManager.isVariableUsedInStudy(Integer.valueOf(request.getId())) || //
				 this.ontologyVariableDataManager.isVariableUsedInGermplasm(Integer.valueOf(request.getId())) || //
				 this.ontologyVariableDataManager.isVariableUsedInBreedingMethods(Integer.valueOf(request.getId()));
			} else {
				isReferred = this.termDataManager.isTermReferred(StringUtil.parseInt(request.getId(), null));
			}

			if (hasUsage || isReferred) {
				this.addCustomError(errors, BaseValidator.RECORD_IS_NOT_DELETABLE, new Object[] {request.getTermName(), request.getId()});
			}

			final List<FormulaDto> formulas = this.formulaService.getByInputId(Integer.valueOf(request.getId()));
			if (!formulas.isEmpty()) {
				final List<String> variableUsingInputInFormula = Lists.transform(formulas, new Function<FormulaDto, String>() {

					@Nullable
					@Override
					public String apply(@Nullable final FormulaDto formulaDto) {
						return formulaDto.getTarget().getName() + (formulaDto.getActive() != null && !formulaDto.getActive() ?
							"(deleted formula)" : "");
					}
				});
				this.addCustomError(
					errors, "variable.formula.record.is.not.deletable",
					new Object[] {
						request.getTermName(), request.getId(), StringUtils.join(new HashSet<>(variableUsingInputInFormula), ", ")});
			}

		} catch (MiddlewareException e) {
			TermDeletableValidator.LOGGER.error("Error while validating object", e);
		}
	}
}
