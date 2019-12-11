package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;

@Component
public class InventoryScaleValidator {

	@Autowired
	private VariableService variableService;

	public void validateInventoryScaleId(final BindingResult errors, final Integer inventoryScaleId) {
		if (inventoryScaleId != null) {

			final VariableFilter variableFilter = new VariableFilter();
			variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
			final List<VariableDetails> variables = this.variableService.getVariablesByFilter(variableFilter);

			VariableDetails selectedInventoryScale = null;
			if (!variables.isEmpty()) {
				selectedInventoryScale = variables.stream()
					.filter(inventoryScale -> inventoryScale.getId().equals(String.valueOf(inventoryScaleId)))
					.findAny()
					.orElse(null);
			}

			if (selectedInventoryScale == null) {
				errors.reject("inventory.scale.invalid", "");
			}
		}
	}

	public void validateNotNullInventoryScaleId(final BindingResult errors, final Integer inventoryScaleId) {
		if (inventoryScaleId == null) {
			errors.reject("inventory.scale.required", "");
			throw new ConflictException(errors.getAllErrors());
		}
	}
}
