package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.when;

public class DatasetGeneratorInputValidatorTest {

	@InjectMocks
	private DatasetGeneratorInputValidator datasetGeneratorInputValidator;

	@Mock
	private DatasetService datasetService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetService studyDatasetService;

	@Mock
	private VariableService variableService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateDataConflicts() {

		final Random random = new Random();
		final int studyId = random.nextInt();

		final String name = "Test";
		final String program = "MAIZE-Program";
		final Study study = new Study();
		study.setProgramUUID(program);
		final DatasetGeneratorInput datasetInputGenerator = new DatasetGeneratorInput();
		datasetInputGenerator.setDatasetTypeId(DataSetType.PLANT_SUBOBSERVATIONS.getId());
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		when(this.datasetService.isDatasetNameAvailable(name, program)).thenReturn(true);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());
		this.datasetGeneratorInputValidator.validateDataConflicts(studyId, datasetInputGenerator, errors);
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

		datasetInputGenerator.setDatasetTypeId(DataSetType.PLANT_SUBOBSERVATIONS.getId());

		when(this.datasetService.isDatasetNameAvailable(name, program)).thenReturn(false);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);

		this.datasetGeneratorInputValidator.validateDataConflicts(studyId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.name.not.available".equalsIgnoreCase(objectError.getCodes()[1]));
	}

	@Test
	public void testValidateDatasetTypeIsImplemented() {

		final Integer datasetTypeId = DataSetType.PLANT_SUBOBSERVATIONS.getId();

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());

		this.datasetGeneratorInputValidator.validateDatasetTypeIsImplemented(datasetTypeId, errors);
	}

	@Test
	public void testValidateDatasetTypeIsImplementedError() {
		final Random random = new Random();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());

		final int datasetTypeId = random.nextInt();

		this.datasetGeneratorInputValidator.validateDatasetTypeIsImplemented(datasetTypeId, errors);
		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.operation.not.implemented".equalsIgnoreCase(objectError.getCodes()[1]));

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
		datasetInputGenerator.setDatasetTypeId(DataSetType.PLANT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 0);
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
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.type.id.not.exist".equalsIgnoreCase(objectError.getCodes()[1]));
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
		datasetInputGenerator.setDatasetTypeId(DataSetType.MEANS_DATA.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.parent.not.allowed".equalsIgnoreCase(objectError.getCodes()[1]));
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
		datasetInputGenerator.setDatasetTypeId(DataSetType.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		this.datasetGeneratorInputValidator.setMaxAllowedDatasetsPerParent(25);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.studyDatasetService.getNumberOfChildren(parentId)).thenReturn(25);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.creation.not.allowed".equalsIgnoreCase(objectError.getCodes()[1]));
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
		datasetInputGenerator.setDatasetTypeId(DataSetType.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName(
			"NAME123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		this.datasetGeneratorInputValidator.setMaxAllowedDatasetsPerParent(25);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.studyDatasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.name.exceed.length".equalsIgnoreCase(objectError.getCodes()[1]));
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
		datasetInputGenerator.setDatasetTypeId(DataSetType.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		this.datasetGeneratorInputValidator.setMaxAllowedDatasetsPerParent(25);
		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.studyDatasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);
		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.name.empty.name".equalsIgnoreCase(objectError.getCodes()[1]));
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
		datasetInputGenerator.setDatasetTypeId(DataSetType.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		this.datasetGeneratorInputValidator.setMaxAllowedDatasetsPerParent(25);

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.studyDatasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);

		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.invalid.instances".equalsIgnoreCase(objectError.getCodes()[1]));

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
		datasetInputGenerator.setDatasetTypeId(DataSetType.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(5);
		this.datasetGeneratorInputValidator.setMaxAllowedDatasetsPerParent(25);

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		when(this.variableService
			.getVariableById("maize", study.getProgramUUID(), String.valueOf(datasetInputGenerator.getSequenceVariableId())))
			.thenReturn(variableDetails);
		when(this.studyDatasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);

		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.invalid.number.subobs.units".equalsIgnoreCase(objectError.getCodes()[1]));

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
		datasetInputGenerator.setDatasetTypeId(DataSetType.QUADRAT_SUBOBSERVATIONS.getId());
		datasetInputGenerator.setDatasetName("NAME");
		datasetInputGenerator.setInstanceIds(instanceIds);
		datasetInputGenerator.setSequenceVariableId(123);
		datasetInputGenerator.setNumberOfSubObservationUnits(10);
		this.datasetGeneratorInputValidator.setMaxAllowedSubobservationUnits(100);
		this.datasetGeneratorInputValidator.setMaxAllowedDatasetsPerParent(25);

		variableDetails.setName("ChangedName");
		variableDetails.setVariableTypes(new HashSet<VariableType>(Collections.singletonList(variableType)));

		when(this.studyDatasetService.getDataset(studyId, parentId)).thenReturn(dataset);
		when(this.studyDataManager.getStudy(studyId)).thenReturn(study);

		when(this.studyDatasetService.getNumberOfChildren(parentId)).thenReturn(2);
		this.datasetGeneratorInputValidator.validateBasicData("maize", studyId, parentId, datasetInputGenerator, errors);

		Assert.assertTrue(errors.getAllErrors().size() == 1);

		final ObjectError objectError = errors.getAllErrors().get(0);
		Assert.assertTrue("dataset.invalid.obs.unit.variable".equalsIgnoreCase(objectError.getCodes()[1]));

	}
}
