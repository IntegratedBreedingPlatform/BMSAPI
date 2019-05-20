package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.pojos.SortedPageRequest;
import org.generationcp.middleware.pojos.dms.DatasetType;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsParamDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.PreconditionFailedException;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationsTableValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyListOf;

public class DatasetServiceImplTest {

	private static final int TEST_STUDY_IDENTIFIER = 2013;
	public static final String OBS_UNIT_ID = "OBS_UNIT_ID";
	public static final String ENTRY_CODE = "ENTRY_CODE";
	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String ENTRY_TYPE = "ENTRY_TYPE";
	public static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	public static final String FIELD_MAP_COLUMN = "FieldMapColumn";
	public static final String FIELD_MAP_RANGE = "FIELD_MAP_RANGE";
	public static final String COL = "COL";
	public static final String ROW = "ROW";
	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String REP_NO = "REP_NO";
	private static final String STOCK_ID = "STOCK_ID";
	private static final String FACT1 = "FACT1";
	public static final String DATASET_NAME = "ABC";
	public static final int PARENT_ID = 123;

	@Mock
	private DatasetService middlewareDatasetService;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private ObservationValidator observationValidator;

	@Spy
	private ObservationsTableValidator observationsTableValidator;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private MeasurementVariable variable;

	@Mock
	private StandardVariable standardVariable;

	@Mock
	private MeasurementVariableTransformer measurementVariableTransformer;

	@InjectMocks
	private DatasetServiceImpl studyDatasetService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testCountPhenotypes() {

		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();

		final List<Integer> traitIds = Arrays.asList(random.nextInt(), random.nextInt(), random.nextInt());

		this.studyDatasetService.countObservationsByVariables(studyId, datasetId, traitIds);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.middlewareDatasetService).countObservationsByVariables(datasetId, traitIds);
	}

	@Test
	public void testCountPhenotypesByInstance() {

		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int instanceId = random.nextInt();
		this.studyDatasetService.countObservationsByInstance(studyId, datasetId, instanceId);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId, false);
		Mockito.verify(this.instanceValidator).validate(datasetId, Sets.newHashSet(instanceId));
		Mockito.verify(this.middlewareDatasetService).countObservationsByInstance(datasetId, instanceId);
	}

	@Test
	public void testAddDatasetVariable() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final VariableType variableType = VariableType.SELECTION_METHOD;
		final int variableTypeId = variableType.getId();
		final int variableId = random.nextInt();
		final String alias = RandomStringUtils.randomAlphabetic(20);
		final DatasetVariable datasetVariable = new DatasetVariable(variableTypeId, variableId, alias);
		Mockito.doReturn(this.standardVariable).when(this.datasetValidator)
			.validateDatasetVariable(studyId, datasetId, true, datasetVariable, false);
		Mockito.doReturn(this.variable).when(this.measurementVariableTransformer).transform(this.standardVariable, false);

		this.studyDatasetService.addDatasetVariable(studyId, datasetId, datasetVariable);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDatasetVariable(studyId, datasetId, true, datasetVariable, false);
		Mockito.verify(this.middlewareDatasetService).addDatasetVariable(datasetId, variableId, variableType, alias);
		Mockito.verify(this.measurementVariableTransformer).transform(this.standardVariable, false);
		Mockito.verify(this.variable).setName(alias);
		Mockito.verify(this.variable).setVariableType(variableType);
		Mockito.verify(this.variable).setRequired(false);
	}

	@Test
	public void testRemoveVariables() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final List<Integer> variableIds = Arrays.asList(random.nextInt(), random.nextInt(), random.nextInt());
		this.studyDatasetService.removeDatasetVariables(studyId, datasetId, variableIds);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateExistingDatasetVariables(studyId, datasetId, true, variableIds);
		Mockito.verify(this.middlewareDatasetService).removeDatasetVariables(datasetId, variableIds);
	}

	@Test
	public void testAddObservation() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final ObservationDto observationDto = new ObservationDto();
		observationDto.setVariableId(random.nextInt());
		this.studyDatasetService.createObservation(studyId, datasetId, observationUnitId, observationDto);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator)
			.validateExistingDatasetVariables(studyId, datasetId, true, Arrays.asList(observationDto.getVariableId()));
		Mockito.verify(this.observationValidator).validateObservationUnit(datasetId, observationUnitId);
		Mockito.verify(this.middlewareDatasetService).createObservation(observationDto);
	}

	@Test
	public void testUpdateObservation() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final int observationId = random.nextInt();
		final ObservationDto observationDto = new ObservationDto();
		observationDto.setCategoricalValueId(random.nextInt());
		observationDto.setValue(random.toString());
		this.studyDatasetService.updateObservation(studyId, datasetId, observationId, observationUnitId, observationDto);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId, true);
		Mockito.verify(this.observationValidator).validateObservation(studyId, datasetId, observationUnitId, observationId,
			observationDto);
		Mockito.verify(this.middlewareDatasetService)
			.updatePhenotype(observationId, observationDto);
	}

	@Test
	public void testDeleteObservation() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final int observationId = random.nextInt();
		this.studyDatasetService.deleteObservation(studyId, datasetId, observationUnitId, observationId);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId, true);
		Mockito.verify(this.observationValidator).validateObservation(studyId, datasetId, observationUnitId, observationId, null);
		Mockito.verify(this.middlewareDatasetService).deletePhenotype(observationId);
	}

	@Test
	public void testGetInstanceObservationUnitRowsMap() {
		final Map<Integer, List<ObservationUnitRow>> instanceObservationUnitRowsMap = new HashMap<>();
		instanceObservationUnitRowsMap.put(1, this.mockObservationUnitRowList());
		Mockito.doReturn(instanceObservationUnitRowsMap).when(this.middlewareDatasetService)
			.getInstanceIdToObservationUnitRowsMap(1, 1, Arrays.asList(1));
		final Map<Integer, List<org.ibp.api.rest.dataset.ObservationUnitRow>> results =
			this.studyDatasetService.getInstanceObservationUnitRowsMap(1, 1, Arrays.asList(1));
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(1, results.get(1).size());
		Assert.assertEquals(this.mapObservationUnitRows(instanceObservationUnitRowsMap.get(1)), results.get(1));
	}

	@Test
	public void testValidateStudyDatasetAndInstances() {
		this.studyDatasetService.validateStudyDatasetAndInstances(1, 1, Arrays.asList(1), true);
		Mockito.verify(this.studyValidator).validate(1, false);
		Mockito.verify(this.datasetValidator).validateDataset(1, 1, true);
		Mockito.verify(this.instanceValidator).validate(1, new HashSet<>(Arrays.asList(1)));
	}

	@Test
	public void testGetObservations() {
		final List<ObservationUnitRow> observationDtoTestData = this.mockObservationUnitRowList();

		Mockito.doReturn(observationDtoTestData).when(this.middlewareDatasetService)
			.getObservationUnitRows(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.any());
		final List<org.ibp.api.rest.dataset.ObservationUnitRow> actualObservations =
			this.studyDatasetService.getObservationUnitRows(TEST_STUDY_IDENTIFIER, 1, new ObservationUnitsSearchDTO());

		Assert.assertEquals(this.mapObservationUnitRows(observationDtoTestData), actualObservations);

	}

	private List<ObservationUnitRow> mockObservationUnitRowList() {
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setAction("1");
		observationUnitRow.setGid(2);
		observationUnitRow.setDesignation("ABCD");
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> variables = new HashMap<>();
		variables.put(TRIAL_INSTANCE, new org.generationcp.middleware.service.api.dataset.ObservationUnitData("10"));
		variables.put(ENTRY_TYPE, new org.generationcp.middleware.service.api.dataset.ObservationUnitData("T"));
		variables.put(ENTRY_NO, new org.generationcp.middleware.service.api.dataset.ObservationUnitData("10000"));
		variables.put(ENTRY_CODE, new org.generationcp.middleware.service.api.dataset.ObservationUnitData("12"));
		variables.put(REP_NO, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		variables.put(PLOT_NO, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		variables.put(BLOCK_NO, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		variables.put(ROW, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		variables.put(COL, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		variables.put(OBS_UNIT_ID, new org.generationcp.middleware.service.api.dataset.ObservationUnitData("obunit123"));
		variables.put(FIELD_MAP_COLUMN, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		variables.put(FIELD_MAP_RANGE, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		variables.put(STOCK_ID, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		variables.put(FACT1, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());
		observationUnitRow.setVariables(variables);

		return Lists.newArrayList(observationUnitRow);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testImportDatasetFails_DatasetWithNoVariables() throws Exception {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", "A");
		final List<String> row = Arrays.asList("1", "1");
		data.add(header);
		data.add(row);
		observationsPutRequestInput.setData(data);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId, true);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("no.variables.dataset"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testImportDatasetFails_NoObsUnitIdMatches() throws Exception {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", "A");
		final List<String> row = Arrays.asList("1", "1");
		data.add(header);
		data.add(row);
		observationsPutRequestInput.setData(data);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariables.add(measurementVariable);
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId, true);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(this.middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, Arrays.asList("1")))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("none.obs.unit.id.matches"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testImportDatasetFails_DataTypeInconsistency() throws Exception {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", "A");
		final List<String> row = Arrays.asList("1", "A");
		data.add(header);
		data.add(row);
		observationsPutRequestInput.setData(data);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataType("Numeric");
		measurementVariables.add(measurementVariable);
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put("A", new org.generationcp.middleware.service.api.dataset.ObservationUnitData("1"));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("A", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId, true);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(this.middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, Arrays.asList("1")))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCellValue"));
			throw e;
		}
	}

	@Test(expected = PreconditionFailedException.class)
	public void testImportDatasetFails_WarningsOverwrittingDataFound() throws Exception {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", "A");
		final List<String> row1 = Arrays.asList("1", "1");

		data.add(header);
		data.add(row1);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		observationsPutRequestInput.setDraftMode(false);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataType("Numeric");
		measurementVariables.add(measurementVariable);
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put("A", new org.generationcp.middleware.service.api.dataset.ObservationUnitData("2"));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId, true);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(this.middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, Arrays.asList("1")))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final PreconditionFailedException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.overwrite.data"));
			throw e;
		}
	}

	@Test(expected = PreconditionFailedException.class)
	public void testImportDatasetFails_WarningsDuplicatedObsUnitId() throws Exception {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", "A");
		final List<String> row1 = Arrays.asList("1", "1");
		final List<String> row2 = Arrays.asList("1", "1");

		data.add(header);
		data.add(row1);
		data.add(row2);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataType("Numeric");
		measurementVariables.add(measurementVariable);
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put("A", new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId, true);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(this.middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, Arrays.asList("1")))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final PreconditionFailedException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("duplicated.obs.unit.id"));
			throw e;
		}
	}

	@Test(expected = PreconditionFailedException.class)
	public void testImportDatasetFails_WarningsObsUnitIdNotBelongToDataset() throws Exception {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", "A");
		final List<String> row1 = Arrays.asList("1", "1");
		final List<String> row2 = Arrays.asList("2", "1");

		data.add(header);
		data.add(row1);
		data.add(row2);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias("A");
		measurementVariable.setDataType("Numeric");
		measurementVariables.add(measurementVariable);
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put("A", new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId, true);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(
			this.middlewareDatasetService.getObservationUnitsAsMap(anyInt(), anyListOf(MeasurementVariable.class), anyListOf(String.class)))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final PreconditionFailedException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("some.obs.unit.id.matches"));
			throw e;
		}
	}

	private List<org.ibp.api.rest.dataset.ObservationUnitRow> mapObservationUnitRows(
		final List<ObservationUnitRow> observationDtoTestData) {
		final ModelMapper observationUnitRowMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<org.ibp.api.rest.dataset.ObservationUnitRow> list = new ArrayList<>();
		for (final org.generationcp.middleware.service.api.dataset.ObservationUnitRow dto : observationDtoTestData) {
			final Map<String, ObservationUnitData> datas = new HashMap<>();
			for (final String data : dto.getVariables().keySet()) {
				datas.put(data, observationUnitRowMapper.map(dto.getVariables().get(data), ObservationUnitData.class));
			}
			final org.ibp.api.rest.dataset.ObservationUnitRow
				observationUnitRow = observationUnitRowMapper.map(dto, org.ibp.api.rest.dataset.ObservationUnitRow.class);
			observationUnitRow.setVariables(datas);
			list.add(observationUnitRow);
		}
		return list;
	}

	@Test
	public void testGenerateDataset() {

		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setName(DATASET_NAME);
		datasetDTO.setParentDatasetId(PARENT_ID);

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(8206);
		datasetDTO.setVariables(Lists.newArrayList(measurementVariable));

		final StudyInstance studyInstance1 = new StudyInstance();
		studyInstance1.setInstanceDbId(1);

		final StudyInstance studyInstance2 = new StudyInstance();
		studyInstance2.setInstanceDbId(2);

		final StudyInstance studyInstance3 = new StudyInstance();
		studyInstance3.setInstanceDbId(3);

		datasetDTO.setInstances(Lists.newArrayList(studyInstance1, studyInstance2, studyInstance3));
		Mockito.doReturn(datasetDTO).when(this.middlewareDatasetService).generateSubObservationDataset(TEST_STUDY_IDENTIFIER, DATASET_NAME,
			DatasetType.QUADRAT_SUBOBSERVATIONS, Lists.newArrayList(1, 2, 3), 8206, 3, PARENT_ID);

		// FIXME test the real method, not the mock IBP-2231
		final DatasetDTO dto =
			this.middlewareDatasetService.generateSubObservationDataset(TEST_STUDY_IDENTIFIER, DATASET_NAME,
				DatasetType.QUADRAT_SUBOBSERVATIONS, Lists.newArrayList(1, 2, 3), 8206, 3, PARENT_ID);

		Assert.assertNotNull(dto);
		Assert.assertTrue(dto.getName().equalsIgnoreCase(datasetDTO.getName()));
		Assert.assertTrue(dto.getParentDatasetId().equals(datasetDTO.getParentDatasetId()));
		Assert.assertTrue(dto.getInstances().size() == 3);
		Assert.assertTrue(dto.getVariables().size() == 1);
		Assert.assertTrue(CollectionUtils.isEqualCollection(dto.getInstances(), datasetDTO.getInstances()));
		Assert.assertTrue(CollectionUtils.isEqualCollection(dto.getVariables(), datasetDTO.getVariables()));
	}

	@Test
	public void testAcceptDraftDataByVariable() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final ObservationDto observationDto = new ObservationDto();
		observationDto.setCategoricalValueId(random.nextInt());
		observationDto.setValue(random.toString());
		final ObservationUnitsParamDTO paramDTO = new ObservationUnitsParamDTO();
		final int instanceId = random.nextInt(10000);

		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();

		final SortedPageRequest sortedRequest = new SortedPageRequest();
		sortedRequest.setPageNumber(1);
		sortedRequest.setPageSize(100);
		searchDTO.setSortedRequest(sortedRequest);
		searchDTO.setInstanceId(instanceId);

		paramDTO.setObservationUnitsSearchDTO(searchDTO);
		paramDTO.setNewValue("123");
		paramDTO.setNewCategoricalValueId(12345);
		searchDTO.setDatasetId(datasetId);
		paramDTO.getObservationUnitsSearchDTO().getFilter().setVariableId(555);
		this.studyDatasetService.acceptDraftDataFilteredByVariable(studyId, datasetId, searchDTO);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId, true);

		Mockito.verify(this.middlewareDatasetService).acceptDraftDataFilteredByVariable(datasetId, searchDTO, studyId);
	}

	@Test
	public void testSetValueToVariable() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final ObservationDto observationDto = new ObservationDto();
		observationDto.setCategoricalValueId(random.nextInt());
		observationDto.setValue(random.toString());
		final ObservationUnitsParamDTO paramDTO = new ObservationUnitsParamDTO();
		final int instanceId = random.nextInt(10000);

		final ObservationUnitsSearchDTO searchDTO = new ObservationUnitsSearchDTO();

		final SortedPageRequest sortedRequest = new SortedPageRequest();
		sortedRequest.setPageNumber(1);
		sortedRequest.setPageSize(100);
		searchDTO.setSortedRequest(sortedRequest);
		searchDTO.setInstanceId(instanceId);

		paramDTO.setObservationUnitsSearchDTO(searchDTO);
		paramDTO.setNewValue("123");
		paramDTO.setNewCategoricalValueId(12345);
		searchDTO.setDatasetId(datasetId);
		paramDTO.getObservationUnitsSearchDTO().getFilter().setVariableId(555);
		this.studyDatasetService.setValueToVariable(studyId, datasetId, paramDTO);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId, true);

		Mockito.verify(this.middlewareDatasetService).setValueToVariable(datasetId, paramDTO, studyId);
	}

}
