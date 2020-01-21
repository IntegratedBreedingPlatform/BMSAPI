package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;

@Component
public class AttributeValidator {


	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public void validateAttributeIds(final BindingResult errors, final List<String> attributeIds) {
		if (attributeIds == null) {
			return;
		}
		for (final String id: attributeIds) {
			final Attribute attribute = this.germplasmDataManager.getAttributeById(Integer.valueOf(id));
			if (attribute == null) {
				errors.reject("attribute.invalid", "");
				return;
			}
		}
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
}
