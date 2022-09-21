package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.crop.CropService;
import org.generationcp.middleware.api.location.LocationDTO;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.domain.dms.InstanceDescriptorData;
import org.generationcp.middleware.domain.dms.InstanceObservationData;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyInstanceService;
import org.ibp.api.java.study.StudyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StudyInstanceServiceImplTest {

	private static final int BOUND = 10;

	@Mock
	private org.generationcp.middleware.service.api.study.StudyInstanceService middlewareStudyInstanceService;

	@Mock
	private CropService cropServiceMW;
	
	@Mock
	private StudyValidator studyValidator;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private ObservationValidator observationValidator;

	@Mock
	private LocationService locationService;

	@Mock
	private StudyService studyService;

	@InjectMocks
	private final StudyInstanceService studyInstanceService = new StudyInstanceServiceImpl();

	private final CropType maizeCropType = new CropType(CropType.CropEnum.MAIZE.name());
	private final Random random = new Random();

	@Before
	public void init() {
		when(this.cropServiceMW.getCropTypeByName(CropType.CropEnum.MAIZE.name())).thenReturn(this.maizeCropType);
	}

	@Test
	public void testCreateStudyInstance() {

		final int studyId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final String programUUID = UUID.randomUUID().toString();
		final int instanceNumber = 99;

		final org.generationcp.middleware.service.impl.study.StudyInstance existingStudyInstance =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				instanceNumber,
				RandomStringUtils.random(BOUND));

		final int nextInstanceNumber = existingStudyInstance.getInstanceNumber() + 1;
		final org.generationcp.middleware.service.impl.study.StudyInstance newStudyInstance =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				nextInstanceNumber,
				RandomStringUtils.random(BOUND));

		final LocationDTO programDefaultLocation = new LocationDTO();
		programDefaultLocation.setId(1);
		programDefaultLocation.setName(newStudyInstance.getLocationName());
		programDefaultLocation.setAbbreviation(newStudyInstance.getLocationAbbreviation());

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);
		when(
			this.middlewareStudyInstanceService.createStudyInstances(this.maizeCropType, studyId, datasetId, programDefaultLocation.getId(),
				1))
			.thenReturn(Collections.singletonList(newStudyInstance));
		when(this.locationService.getDefaultBreedingLocation(programUUID)).thenReturn(programDefaultLocation);

		final List<org.ibp.api.domain.study.StudyInstance>
			result = this.studyInstanceService.createStudyInstances(this.maizeCropType.getCropName(), studyId, programUUID, 1);

		verify(this.studyValidator).validate(studyId, true);

		assertEquals(result.get(0).getInstanceId(), newStudyInstance.getInstanceId());
		assertEquals(nextInstanceNumber, newStudyInstance.getInstanceNumber());
		assertEquals(result.get(0).getLocationName(), newStudyInstance.getLocationName());
		assertEquals(result.get(0).getLocationAbbreviation(), newStudyInstance.getLocationAbbreviation());
		assertEquals(result.get(0).getCustomLocationAbbreviation(), newStudyInstance.getCustomLocationAbbreviation());
		assertEquals(result.get(0).getHasFieldLayout(), newStudyInstance.getHasFieldLayout());

	}

	@Test
	public void testGetStudyInstances() {

		final int studyId = this.random.nextInt(BOUND);

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				1,
				RandomStringUtils.random(BOUND));

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance2 =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				2,
				RandomStringUtils.random(BOUND));

		when(this.middlewareStudyInstanceService.getStudyInstances(studyId))
			.thenReturn(Arrays.asList(studyInstance, studyInstance2));

		final List<org.ibp.api.domain.study.StudyInstance>
			studyInstances = this.studyInstanceService.getStudyInstances(studyId);

		Mockito.verify(this.studyValidator).validate(studyId, false);
		assertEquals(2, studyInstances.size());
		final org.ibp.api.domain.study.StudyInstance result1 = studyInstances.get(0);
		assertEquals(result1.getInstanceId(), studyInstance.getInstanceId());
		assertEquals(result1.getInstanceNumber(), studyInstance.getInstanceNumber());
		assertEquals(result1.getLocationName(), studyInstance.getLocationName());
		assertEquals(result1.getLocationAbbreviation(), studyInstance.getLocationAbbreviation());
		assertEquals(result1.getCustomLocationAbbreviation(), studyInstance.getCustomLocationAbbreviation());
		assertEquals(result1.getHasFieldLayout(), studyInstance.getHasFieldLayout());

		final org.ibp.api.domain.study.StudyInstance result2 = studyInstances.get(1);
		assertEquals(result2.getInstanceId(), studyInstance2.getInstanceId());
		assertEquals(result2.getInstanceNumber(), studyInstance2.getInstanceNumber());
		assertEquals(result2.getLocationName(), studyInstance2.getLocationName());
		assertEquals(result2.getLocationAbbreviation(), studyInstance2.getLocationAbbreviation());
		assertEquals(result2.getCustomLocationAbbreviation(), studyInstance2.getCustomLocationAbbreviation());
		assertEquals(result2.getHasFieldLayout(), studyInstance2.getHasFieldLayout());
	}

	@Test
	public void testGetStudyInstance() {

		final int studyId = this.random.nextInt(BOUND);

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				1,
				RandomStringUtils.random(BOUND));

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance2 =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				2,
				RandomStringUtils.random(BOUND));

		when(this.middlewareStudyInstanceService.getStudyInstance(studyId, 101))
			.thenReturn(Optional.of(studyInstance));
		when(this.middlewareStudyInstanceService.getStudyInstance(studyId, 102))
			.thenReturn(Optional.of(studyInstance2));
		when(this.middlewareStudyInstanceService.getStudyInstance(studyId, 103))
			.thenReturn(Optional.empty());

		final org.ibp.api.domain.study.StudyInstance result1 = this.studyInstanceService.getStudyInstance(studyId, 101).get();
		assertEquals(result1.getInstanceId(), studyInstance.getInstanceId());
		assertEquals(result1.getInstanceNumber(), studyInstance.getInstanceNumber());
		assertEquals(result1.getLocationName(), studyInstance.getLocationName());
		assertEquals(result1.getLocationAbbreviation(), studyInstance.getLocationAbbreviation());
		assertEquals(result1.getCustomLocationAbbreviation(), studyInstance.getCustomLocationAbbreviation());
		assertEquals(result1.getHasFieldLayout(), studyInstance.getHasFieldLayout());

		final org.ibp.api.domain.study.StudyInstance result2 = this.studyInstanceService.getStudyInstance(studyId, 102).get();
		assertEquals(result2.getInstanceId(), studyInstance2.getInstanceId());
		assertEquals(result2.getInstanceNumber(), studyInstance2.getInstanceNumber());
		assertEquals(result2.getLocationName(), studyInstance2.getLocationName());
		assertEquals(result2.getLocationAbbreviation(), studyInstance2.getLocationAbbreviation());
		assertEquals(result2.getCustomLocationAbbreviation(), studyInstance2.getCustomLocationAbbreviation());
		assertEquals(result2.getHasFieldLayout(), studyInstance2.getHasFieldLayout());

		Assert.assertFalse(this.middlewareStudyInstanceService.getStudyInstance(studyId, 103).isPresent());
		Mockito.verify(this.studyValidator, Mockito.times(2)).validate(studyId, false);
		Mockito.verify(this.instanceValidator, Mockito.times(2))
			.validateStudyInstance(ArgumentMatchers.eq(studyId), ArgumentMatchers.anySet());
	}

	@Test
	public void testDeleteStudyInstances() {
		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		this.studyInstanceService.deleteStudyInstances(studyId, Arrays.asList(instanceId));
		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId), true);
		Mockito.verify(this.middlewareStudyInstanceService).deleteStudyInstances(studyId, Collections.singletonList(instanceId));
	}

	@Test
	public void testAddInstanceObservation() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int observationId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final InstanceObservationData instanceObservationData = new InstanceObservationData();
		instanceObservationData.setVariableId(TermId.ALTITUDE.getId());

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);

		this.studyInstanceService.addInstanceObservation(studyId, instanceId, instanceObservationData);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
		Mockito.verify(this.datasetValidator)
			.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
				instanceObservationData.getVariableId()));
		Mockito.verify(this.observationValidator)
			.validateVariableValue(instanceObservationData.getVariableId(), instanceObservationData.getValue());
		Mockito.verify(this.middlewareStudyInstanceService).addInstanceObservation(instanceObservationData);
		Mockito.verify(this.datasetValidator)
			.validateVariableBelongsToVariableType(datasetId, instanceObservationData.getVariableId(),
				VariableType.ENVIRONMENT_CONDITION.getId());

	}

	@Test
	public void testUpdateInstanceObservation() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int observationId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final InstanceObservationData instanceObservationData = new InstanceObservationData();
		instanceObservationData.setVariableId(TermId.ALTITUDE.getId());

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);

		when(this.middlewareStudyInstanceService.getInstanceObservation(instanceId, observationId, instanceObservationData.getVariableId()))
			.thenReturn(Optional.of(instanceObservationData));

		this.studyInstanceService.updateInstanceObservation(studyId, instanceId, observationId, instanceObservationData);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
		Mockito.verify(this.datasetValidator)
			.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
				instanceObservationData.getVariableId()));
		Mockito.verify(this.observationValidator)
			.validateVariableValue(instanceObservationData.getVariableId(), instanceObservationData.getValue());
		Mockito.verify(this.middlewareStudyInstanceService).updateInstanceObservation(instanceObservationData);
		Mockito.verify(this.datasetValidator)
			.validateVariableBelongsToVariableType(datasetId, instanceObservationData.getVariableId(),
				VariableType.ENVIRONMENT_CONDITION.getId());

	}

	@Test
	public void testUpdateInstanceObservationa_InvalidObservationDataId() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int observationDataId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final InstanceObservationData instanceObservationData = new InstanceObservationData();
		instanceObservationData.setVariableId(TermId.ALTITUDE.getId());

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);

		when(this.middlewareStudyInstanceService.getInstanceObservation(instanceId, observationDataId,
			instanceObservationData.getVariableId()))
			.thenReturn(Optional.empty());

		try {
			this.studyInstanceService.updateInstanceObservation(studyId, instanceId, observationDataId, instanceObservationData);
			fail("method should throw an error");
		} catch (final ApiRequestValidationException e) {
			Assert
				.assertTrue(Arrays.asList(e.getErrors().get(0).getCodes()).contains(StudyInstanceServiceImpl.INVALID_ENVIRONMENT_DATA_ID));
			Mockito.verify(this.studyValidator).validate(studyId, true);
			Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
			Mockito.verify(this.datasetValidator)
				.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
					instanceObservationData.getVariableId()));
			Mockito.verify(this.observationValidator).validateVariableValue(
				instanceObservationData.getVariableId(), instanceObservationData.getValue());
			Mockito.verify(this.datasetValidator)
				.validateVariableBelongsToVariableType(datasetId, instanceObservationData.getVariableId(),
					VariableType.ENVIRONMENT_CONDITION.getId());
		}

	}

	@Test
	public void testUpdateInstanceObservation_InvalidVariableForObservationData() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int observationId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final InstanceObservationData instanceObservationData = new InstanceObservationData();
		instanceObservationData.setVariableId(TermId.ALTITUDE.getId());

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);

		final InstanceObservationData differentInstanceObservationData = new InstanceObservationData();
		differentInstanceObservationData.setVariableId(TermId.BLOCK_NAME.getId());
		when(this.middlewareStudyInstanceService.getInstanceObservation(instanceId, observationId, instanceObservationData.getVariableId()))
			.thenReturn(Optional.of(differentInstanceObservationData));

		try {
			this.studyInstanceService.updateInstanceObservation(studyId, instanceId, observationId, instanceObservationData);
			fail("method should throw an error");
		} catch (final ApiRequestValidationException e) {
			Assert.assertTrue(
				Arrays.asList(e.getErrors().get(0).getCodes()).contains(StudyInstanceServiceImpl.INVALID_VARIABLE_FOR_ENVIRONMENT_DATA));
			Mockito.verify(this.studyValidator).validate(studyId, true);
			Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
			Mockito.verify(this.datasetValidator)
				.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
					instanceObservationData.getVariableId()));
			Mockito.verify(this.observationValidator).validateVariableValue(
				instanceObservationData.getVariableId(), instanceObservationData.getValue());
			Mockito.verify(this.datasetValidator)
				.validateVariableBelongsToVariableType(datasetId, instanceObservationData.getVariableId(),
					VariableType.ENVIRONMENT_CONDITION.getId());
		}

	}

	@Test
	public void testUpdateInstanceDescriptor() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int descriptorDataId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final InstanceDescriptorData instanceDescriptorData = new InstanceDescriptorData();
		instanceDescriptorData.setVariableId(TermId.ALTITUDE.getId());

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);

		when(this.middlewareStudyInstanceService.getInstanceDescriptorData(instanceId, descriptorDataId,
			instanceDescriptorData.getVariableId()))
			.thenReturn(Optional.of(instanceDescriptorData));

		this.studyInstanceService.updateInstanceDescriptorData(studyId, instanceId, descriptorDataId, instanceDescriptorData);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
		Mockito.verify(this.datasetValidator)
			.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
				instanceDescriptorData.getVariableId()));
		Mockito.verify(this.observationValidator)
			.validateVariableValue(instanceDescriptorData.getVariableId(), instanceDescriptorData.getValue());
		Mockito.verify(this.middlewareStudyInstanceService).updateInstanceDescriptorData(instanceDescriptorData);
		Mockito.verify(this.datasetValidator)
			.validateVariableBelongsToVariableType(datasetId, instanceDescriptorData.getVariableId(),
				VariableType.ENVIRONMENT_DETAIL.getId());

	}

	@Test
	public void testUpdateInstanceDescriptor_InvalidDesciptorDataId() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int observationDataId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final InstanceDescriptorData instanceDescriptorData = new InstanceDescriptorData();
		instanceDescriptorData.setVariableId(TermId.ALTITUDE.getId());

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);

		when(this.middlewareStudyInstanceService.getInstanceDescriptorData(instanceId, observationDataId,
			instanceDescriptorData.getVariableId()))
			.thenReturn(Optional.empty());

		try {
			this.studyInstanceService.updateInstanceDescriptorData(studyId, instanceId, observationDataId, instanceDescriptorData);
			fail("method should throw an error");
		} catch (final ApiRequestValidationException e) {
			Assert
				.assertTrue(Arrays.asList(e.getErrors().get(0).getCodes()).contains(StudyInstanceServiceImpl.INVALID_ENVIRONMENT_DATA_ID));
			Mockito.verify(this.studyValidator).validate(studyId, true);
			Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
			Mockito.verify(this.datasetValidator)
				.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
					instanceDescriptorData.getVariableId()));
			Mockito.verify(this.observationValidator)
				.validateVariableValue(instanceDescriptorData.getVariableId(), instanceDescriptorData.getValue());
			Mockito.verify(this.datasetValidator)
				.validateVariableBelongsToVariableType(datasetId, instanceDescriptorData.getVariableId(),
					VariableType.ENVIRONMENT_DETAIL.getId());

		}

	}

	@Test
	public void testUpdateInstanceDescriptor_InvalidVariableForDescriptorData() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int descriptorDataId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final InstanceDescriptorData instanceDescriptorData = new InstanceDescriptorData();
		instanceDescriptorData.setVariableId(TermId.ALTITUDE.getId());

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);

		final InstanceDescriptorData differentInstanceDescriptorData = new InstanceDescriptorData();
		differentInstanceDescriptorData.setVariableId(TermId.BLOCK_NAME.getId());
		when(this.middlewareStudyInstanceService.getInstanceDescriptorData(instanceId, descriptorDataId,
			instanceDescriptorData.getVariableId()))
			.thenReturn(Optional.of(differentInstanceDescriptorData));

		try {
			this.studyInstanceService.updateInstanceDescriptorData(studyId, instanceId, descriptorDataId, instanceDescriptorData);
			fail("method should throw an error");
		} catch (final ApiRequestValidationException e) {
			Assert.assertTrue(
				Arrays.asList(e.getErrors().get(0).getCodes()).contains(StudyInstanceServiceImpl.INVALID_VARIABLE_FOR_ENVIRONMENT_DATA));
			Mockito.verify(this.studyValidator).validate(studyId, true);
			Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
			Mockito.verify(this.datasetValidator)
				.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
					instanceDescriptorData.getVariableId()));
			Mockito.verify(this.observationValidator)
				.validateVariableValue(instanceDescriptorData.getVariableId(), instanceDescriptorData.getValue());
			Mockito.verify(this.datasetValidator)
				.validateVariableBelongsToVariableType(datasetId, instanceDescriptorData.getVariableId(),
					VariableType.ENVIRONMENT_DETAIL.getId());
		}

	}

}
