package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.domain.dms.InstanceDescriptorData;
import org.generationcp.middleware.domain.dms.InstanceObservationData;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.brapi.v2.study.StudyImportResponse;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyImportRequestValidator;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.nullValue;
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
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private org.ibp.api.java.dataset.DatasetService datasetService;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private ObservationValidator observationValidator;

	@Mock
	private StudyService studyService;

	@Mock
	private StudyImportRequestValidator studyImportRequestValidator;

	@Mock
	private SecurityService securityService;

	@InjectMocks
	private final StudyInstanceService studyInstanceService = new StudyInstanceServiceImpl();

	private final CropType maizeCropType = new CropType(CropType.CropEnum.MAIZE.name());
	private final Random random = new Random();

	private WorkbenchUser testUser;

	@Before
	public void init() {
		when(this.workbenchDataManager.getCropTypeByName(CropType.CropEnum.MAIZE.name())).thenReturn(this.maizeCropType);
		if (this.testUser == null) {
			this.testUser = new WorkbenchUser(new Random().nextInt(100));
			Mockito.doReturn(this.testUser).when(this.securityService).getCurrentlyLoggedInUser();
		}
	}

	@Test
	public void testCreateStudyInstance() {

		final int studyId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final int instanceNumber = 99;

		final org.generationcp.middleware.service.impl.study.StudyInstance existingStudyInstance =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				instanceNumber,
				RandomStringUtils.random(BOUND), false);

		final int nextInstanceNumber = existingStudyInstance.getInstanceNumber() + 1;
		final org.generationcp.middleware.service.impl.study.StudyInstance newStudyInstance =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				nextInstanceNumber,
				RandomStringUtils.random(BOUND), false);

		when(this.studyService.getEnvironmentDatasetId(studyId))
			.thenReturn(datasetId);
		when(this.middlewareStudyInstanceService.createStudyInstances(this.maizeCropType, studyId, datasetId, 1))
			.thenReturn(Collections.singletonList(newStudyInstance));

		final List<org.ibp.api.domain.study.StudyInstance>
			result = this.studyInstanceService.createStudyInstances(this.maizeCropType.getCropName(), studyId, 1);

		verify(this.studyValidator).validate(studyId, true);

		assertEquals(result.get(0).getInstanceId(), newStudyInstance.getInstanceId());
		assertEquals(nextInstanceNumber, newStudyInstance.getInstanceNumber());
		assertEquals(result.get(0).getLocationName(), newStudyInstance.getLocationName());
		assertEquals(result.get(0).getLocationAbbreviation(), newStudyInstance.getLocationAbbreviation());
		assertEquals(result.get(0).getCustomLocationAbbreviation(), newStudyInstance.getCustomLocationAbbreviation());
		assertEquals(result.get(0).getHasFieldmap(), newStudyInstance.isHasFieldmap());

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
				RandomStringUtils.random(BOUND), this.random.nextBoolean());

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance2 =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				2,
				RandomStringUtils.random(BOUND), this.random.nextBoolean());

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
		assertEquals(result1.getHasFieldmap(), studyInstance.isHasFieldmap());

		final org.ibp.api.domain.study.StudyInstance result2 = studyInstances.get(1);
		assertEquals(result2.getInstanceId(), studyInstance2.getInstanceId());
		assertEquals(result2.getInstanceNumber(), studyInstance2.getInstanceNumber());
		assertEquals(result2.getLocationName(), studyInstance2.getLocationName());
		assertEquals(result2.getLocationAbbreviation(), studyInstance2.getLocationAbbreviation());
		assertEquals(result2.getCustomLocationAbbreviation(), studyInstance2.getCustomLocationAbbreviation());
		assertEquals(result2.getHasFieldmap(), studyInstance2.isHasFieldmap());
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
				RandomStringUtils.random(BOUND), this.random.nextBoolean());

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance2 =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				2,
				RandomStringUtils.random(BOUND), this.random.nextBoolean());

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
		assertEquals(result1.getHasFieldmap(), studyInstance.isHasFieldmap());

		final org.ibp.api.domain.study.StudyInstance result2 = this.studyInstanceService.getStudyInstance(studyId, 102).get();
		assertEquals(result2.getInstanceId(), studyInstance2.getInstanceId());
		assertEquals(result2.getInstanceNumber(), studyInstance2.getInstanceNumber());
		assertEquals(result2.getLocationName(), studyInstance2.getLocationName());
		assertEquals(result2.getLocationAbbreviation(), studyInstance2.getLocationAbbreviation());
		assertEquals(result2.getCustomLocationAbbreviation(), studyInstance2.getCustomLocationAbbreviation());
		assertEquals(result2.getHasFieldmap(), studyInstance2.isHasFieldmap());

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
		Mockito.verify(this.observationValidator).validateVariableValue(instanceObservationData.getVariableId(), instanceObservationData.getValue());
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
		Mockito.verify(this.observationValidator).validateVariableValue(instanceObservationData.getVariableId(), instanceObservationData.getValue());
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

		when(this.middlewareStudyInstanceService.getInstanceObservation(instanceId, observationDataId, instanceObservationData.getVariableId()))
			.thenReturn(Optional.empty());

		try {
			this.studyInstanceService.updateInstanceObservation(studyId, instanceId, observationDataId, instanceObservationData);
			fail("method should throw an error");
		} catch (ApiRequestValidationException e) {
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
		} catch (ApiRequestValidationException e) {
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

		when(this.middlewareStudyInstanceService.getInstanceDescriptorData(instanceId, descriptorDataId, instanceDescriptorData.getVariableId()))
			.thenReturn(Optional.of(instanceDescriptorData));

		this.studyInstanceService.updateInstanceDescriptorData(studyId, instanceId, descriptorDataId, instanceDescriptorData);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
		Mockito.verify(this.datasetValidator)
			.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
				instanceDescriptorData.getVariableId()));
		Mockito.verify(this.observationValidator).validateVariableValue(instanceDescriptorData.getVariableId(), instanceDescriptorData.getValue());
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

		when(this.middlewareStudyInstanceService.getInstanceDescriptorData(instanceId, observationDataId, instanceDescriptorData.getVariableId()))
			.thenReturn(Optional.empty());

		try {
			this.studyInstanceService.updateInstanceDescriptorData(studyId, instanceId, observationDataId, instanceDescriptorData);
			fail("method should throw an error");
		} catch (ApiRequestValidationException e) {
			Assert
				.assertTrue(Arrays.asList(e.getErrors().get(0).getCodes()).contains(StudyInstanceServiceImpl.INVALID_ENVIRONMENT_DATA_ID));
			Mockito.verify(this.studyValidator).validate(studyId, true);
			Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
			Mockito.verify(this.datasetValidator)
				.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
					instanceDescriptorData.getVariableId()));
			Mockito.verify(this.observationValidator).validateVariableValue(instanceDescriptorData.getVariableId(), instanceDescriptorData.getValue());
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
		when(this.middlewareStudyInstanceService.getInstanceDescriptorData(instanceId, descriptorDataId, instanceDescriptorData.getVariableId()))
			.thenReturn(Optional.of(differentInstanceDescriptorData));

		try {
			this.studyInstanceService.updateInstanceDescriptorData(studyId, instanceId, descriptorDataId, instanceDescriptorData);
			fail("method should throw an error");
		} catch (ApiRequestValidationException e) {
			Assert.assertTrue(
				Arrays.asList(e.getErrors().get(0).getCodes()).contains(StudyInstanceServiceImpl.INVALID_VARIABLE_FOR_ENVIRONMENT_DATA));
			Mockito.verify(this.studyValidator).validate(studyId, true);
			Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
			Mockito.verify(this.datasetValidator)
				.validateExistingDatasetVariables(studyId, datasetId, Collections.singletonList(
					instanceDescriptorData.getVariableId()));
			Mockito.verify(this.observationValidator).validateVariableValue(instanceDescriptorData.getVariableId(), instanceDescriptorData.getValue());
			Mockito.verify(this.datasetValidator)
				.validateVariableBelongsToVariableType(datasetId, instanceDescriptorData.getVariableId(),
					VariableType.ENVIRONMENT_DETAIL.getId());
		}

	}

	@Test
	public void testCreateStudies_AllCreated(){
		final String crop = RandomStringUtils.randomAlphabetic(10);
		final BindingResult result = Mockito.mock(BindingResult.class);
		Mockito.doReturn(false).when(result).hasErrors();
		Mockito.doReturn(result).when(this.studyImportRequestValidator).pruneStudiesInvalidForImport(ArgumentMatchers.anyList());

		final List<StudyImportRequestDTO> importList = new ArrayList<>();
		final StudyImportRequestDTO request1 = new StudyImportRequestDTO();
		request1.setTrialDbId(RandomStringUtils.randomAlphabetic(20));
		final StudyImportRequestDTO request2 = new StudyImportRequestDTO();
		request2.setTrialDbId(RandomStringUtils.randomAlphabetic(20));
		importList.add(request1);
		importList.add(request2);
		final StudyInstanceDto study1 = new StudyInstanceDto();
		study1.setTrialDbId(request1.getTrialDbId());
		study1.setStudyDbId(String.valueOf(new Random().nextInt()));
		final StudyInstanceDto study2 = new StudyInstanceDto();
		study2.setTrialDbId(request2.getTrialDbId());
		study2.setStudyDbId(String.valueOf(new Random().nextInt()));
		final List<StudyInstanceDto> studies = Arrays.asList(study1, study2);
		Mockito.doReturn(studies).when(this.middlewareStudyInstanceService).saveStudyInstances(crop, importList, this.testUser.getUserid());

		final StudyImportResponse importResponse = this.studyInstanceService.createStudies(crop, importList);
		final int size = studies.size();
		Assert.assertThat(importResponse.getCreatedSize(), is(size));
		Assert.assertThat(importResponse.getImportListSize(), is(size));
		Assert.assertThat(importResponse.getEntityList(), iterableWithSize(studies.size()));
		Assert.assertThat(importResponse.getErrors(), nullValue());
	}

	@Test
	public void testCreateStudies_InvalidNotCreated(){
		final String crop = RandomStringUtils.randomAlphabetic(10);
		final BindingResult result = Mockito.mock(BindingResult.class);
		final ObjectError error = Mockito.mock(ObjectError.class);
		Mockito.doReturn(true).when(result).hasErrors();
		Mockito.doReturn(Lists.newArrayList(error)).when(result).getAllErrors();
		Mockito.doReturn(result).when(this.studyImportRequestValidator).pruneStudiesInvalidForImport(ArgumentMatchers.anyList());

		final List<StudyImportRequestDTO> importList = new ArrayList<>();
		final StudyImportRequestDTO request1 = new StudyImportRequestDTO();
		request1.setTrialDbId(RandomStringUtils.randomAlphabetic(20));
		final StudyImportRequestDTO request2 = new StudyImportRequestDTO();
		request2.setTrialDbId(RandomStringUtils.randomAlphabetic(20));
		importList.add(request1);
		importList.add(request2);
		final StudyInstanceDto study1 = new StudyInstanceDto();
		study1.setTrialDbId(request1.getTrialDbId());
		study1.setStudyDbId(String.valueOf(new Random().nextInt()));
		final List<StudyInstanceDto> studies = Collections.singletonList(study1);
		Mockito.doReturn(studies).when(this.middlewareStudyInstanceService).saveStudyInstances(crop, importList, this.testUser.getUserid());

		final StudyImportResponse importResponse = this.studyInstanceService.createStudies(crop, importList);
		final int size = studies.size();
		Assert.assertThat(importResponse.getCreatedSize(), is(size));
		Assert.assertThat(importResponse.getImportListSize(), is(importList.size()));
		Assert.assertThat(importResponse.getEntityList(), iterableWithSize(studies.size()));
		Assert.assertThat(importResponse.getErrors(), is(Lists.newArrayList(error)));
	}

}
