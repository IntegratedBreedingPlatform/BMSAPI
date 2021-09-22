package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class GermplasmListValidator {

	private BindingResult errors;

	@Autowired
	private GermplasmListService germplasmListService;

	public GermplasmList validateGermplasmListExists(final Integer germplasmListId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		if (!Util.isPositiveInteger(String.valueOf(germplasmListId))) {
			this.errors.reject("list.id.invalid", new String[] {String.valueOf(germplasmListId)}, "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		return this.germplasmListService.getGermplasmListById(germplasmListId)
			.orElseThrow(() -> {
				this.errors.reject("list.id.invalid", new String[] {germplasmListId.toString()}, "");
				return new ResourceNotFoundException(this.errors.getAllErrors().get(0));
			});
	}

	public void validateListIsNotAFolder(final GermplasmList germplasmList) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		if (germplasmList.isFolder()) {
			this.errors.reject("list.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateListIsUnlocked(final GermplasmList germplasmList) {
		if (germplasmList.isLockedList()) {
			this.errors.reject("list.locked", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
