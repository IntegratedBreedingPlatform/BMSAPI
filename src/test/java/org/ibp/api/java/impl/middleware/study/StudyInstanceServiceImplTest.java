package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.fest.util.Collections;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.study.StudyInstanceService;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StudyInstanceServiceImplTest {

	public static final int BOUND = 10;
	@Mock
	private org.generationcp.middleware.service.api.study.StudyInstanceService studyInstanceMiddlewareService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private DatasetService datasetService;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Mock
	private InstanceValidator instanceValidator;

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
		final int instanceNumber = this.random.nextInt(BOUND);
		final DatasetDTO summaryDataset = new DatasetDTO();
		summaryDataset.setDatasetId(datasetId);
		final List<DatasetDTO> datasets = Arrays.asList(summaryDataset);

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				instanceNumber,
				RandomStringUtils.random(BOUND), false);

		when(this.datasetService.getDatasets(studyId, Collections.set(DatasetTypeEnum.SUMMARY_DATA.getId()))).thenReturn(datasets);
		when(this.studyInstanceMiddlewareService.createStudyInstance(this.maizeCropType, datasetId, instanceNumber))
			.thenReturn(studyInstance);

		final org.ibp.api.domain.study.StudyInstance
			result = this.studyInstanceService.createStudyInstance(this.maizeCropType.getCropName(), studyId, instanceNumber);

		verify(this.studyValidator).validate(studyId, true);
		verify(this.instanceValidator).checkStudyInstanceAlreadyExists(studyId, instanceNumber);

		assertEquals(result.getInstanceDbId(), studyInstance.getInstanceDbId());
		assertEquals(result.getExperimentId(), studyInstance.getExperimentId());
		assertEquals(result.getInstanceNumber(), studyInstance.getInstanceNumber());
		assertEquals(result.getLocationName(), studyInstance.getLocationName());
		assertEquals(result.getLocationAbbreviation(), studyInstance.getLocationAbbreviation());
		assertEquals(result.getCustomLocationAbbreviation(), studyInstance.getCustomLocationAbbreviation());
		assertEquals(result.getHasFieldmap(), studyInstance.isHasFieldmap());

	}

	@Test
	public void testCreateStudyInstanceNoEnvironmentDataset() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceNumber = this.random.nextInt(BOUND);
		when(this.datasetService.getDatasets(studyId, Collections.set(DatasetTypeEnum.SUMMARY_DATA.getId()))).thenReturn(new ArrayList<>());

		try {
			this.studyInstanceService.createStudyInstance(this.maizeCropType.getCropName(), studyId, instanceNumber);
			fail("Method should throw an exception.");
		} catch (final ApiRuntimeException e) {
			verify(this.studyValidator).validate(studyId, true);
			verify(this.instanceValidator).checkStudyInstanceAlreadyExists(studyId, instanceNumber);
		}

	}

	@Test
	public void testGetStudyInstances() {

		final int studyId = this.random.nextInt(BOUND);
		final int instanceNumber = this.random.nextInt(BOUND);

		final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance =
			new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND), this.random.nextInt(BOUND),
				RandomStringUtils.random(BOUND),
				RandomStringUtils.random(
					BOUND),
				instanceNumber,
				RandomStringUtils.random(BOUND), false);

		when(this.studyInstanceMiddlewareService.getStudyInstances(studyId))
			.thenReturn(Arrays.asList(studyInstance));

		final List<org.ibp.api.domain.study.StudyInstance>
			studyInstances = this.studyInstanceService.getStudyInstances(studyId);

		final org.ibp.api.domain.study.StudyInstance result = studyInstances.get(0);
		assertEquals(result.getInstanceDbId(), studyInstance.getInstanceDbId());
		assertEquals(result.getExperimentId(), studyInstance.getExperimentId());
		assertEquals(result.getInstanceNumber(), studyInstance.getInstanceNumber());
		assertEquals(result.getLocationName(), studyInstance.getLocationName());
		assertEquals(result.getLocationAbbreviation(), studyInstance.getLocationAbbreviation());
		assertEquals(result.getCustomLocationAbbreviation(), studyInstance.getCustomLocationAbbreviation());
		assertEquals(result.getHasFieldmap(), studyInstance.isHasFieldmap());

	}

}
