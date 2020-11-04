package org.ibp.api.java.impl.middleware.inventory.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.Util;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InventoryCommonValidator {

	private static final Integer PREFIX_MAX_LENGTH = 15;

	private static final String STOCK_ID_PREFIX_REGEXP = "[a-zA-Z0-9]{1,14}[a-zA-Z]";

	private static Integer TRANSACTION_NOTES_MAX_LENGTH = 255;

	private static Integer LOT_NOTES_MAX_LENGTH = 255;

	@Autowired
	private VariableService variableService;

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

	public void validateUnitNames(final List<String> unitNames, final BindingResult errors) {
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		final List<VariableDetails> existingInventoryUnits = this.variableService.getVariablesByFilter(variableFilter);
		final List<String> supportedUnitNames = existingInventoryUnits.stream().map(VariableDetails::getName).collect(Collectors.toList());

		if (!supportedUnitNames.containsAll(unitNames)) {
			final List<String> invalidUnitNames = new ArrayList<>(unitNames);
			invalidUnitNames.removeAll(supportedUnitNames);
			errors.reject("lot.input.invalid.units", new String[] {Util.buildErrorMessageFromList(invalidUnitNames, 3)}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateTransactionNotes(final String notes, final BindingResult errors) {
		if (notes != null && notes.length() > TRANSACTION_NOTES_MAX_LENGTH) {
			errors.reject("transaction.notes.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateLotNotes(final String notes, final BindingResult errors) {
		if (notes != null && notes.length() > LOT_NOTES_MAX_LENGTH) {
			errors.reject("lot.notes.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateLotNotes(final List<String> notes, final BindingResult errors) {
		if (notes.stream().filter(s -> StringUtils.isBlank(s)).findAny().isPresent()) {
			errors.reject("lot.input.list.notes.null.or.empty", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final List<String> filteredNotes =
			notes.stream().filter(loteNotes -> loteNotes.length() > LOT_NOTES_MAX_LENGTH).map(s -> s).collect(Collectors.toList());
		if (!Collections.isEmpty(filteredNotes)) {
			errors.reject("lot.notes.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
