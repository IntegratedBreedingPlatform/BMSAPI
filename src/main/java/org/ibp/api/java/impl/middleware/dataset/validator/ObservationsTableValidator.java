package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.util.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class ObservationsTableValidator {

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private MeasurementVariableTransformer measurementVariableTransformer;

	private static final String DATA_TYPE_NUMERIC = "Numeric";

	public void validate(final String programUUID, final Table<String, String, String> inputData,
			final Map<String, ObservationUnitRow> storedData) throws ApiRequestValidationException {

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());

		for (final String observationUnitId : inputData.rowKeySet()) {
			final org.generationcp.middleware.service.api.dataset.ObservationUnitRow storedObservations = storedData.get(observationUnitId);

			for (final String variableName : inputData.columnKeySet()) {

				final org.generationcp.middleware.service.api.dataset.ObservationUnitData observation =
						storedObservations.getVariables().get(variableName);

				final StandardVariable standardVariable =
						this.ontologyDataManager.getStandardVariable(observation.getVariableId(), programUUID);
				final MeasurementVariable measurementVariable = this.measurementVariableTransformer.transform(standardVariable, false);
				if (!this.isValidValue(measurementVariable, inputData.get(observationUnitId, variableName))) {
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
