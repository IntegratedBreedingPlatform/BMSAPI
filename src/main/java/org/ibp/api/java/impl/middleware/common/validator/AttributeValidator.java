package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections.CollectionUtils;
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

		final List<Integer> ids = (List<Integer>) CollectionUtils.collect(attributeIds, new org.apache.commons.collections.Transformer() {
			@Override
			public Integer transform(final Object input) {
				final String id = (String) input;
				return Integer.valueOf(id);
			}
		});

		final List<Attribute> attributes = this.germplasmDataManager.getAttributeByIds(ids);
		if (attributes.size() != attributeIds.size()) {
			errors.reject("attribute.invalid", "");
			return;
		}
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
}
