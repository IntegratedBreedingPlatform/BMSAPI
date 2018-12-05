package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Table;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsCollectionContaining.hasItems;


public class ObservationUnitsTableBuilderTest extends ApiUnitTestBase {

	private ObservationUnitsTableBuilder observationUnitsTableBuilder = new ObservationUnitsTableBuilder();

	@Test(expected = ApiRequestValidationException.class)
	public void testBuildFailsWhenHeaderDoesNotContainsObsUnitId() throws Exception {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("A", "B");
		final List<String> row = Arrays.asList("1", "2");
		data.add(headers);
		data.add(row);
		try {
			observationUnitsTableBuilder.build(data, null);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("required.header.obs.unit.id"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testBuildFailsWhenHeadersContainsDuplicatedvariables() throws Exception {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("OBS_UNIT_ID", "A", "A");
		final List<String> row = Arrays.asList("1", "2", "3");
		data.add(headers);
		data.add(row);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariables.add(measurementVariable);
		try {
			observationUnitsTableBuilder.build(data, measurementVariables);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("duplicated.measurement.variables.not.allowed"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testBuildFailsWhenHeadersContainsNoMeasurementVariables() throws Exception {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("OBS_UNIT_ID", "A");
		final List<String> row = Arrays.asList("1", "2");
		data.add(headers);
		data.add(row);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("B");
		measurementVariables.add(measurementVariable);
		try {
			observationUnitsTableBuilder.build(data, measurementVariables);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("no.measurement.variables.input"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testBuildFailsWhenDataContainsEmptyObsUnitId() throws Exception {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("OBS_UNIT_ID", "A");
		final List<String> row = Arrays.asList("", "2");
		data.add(headers);
		data.add(row);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariables.add(measurementVariable);
		try {
			observationUnitsTableBuilder.build(data, measurementVariables);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("empty.observation.unit.id"));
			throw e;
		}
	}

	@Test
	public void testBuildOk() throws Exception {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("OBS_UNIT_ID", "A", "B");
		final List<String> row1 = Arrays.asList("Obs1", "A1", "B1");
		final List<String> row2 = Arrays.asList("Obs2", "A2", "B2");
		final List<String> row3 = Arrays.asList("Obs2", "A3", "B3");
		data.add(headers);
		data.add(row1);
		data.add(row2);
		data.add(row3);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariables.add(measurementVariable);
		final Table<String, String, String> table = observationUnitsTableBuilder.build(data, measurementVariables);
		assertThat(table.columnKeySet(), hasSize(1));
		assertThat(table.rowKeySet(), hasSize(2));
		assertThat(table.columnKeySet(), hasItems("A"));
		assertThat(table.rowKeySet(), hasItems("Obs1", "Obs2"));
		assertThat(observationUnitsTableBuilder.getDuplicatedFoundNumber(), is(1));
		assertThat(table.get("Obs2", "A"), is("A2"));
	}

}
