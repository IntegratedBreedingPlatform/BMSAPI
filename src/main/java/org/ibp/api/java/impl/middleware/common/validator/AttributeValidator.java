package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.UDTableType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttributeValidator {

	private static List<String> ALLOWED_ATTRIBUTE_TYPES = Arrays.asList(UDTableType.ATRIBUTS_ATTRIBUTE.getType(),
		UDTableType.ATRIBUTS_PASSPORT.getType());

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public void validateAttributeIds(final BindingResult errors, final List<String> attributeIds) {
		if (attributeIds == null || attributeIds.isEmpty()) {
			return;
		}

		final List<Integer> ids =
			attributeIds.stream().map(attributeId -> Integer.valueOf(attributeId)).distinct().collect(Collectors.toList());

		final List<Attribute> attributes = this.germplasmDataManager.getAttributeByIds(ids);
		if (attributes.size() != ids.size()) {
			errors.reject("attribute.invalid", "");
			return;
		}
	}

	public void validateAttributeType(final BindingResult errors, final String attributeType) {
		if (StringUtils.isEmpty(attributeType)) {
			return;
		}

		if (!AttributeValidator.ALLOWED_ATTRIBUTE_TYPES.contains(attributeType.toUpperCase())) {
			errors.reject("attribute.type.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
}
