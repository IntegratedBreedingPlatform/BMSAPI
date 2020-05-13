package org.ibp.api.java.impl.middleware.inventory.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class InventoryCommonValidator {

	private static final Integer PREFIX_MAX_LENGTH = 15;

	private static final String STOCK_ID_PREFIX_REGEXP = "[a-zA-Z0-9]{1,14}[a-zA-Z]";

	public void validateStockIdPrefix(final String stockIdPrefix, final BindingResult errors) {
		if (stockIdPrefix != null && stockIdPrefix.length() > PREFIX_MAX_LENGTH) {
			errors.reject("lot.stock.prefix.invalid.length", new String[] {String.valueOf(PREFIX_MAX_LENGTH)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (!StringUtils.isEmpty(stockIdPrefix) && !stockIdPrefix.matches(STOCK_ID_PREFIX_REGEXP)) {
			errors.reject("lot.stock.prefix.invalid.pattern", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

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
