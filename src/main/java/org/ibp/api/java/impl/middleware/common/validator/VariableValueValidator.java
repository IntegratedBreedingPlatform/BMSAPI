package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.springframework.stereotype.Component;

@Component
public class VariableValueValidator {

	public boolean isValidObservationValue(final Variable var, final String value) {
		return this.isValidValue(var, value, true);
	}

	public boolean isValidAttributeValue(final Variable var, final String value) {
		return this.isValidValue(var, value, false);
	}

	private boolean isValidValue(final Variable var, final String value, final boolean isMissingAccepted) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		if (var.getMinValue() != null && var.getMaxValue() != null) {
			return validateIfValueIsMissingOrNumber(value.trim(), isMissingAccepted);
		} else if (var.getScale().getDataType() == DataType.NUMERIC_VARIABLE) {
			return validateIfValueIsMissingOrNumber(value.trim(), isMissingAccepted);
		} else if (var.getScale().getDataType() == DataType.DATE_TIME_VARIABLE) {
			return new DateValidator().isValid(value, "yyyyMMdd");
		}
		return true;
	}

	private boolean validateIfValueIsMissingOrNumber(final String value, final boolean isMissingAccepted) {
		if (isMissingAccepted && MeasurementData.MISSING_VALUE.equals(value.trim())) {
			return true;
		}
		return NumberUtils.isNumber(value);
	}

}
