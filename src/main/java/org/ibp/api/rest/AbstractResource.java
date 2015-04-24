
package org.ibp.api.rest;

import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.CropNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

public abstract class AbstractResource {

	@Autowired
	protected CropNameValidator cropNameValidator;

	protected void validateCropName(String cropname, Errors bindingResult) {
		this.cropNameValidator.validate(cropname, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
	}

}
