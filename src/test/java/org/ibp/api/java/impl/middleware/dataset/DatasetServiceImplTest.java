package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v1.observation.ObservationDTO;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitEntryReplaceRequest;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsParamDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.PreconditionFailedException;
import org.ibp.api.java.impl.middleware.common.validator.SearchCompositeDtoValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationsTableValidator;
import org.ibp.api.java.impl.middleware.inventory.common.validator.InventoryCommonValidator;
import org.ibp.api.java.impl.middleware.inventory.study.StudyTransactionsService;
import org.ibp.api.java.impl.middleware.study.ObservationUnitsMetadata;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
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
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

public class DatasetServiceImplTest {

	private static final int TEST_STUDY_IDENTIFIER = 2013;
	private static final String OBS_UNIT_ID = "OBS_UNIT_ID";
	private static final String ENTRY_CODE = "ENTRY_CODE";
	public static final String ENTRY_NO = "ENTRY_NO";
	private static final String ENTRY_TYPE = "ENTRY_TYPE";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String FIELD_MAP_COLUMN = "FieldMapColumn";
	private static final String FIELD_MAP_RANGE = "FIELD_MAP_RANGE";
	private static final String COL = "COL";
	private static final String ROW = "ROW";
	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String REP_NO = "REP_NO";
	private static final String STOCK_ID = "STOCK_ID";
	private static final String FACT1 = "FACT1";
	private static final String ENVFACTOR1 = "ENVFACTOR1";
	public static final String DATASET_NAME = "ABC";
	public static final int PARENT_ID = 123;
	public static final String ALIAS = "ALIAS";

	@Mock
	private DatasetService middlewareDatasetService;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private StudyEntryValidator studyEntryValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private ObservationValidator observationValidator;

	@Spy
	private ObservationsTableValidator observationsTableValidator;

	@Mock
	private SearchCompositeDtoValidator searchCompositeDtoValidator;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private MeasurementVariable variable;

	@Mock
	private StandardVariable standardVariable;

	@Mock
	private MeasurementVariableTransformer measurementVariableTransformer;

	@Mock
	private InventoryCommonValidator inventoryCommonValidator;

	@Mock
	private StudyTransactionsService studyTransactionsService;

	@Mock
	private StudyService studyService;

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
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId);
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
			.validateDatasetVariable(studyId, datasetId, datasetVariable, false);
		Mockito.doReturn(this.variable).when(this.measurementVariableTransformer).transform(this.standardVariable, false);

		this.studyDatasetService.addDatasetVariable(studyId, datasetId, datasetVariable);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDatasetVariable(studyId, datasetId, datasetVariable, false);
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
		Mockito.verify(this.datasetValidator).validateExistingDatasetVariables(studyId, datasetId, variableIds);
		Mockito.verify(this.middlewareDatasetService).removeDatasetVariables(studyId, datasetId, variableIds);
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
			.validateExistingDatasetVariables(studyId, datasetId, Arrays.asList(observationDto.getVariableId()));
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
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId);
		Mockito.verify(this.observationValidator).validateObservation(datasetId, observationUnitId, observationId,
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
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId);
		Mockito.verify(this.observationValidator).validateObservation(datasetId, observationUnitId, observationId, null);
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
		Assert.assertNotNull(results.get(1).get(0).getVariables());
		Assert.assertNotNull(results.get(1).get(0).getEnvironmentVariables());
	}

	@Test
	public void testValidateStudyDatasetAndInstances() {
		this.studyDatasetService.validateStudyDatasetAndInstances(1, 1, Arrays.asList(1));
		Mockito.verify(this.studyValidator).validate(1, false);
		Mockito.verify(this.datasetValidator).validateDataset(1, 1);
		Mockito.verify(this.instanceValidator).validate(1, new HashSet<>(Arrays.asList(1)));
	}

	@Test
	public void testGetObservations() {
		final List<ObservationUnitRow> observationDtoTestData = this.mockObservationUnitRowList();

		Mockito.doReturn(observationDtoTestData).when(this.middlewareDatasetService)
			.getObservationUnitRows(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.any(), ArgumentMatchers.any());
		final List<org.ibp.api.rest.dataset.ObservationUnitRow> actualObservations =
			this.studyDatasetService.getObservationUnitRows(TEST_STUDY_IDENTIFIER, 1, new ObservationUnitsSearchDTO(), Mockito.mock(
				PageRequest.class));

		Assert.assertEquals(this.mapObservationUnitRows(observationDtoTestData), actualObservations);

	}

	@Test
	public void testGetObservationUnitRowsAsMapList() {
		final List<Map<String, Object>> listOfMap = new ArrayList<>();

		Mockito.doReturn(listOfMap).when(this.middlewareDatasetService)
			.getObservationUnitRowsAsMapList(ArgumentMatchers.eq(TEST_STUDY_IDENTIFIER), ArgumentMatchers.eq(1), ArgumentMatchers.any(ObservationUnitsSearchDTO.class), ArgumentMatchers.isNull());
		final List<Map<String, Object>> result =
			this.studyDatasetService.getObservationUnitRowsAsMapList(TEST_STUDY_IDENTIFIER, 1, new ObservationUnitsSearchDTO());

		Assert.assertSame(listOfMap, result);

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

		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> environmentVariables = new HashMap<>();
		environmentVariables.put(ENVFACTOR1, new org.generationcp.middleware.service.api.dataset.ObservationUnitData());

		observationUnitRow.setVariables(variables);
		observationUnitRow.setEnvironmentVariables(environmentVariables);

		return Lists.newArrayList(observationUnitRow);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testImportDatasetFails_DatasetWithNoVariables() {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS);
		final List<String> row = Arrays.asList("1", "1");
		data.add(header);
		data.add(row);
		observationsPutRequestInput.setData(data);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("no.variables.dataset"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testImportDatasetFails_NoObsUnitIdMatches() {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS);
		final List<String> row = Arrays.asList("1", "1");
		data.add(header);
		data.add(row);
		observationsPutRequestInput.setData(data);
		final List<MeasurementVariable> measurementVariables = this.createMeasurementVariableList();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
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
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS);
		final List<String> row = Arrays.asList("1", ALIAS);
		data.add(header);
		data.add(row);
		observationsPutRequestInput.setData(data);
		final List<MeasurementVariable> measurementVariables = this.createMeasurementVariableList();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(ALIAS, new org.generationcp.middleware.service.api.dataset.ObservationUnitData("1"));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put(ALIAS, observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(this.middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, Arrays.asList("1")))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalid.cell.numeric.value"));
			throw e;
		}
	}

	@Test(expected = PreconditionFailedException.class)
	public void testImportDatasetFails_WarningsOverwrittingDataFound() {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS);
		final List<String> row1 = Arrays.asList("1", "1");

		data.add(header);
		data.add(row1);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		observationsPutRequestInput.setDraftMode(false);
		final List<MeasurementVariable> measurementVariables = this.createMeasurementVariableList();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(ALIAS, new org.generationcp.middleware.service.api.dataset.ObservationUnitData("2"));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
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
	public void testImportDatasetFails_WarningsDuplicatedObsUnitId() {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS);
		final List<String> row1 = Arrays.asList("1", "1");
		final List<String> row2 = Arrays.asList("1", "1");

		data.add(header);
		data.add(row1);
		data.add(row2);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		final List<MeasurementVariable> measurementVariables = this.createMeasurementVariableList();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(ALIAS, new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
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

	private List<MeasurementVariable> createMeasurementVariableList() {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(this.createMeasurementVariable());
		return measurementVariables;
	}

	private MeasurementVariable createMeasurementVariable() {
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setAlias(ALIAS);
		measurementVariable.setName("Variable Name");
		measurementVariable.setDataType("Numeric");
		measurementVariable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		return measurementVariable;
	}

	@Test(expected = PreconditionFailedException.class)
	public void testImportDatasetFails_WarningsObsUnitIdNotBelongToDataset() {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS);
		final List<String> row1 = Arrays.asList("1", "1");
		final List<String> row2 = Arrays.asList("2", "1");

		data.add(header);
		data.add(row1);
		data.add(row2);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		final List<MeasurementVariable> measurementVariables = this.createMeasurementVariableList();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(ALIAS, new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
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

	@Test
	public void testImportDataset_ValidDateFormat() {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS, "KSU_Date");
		final List<String> row1 = Arrays.asList("1", "1", "5/9/20");
		final List<String> row2 = Arrays.asList("2", "1", "20201201");
		data.add(header);
		data.add(row1);
		data.add(row2);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = this.createMeasurementVariable();
		final MeasurementVariable dateMV = new MeasurementVariable();
		dateMV.setAlias("KSU_Date");
		dateMV.setName("KSU_Date");
		dateMV.setDataType("Date");
		dateMV.setDataTypeId(TermId.DATE_VARIABLE.getId());
		measurementVariables.add(measurementVariable);
		measurementVariables.add(dateMV);
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(ALIAS, new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		observationUnitDataMap.put("KSU_Date", new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow2 = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(2);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		storedData.put("2", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(this.middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, Arrays.asList("1", "2")))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final PreconditionFailedException e) {
			e.printStackTrace();
			Assert.fail("yyyyMMdd and d/M/yy should be supported");
			throw e;
		}
	}


	@Test
	public void testImportDataset_ImportKSUDateFormat() {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS, "KSU_Date");
		final List<String> row1 = Arrays.asList("1", "1", "5/9/20");

		data.add(header);
		data.add(row1);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = this.createMeasurementVariable();
		final MeasurementVariable dateMV = new MeasurementVariable();
		dateMV.setAlias("KSU_Date");
		dateMV.setName("KSU_Date");
		dateMV.setDataType("Date");
		dateMV.setDataTypeId(TermId.DATE_VARIABLE.getId());
		measurementVariables.add(measurementVariable);
		measurementVariables.add(dateMV);
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(ALIAS, new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		observationUnitDataMap.put("KSU_Date", new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(this.middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, Arrays.asList("1")))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final Exception e) {
			Assert.fail("KSU Date format supported: d/M/yy");
		}

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testImportDataset_ImportInvalidDateFormat() {
		final Integer studyId = 1;
		final Integer datasetId = 3;
		final ObservationsPutRequestInput observationsPutRequestInput = new ObservationsPutRequestInput();
		final List<List<String>> data = new ArrayList<>();
		final List<String> header = Arrays.asList("OBS_UNIT_ID", ALIAS, "KSU_Date");
		final List<String> row1 = Arrays.asList("1", "1", "5/19/12");

		data.add(header);
		data.add(row1);

		observationsPutRequestInput.setData(data);
		observationsPutRequestInput.setProcessWarnings(true);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final MeasurementVariable measurementVariable = this.createMeasurementVariable();
		final MeasurementVariable dateMV = new MeasurementVariable();
		dateMV.setAlias("KSU_Date");
		dateMV.setName("KSU_Date");
		dateMV.setDataType("Date");
		dateMV.setDataTypeId(TermId.DATE_VARIABLE.getId());
		measurementVariables.add(measurementVariable);
		measurementVariables.add(dateMV);
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = new HashMap<>();
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitData> observationUnitDataMap = new HashMap<>();
		observationUnitDataMap.put(ALIAS, new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		observationUnitDataMap.put("KSU_Date", new org.generationcp.middleware.service.api.dataset.ObservationUnitData(""));
		final org.generationcp.middleware.service.api.dataset.ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setObservationUnitId(1);
		observationUnitRow.setVariables(observationUnitDataMap);
		storedData.put("1", observationUnitRow);
		Mockito.doNothing().when(this.studyValidator).validate(studyId, true);
		Mockito.doNothing().when(this.datasetValidator).validateDataset(studyId, datasetId);
		Mockito.when(this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId)).thenReturn(measurementVariables);
		Mockito.when(this.middlewareDatasetService.getObservationUnitsAsMap(datasetId, measurementVariables, Arrays.asList("1")))
			.thenReturn(storedData);
		try {
			this.studyDatasetService.importObservations(studyId, datasetId, observationsPutRequestInput);
		} catch (final PreconditionFailedException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalid.cell.date.value"));
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
		studyInstance1.setInstanceId(1);

		final StudyInstance studyInstance2 = new StudyInstance();
		studyInstance2.setInstanceId(2);

		final StudyInstance studyInstance3 = new StudyInstance();
		studyInstance3.setInstanceId(3);

		datasetDTO.setInstances(Lists.newArrayList(studyInstance1, studyInstance2, studyInstance3));
		Mockito.doReturn(datasetDTO).when(this.middlewareDatasetService).generateSubObservationDataset(TEST_STUDY_IDENTIFIER, DATASET_NAME,
			DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId(), Lists.newArrayList(1, 2, 3), 8206, 3, PARENT_ID);

		// FIXME test the real method, not the mock IBP-2231
		final DatasetDTO dto =
			this.middlewareDatasetService.generateSubObservationDataset(TEST_STUDY_IDENTIFIER, DATASET_NAME,
				DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId(), Lists.newArrayList(1, 2, 3), 8206, 3, PARENT_ID);

		Assert.assertNotNull(dto);
		Assert.assertTrue(dto.getName().equalsIgnoreCase(datasetDTO.getName()));
		Assert.assertEquals(dto.getParentDatasetId(), datasetDTO.getParentDatasetId());
		Assert.assertEquals(dto.getInstances().size(), 3);
		Assert.assertEquals(dto.getVariables().size(), 1);
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
		searchDTO.setInstanceId(instanceId);

		paramDTO.setObservationUnitsSearchDTO(searchDTO);
		paramDTO.setNewValue("123");
		paramDTO.setNewCategoricalValueId(12345);
		searchDTO.setDatasetId(datasetId);
		paramDTO.getObservationUnitsSearchDTO().getFilter().setVariableId(555);
		this.studyDatasetService.acceptDraftDataFilteredByVariable(studyId, datasetId, searchDTO);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId);

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
		searchDTO.setInstanceId(instanceId);

		paramDTO.setObservationUnitsSearchDTO(searchDTO);
		paramDTO.setNewValue("123");
		paramDTO.setNewCategoricalValueId(12345);
		searchDTO.setDatasetId(datasetId);
		paramDTO.getObservationUnitsSearchDTO().getFilter().setVariableId(555);
		this.studyDatasetService.setValueToVariable(studyId, datasetId, paramDTO);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId);

		Mockito.verify(this.middlewareDatasetService).setValueToVariable(datasetId, paramDTO, studyId);
	}

	@Test
	public void testAcceptDraftDataAndSetOutOfBoundsToMissing() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		this.studyDatasetService.acceptDraftDataAndSetOutOfBoundsToMissing(studyId, datasetId);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId);
	}

	@Test
	public void testAcceptAllDatasetDraftData() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		this.studyDatasetService.acceptAllDatasetDraftData(studyId, datasetId);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId);
	}

	@Test
	public void testTransformObservations() {
		final List<ObservationDTO> list = new ArrayList<>();
		final ObservationDTO obs = new ObservationDTO();
		obs.setObservationDbId(12);
		obs.setObservationUnitDbId(RandomStringUtils.randomAlphanumeric(36));
		obs.setObservationVariableDbId(24);
		obs.setValue("356");
		list.add(obs);
		final ObservationDTO obs2 = new ObservationDTO();
		obs2.setObservationDbId(13);
		final String obs2ObsUnitDbId = RandomStringUtils.randomAlphanumeric(36);
		obs2.setObservationUnitDbId(obs2ObsUnitDbId);
		obs2.setObservationVariableDbId(25);
		obs2.setValue("200");
		list.add(obs2);
		final ObservationDTO obs3 = new ObservationDTO();
		obs3.setObservationUnitDbId(obs2ObsUnitDbId);
		obs3.setObservationVariableDbId(27);
		obs3.setValue("280");
		list.add(obs3);
		final ObservationDTO obs4 = new ObservationDTO();
		obs4.setObservationUnitDbId(obs2ObsUnitDbId);
		obs4.setObservationVariableDbId(28);
		obs4.setValue("NA");
		list.add(obs4);
		final ObservationDTO obs5 = new ObservationDTO();
		obs5.setObservationUnitDbId(obs2ObsUnitDbId);
		obs5.setObservationVariableDbId(29);
		obs5.setValue("NA");
		list.add(obs5);
		final ObservationDTO obs6 = new ObservationDTO();
		obs6.setObservationUnitDbId(obs2ObsUnitDbId);
		obs6.setObservationVariableDbId(30);
		obs6.setValue("NA");
		list.add(obs6);

		final List<MeasurementVariable> variables = new ArrayList<>();
		final MeasurementVariable var1 = new MeasurementVariable();
		var1.setAlias("Alias 1");
		var1.setName("Var 1");
		var1.setTermId(24);
		var1.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		variables.add(var1);
		final MeasurementVariable var2 = new MeasurementVariable();
		var2.setAlias("Alias 2");
		var2.setName("Var 2");
		var2.setTermId(25);
		var2.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		variables.add(var2);
		final MeasurementVariable var3 = new MeasurementVariable();
		var3.setAlias("Alias 3");
		var3.setName("Var 3");
		var3.setTermId(27);
		var3.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		variables.add(var3);
		final MeasurementVariable var4 = new MeasurementVariable();
		var4.setAlias("Alias 4");
		var4.setName("Var 4");
		var4.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		var4.setTermId(28);
		variables.add(var4);
		final MeasurementVariable var5 = new MeasurementVariable();
		var1.setAlias("Alias 5");
		var5.setName("Var 5");
		var5.setDataTypeId(DataType.DATE_TIME_VARIABLE.getId());
		var5.setTermId(29);
		variables.add(var5);
		final MeasurementVariable var6 = new MeasurementVariable();
		var6.setAlias("Alias 6");
		var6.setName("Var 6");
		var6.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		var6.setTermId(30);
		variables.add(var6);

		final ObservationsPutRequestInput t = DatasetServiceImpl.transformObservations(list, variables);

		final List<List<String>> data = t.getData();
		Assert.assertThat(data.get(1).get(0), is(obs.getObservationUnitDbId()));
		Assert.assertThat(data.get(1).get(1), is("356"));
		Assert.assertThat(data.get(2).get(0), is(obs2.getObservationUnitDbId()));
		Assert.assertThat(data.get(2).get(2), is("200"));
		Assert.assertThat(data.get(2).get(3), is("280"));
		Assert.assertThat(data.get(2).get(4), is("missing"));
		Assert.assertThat(data.get(2).get(5), is(""));
		Assert.assertThat(data.get(2).get(6), is("missing"));

		final List<String> header = data.get(0);
		Assert.assertEquals(7, header.size());
		Assert.assertTrue(header.contains(OBS_UNIT_ID));
		Assert.assertTrue(header.contains(var1.getAlias()));
		Assert.assertTrue(header.contains(var2.getAlias()));
		Assert.assertTrue(header.contains(var3.getAlias()));
		Assert.assertTrue(header.contains(var4.getAlias()));
		Assert.assertTrue(header.contains(var5.getAlias()));
		Assert.assertTrue(header.contains(var6.getAlias()));
	}

	@Test
	public void getObservationUnitsMetadata() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int obsUnitId1 = random.nextInt();
		final int obsUnitId2 = random.nextInt();
		final int obsUnitId3 = random.nextInt();
		final int obsUnitId4 = random.nextInt();

		final ObservationUnitRow observationUnitRow1 = new ObservationUnitRow();
		observationUnitRow1.setObservationUnitId(obsUnitId1);
		observationUnitRow1.setTrialInstance(1);
		observationUnitRow1.setVariables(new HashMap<>());
		observationUnitRow1.setEnvironmentVariables(new HashMap<>());

		final ObservationUnitRow observationUnitRow2 = new ObservationUnitRow();
		observationUnitRow2.setObservationUnitId(obsUnitId2);
		observationUnitRow2.setTrialInstance(1);
		observationUnitRow2.setVariables(new HashMap<>());
		observationUnitRow2.setEnvironmentVariables(new HashMap<>());

		final ObservationUnitRow observationUnitRow3 = new ObservationUnitRow();
		observationUnitRow3.setObservationUnitId(obsUnitId3);
		observationUnitRow3.setTrialInstance(2);
		observationUnitRow3.setVariables(new HashMap<>());
		observationUnitRow3.setEnvironmentVariables(new HashMap<>());

		final ObservationUnitRow observationUnitRow4 = new ObservationUnitRow();
		observationUnitRow4.setObservationUnitId(obsUnitId4);
		observationUnitRow4.setTrialInstance(3);
		observationUnitRow4.setVariables(new HashMap<>());
		observationUnitRow4.setEnvironmentVariables(new HashMap<>());

		final List<ObservationUnitRow> selectedRows =
			Lists.newArrayList(observationUnitRow1, observationUnitRow2, observationUnitRow3, observationUnitRow4);
		final Set<Integer> itemIds = Sets.newHashSet(obsUnitId1, obsUnitId2, obsUnitId3, obsUnitId4);
		final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto = new SearchCompositeDto<>();
		searchCompositeDto.setItemIds(itemIds);

		Mockito.when(this.middlewareDatasetService.getObservationUnitRows(eq(studyId), eq(datasetId), Mockito.any(), Mockito.any()))
			.thenReturn(selectedRows);

		final ObservationUnitsMetadata metadata =
			this.studyDatasetService.getObservationUnitsMetadata(studyId, datasetId, searchCompositeDto);
		Assert.assertEquals(metadata.getInstancesCount(), Long.valueOf(3));
		Assert.assertEquals(metadata.getObservationUnitsCount(), Long.valueOf(selectedRows.size()));
	}

	@Test
	public void testGetReplaceObservationUnitsEntry_NoObservationSelected() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();

		final Set<Integer> itemIds = Sets.newHashSet(random.nextInt(), random.nextInt(), random.nextInt(), random.nextInt());
		final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto = new SearchCompositeDto<>();
		searchCompositeDto.setItemIds(itemIds);

		final ObservationUnitEntryReplaceRequest observationUnitEntryReplaceRequest = new ObservationUnitEntryReplaceRequest();
		observationUnitEntryReplaceRequest.setSearchRequest(searchCompositeDto);
		observationUnitEntryReplaceRequest.setEntryId(random.nextInt());

		Mockito.when(this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(Boolean.FALSE);
		Mockito.when(this.studyService.hasCrossesOrSelections(studyId)).thenReturn(Boolean.FALSE);
		Mockito.when(this.middlewareDatasetService.getObservationUnitRows(eq(studyId), eq(datasetId), Mockito.any(), Mockito.any()))
			.thenReturn(new ArrayList<>());

		try {
			this.studyDatasetService.replaceObservationUnitsEntry(studyId, datasetId, observationUnitEntryReplaceRequest);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.entry.replace.empty.units"));
		}
	}

	@Test
	public void testGetReplaceObservationUnitsEntry_SamplesFound() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int obsUnitId1 = random.nextInt();
		final ObservationUnitRow observationUnitRow1 = new ObservationUnitRow();
		observationUnitRow1.setObservationUnitId(obsUnitId1);
		observationUnitRow1.setTrialInstance(1);
		observationUnitRow1.setVariables(new HashMap<>());
		observationUnitRow1.setEnvironmentVariables(new HashMap<>());
		observationUnitRow1.setSamplesCount("6");

		final Set<Integer> itemIds = Sets.newHashSet(obsUnitId1);
		final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto = new SearchCompositeDto<>();
		searchCompositeDto.setItemIds(itemIds);

		final ObservationUnitEntryReplaceRequest observationUnitEntryReplaceRequest = new ObservationUnitEntryReplaceRequest();
		observationUnitEntryReplaceRequest.setSearchRequest(searchCompositeDto);
		observationUnitEntryReplaceRequest.setEntryId(random.nextInt());

		Mockito.when(this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(Boolean.FALSE);
		Mockito.when(this.studyService.hasCrossesOrSelections(studyId)).thenReturn(Boolean.FALSE);
		Mockito.when(this.middlewareDatasetService.getObservationUnitRows(eq(studyId), eq(datasetId), Mockito.any(), Mockito.any()))
			.thenReturn(Collections.singletonList(observationUnitRow1));

		try {
			this.studyDatasetService.replaceObservationUnitsEntry(studyId, datasetId, observationUnitEntryReplaceRequest);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.entry.replace.samples.found"));
		}

	}

	@Test
	public void testGetReplaceObservationUnitsEntry_TransactionsFound() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int obsUnitId1 = random.nextInt();
		final ObservationUnitRow observationUnitRow1 = new ObservationUnitRow();
		observationUnitRow1.setObservationUnitId(obsUnitId1);
		observationUnitRow1.setTrialInstance(1);
		observationUnitRow1.setVariables(new HashMap<>());
		observationUnitRow1.setEnvironmentVariables(new HashMap<>());
		observationUnitRow1.setSamplesCount("-");

		final Set<Integer> itemIds = Sets.newHashSet(obsUnitId1);
		final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto = new SearchCompositeDto<>();
		searchCompositeDto.setItemIds(itemIds);

		final ObservationUnitEntryReplaceRequest observationUnitEntryReplaceRequest = new ObservationUnitEntryReplaceRequest();
		observationUnitEntryReplaceRequest.setSearchRequest(searchCompositeDto);
		observationUnitEntryReplaceRequest.setEntryId(random.nextInt());

		Mockito.when(this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(Boolean.FALSE);
		Mockito.when(this.studyService.hasCrossesOrSelections(studyId)).thenReturn(Boolean.FALSE);
		Mockito.when(this.middlewareDatasetService.getObservationUnitRows(eq(studyId), eq(datasetId), Mockito.any(), Mockito.any()))
			.thenReturn(Collections.singletonList(observationUnitRow1));
		Mockito.when(this.studyTransactionsService.countStudyTransactions(eq(studyId), Mockito.any())).thenReturn(1L);

		try {
			this.studyDatasetService.replaceObservationUnitsEntry(studyId, datasetId, observationUnitEntryReplaceRequest);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.entry.replace.transactions.found"));
		}
	}

	@Test
	public void testGetReplaceObservationUnitsEntry_Ok() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int obsUnitId1 = random.nextInt();
		final int newEntryId = random.nextInt();
		final ObservationUnitRow observationUnitRow1 = new ObservationUnitRow();
		observationUnitRow1.setObservationUnitId(obsUnitId1);
		observationUnitRow1.setTrialInstance(1);
		observationUnitRow1.setVariables(new HashMap<>());
		observationUnitRow1.setEnvironmentVariables(new HashMap<>());
		observationUnitRow1.setSamplesCount("-");

		final List<Integer> itemIds = Lists.newArrayList(obsUnitId1);
		final SearchCompositeDto<ObservationUnitsSearchDTO, Integer> searchCompositeDto = new SearchCompositeDto<>();
		searchCompositeDto.setItemIds(Sets.newHashSet(itemIds));

		final ObservationUnitEntryReplaceRequest observationUnitEntryReplaceRequest = new ObservationUnitEntryReplaceRequest();
		observationUnitEntryReplaceRequest.setSearchRequest(searchCompositeDto);
		observationUnitEntryReplaceRequest.setEntryId(newEntryId);

		Mockito.when(this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(Boolean.FALSE);
		Mockito.when(this.studyService.hasCrossesOrSelections(studyId)).thenReturn(Boolean.FALSE);
		Mockito.when(this.middlewareDatasetService.getObservationUnitRows(eq(studyId), eq(datasetId), Mockito.any(), Mockito.any()))
			.thenReturn(Collections.singletonList(observationUnitRow1));
		Mockito.when(this.studyTransactionsService.countStudyTransactions(eq(studyId), Mockito.any())).thenReturn(0L);

		this.studyDatasetService.replaceObservationUnitsEntry(studyId, datasetId, observationUnitEntryReplaceRequest);

		Mockito.verify(this.middlewareDatasetService, times(1)).replaceObservationUnitEntry(eq(itemIds), eq(newEntryId));
	}

}
