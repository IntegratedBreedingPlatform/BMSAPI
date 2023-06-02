package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.dataset.StudyBookTableBuilder;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StudyBookTableValidator {

	@Autowired
	private LocationService locationService;

	private static final String LOCATION_ID = "LOCATION_ID";

	private static final String DATA_TYPE_NUMERIC = "Numeric";
	private static final String LOCATION_NAME = "LOCATION_NAME";

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

	public void validateStudyBookValuesDataTypes(final Table<String, String, String> inputData,
												 final List<MeasurementVariable> measurementVariables, final boolean validateCategoricalValues) throws ApiRequestValidationException {
		final Map<String, MeasurementVariable> mappedVariables = new HashMap<>();
		measurementVariables.forEach(measurementVariable -> {
			mappedVariables.putIfAbsent(measurementVariable.getName(), measurementVariable);
			mappedVariables.putIfAbsent(measurementVariable.getAlias(), measurementVariable);
		});
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());

		final Map<String, List<String>> validValuesMap = this.createValidValuesMap(validateCategoricalValues, measurementVariables);
		final List<String> locationNames = new ArrayList<>();
		List<String> validLocationNames = new ArrayList<>();
		if (inputData.columnKeySet().contains(LOCATION_NAME)) {
			for (final String observationUnitId : inputData.rowKeySet()) {
				locationNames.add(inputData.get(observationUnitId, LOCATION_NAME));
			}
			if (!CollectionUtils.isEmpty(locationNames)) {
				final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
				locationSearchRequest.setLocationNames(locationNames);
				validLocationNames = this.locationService.searchLocations(locationSearchRequest, null, null)
						.stream().map(loc -> loc.getName().toUpperCase()).collect(Collectors.toList());
			}
		}


		for (final String observationUnitId : inputData.rowKeySet()) {
			for (final String variableName : inputData.columnKeySet()) {
				if (!validateCategoricalVariableHasAPossibleValue(mappedVariables.get(variableName))) {
					errors.reject("warning.import.save.invalidCategoricalValue", new String[] {variableName}, "");
					throw new ApiRequestValidationException(errors.getAllErrors());
				} else if (!LOCATION_ID.equalsIgnoreCase(variableName) && !validateValue(mappedVariables.get(variableName), inputData.get(observationUnitId, variableName),
						validateCategoricalValues, validValuesMap, errors, validLocationNames)) {
					throw new ApiRequestValidationException(errors.getAllErrors());
				}
			}
		}
	}

	private static Map<String, List<String>> createValidValuesMap(final boolean validateCategoricalValues, final List<MeasurementVariable> measurementVariables) {
		final Map<String, List<String>> validValuesMap = new HashMap<>();
		if (validateCategoricalValues) {
			for(MeasurementVariable var: measurementVariables) {
				if (var.getDataTypeId() != null && var.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId()) {
					final List<String> validValues = var.getPossibleValues().stream().map(ref -> ref.getName().toUpperCase()).collect(Collectors.toList());
					validValuesMap.putIfAbsent(var.getName(), validValues);
					validValuesMap.putIfAbsent(var.getAlias(), validValues);
				}
			}
		}
		return validValuesMap;
	}

	private static boolean validateValue(final MeasurementVariable var, final String value, final boolean validateCategoricalValues,
										 final Map<String, List<String>> validValuesMap, final BindingResult errors,
										 final List<String> validLocationNames) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		if (isInvalidNumber(var, value)) {
			errors.reject("warning.import.save.invalid.cell.numeric.value", new String[] {var.getAlias(), value}, "");
			return false;
		} else if (isInvalidDate(var, value)) {
			errors.reject("warning.import.save.invalid.cell.date.value", new String[] {var.getAlias(), value}, "");
			return false;
		} else if (validateCategoricalValues && isInvalidCategoricalValue(var, value, validValuesMap)) {
			errors.reject("warning.import.save.invalid.cell.categorical.value", new String[] {var.getAlias(), value}, "");
			return false;
		} else if (validateCategoricalValues && isInvalidLocationName(var, value, validLocationNames)) {
			errors.reject("warning.import.save.invalid.cell.location.value", new String[] {value}, "");
			return false;
		}
		return true;
	}

	private static boolean isInvalidLocationName(final MeasurementVariable var, final String value, final List<String> validLocationNames) {
		return var.getTermId() == TermId.LOCATION_ID.getId() && !validLocationNames.contains(value.toUpperCase());
	}

	private static boolean isInvalidCategoricalValue(final MeasurementVariable var, final String value, final Map<String, List<String>> validValuesMap) {
		return var.getDataTypeId() != null && var.getDataTypeId() == TermId.CATEGORICAL_VARIABLE.getId() &&
				!validValuesMap.get(var.getName()).contains(value.toUpperCase());
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
