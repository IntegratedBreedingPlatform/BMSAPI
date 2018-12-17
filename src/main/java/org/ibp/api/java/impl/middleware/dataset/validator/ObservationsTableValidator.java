package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
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

		Map<String, MeasurementVariable> mappedVariables =
				Maps.uniqueIndex(measurementVariables, new Function<MeasurementVariable, String>() {

					public String apply(MeasurementVariable from) {
						return from.getAlias();
					}
				});

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());

		for (final String observationUnitId : inputData.rowKeySet()) {

			for (final String variableName : inputData.columnKeySet()) {

				if (!this.isValidValue(mappedVariables.get(variableName), inputData.get(observationUnitId, variableName))) {
					errors.reject("warning.import.save.invalidCellValue",
							new String[] {variableName, inputData.get(observationUnitId, variableName)}, "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
			}
		}
	}

	private boolean isValidValue(final MeasurementVariable var, final String value) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		if (var.getMinRange() != null && var.getMaxRange() != null) {
			return this.validateIfValueIsMissingOrNumber(value.trim());
		} else if (var != null && var.getDataTypeId() != null && var.getDataTypeId() == TermId.DATE_VARIABLE.getId()) {
			return Util.isValidDate(value);
		} else if (StringUtils.isNotBlank(var.getDataType()) && var.getDataType().equalsIgnoreCase(DATA_TYPE_NUMERIC)) {
			return this.validateIfValueIsMissingOrNumber(value.trim());
		}
		return true;
	}

	private boolean validateIfValueIsMissingOrNumber(final String value) {
		if (MeasurementData.MISSING_VALUE.equals(value.trim())) {
			return true;
		}
		return NumberUtils.isNumber(value);
	}

}
