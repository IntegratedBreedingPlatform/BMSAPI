package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.dataset.ObservationValue;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DatasetServiceImplTest {

	private static final int TEST_STUDY_IDENTIFIER = 2013;
	public static final String OBS_UNIT_ID = "OBS_UNIT_ID";
	public static final String ENTRY_CODE = "ENTRY_CODE";
	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String DESIGNATION = "DESIGNATION";
	public static final String GID = "GID";
	public static final String ENTRY_TYPE = "ENTRY_TYPE";
	public static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	public static final String FIELD_MAP_ROW = "FieldMapRow";
	public static final String FIELD_MAP_COLUMN = "FieldMapColumn";
	public static final String FIELD_MAP_RANGE = "FIELD_MAP_RANGE";
	public static final String LOCATION_ABBREVIATION = "LocationAbbreviation";
	public static final String LOCATION_NAME = "LocationName";
	public static final String COL = "COL";
	public static final String ROW = "ROW";
	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String REP_NO = "REP_NO";
	private static final String STOCK_ID = "STOCK_ID";
	private static final String FACT1 = "FACT1";

	@Mock
	private DatasetService middlewareDatasetService;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private ObservationValidator observationValidator;

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

		this.studyDatasetService.countPhenotypes(studyId, datasetId, traitIds);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.middlewareDatasetService).countPhenotypes(datasetId, traitIds);
	}

	@Test
	public void testCountPhenotypesByInstance() {

		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int instanceId = random.nextInt();
		this.studyDatasetService.countPhenotypesByInstance(studyId, datasetId, instanceId);
		Mockito.verify(this.studyValidator).validate(studyId, false);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId, false);
		Mockito.verify(this.instanceValidator).validate(datasetId, instanceId);
		Mockito.verify(this.middlewareDatasetService).countPhenotypesByInstance(datasetId, instanceId);
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
		Mockito.verify(this.middlewareDatasetService).addVariable(datasetId, variableId, variableType, alias);
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
		this.studyDatasetService.removeVariables(studyId, datasetId, variableIds);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateExistingDatasetVariables(studyId, datasetId, true, variableIds);
		Mockito.verify(this.middlewareDatasetService).removeVariables(datasetId, variableIds);
	}

	@Test
	public void testAddObservation() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final ObservationDto observationDto = new ObservationDto();
		observationDto.setVariableId(random.nextInt());
		this.studyDatasetService.addObservation(studyId, datasetId, observationUnitId, observationDto);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator)
			.validateExistingDatasetVariables(studyId, datasetId, true, Arrays.asList(observationDto.getVariableId()));
		Mockito.verify(this.observationValidator).validateObservationUnit(datasetId, observationUnitId);
		Mockito.verify(this.middlewareDatasetService).addPhenotype(observationDto);
	}

	@Test
	public void testUpdateObservation() {
		final Random random = new Random();
		final int studyId = random.nextInt();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final int observationId = random.nextInt();
		final ObservationValue observationValue = new ObservationValue();
		observationValue.setCategoricalValueId(random.nextInt());
		observationValue.setValue(random.toString());
		this.studyDatasetService.updateObservation(studyId, datasetId, observationId, observationUnitId, observationValue);
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.datasetValidator).validateDataset(studyId, datasetId, true);
		Mockito.verify(this.observationValidator).validateObservation(datasetId, observationUnitId, observationId);
		Mockito.verify(this.middlewareDatasetService)
			.updatePhenotype(observationUnitId, observationId, observationValue.getCategoricalValueId(), observationValue.getValue());
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
		Mockito.verify(this.observationValidator).validateObservation(datasetId, observationUnitId, observationId);
		Mockito.verify(this.middlewareDatasetService).deletePhenotype(observationId);
	}



	@Test
	public void testGetObservations() {
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

		final List<ObservationUnitRow> observationDtoTestData =
			Lists.newArrayList(observationUnitRow);

		Mockito.doReturn(observationDtoTestData).when(this.middlewareDatasetService).getObservationUnitRows(TEST_STUDY_IDENTIFIER, 1, 1, 1, 100, null, null);
		final List<org.ibp.api.rest.dataset.ObservationUnitRow> actualObservations =
			this.studyDatasetService.getObservationUnitRows(TEST_STUDY_IDENTIFIER, 1, 1, 1, 100, null, null);

		Assert.assertEquals(this.mapObservationUnitRows(observationDtoTestData), actualObservations);

	}

	private List<org.ibp.api.rest.dataset.ObservationUnitRow> mapObservationUnitRows(final List<ObservationUnitRow> observationDtoTestData) {
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
}
