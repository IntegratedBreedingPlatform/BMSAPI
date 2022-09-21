package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Sets;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;

public class InstanceValidatorTest extends ApiUnitTestBase {

	private final Random random = new Random();

	@Mock
	private org.generationcp.middleware.service.api.study.StudyService middlewareStudyService;

	@Mock
	private StudyInstanceService studyInstanceService;

	@InjectMocks
	private InstanceValidator instanceValidator;

	private Integer datasetId;

	@Before
	public void init() {
		this.datasetId = this.random.nextInt();
		Mockito.doReturn(this.datasetId).when(this.middlewareStudyService).getEnvironmentDatasetId(ArgumentMatchers.anyInt());
		when(this.studyDataManager.areAllInstancesExistInDataset(ArgumentMatchers.eq(this.datasetId), ArgumentMatchers.anySet()))
			.thenReturn(true);
		when(this.studyDataManager.areAllInstancesExistInDataset(ArgumentMatchers.anyInt(), ArgumentMatchers.anySet())).thenReturn(true);
	}

	@Test
	public void testValidateInstanceIds_Success() {
		final int instanceId = this.random.nextInt();
		this.instanceValidator.validate(this.datasetId, Sets.newHashSet(instanceId));
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateInstanceIds_Fail() {
		final int instanceId = this.random.nextInt();

		when(this.studyDataManager.areAllInstancesExistInDataset(this.datasetId, Sets.newHashSet(instanceId))).thenReturn(false);
		this.instanceValidator.validate(this.datasetId, Sets.newHashSet(instanceId));
	}

	@Test
	public void testValidateInstanceDeletionFail_EmptyInstanceNumbers() {
		try {
			this.instanceValidator.validateStudyInstance(this.random.nextInt(), null, this.random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.instances.required"));
		}
	}

	@Test
	public void testValidateInstanceDeletionFail_StudyHasNoInstances() {
		final HashSet<Integer> instanceIds = new HashSet<>(Arrays.asList(this.random.nextInt(), this.random.nextInt()));
		when(this.studyDataManager.areAllInstancesExistInDataset(this.datasetId, instanceIds)).thenReturn(false);
		try {
			this.instanceValidator.validateStudyInstance(this.datasetId, instanceIds, this.random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateInstanceNumbersFail_StudyHasNoInstances() {
		Mockito.doReturn(Collections.emptyList()).when(this.studyInstanceService).getStudyInstances(ArgumentMatchers.anyInt());
		try {
			this.instanceValidator.validateInstanceNumbers(this.random.nextInt(), Collections.singleton(this.random.nextInt()));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateInstanceNumbersFail_InstanceNumbersDontExistInStudy() {
		final List<StudyInstance> instances = new ArrayList<>();
		instances.add(new StudyInstance(101, 1, false, false, true));
		instances.add(new StudyInstance(202, 2, false, false, true));
		instances.add(new StudyInstance(303, 3, false, false, true));
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(ArgumentMatchers.anyInt());
		try {
			this.instanceValidator.validateInstanceNumbers(this.random.nextInt(), new HashSet<>(Arrays.asList(1, 102)));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateInstanceNumbersFail_AllSelectedInstancesCantBeDeleted() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances();
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		instances.get(1).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateInstanceNumbers(studyId, new HashSet<>(Arrays.asList(1, 2)));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("all.selected.instances.cannot.be.regenerated"));
		}
	}

	@Test
	public void testValidateInstanceNumbersSuccess_SomeSelectedInstancesCantBeDeleted() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances(3);
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceNumbers(studyId, new HashSet<>(Arrays.asList(1, 2)));
	}

	@Test
	public void testValidateInstanceNumbersSuccess_AllInstancesCanBeDeleted() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances(2);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceNumbers(studyId, new HashSet<>(Arrays.asList(1, 2)));
	}

	@Test
	public void testValidateInstanceNumbersSuccess_StudyHasOneInstance() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances(1);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceNumbers(studyId, Collections.singleton(1));
	}

	@Test
	public void testValidateInstanceDeletionFail_AllSelectedInstancesCantBeDeleted() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances();
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		instances.get(1).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(Arrays.asList(101, 102)), false);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("all.selected.instances.cannot.be.regenerated"));
		}
	}

	@Test
	public void testValidateInstanceDeletionFail_OnlyOneStudyInstanceRemaining() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances(1);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(Collections.singletonList(101)), true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("cannot.delete.last.instance"));
		}
	}

	private List<StudyInstance> createTestInstances() {
		return this.createTestInstances(3);
	}

	private List<StudyInstance> createTestInstances(final Integer count) {
		final List<StudyInstance> instances = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			final StudyInstance instance = new StudyInstance();
			instance.setInstanceNumber(i);
			instance.setInstanceId(100 + i);
			instance.setCanBeDeleted(Boolean.TRUE);
			instance.setHasExperimentalDesign(true);
			instances.add(instance);
		}
		return instances;
	}

	@Test
	public void testValidateInstanceDeletionFail_AllInstancesShouldBeDeletableButOneIsNot() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances();
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		instances.get(1).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(Arrays.asList(101, 102)), true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("at.least.one.instance.cannot.be.deleted"));
		}
	}

	@Test
	public void testValidateInstanceDeletionSuccess_AllSelectedInstancesShouldBeDeletable() {

		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances();
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateStudyInstance(studyId, Collections.singleton(101), true);
	}


	@Test
	public void testValidateInstanceDeletionSuccess_SomeSelectedInstancesCantBeDeleted() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances();
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(Arrays.asList(101, 102)), false);
	}

	@Test
	public void testValidateInstanceDeletionSuccess_AllInstancesCanBeDeleted() {
		final int studyId = this.random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances();
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(Arrays.asList(101, 102, 1033)), false);
	}

}
