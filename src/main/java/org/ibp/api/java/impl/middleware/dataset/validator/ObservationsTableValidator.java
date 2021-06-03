package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ObservationsTableValidator {

	private static final String DATA_TYPE_NUMERIC = "Numeric";

	public void validateList(final List<List<String>> inputData) throws ApiRequestValidationException {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());

		if (inputData.size() < 2) {
			errors.reject("table.should.have.at.least.two.rows", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		final int rowLength = inputData.get(0).size();
		boolean formatErrorFound = false;
		for (final List<String> row : inputData) {
			if (row.size() != rowLength) {
				formatErrorFound = true;
				break;
			}
		}
		if (rowLength == 0 || formatErrorFound) {
			errors.reject("table.format.inconsistency", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}

	public void validateObservationsValuesDataTypes(final Table<String, String, String> inputData,
			final List<MeasurementVariable> measurementVariables) throws ApiRequestValidationException {

		final Map<String, MeasurementVariable> mappedVariables = Maps.uniqueIndex(measurementVariables, MeasurementVariable::getAlias);

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());

		for (final String observationUnitId : inputData.rowKeySet()) {
			for (final String variableName : inputData.columnKeySet()) {
				if (!validateCategoricalVariableHasAPossibleValue(mappedVariables.get(variableName))) {
					errors.reject("warning.import.save.invalidCategoricalValue", new String[] {variableName}, "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				} else if (!validateValue(mappedVariables.get(variableName), inputData.get(observationUnitId, variableName), errors)) {
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
			}
		}
	}

	private static boolean validateValue(final MeasurementVariable var, final String value, final BindingResult errors) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		if (isInvalidNumber(var, value)) {
			errors.reject("warning.import.save.invalid.cell.numeric.value", new String[] {var.getAlias(), value}, "");
			return false;
		} else if (isInvalidDate(var, value)) {
			errors.reject("warning.import.save.invalid.cell.date.value", new String[] {var.getAlias(), value}, "");
			return false;
		}
		return true;
	}

	private static boolean isInvalidDate(final MeasurementVariable var, final String value) {
		return var.getDataTypeId() != null && var.getDataTypeId() == TermId.DATE_VARIABLE.getId() && !Util.isValidDate(value);
	}

	private static boolean isInvalidNumber(final MeasurementVariable var, final String value) {
		if ((var.getMinRange() != null && var.getMaxRange() != null)
			|| (StringUtils.isNotBlank(var.getDataType()) && var.getDataType().equalsIgnoreCase(DATA_TYPE_NUMERIC))) {
			return !isValueMissingOrNumber(value.trim());
		}
		return false;
	}

	private static boolean isValueMissingOrNumber(final String value) {
		if (MeasurementData.MISSING_VALUE.equals(value.trim())) {
			return true;
		}
		return NumberUtils.isNumber(value);
	}

	private static boolean validateCategoricalVariableHasAPossibleValue(final MeasurementVariable var) {
		if (var.getDataTypeId() !=null && var.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
			return !CollectionUtils.isEmpty(var.getPossibleValues());
		}
		return true;
	}

}
