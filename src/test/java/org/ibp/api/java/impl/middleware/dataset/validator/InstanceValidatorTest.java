package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Sets;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
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

	@Mock
	private StudyInstanceService studyInstanceService;

	@InjectMocks
	private InstanceValidator instanceValidator;

	@Test
	public void testValidateInstanceIds_Success() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int instanceId = ran.nextInt();

		when(this.studyDataManager.areAllInstancesExistInDataset(datasetId, Sets.newHashSet(instanceId))).thenReturn(true);
		when(this.studyDataManager.existInstances(Sets.newHashSet(instanceId))).thenReturn(true);
		this.instanceValidator.validate(datasetId, Sets.newHashSet(instanceId));
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateInstanceIds_Fail() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int instanceId = ran.nextInt();

		when(this.studyDataManager.areAllInstancesExistInDataset(datasetId, Sets.newHashSet(instanceId))).thenReturn(false);
		this.instanceValidator.validate(datasetId, Sets.newHashSet(instanceId));
	}

	@Test
	public void testValidateInstanceDeletionFail_EmptyInstanceNumbers() {
		try {
			final Random random = new Random();
			this.instanceValidator.validateInstanceDeletion(random.nextInt(), null, random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.instances.required"));
		}
	}

	@Test
	public void testValidateInstanceDeletionFail_StudyHasNoInstances() {
		Mockito.doReturn(Collections.emptyMap()).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());
		try {
			final Random random = new Random();
			this.instanceValidator.validateInstanceDeletion(random.nextInt(), new HashSet<>(Arrays.asList(1, 2)), random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateInstanceDeletionFail_InstancesDontExistInStudy() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());
		final Random random = new Random();
		try {
			this.instanceValidator.validateInstanceDeletion(random.nextInt(), new HashSet<>(Arrays.asList(1, 2, 4)), random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateInstanceDeletionFail_AllSelectedInstancesCantBeDeleted() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final int studyId = new Random().nextInt();
		final List<StudyInstance> instances = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			final StudyInstance instance = new StudyInstance();
			instance.setInstanceNumber(i);
			instance.setCanBeDeleted(Boolean.TRUE);
			instance.setHasExperimentalDesign(true);
			instances.add(instance);
		}
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		instances.get(1).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(1, 2)), false);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("all.selected.instances.cannot.be.regenerated"));
		}
	}

	@Test
	public void testValidateInstanceDeletionFail_AllInstancesShouldBeDeletableButOneIsNot() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final int studyId = new Random().nextInt();
		final List<StudyInstance> instances = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			final StudyInstance instance = new StudyInstance();
			instance.setInstanceNumber(i);
			instance.setCanBeDeleted(Boolean.TRUE);
			instance.setHasExperimentalDesign(true);
			instances.add(instance);
		}
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		instances.get(1).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(1, 2)), true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("at.least.one.instance.cannot.be.deleted"));
		}
	}

	@Test
	public void testValidateInstanceDeletionSuccess_AllSelectedInstancesShouldBeDeletable() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final int studyId = new Random().nextInt();
		final List<StudyInstance> instances = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			final StudyInstance instance = new StudyInstance();
			instance.setInstanceNumber(i);
			instance.setCanBeDeleted(Boolean.TRUE);
			instance.setHasExperimentalDesign(true);
			instances.add(instance);
		}
		instances.get(1).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(1)), true);
	}


	@Test
	public void testValidateInstanceDeletionSuccess_SomeSelectedInstancesCantBeDeleted() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final int studyId = new Random().nextInt();
		final List<StudyInstance> instances = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			final StudyInstance instance = new StudyInstance();
			instance.setInstanceNumber(i);
			instance.setCanBeDeleted(Boolean.TRUE);
			instances.add(instance);
		}
		instances.get(0).setCanBeDeleted(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(1, 2)), false);
	}

	@Test
	public void testValidateInstanceDeletionSuccess_AllInstancesCanBeDeleted() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final Random random = new Random();
		final int studyId = random.nextInt();
		final List<StudyInstance> instances = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			final StudyInstance instance = new StudyInstance();
			instance.setInstanceNumber(i);
			instance.setCanBeDeleted(Boolean.TRUE);
			instances.add(instance);
		}
		Mockito.doReturn(instances).when(this.studyInstanceService).getStudyInstances(studyId);

		this.instanceValidator.validateInstanceDeletion(studyId, new HashSet<>(Arrays.asList(1, 2, 3)), random.nextBoolean());
	}

}
