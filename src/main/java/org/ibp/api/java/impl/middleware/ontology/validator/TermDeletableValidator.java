
package org.ibp.api.java.impl.middleware.ontology.validator;

import java.util.Objects;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class TermDeletableValidator extends OntologyValidator implements org.springframework.validation.Validator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TermDeletableValidator.class);

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
				hasUsage = this.ontologyVariableDataManager.isVariableUsedInStudy(Integer.valueOf(request.getId()));
			} else {
				isReferred = this.termDataManager.isTermReferred(StringUtil.parseInt(request.getId(), null));
			}

			if (hasUsage || isReferred) {
				this.addCustomError(errors, BaseValidator.RECORD_IS_NOT_DELETABLE, new Object[] {request.getTermName(), request.getId()});
			}


		} catch (MiddlewareException e) {
			TermDeletableValidator.LOGGER.error("Error while validating object", e);
		}
	}
}
