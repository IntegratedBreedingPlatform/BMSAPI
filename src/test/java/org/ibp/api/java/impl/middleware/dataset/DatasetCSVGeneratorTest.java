package org.ibp.api.java.impl.middleware.dataset;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DatasetCSVGeneratorTest {

	private static final int RANDOM_STRING_LENGTH = 10;
	private static final String STUDY_DETAIL_VALUE = "Study Detail Value";
	private static final String ENVIRONMENT_VALUE = "Environment Value";
	private static final String STUDY_DETAIL_CATEGORICAL_VALUE = "study detail value 1";
	private static final String ENVIRONMENT_DETAIL_CATEGORICAL_VALUE = "environment detail value 1";

	private List<ObservationUnitRow> observationUnitRows;
	private final String variableName1 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableName2 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableName3 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableName4 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variablAlias1 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variablAlias2 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variablAlias3 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variablAlias4 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableValue1 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableValue2 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableValue3 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableValue4 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final List<String> headerNames = Arrays.asList(this.variableName1, this.variableName2, this.variableName3, this.variableName4);
	private List<MeasurementVariable> measurementVariables;

	@InjectMocks
	private DatasetCSVGenerator datasetCSVGenerator;

	@Before
	public void setUp() {

		final ObservationUnitData observationUnitData1 = new ObservationUnitData();
		observationUnitData1.setValue(this.variableValue1);
		final ObservationUnitData observationUnitData2 = new ObservationUnitData();
		observationUnitData2.setValue(this.variableValue2);
		final ObservationUnitData observationUnitData3 = new ObservationUnitData();
		observationUnitData3.setValue(this.variableValue3);
		final ObservationUnitData observationUnitData4 = new ObservationUnitData();
		observationUnitData4.setValue(this.variableValue4);
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> variables = new HashMap<>();
		variables.put(this.variableName1, observationUnitData1);
		variables.put(this.variableName2, observationUnitData2);
		observationUnitRow.setVariables(variables);

		final Map<String, ObservationUnitData> environmentVariables = new HashMap<>();
		environmentVariables.put(this.variableName3, observationUnitData3);
		environmentVariables.put(this.variableName4, observationUnitData4);
		observationUnitRow.setEnvironmentVariables(environmentVariables);
		this.observationUnitRows = Arrays.asList(observationUnitRow);

		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		measurementVariable1.setAlias(this.variablAlias1);
		measurementVariable1.setName(this.variableName1);
		measurementVariable1.setVariableType(VariableType.TRAIT);

		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		measurementVariable2.setAlias(this.variablAlias2);
		measurementVariable2.setName(this.variableName2);
		measurementVariable2.setVariableType(VariableType.STUDY_DETAIL);
		final List<ValueReference> studyDetailPossibleValues = new ArrayList<>();
		studyDetailPossibleValues.add(new ValueReference(1, DatasetCSVGeneratorTest.STUDY_DETAIL_CATEGORICAL_VALUE));
		measurementVariable2.setPossibleValues(studyDetailPossibleValues);
		measurementVariable2.setDataType(DataType.CATEGORICAL_VARIABLE.getName());

		final MeasurementVariable measurementVariable3 = new MeasurementVariable();
		measurementVariable3.setAlias(this.variablAlias3);
		measurementVariable3.setName(this.variableName3);
		measurementVariable3.setVariableType(VariableType.ENVIRONMENT_DETAIL);
		final List<ValueReference> environmentDetailPossibleValues = new ArrayList<>();
		environmentDetailPossibleValues.add(new ValueReference(1, ENVIRONMENT_DETAIL_CATEGORICAL_VALUE));
		measurementVariable3.setPossibleValues(environmentDetailPossibleValues);
		measurementVariable3.setDataType(DataType.CATEGORICAL_VARIABLE.getName());

		final MeasurementVariable measurementVariable4 = new MeasurementVariable();
		measurementVariable4.setAlias(this.variablAlias4);
		measurementVariable4.setName(this.variableName4);
		measurementVariable4.setVariableType(VariableType.ENVIRONMENT_CONDITION);

		this.measurementVariables = Arrays.asList(measurementVariable1, measurementVariable2, measurementVariable3, measurementVariable4);

	}

	@Test
	public void testGetColumnValues() {
		final Map<String, Map<String, String>> categoricalValuesMap = new HashMap<>();

		final Map<String, String> categoricalValuesForStudyDetailVariable = new HashMap<>();
		categoricalValuesForStudyDetailVariable.put(this.variableValue2, STUDY_DETAIL_VALUE);
		categoricalValuesMap.put(this.variableName2, categoricalValuesForStudyDetailVariable);

		final Map<String, String> categoricalValuesForEnvironmentVariable = new HashMap<>();
		categoricalValuesForEnvironmentVariable.put(this.variableValue3, ENVIRONMENT_VALUE);
		categoricalValuesMap.put(this.variableName3, categoricalValuesForEnvironmentVariable);

		final String[] result = this.datasetCSVGenerator.getColumnValues(this.observationUnitRows.get(0), this.measurementVariables, categoricalValuesMap);
		assertEquals(result.length, this.headerNames.size());
		assertEquals(this.variableValue1, result[0]);
		assertEquals(STUDY_DETAIL_VALUE, result[1]);
		assertEquals(ENVIRONMENT_VALUE, result[2]);
		assertEquals(this.variableValue4, result[3]);
	}

	@Test
	public void testGetHeaderNames() {
		final List<String> result = this.datasetCSVGenerator.getHeaderNames(this.measurementVariables);
		assertEquals(this.measurementVariables.size(), result.size());
		assertTrue(result.contains(this.variablAlias1));
		assertTrue(result.contains(this.variablAlias2));
	}

	@Test
	public void testGenerateMultiInstanceFile() throws IOException {
		final String filename = "filename";
		final File file = this.datasetCSVGenerator
			.generateMultiInstanceFile(new HashMap<>(), new ArrayList<>(), filename);
		Assert.assertEquals(filename, file.getName());
	}

	@Test
	public void testGenerateSingleInstanceFile() throws IOException {
		final String filename = "filename";
		final File file = this.datasetCSVGenerator
			.generateSingleInstanceFile(null, null, new ArrayList<>(), new ArrayList<>(), filename,
				null);
		Assert.assertEquals(filename, file.getName());
	}

	@Test
	public void testGetStudyAndEnvironmentCategoricalValuesMap() {
		final Map<String, Map<String, String>> categoricalValuesMap = this.datasetCSVGenerator.getStudyAndEnvironmentCategoricalValuesMap(this.measurementVariables);
		Assert.assertEquals(2, categoricalValuesMap.size());
		Assert.assertNotNull(categoricalValuesMap.get(this.variableName2));
		Assert.assertEquals(STUDY_DETAIL_CATEGORICAL_VALUE, categoricalValuesMap.get(this.variableName2).get("1"));
		Assert.assertNotNull(categoricalValuesMap.get(this.variableName3));
		Assert.assertEquals(ENVIRONMENT_DETAIL_CATEGORICAL_VALUE, categoricalValuesMap.get(this.variableName3).get("1"));
	}
}
