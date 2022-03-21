package org.ibp.api.java.impl.middleware.study.validator;

import com.google.common.collect.Sets;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyEntryService;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class StudyValidatorTest {

	private static final Integer USER_ID = 10;
	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(10);

	@Mock
	private SecurityService securityService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private StudyInstanceService studyInstanceService;

	@Mock
	private StudyEntryService studyEntryService;

	@Mock
	private StudyService studyService;

	@InjectMocks
	private StudyValidator studyValidator;

	@Mock
	private UserService userService;

	@Before
	public void setup() {
		ContextHolder.setCurrentProgram(PROGRAM_UUID);
		ContextHolder.setCurrentCrop("maize");
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testStudyDoesNotExist() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(null);
		this.studyValidator.validate(studyId, ran.nextBoolean());
	}

	@Test(expected = ForbiddenException.class)
	public void testStudyIsLocked() {
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(USER_ID, new Role(2, "Breeder"));
		doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		study.setCreatedBy("1");
		study.setStudyType(new StudyTypeDto());
		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		this.studyValidator.validate(studyId, true);
	}

	@Test
	public void testStudyIsLockedButUserIsOwner() {
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(USER_ID, new Role(2, "Breeder"));
		doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		study.setCreatedBy(String.valueOf(USER_ID));
		study.setProgramUUID(PROGRAM_UUID);
		study.setStudyType(new StudyTypeDto());
		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		this.studyValidator.validate(studyId, true);
	}

	@Test
	public void testStudyIsLockedButUserIsSuperAdmin() {
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(USER_ID, new Role(2, Role.SUPERADMIN));
		doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();

		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		study.setCreatedBy("1");
		study.setProgramUUID(PROGRAM_UUID);
		study.setStudyType(new StudyTypeDto());
		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		this.studyValidator.validate(studyId, true);
	}

	@Test
	public void testStudyAllInstancesMustBeDeletableButOneIsNot() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(false);
		study.setCreatedBy("1");
		study.setProgramUUID(PROGRAM_UUID);
		study.setStudyType(new StudyTypeDto());

		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		final StudyInstance studyInstance = new StudyInstance(ran.nextInt(), ran.nextInt(),
			RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10),
			1,
			RandomStringUtils.randomAlphabetic(10), false);
		studyInstance.setCanBeDeleted(false);
		final StudyInstance studyInstance2 = new StudyInstance(ran.nextInt(), ran.nextInt(),
			RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10),
			1,
			RandomStringUtils.randomAlphabetic(10), false);
		studyInstance2.setCanBeDeleted(true);
		Mockito.when(this.studyInstanceService.getStudyInstances(studyId))
			.thenReturn(Arrays.asList(studyInstance, studyInstance2));
		try {
			this.studyValidator.validate(studyId, ran.nextBoolean(), true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("at.least.one.instance.cannot.be.deleted"));
		}
	}

	@Test
	public void testStudyAllInstancesMustBeDeletableSuccess() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(false);
		study.setCreatedBy("1");
		study.setProgramUUID(PROGRAM_UUID);
		study.setStudyType(new StudyTypeDto());

		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		final StudyInstance studyInstance = new StudyInstance(ran.nextInt(), ran.nextInt(),
			RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10),
			1,
			RandomStringUtils.randomAlphabetic(10), false);
		studyInstance.setCanBeDeleted(true);
		final StudyInstance studyInstance2 = new StudyInstance(ran.nextInt(), ran.nextInt(),
			RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10),
			1,
			RandomStringUtils.randomAlphabetic(10), false);
		studyInstance2.setCanBeDeleted(true);
		Mockito.when(this.studyInstanceService.getStudyInstances(studyId))
			.thenReturn(Arrays.asList(studyInstance, studyInstance2));

		this.studyValidator.validate(studyId, ran.nextBoolean(), true);
	}

	@Test
	public void testStudyNotAllInstancesMustBeDeletable() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(false);
		study.setCreatedBy("1");
		study.setProgramUUID(PROGRAM_UUID);
		study.setStudyType(new StudyTypeDto());

		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		this.studyValidator.validate(studyId, ran.nextBoolean(), false);
		Mockito.verifyZeroInteractions(this.studyInstanceService);
	}

	@Test
	public void testValidateHasNoCrossesOrSelections() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		Mockito.when(this.studyService.hasCrossesOrSelections(studyId)).thenReturn(Boolean.TRUE);
		try {
			this.studyValidator.validateHasNoCrossesOrSelections(studyId);
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.has.crosses.or.selections"));
		}
	}

	@Test
	public void testValidateStudyHasNoMeansDataset() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		Mockito.when(this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.MEANS_DATA.getId())).thenReturn(Boolean.TRUE);
		try {
			this.studyValidator.validateStudyHasNoMeansDataset(studyId);
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.has.means.dataset"));
		}
	}

	@Test
	public void testValidateStudyHasNoSummaryStatisticsDataset() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		Mockito.when(this.studyService.studyHasGivenDatasetType(studyId, DatasetTypeEnum.SUMMARY_STATISTICS_DATA.getId()))
			.thenReturn(Boolean.TRUE);
		try {
			this.studyValidator.validateStudyHasNoSummaryStatisticsDataset(studyId);
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.has.summary.statistics.dataset"));
		}
	}

	@Test
	public void testValidateDeleteStudy_ThrowsException_WhenStudyNotExist() {
		try {
			final Integer studyId = RandomUtils.nextInt();
			this.studyValidator.validateDeleteStudy(studyId);
		} catch (final ResourceNotFoundException e) {
			assertThat(Arrays.asList(e.getError().getCodes()), hasItem("study.not.exist"));
		}
	}

	@Test
	public void testValidateDeleteStudy_ThrowsException_WhenStudyHasProgramUUID() {
		try {
			final Integer studyId = RandomUtils.nextInt();
			final Study study = new Study();
			study.setId(studyId);
			study.setCreatedBy(USER_ID.toString());
			Mockito.doReturn(study).when(studyDataManager).getStudy(studyId, false);
			this.studyValidator.validateDeleteStudy(studyId);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.template.delete.not.permitted"));
		}
	}

	@Test
	public void testValidateDeleteStudy_ThrowsException_WhenTheUserIsNotOwnerOfStudy() {
		try {
			final Integer studyId = RandomUtils.nextInt();
			final Study study = new Study();
			final WorkbenchUser userLogged = new WorkbenchUser();
			final WorkbenchUser userStudy = new WorkbenchUser();

			final Person person = new Person();

			userStudy.setPerson(person);
			userLogged.setUserid(RandomUtils.nextInt());
			study.setId(studyId);
			study.setProgramUUID(StudyValidatorTest.PROGRAM_UUID);
			study.setCreatedBy(USER_ID.toString());
			Mockito.doReturn(study).when(studyDataManager).getStudy(studyId, false);
			Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(userLogged);
			Mockito.when(this.userService.getUserById(study.getUser())).thenReturn(userStudy);

			this.studyValidator.validateDeleteStudy(studyId);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.delete.not.permitted"));
		}
	}

	@Test
	public void testValidateStudyInstanceNumbers_SomeTrialInstanceNumberDoNotExist() {
		final Integer studyId = RandomUtils.nextInt();
		final Map<String, Integer> instanceGeolocationIdMap = new HashMap<>();
		instanceGeolocationIdMap.put("1", RandomUtils.nextInt());
		Mockito.when(this.studyDataManager.getInstanceGeolocationIdsMap(studyId)).thenReturn(instanceGeolocationIdMap);
		try {
			this.studyValidator.validateStudyInstanceNumbers(studyId, Sets.newHashSet(1, 2, 3));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.trial.instances.do.not.exist"));
			assertEquals(new Object[] {"2, 3"}, e.getErrors().get(0).getArguments());
		}

	}

	@Test
	public void testValidateStudyInstanceNumbers_Success() {
		final Integer studyId = RandomUtils.nextInt();
		final Map<String, Integer> instanceGeolocationIdMap = new HashMap<>();
		instanceGeolocationIdMap.put("1", RandomUtils.nextInt());
		instanceGeolocationIdMap.put("2", RandomUtils.nextInt());
		instanceGeolocationIdMap.put("3", RandomUtils.nextInt());
		Mockito.when(this.studyDataManager.getInstanceGeolocationIdsMap(studyId)).thenReturn(instanceGeolocationIdMap);
		try {
			this.studyValidator.validateStudyInstanceNumbers(studyId, Sets.newHashSet(1, 2, 3));
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Should not throw an exception");
		}
	}

	@Test
	public void testValidateStudyHasPlotDataset_OK() {
		final Integer studyId = RandomUtils.nextInt();
		final DataSet dataset = Mockito.mock(DataSet.class);

		Mockito.when(this.studyDataManager.findOneDataSetByType(studyId, DatasetTypeEnum.PLOT_DATA.getId())).thenReturn(dataset);

		final DataSet actualDataset = this.studyValidator.validateStudyHasPlotDataset(studyId);
		assertThat(actualDataset, is(dataset));
	}

	@Test
	public void testValidateStudyHasPlotDataset_invalidDatasetNotFound() {
		final Integer studyId = RandomUtils.nextInt();

		Mockito.when(this.studyDataManager.findOneDataSetByType(studyId, DatasetTypeEnum.PLOT_DATA.getId())).thenReturn(null);

		try {
			this.studyValidator.validateStudyHasPlotDataset(studyId);
			Assert.fail("Should have thrown an exception");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.not.has.plot.dataset"));
		}
	}

}
