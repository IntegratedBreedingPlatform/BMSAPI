package org.ibp.api.java.impl.middleware.common.validator;

import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class GermplasmValidator {

	@Autowired
	private GermplasmService germplasmService;

	public void validateGermplasmId(final BindingResult errors, final Integer germplasmId) {
		if (germplasmId == null) {
			errors.reject("germplasm.required", "");
			return;
		}
		final GermplasmSummary germplasmSummary = germplasmService.getGermplasm(String.valueOf(germplasmId));
		if (germplasmSummary == null) {
			errors.reject("germplasm.invalid", "");
		}
	}

}
