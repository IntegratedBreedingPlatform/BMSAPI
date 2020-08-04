package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.ObservationData;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyInstanceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

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
	private DatasetService middlewareDatasetService;

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

	@InjectMocks
	private final StudyInstanceService studyInstanceService = new StudyInstanceServiceImpl();

	private final CropType maizeCropType = new CropType(CropType.CropEnum.MAIZE.name());
	private final Random random = new Random();

	@Before
	public void init() {
		when(this.workbenchDataManager.getCropTypeByName(CropType.CropEnum.MAIZE.name())).thenReturn(this.maizeCropType);
	}

	@Test
	public void testCreateStudyInstance() {

		final int studyId = this.random.nextInt(BOUND);
		final int datasetId = this.random.nextInt(BOUND);
		final int instanceNumber = 99;
		final DatasetDTO summaryDataset = new DatasetDTO();
		summaryDataset.setDatasetId(datasetId);
		final List<DatasetDTO> datasets = Arrays.asList(summaryDataset);

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

		when(this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(datasets);
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
	public void testCreateStudyInstanceNoEnvironmentDataset() {

		final int studyId = this.random.nextInt(BOUND);

		try {
			this.studyInstanceService.createStudyInstances(this.maizeCropType.getCropName(), studyId, 1);
			fail("Method should throw an exception.");
		} catch (final ApiRuntimeException e) {
			verify(this.studyValidator).validate(studyId, true);
		}

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
	public void testAddInstanceData() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int instanceDataId = this.random.nextInt(BOUND);
		final ObservationData observationData = new ObservationData();
		observationData.setVariableId(TermId.ALTITUDE.getId());

		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(this.random.nextInt(BOUND));

		when(this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(Arrays.asList(datasetDTO));

		final MeasurementVariableDto measurementVariableDto = new MeasurementVariableDto(observationData.getVariableId(), "ALTITUDE");
		when(this.datasetService.getDatasetVariablesByType(studyId, datasetDTO.getDatasetId(), VariableType.STUDY_CONDITION))
			.thenReturn(Arrays.asList(measurementVariableDto));

		this.studyInstanceService.addInstanceObservation(studyId, instanceId, observationData);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
		Mockito.verify(this.datasetValidator)
			.validateExistingDatasetVariables(studyId, datasetDTO.getDatasetId(), Collections.singletonList(
				observationData.getVariableId()));
		Mockito.verify(this.observationValidator).validateObservationValue(observationData.getVariableId(), observationData.getValue());
		Mockito.verify(this.middlewareStudyInstanceService).addInstanceObservation(observationData, true);

	}

	@Test
	public void testUpdateInstanceData() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int instanceDataId = this.random.nextInt(BOUND);
		final ObservationData observationData = new ObservationData();
		observationData.setVariableId(TermId.ALTITUDE.getId());

		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(this.random.nextInt(BOUND));

		when(this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(Arrays.asList(datasetDTO));

		final MeasurementVariableDto measurementVariableDto = new MeasurementVariableDto(observationData.getVariableId(), "ALTITUDE");
		when(this.datasetService.getDatasetVariablesByType(studyId, datasetDTO.getDatasetId(), VariableType.STUDY_CONDITION))
			.thenReturn(Arrays.asList(measurementVariableDto));

		when(this.middlewareStudyInstanceService.getInstanceObservation(instanceId, instanceDataId, observationData.getVariableId(), true))
			.thenReturn(Optional.of(observationData));

		this.studyInstanceService.updateInstanceObservation(studyId, instanceId, instanceDataId, observationData);

		Mockito.verify(this.studyValidator).validate(studyId, true);
		Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
		Mockito.verify(this.datasetValidator)
			.validateExistingDatasetVariables(studyId, datasetDTO.getDatasetId(), Collections.singletonList(
				observationData.getVariableId()));
		Mockito.verify(this.observationValidator).validateObservationValue(observationData.getVariableId(), observationData.getValue());
		Mockito.verify(this.middlewareStudyInstanceService).updateInstanceObservation(observationData, true);

	}

	@Test
	public void testUpdateInstanceData_InvalidInstaceDataId() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int instanceDataId = this.random.nextInt(BOUND);
		final ObservationData observationData = new ObservationData();
		observationData.setVariableId(TermId.ALTITUDE.getId());

		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(this.random.nextInt(BOUND));

		when(this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(Arrays.asList(datasetDTO));

		final MeasurementVariableDto measurementVariableDto = new MeasurementVariableDto(observationData.getVariableId(), "ALTITUDE");
		when(this.datasetService.getDatasetVariablesByType(studyId, datasetDTO.getDatasetId(), VariableType.STUDY_CONDITION))
			.thenReturn(Arrays.asList(measurementVariableDto));

		when(this.middlewareStudyInstanceService.getInstanceObservation(instanceId, instanceDataId, observationData.getVariableId(), true))
			.thenReturn(Optional.empty());

		try {
			this.studyInstanceService.updateInstanceObservation(studyId, instanceId, instanceDataId, observationData);
			fail("method should throw an error");
		} catch (ApiRequestValidationException e) {
			Assert
				.assertTrue(Arrays.asList(e.getErrors().get(0).getCodes()).contains(StudyInstanceServiceImpl.INVALID_ENVIRONMENT_DATA_ID));
			Mockito.verify(this.studyValidator).validate(studyId, true);
			Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
			Mockito.verify(this.datasetValidator)
				.validateExistingDatasetVariables(studyId, datasetDTO.getDatasetId(), Collections.singletonList(
					observationData.getVariableId()));
			Mockito.verify(this.observationValidator).validateObservationValue(observationData.getVariableId(), observationData.getValue());
		}

	}

	@Test
	public void testUpdateInstanceData_InvalidVariableForEnvironmentData() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceId = this.random.nextInt(BOUND);
		final int instanceDataId = this.random.nextInt(BOUND);
		final ObservationData observationData = new ObservationData();
		observationData.setVariableId(TermId.ALTITUDE.getId());

		final DatasetDTO datasetDTO = new DatasetDTO();
		datasetDTO.setDatasetId(this.random.nextInt(BOUND));

		when(this.middlewareDatasetService.getDatasets(studyId, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(Arrays.asList(datasetDTO));

		final MeasurementVariableDto measurementVariableDto = new MeasurementVariableDto(observationData.getVariableId(), "ALTITUDE");
		when(this.datasetService.getDatasetVariablesByType(studyId, datasetDTO.getDatasetId(), VariableType.STUDY_CONDITION))
			.thenReturn(Arrays.asList(measurementVariableDto));

		final ObservationData differentObservationData = new ObservationData();
		differentObservationData.setVariableId(TermId.BLOCK_NAME.getId());
		when(this.middlewareStudyInstanceService.getInstanceObservation(instanceId, instanceDataId, observationData.getVariableId(), true))
			.thenReturn(Optional.of(differentObservationData));

		try {
			this.studyInstanceService.updateInstanceObservation(studyId, instanceId, instanceDataId, observationData);
			fail("method should throw an error");
		} catch (ApiRequestValidationException e) {
			Assert.assertTrue(
				Arrays.asList(e.getErrors().get(0).getCodes()).contains(StudyInstanceServiceImpl.INVALID_VARIABLE_FOR_ENVIRONMENT_DATA));
			Mockito.verify(this.studyValidator).validate(studyId, true);
			Mockito.verify(this.instanceValidator).validateStudyInstance(studyId, Collections.singleton(instanceId));
			Mockito.verify(this.datasetValidator)
				.validateExistingDatasetVariables(studyId, datasetDTO.getDatasetId(), Collections.singletonList(
					observationData.getVariableId()));
			Mockito.verify(this.observationValidator).validateObservationValue(observationData.getVariableId(), observationData.getValue());
		}

	}

}
