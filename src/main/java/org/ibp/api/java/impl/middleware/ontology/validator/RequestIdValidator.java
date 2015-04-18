package org.ibp.api.java.impl.middleware.ontology.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class RequestIdValidator extends OntologyValidator implements
org.springframework.validation.Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return String.class.isAssignableFrom(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {
		this.shouldNotNullOrEmpty("ID", "id", target, errors);
		if (errors.hasErrors()) {
			return;
		}

		if (target instanceof Integer) {
			return;
		}

		String id = (String) target;
		this.checkNumberField(id, errors);
	}
}
