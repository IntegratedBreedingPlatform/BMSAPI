package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class GermplasmListValidator {

	private BindingResult errors;

	@Autowired
	private GermplasmListManager germplasmListManager;

	public void validateGermplasmList(final Integer germplasmListId) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (this.germplasmListManager.getGermplasmListById(germplasmListId) == null) {
			errors.reject("list.germplasm.does.not.exist", "");
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
