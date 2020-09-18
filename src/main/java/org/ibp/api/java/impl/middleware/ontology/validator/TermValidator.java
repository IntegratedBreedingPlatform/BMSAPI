
package org.ibp.api.java.impl.middleware.ontology.validator;

import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class TermValidator extends OntologyValidator implements org.springframework.validation.Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return TermRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {
		TermRequest request = (TermRequest) target;
		this.checkTermExist(request.getTermName(), request.getId(), request.getCvId(), errors);
	}

	public void validate(Integer termId) {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (this.termDataManager.getTermById(termId) == null) {
			errors.reject("variable.does.not.exist", new Object[] {termId}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
