package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.hamcrest.CoreMatchers;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetGeneratorInputValidatorTest {

	@InjectMocks
	private DatasetGeneratorInputValidator datasetGeneratorInputValidator;

	@Mock
	private DatasetService datasetService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetTypeService datasetTypeService;

	@Mock
	private VariableService variableService;

	@Mock
	final Environment environment = new StandardEnvironment();

	@Before
	public void setup() {

		final DatasetTypeDTO quadratDatasetType = new DatasetTypeDTO(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId(), "QUADRAT_SUBOBSERVATIONS");
		when(this.datasetTypeService.getDatasetTypeById(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId())).thenReturn(quadratDatasetType);

		final DatasetTypeDTO plantDatasetType = new DatasetTypeDTO(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId(), "PLANT_SUBOBSERVATIONS");
		when(this.datasetTypeService.getDatasetTypeById(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId())).thenReturn(plantDatasetType);

		final DatasetTypeDTO meansDatasetType = new DatasetTypeDTO(DatasetTypeEnum.MEANS_DATA.getId(), "MEANS_DATA");
		when(this.datasetTypeService.getDatasetTypeById(DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(meansDatasetType);

	}

	@Test
	public void testValidateDataConflicts() {

		final Random random = new Random();
		final int studyId = random.nextInt();

		final String name = "Test";
		final String program = "MAIZE-Program";
		final Study study = new Study();
		study.setProgramUUID(program);
		study.setId(random.nextInt());
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName(name);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.datasetService.isDatasetNameAvailable(datasetInputGenerator.getDatasetName(), study.getId())).thenReturn(true);

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		this.datasetGeneratorInputValidator.validateDataConflicts(studyId, datasetInputGenerator, errors);
		Assert.assertTrue(errors.getAllErrors().isEmpty());
	}

	@Test
	public void testValidateDataConflictsReject() {
		final Study study = new Study();
		final Random random = new Random();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());

		final int studyId = random.nextInt();
		final String name = "Test";
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);

		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());

		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);

		this.datasetGeneratorInputValidator.validateDataConflicts(studyId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.name.not.available"));
	}

	@Test
	public void testValidateDatasetTypeIsImplemented() {

		final Integer datasetTypeId = DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId();

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());

		this.datasetGeneratorInputValidator.validateDatasetTypeIsImplemented(datasetTypeId, errors);
		Assert.assertTrue(errors.getAllErrors().isEmpty());

	}

	@Test
	public void testValidateDatasetTypeIsImplementedError() {
		final Random random = new Random();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());

		final DatasetTypeDTO datasetType = new DatasetTypeDTO(DatasetTypeEnum.PLOT_DATA.getId(), "PLOT_DATA");
		when(this.datasetTypeService.getDatasetTypeById(DatasetTypeEnum.PLOT_DATA.getId())).thenReturn(datasetType);

		this.datasetGeneratorInputValidator.validateDatasetTypeIsImplemented(DatasetTypeEnum.PLOT_DATA.getId(), errors);
		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.operation.not.implemented"));
	}

	@Test
	public void testValidateBasicData() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);

		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();
		datasetInputGenerator.setNumberOfSubObservationUnits(25);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		final DatasetTypeDTO datasetType = new DatasetTypeDTO(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId(), "PLANT_SUBOBSERVATIONS");
		when(this.datasetTypeService.getDatasetTypeById(datasetType.getDatasetTypeId())).thenReturn(datasetType);

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().isEmpty());
	}

	@Test
	public void testValidateBasicDataDatasetTypeInexistent() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(random.nextInt());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.type.id.not.exist"));
	}

	@Test
	public void testValidateBasicDataDatasetTypeIsObservation() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.MEANS_DATA.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.parent.not.allowed"));
	}

	@Test
	public void testValidateBasicDataMaxAllowed() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.datasetService.getNumberOfChildren(parentId)).thenReturn(25);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.creation.not.allowed"));
	}

	@Test
	public void testValidateBasicDataLongName() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName(
			"NAME123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.datasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.name.exceed.length"));
	}

	@Test
	public void testValidateBasicDataEmptyName() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.datasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.name.empty.name"));
	}

	@Test
	public void testValidateBasicDataInvalidVariable() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {5};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.datasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);

		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.invalid.instances"));
	}

	@Test
	public void testValidateBasicDataMaxAllowedSubObservations() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("25");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("5");

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.datasetService.getNumberOfChildren(parentId)).thenReturn(2);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("1");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();

		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);

		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.invalid.number.subobs.units"));
	}

	@Test
	public void testValidateBasicDataInvalidInstance() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);

		when(this.datasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);

		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.invalid.obs.unit.variable"));
	}

	@Test
	public void testValidateDataSpecialCharacters() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("Dataset name \\ / : * ? \" < > | .");

		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();
		datasetInputGenerator.setNumberOfSubObservationUnits(25);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);
		Assert.assertTrue(errors.getAllErrors().isEmpty());
	}

	@Test
	public void testValidateDataSpecialCharactersError() {
		final Study study = new Study();
		final Random random = new Random();
		final List<StudyInstance> studyInstances = new ArrayList<>();
		final StudyInstance studyInstance = new StudyInstance();
		final Integer[] instanceIds = new Integer[] {1};
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		final DatasetDTO dataset = new DatasetDTO();
		final Integer parentId = random.nextInt();
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();
		final VariableType variableType = new VariableType(
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getId().toString(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getName(),
			org.generationcp.middleware.domain.ontology.VariableType.OBSERVATION_UNIT.getDescription());

		final int studyId = random.nextInt();
		final String program = "MAIZE-Program";

		study.setProgramUUID(program);
		studyInstance.setInstanceDbId(1);
		studyInstances.add(studyInstance);
		dataset.setInstances(studyInstances);
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		datasetInputGenerator.setDatasetTypeId(DatasetTypeEnum.PLANT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("Dataset+");

		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.parent.unit"))
			.thenReturn("100");
		when(this.datasetGeneratorInputValidator.getEnvironment().getProperty("maximum.number.of.sub.observation.sets")).thenReturn("25");
		this.datasetGeneratorInputValidator.setEnvironment(this.environment);
		this.datasetGeneratorInputValidator.init();
		datasetInputGenerator.setNumberOfSubObservationUnits(25);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(variableType)));

		when(this.datasetService.getDataset(parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		assertThat(Arrays.asList(objectError.getCodes()), CoreMatchers.hasItem("dataset.name.invalid"));
	}
}
