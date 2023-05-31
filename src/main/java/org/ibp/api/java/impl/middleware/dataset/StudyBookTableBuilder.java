package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;


public class StudyBookTableBuilder {

	private static final String OBS_UNIT_ID = "OBS_UNIT_ID";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";

	private Integer duplicatedFoundNumber;

	public Table<String, String, String> buildObservationsTable(final List<List<String>> data, final List<MeasurementVariable> datasetMeasurementVariables) throws ApiRequestValidationException {

		this.duplicatedFoundNumber = 0;

		final BindingResult
				errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());

		final List<String> headers = data.get(0);
		final List<List<String>> values = data.subList(1, data.size());

		// check that headers contains OBS_UNIT_ID
		if (!headers.contains(OBS_UNIT_ID)) {
			errors.reject("required.header.obs.unit.id", null , "");
			throw new ApiRequestValidationException (errors.getAllErrors());
		}

		// filter measurement variables from header
		final List<Integer> importMeasurementVariablesIndex = new ArrayList<>();

		for (final String header: headers) {
			for (final MeasurementVariable measurementVariableDto: datasetMeasurementVariables) {
				if (measurementVariableDto.getName().equals(header) || measurementVariableDto.getAlias().equals(header)) {
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

		if (importMeasurementVariablesIndex.isEmpty()) {
			errors.reject("no.measurement.variables.input", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final int obsUnitIdIndex = headers.indexOf(OBS_UNIT_ID);

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
				this.duplicatedFoundNumber++;
			}
		}
		return table;
	}

	Integer getDuplicatedFoundNumber() {
		return this.duplicatedFoundNumber;
	}

	public Table<String, String, String> buildEnvironmentVariablesTable(final List<List<String>> data, final List<MeasurementVariable> datasetMeasurementVariables) throws ApiRequestValidationException {

		this.duplicatedFoundNumber = 0;

		final BindingResult
				errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());

		final List<String> headers = data.get(0);
		final List<List<String>> values = data.subList(1, data.size());

		// check that headers contains TRIAL_INSTANCE
		if (!headers.contains(TRIAL_INSTANCE)) {
			errors.reject("required.header.trial.instance", null , "");
			throw new ApiRequestValidationException (errors.getAllErrors());
		}

		// filter measurement variables from header
		final List<Integer> importMeasurementVariablesIndex = new ArrayList<>();

		for (final String header: headers) {
			for (final MeasurementVariable measurementVariableDto: datasetMeasurementVariables) {
				if (measurementVariableDto.getName().equals(header) || measurementVariableDto.getAlias().equals(header)) {
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

		if (importMeasurementVariablesIndex.isEmpty()) {
			errors.reject("no.measurement.variables.input", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final int trialInstanceIndex = headers.indexOf(TRIAL_INSTANCE);

		// Start table building, Table<row, column, value>
		final Table<String, String, String> table = HashBasedTable.create();
		for (final List<String> row: values) {
			final String trialInstanceNumber = row.get(trialInstanceIndex);

			if (trialInstanceNumber.isEmpty()) {
				errors.reject("empty.trial.instance", null, "");
				throw new ApiRequestValidationException (errors.getAllErrors());
			}

			if (!table.containsRow(row.get(trialInstanceIndex))) {
				for (final Integer index : importMeasurementVariablesIndex) {
					table.put(trialInstanceNumber, headers.get(index), row.get(index));
				}
			} else {
				this.duplicatedFoundNumber++;
			}
		}
		return table;
	}

}
