package org.ibp.api.domain.common;

import java.util.HashMap;

import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@Component
public class ValidationUtil {

	/**
	 * Utility method so that one does not need to create the error object and check for validation errors
	 * @param objectName passed into the {@link MapBindingResult} constructor
	 * @param command the command to execute to do the validation and other custom things
	 */
	public void invokeValidation(final String objectName, Command command) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), objectName);
		command.execute(errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
