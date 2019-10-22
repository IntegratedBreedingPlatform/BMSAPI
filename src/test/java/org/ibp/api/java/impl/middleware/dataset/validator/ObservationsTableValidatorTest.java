package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObservationsTableValidatorTest {

	private final ObservationsTableValidator observationsTableValidator = new ObservationsTableValidator();

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateListFailsWhenDataHasLessThan2Elements() {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("A", "B");
		data.add(headers);
		try {
			observationsTableValidator.validateList(data);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("table.should.have.at.least.two.rows"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateListFailsWhenDataElementsHasDifferentLength() {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("A", "B");
		final List<String> rows = Collections.singletonList("A");
		data.add(headers);
		data.add(rows);
		try {
			observationsTableValidator.validateList(data);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("table.format.inconsistency"));
			throw e;
		}
	}

	@Test
	public void testValidateListOk() {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("A", "B");
		final List<String> rows = Arrays.asList("A", "B");
		data.add(headers);
		data.add(rows);
		observationsTableValidator.validateList(data);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateObservationsValuesDataTypesInvalidNumberGivenRanges() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "A");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setMinRange(1D);
		measurementVariable.setMaxRange(2D);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		try {
			observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCellValue"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateObservationsValuesDataTypesInvalidNumberGivenVariableType() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "A");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataType("Numeric");
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		try {
			observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCellValue"));
			throw e;
		}
	}

	@Test
	public void testValidateObservationsValuesDataTypesValidNumber() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "1");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataType("Numeric");
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
	}

	@Test
	public void testValidateObservationsValuesDataTypesMissingValue() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "missing");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataType("Numeric");
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateObservationsValuesDataTypesInvalidDate() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "A");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataTypeId(TermId.DATE_VARIABLE.getId());
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		try {
			observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCellValue"));
			throw e;
		}
	}

	@Test
	public void testValidateObservationsValuesDataTypesValidDate() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "20181010");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataTypeId(TermId.DATE_VARIABLE.getId());
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateObservationsValuesDataTypesNoCategoricalScaleDefinedWithImportValue() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "1");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		try {
			observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCategoricalValue"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateObservationsValuesDataTypesNoCategoricalScaleDefinedNoImportValue() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		try {
			observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCategoricalValue"));
			throw e;
		}
	}

	@Test
	public void testValidateObservationsValuesDataWithCategoricalDefined() {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "c");
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		final List<ValueReference> possibleValue = new ArrayList<>();
		possibleValue.add(new ValueReference("c", "category1"));
		measurementVariable.setPossibleValues(possibleValue);
		observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
	}

}
