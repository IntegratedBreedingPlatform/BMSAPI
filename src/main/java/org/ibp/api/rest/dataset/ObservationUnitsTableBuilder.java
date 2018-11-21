package org.ibp.api.rest.dataset;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;

/**
 * Created by clarysabel on 11/21/18.
 */
@Component
public class ObservationUnitsTableBuilder {

	public static String OBS_UNIT_ID = "OBS_UNIT_ID";

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	public Table buildObservationUnitsTable (final List<List<String>> data, List<MeasurementVariableDto> datasetMeasurementVariables, List<String> warnings) {

		final BindingResult
				errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());

		final List<String> headers = data.get(0);
		final List<List<String>> values = data.subList(1, data.size());

		// check that headers contains OBS_UNIT_ID
		if (!headers.contains(OBS_UNIT_ID)) {
			errors.reject("required.header.obs.unit.id", null , "");
			throw new ApiRequestValidationException (errors.getAllErrors());
		}

		// filter measurement variables from header
		List<Integer> importMeasurementVariablesIndex = new ArrayList<>();

		for (final String header: headers) {
			for (final MeasurementVariableDto measurementVariableDto: datasetMeasurementVariables) {
				if (measurementVariableDto.getName().equals(header)) {
					if (importMeasurementVariablesIndex.contains(headers.indexOf(header))) {
						errors.reject("duplicated.measurement.variables.not.allowed", null , "");
						throw new ApiRequestValidationException (errors.getAllErrors());
					} else {
						importMeasurementVariablesIndex.add(headers.indexOf(header));
						break;
					}
				}
			}
		}

		final Integer obsUnitIdIndex = headers.indexOf(OBS_UNIT_ID);

		// Start table building, Table<row, column, value>
		final Table<String, String, String> table = HashBasedTable.create();
		for (final List<String> row: values) {
			final String observationUnitId = row.get(obsUnitIdIndex);

			if (observationUnitId.isEmpty()) {
				errors.reject("empty.observation.unit.id", null, "");
				throw new ApiRequestValidationException (errors.getAllErrors());
			}

			if (!table.containsRow(row.get(obsUnitIdIndex))) {
				for (final Integer index : importMeasurementVariablesIndex) {
					table.put(observationUnitId, headers.get(index), row.get(index));
				}
			} else {
				warnings.add(resourceBundleMessageSource.getMessage("duplicated.obs.unit.id", null, LocaleContextHolder.getLocale()));
			}
		}

		return table;
	}
}
