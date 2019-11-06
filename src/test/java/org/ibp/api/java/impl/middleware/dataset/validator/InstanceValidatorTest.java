package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Sets;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;

public class InstanceValidatorTest {

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private StudyService middlewareStudyService;

	@InjectMocks
	private InstanceValidator instanceValidator;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateInstanceIds_Success() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int instanceId = ran.nextInt();

		Mockito.when(studyDataManager.areAllInstancesExistInDataset(datasetId, Sets.newHashSet(instanceId))).thenReturn(true);
		Mockito.when(studyDataManager.existInstances(Sets.newHashSet(instanceId))).thenReturn(true);
		instanceValidator.validate(datasetId, Sets.newHashSet(instanceId));
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateInstanceIds_Fail() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int instanceId = ran.nextInt();

		Mockito.when(studyDataManager.areAllInstancesExistInDataset(datasetId, Sets.newHashSet(instanceId))).thenReturn(false);
		instanceValidator.validate(datasetId, Sets.newHashSet(instanceId));
	}

	@Test
	public void testValidateForDesignGenerationFail_EmptyInstanceNumbers() {
		try {
			final Random random = new Random();
			this.instanceValidator.validateForDesignGeneration(random.nextInt(), null);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.instances.required"));
		}
	}

	@Test
	public void testValidateForDesignGenerationFail_StudyHasNoInstances() {
		Mockito.doReturn(Collections.emptyMap()).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());
		try {
			final Random random = new Random();
			this.instanceValidator.validateForDesignGeneration(random.nextInt(), new HashSet<>(Arrays.asList(1,2)));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateForDesignGenerationFail_InstancesDontExistInStudy() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());
		final Random random = new Random();
		try {
			this.instanceValidator.validateForDesignGeneration(random.nextInt(), new HashSet<>(Arrays.asList(1,2,4)));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testValidateForDesignGenerationFail_AllSelectedInstancesCantBeRegenerated() {
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
			instance.setDesignRegenerationAllowed(Boolean.TRUE);
			instance.setHasExperimentalDesign(true);
			instances.add(instance);
		}
		instances.get(0).setDesignRegenerationAllowed(Boolean.FALSE);
		instances.get(1).setDesignRegenerationAllowed(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.middlewareStudyService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateForDesignGeneration(studyId, new HashSet<>(Arrays.asList(1,2)));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("all.selected.instances.cannot.be.regenerated"));
		}
	}

	@Test
	public void testValidateForDesignGenerationFail_InstanceForRegenerationWithCrossOrAdvanceList() {
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
			instances.add(instance);
		}
		// First instance is for regeneration
		instances.get(0).setHasExperimentalDesign(Boolean.TRUE);
		instances.get(0).setDesignRegenerationAllowed(new Random().nextBoolean());
		Mockito.doReturn(true).when(this.middlewareStudyService).hasAdvancedOrCrossesList(studyId);
		Mockito.doReturn(instances).when(this.middlewareStudyService).getStudyInstances(studyId);

		try {
			this.instanceValidator.validateForDesignGeneration(studyId, new HashSet<>(Arrays.asList(1,2)));
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.has.advance.or.cross.list"));
		}
	}

	@Test
	public void testValidateForDesignGenerationSuccess_AllNewInstancesWithCrossOrAdvanceList() {
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
			instances.add(instance);
		}
		Mockito.doReturn(true).when(this.middlewareStudyService).hasAdvancedOrCrossesList(studyId);
		Mockito.doReturn(instances).when(this.middlewareStudyService).getStudyInstances(studyId);

		this.instanceValidator.validateForDesignGeneration(studyId, new HashSet<>(Arrays.asList(1,2)));

	}

	@Test
	public void testValidateForDesignGenerationSuccess_SomeSelectedInstancesCantBeRegenerated() {
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
			instance.setDesignRegenerationAllowed(Boolean.TRUE);
			instances.add(instance);
		}
		instances.get(0).setDesignRegenerationAllowed(Boolean.FALSE);
		Mockito.doReturn(instances).when(this.middlewareStudyService).getStudyInstances(studyId);

		this.instanceValidator.validateForDesignGeneration(studyId, new HashSet<>(Arrays.asList(1,2)));
	}

	@Test
	public void testValidateForDesignGenerationSuccess_AllSelectedInstancesCanBeRegenerated() {
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
			instance.setDesignRegenerationAllowed(Boolean.TRUE);
			instances.add(instance);
		}
		Mockito.doReturn(instances).when(this.middlewareStudyService).getStudyInstances(studyId);

		this.instanceValidator.validateForDesignGeneration(studyId, new HashSet<>(Arrays.asList(1,2)));
	}



}
