package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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

		if (request == null) {
			this.addCustomError(errors, OntologyValidator.SHOULD_NOT_NULL_OR_EMPTY, null);
			return;
		}

		try {
			this.checkTermExist(request.getTermName(), request.getId(), request.getCvId(), errors);

			if (errors.hasErrors()) {
				return;
			}

			boolean isReferred = this.ontologyManagerService.isTermReferred(CommonUtil.tryParseSafe(request.getId()));
			if (!isReferred) {
				return;
			}

			this.addCustomError(errors, OntologyValidator.CAN_NOT_DELETE_REFERRED_TERM,
					new Object[] { request.getId(), request.getTermName() });

		} catch (MiddlewareQueryException e) {
			this.log.error("Error while validating object", e);
		}
	}
}
