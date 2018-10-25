package org.ibp.api.java.impl.middleware.dataset.validator;

import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Created by clarysabel on 10/24/18.
 */
public class DatasetGeneratorInputValidator implements Validator {

	@Override
	public boolean supports(final Class<?> aClass) {
		return DatasetGeneratorInput.class.equals(aClass);
	}

	@Override
	public void validate(final Object o, final Errors errors) {

	}
}
