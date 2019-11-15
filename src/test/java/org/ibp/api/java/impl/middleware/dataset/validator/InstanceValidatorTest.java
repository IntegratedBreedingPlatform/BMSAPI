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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
		this.datasetId = random.nextInt();
		Mockito.doReturn(datasetId).when(this.middlewareStudyService).getEnvironmentDatasetId(ArgumentMatchers.anyInt());
		when(this.studyDataManager.areAllInstancesExistInDataset(ArgumentMatchers.eq(this.datasetId), ArgumentMatchers.anySet()))
			.thenReturn(true);
		when(this.studyDataManager.existInstances(ArgumentMatchers.anySet())).thenReturn(true);
	}

	@Test
	public void testValidateInstanceIds_Success() {
		final int instanceId = random.nextInt();
		this.instanceValidator.validate(this.datasetId, Sets.newHashSet(instanceId));
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateInstanceIds_Fail() {
		final int instanceId = random.nextInt();

		when(this.studyDataManager.areAllInstancesExistInDataset(this.datasetId, Sets.newHashSet(instanceId))).thenReturn(false);
		this.instanceValidator.validate(this.datasetId, Sets.newHashSet(instanceId));
	}

	@Test
	public void testValidateInstanceDeletionFail_EmptyInstanceNumbers() {
		try {
			this.instanceValidator.validateInstanceDeletion(random.nextInt(), null, random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.instances.required"));
		}
	}

	@Test
	public void testValidateInstanceDeletionFail_StudyHasNoInstances() {
		final HashSet<Integer> instanceIds = new HashSet<>(Arrays.asList(random.nextInt(), random.nextInt()));
		when(this.studyDataManager.areAllInstancesExistInDataset(this.datasetId, instanceIds)).thenReturn(false);
		try {
			this.instanceValidator.validateInstanceDeletion(this.datasetId, instanceIds, random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.invalid.instances"));
		}
	}

	@Test
	public void testValidateInstanceNumbersFail_StudyHasNoInstances() {
		Mockito.doReturn(Collections.emptyMap()).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());
		try {
			this.instanceValidator.validateInstanceNumbers(random.nextInt(), Collections.singleton(random.nextInt()));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateInstanceNumbersFail_InstanceNumbersDontExistInStudy() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());
		try {
			this.instanceValidator.validateInstanceNumbers(random.nextInt(), new HashSet<>(Arrays.asList(1, 102)));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateInstanceNumbersSuccess_AllInstancesCanBeDeleted() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final int studyId = random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances(2);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceNumbers(studyId, new HashSet<>(Arrays.asList(1, 2)));
	}

	@Test
	public void testValidateInstanceNumbersSuccess_StudyHasOneInstance() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final int studyId = random.nextInt();
		final List<StudyInstance> instances = this.createTestInstances(1);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceNumbers(studyId, Collections.singleton(1));
	}

	@Test
	public void testValidateInstanceDeletionFail_AllSelectedInstancesCantBeDeleted() {
		final int studyId = random.nextInt();
		final List<StudyInstance> instances = createTestInstances();
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		instances.get(1).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(101, 102)), false);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("all.selected.instances.cannot.be.regenerated"));
		}
	}

	@Test
	public void testValidateInstanceDeletionFail_OnlyOneStudyInstanceRemaining() {
		final int studyId = random.nextInt();
		final List<StudyInstance> instances = createTestInstances(1);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Collections.singletonList(101)), true);
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
			instance.setInstanceDbId(100 + i);
			instance.setCanBeDeleted(Boolean.TRUE);
			instance.setHasExperimentalDesign(true);
			instances.add(instance);
		}
		return instances;
	}

	@Test
	public void testValidateInstanceDeletionFail_AllInstancesShouldBeDeletableButOneIsNot() {
		final int studyId = random.nextInt();
		final List<StudyInstance> instances = createTestInstances();
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		instances.get(1).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(101, 102)), true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("at.least.one.instance.cannot.be.deleted"));
		}
	}

	@Test
	public void testValidateInstanceDeletionSuccess_AllSelectedInstancesShouldBeDeletable() {

		final int studyId = random.nextInt();
		final List<StudyInstance> instances = createTestInstances();
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceDeletion(studyId, Collections.singleton(101), true);
	}


	@Test
	public void testValidateInstanceDeletionSuccess_SomeSelectedInstancesCantBeDeleted() {
		final int studyId = random.nextInt();
		final List<StudyInstance> instances = createTestInstances();
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(101, 102)), false);
	}

	@Test
	public void testValidateInstanceDeletionSuccess_AllInstancesCanBeDeleted() {
		final int studyId = random.nextInt();
		final List<StudyInstance> instances = createTestInstances();
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(101, 102, 1033)), random.nextBoolean());
	}

}
