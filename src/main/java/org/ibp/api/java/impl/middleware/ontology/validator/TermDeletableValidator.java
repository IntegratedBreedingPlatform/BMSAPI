package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.CommonUtil;
import org.ibp.api.domain.ontology.TermRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class TermDeletableValidator extends OntologyValidator implements
org.springframework.validation.Validator {

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

			boolean isReferred = this.ontologyManagerService.isTermReferred(CommonUtil.tryParseSafe(request.getId()));
			if (!isReferred) {
				return;
			}

			this.addCustomError(errors, RECORD_IS_NOT_DELETABLE, new Object[] { request.getTermName(), request.getId() });

		} catch (MiddlewareException e) {
			this.log.error("Error while validating object", e);
		}
	}
}
