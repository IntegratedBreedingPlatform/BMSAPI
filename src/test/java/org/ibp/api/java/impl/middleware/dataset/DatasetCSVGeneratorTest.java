package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DatasetCSVGeneratorTest {

	public static final int RANDOM_STRING_LENGTH = 10;

	private List<ObservationUnitRow> observationUnitRows;
	private final String variableName1 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableName2 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableValue1 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableValue2 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final List<String> headerNames = Arrays.asList(this.variableName1, this.variableName2);
	private List<MeasurementVariable> measurementVariables;

	@InjectMocks
	private DatasetCSVGenerator datasetCSVGenerator;
	private Integer studyId;
	private DatasetDTO datasetDTO;

	@Before
	public void setUp() {

		final ObservationUnitData observationUnitData1 = new ObservationUnitData();
		observationUnitData1.setValue(this.variableValue1);
		final ObservationUnitData observationUnitData2 = new ObservationUnitData();
		observationUnitData2.setValue(this.variableValue2);
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> variables = new HashMap<>();
		variables.put(this.variableName1, observationUnitData1);
		variables.put(this.variableName2, observationUnitData2);
		observationUnitRow.setVariables(variables);
		this.observationUnitRows = Arrays.asList(observationUnitRow);

		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		measurementVariable1.setAlias(this.variableName1);
		measurementVariable1.setName(this.variableName1);

		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		measurementVariable2.setAlias(this.variableName2);
		measurementVariable2.setName(this.variableName2);

		this.measurementVariables = Arrays.asList(measurementVariable1, measurementVariable2);

	}

	@Test
	public void testGenerateCSVFile() throws IOException {

		final CSVWriter csvWriter = Mockito.mock(CSVWriter.class);
		final String fileNameFullPath = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
		this.datasetCSVGenerator.setCSVWriter(csvWriter);
		this.datasetCSVGenerator.generateFile(this.studyId, this.datasetDTO,
			this.measurementVariables, this.observationUnitRows, fileNameFullPath);

		Mockito.verify(csvWriter).writeAll(Mockito.anyListOf(String[].class));
		Mockito.verify(csvWriter).close();

	}

	@Test
	public void testGetColumnValues() {

		final String[] result = this.datasetCSVGenerator.getColumnValues(this.observationUnitRows.get(0), this.measurementVariables);
		assertEquals(result.length, this.headerNames.size());
		assertEquals(this.variableValue1, result[0]);
		assertEquals(this.variableValue2, result[1]);
	}

	@Test
	public void testGetHeaderNames() {
		final List<String> result = this.datasetCSVGenerator.getHeaderNames(this.measurementVariables);
		assertEquals(this.measurementVariables.size(), result.size());
		assertTrue(result.contains(this.variableName1));
		assertTrue(result.contains(this.variableName2));
	}
}
