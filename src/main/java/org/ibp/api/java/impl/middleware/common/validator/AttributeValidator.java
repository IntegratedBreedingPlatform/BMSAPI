package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.collections4.CollectionUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.validation.BindingResult;

import java.util.List;

public class AttributeValidator {

	public static final Integer ATTRIBUTE_VALUE_MAX_LENGTH = 5000;

	void validateAttributeValue(final BindingResult errors, final String value) {
		if (value.length() > ATTRIBUTE_VALUE_MAX_LENGTH) {
			errors.reject("attribute.value.invalid.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void validateAttributeDate(final BindingResult errors, final String date) {
		if (!DateUtil.isValidDate(date)) {
			errors.reject("attribute.date.invalid.format", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateVariableDataTypeValue(final BindingResult errors, final Variable variable, final String value) {
		if (!VariableValueUtil.isValidAttributeValue(variable, value)) {
			errors.reject("invalid.variable.value", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateAttributeVariable(final BindingResult errors, final Variable variable,
		final List<VariableType> validVariableTypes) {
		if (variable == null) {
			errors.reject("attribute.variable.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (!CollectionUtils.containsAny(variable.getVariableTypes(), validVariableTypes)) {
			errors.reject("attribute.variable.type.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}


}
