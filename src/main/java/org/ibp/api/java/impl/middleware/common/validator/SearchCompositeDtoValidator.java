package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class SearchCompositeDtoValidator {


	public void validateSearchCompositeDto(
		final SearchCompositeDto searchCompositeDto,
		final BindingResult errors) {

		// Validate that searchId or list of elements are provided
		if (searchCompositeDto == null || !searchCompositeDto.isValid()) {
			errors.reject("search.composite.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
