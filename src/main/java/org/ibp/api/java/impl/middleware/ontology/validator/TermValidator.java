package org.ibp.api.java.impl.middleware.ontology.validator;

import org.ibp.api.domain.ontology.TermRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class TermValidator extends OntologyValidator implements
		org.springframework.validation.Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return TermRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {
		TermRequest request = (TermRequest) target;
		this.checkTermExist(request.getTermName(), request.getId(), request.getCvId(), errors);
	}
}
