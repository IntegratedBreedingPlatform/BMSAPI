package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Sets;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;

public class InstanceValidatorTest {

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private DatasetService middlewareDatasetService;

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
	public void testStudyInstanceNumbersFail_EmptyInstanceNumbers() {
		try {
			final Random random = new Random();
			this.instanceValidator.validate(random.nextInt(), null, random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.instances.required"));
		}
	}

	@Test
	public void testStudyInstanceNumbersFail_StudyHasNoInstances() {
		Mockito.doReturn(Collections.emptyMap()).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());
		try {
			final Random random = new Random();
			this.instanceValidator.validate(random.nextInt(), new HashSet<>(Arrays.asList(1,2)), random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testStudyInstanceNumbersFail_InstancesDontExistInStudy() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());
		final Random random = new Random();
		try {
			this.instanceValidator.validate(random.nextInt(), new HashSet<>(Arrays.asList(1,2,4)), random.nextBoolean());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("dataset.non.existent.instances"));
		}
	}

	@Test
	public void testStudyInstanceNumbersFail_InstanceAlreadyHasObservations() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final Random random = new Random();
		final int studyId = random.nextInt();
		final Integer plotDatasetId = random.nextInt();
		Mockito.doReturn(plotDatasetId).when(this.middlewareStudyService).getPlotDatasetId(studyId);
		final Map<String, Long> observationsMap = new HashMap<>();
		observationsMap.put("1", 10L);
		observationsMap.put("2", 10L);
		Mockito.doReturn(observationsMap).when(this.middlewareDatasetService).countObservationsGroupedByInstance(plotDatasetId);
		try {
			this.instanceValidator.validate(studyId, new HashSet<>(Arrays.asList(1,2,3)), false);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("instances.already.have.observation"));
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getArguments()),
				hasItem("1, 2"));
		}
	}

	@Test
	public void testStudyInstanceNumbersFail_InstanceDoesntHaveExpectedObservations() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final Random random = new Random();
		final int studyId = random.nextInt();
		final Integer plotDatasetId = random.nextInt();
		Mockito.doReturn(plotDatasetId).when(this.middlewareStudyService).getPlotDatasetId(studyId);
		final Map<String, Long> observationsMap = new HashMap<>();
		observationsMap.put("1", 10L);
		observationsMap.put("2", 10L);
		Mockito.doReturn(observationsMap).when(this.middlewareDatasetService).countObservationsGroupedByInstance(plotDatasetId);
		try {
			this.instanceValidator.validate(studyId, new HashSet<>(Arrays.asList(1,3)), true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("instances.should.have.observations"));
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getArguments()),
				hasItem("3"));
		}
	}

	@Test
	public void testStudyInstanceNumbersSuccess_StudyHasNoObservations() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final Random random = new Random();
		final int studyId = random.nextInt();
		final Integer plotDatasetId = random.nextInt();
		Mockito.doReturn(plotDatasetId).when(this.middlewareStudyService).getPlotDatasetId(studyId);
		Mockito.doReturn(Collections.emptyMap()).when(this.middlewareDatasetService).countObservationsGroupedByInstance(plotDatasetId);

		this.instanceValidator.validate(studyId, new HashSet<>(Arrays.asList(1,3)), false);
	}

	@Test
	public void testStudyInstanceNumbersSuccess_InstanceHasNoObservations() {
		final Map<String, Integer> instancesMap = new HashMap<>();
		instancesMap.put("1", 101);
		instancesMap.put("2", 202);
		instancesMap.put("3", 303);
		Mockito.doReturn(instancesMap).when(this.studyDataManager).getInstanceGeolocationIdsMap(ArgumentMatchers.anyInt());

		final Random random = new Random();
		final int studyId = random.nextInt();
		final Integer plotDatasetId = random.nextInt();
		Mockito.doReturn(plotDatasetId).when(this.middlewareStudyService).getPlotDatasetId(studyId);
		final Map<String, Long> observationsMap = new HashMap<>();
		observationsMap.put("1", 10L);
		observationsMap.put("2", 10L);
		Mockito.doReturn(observationsMap).when(this.middlewareDatasetService).countObservationsGroupedByInstance(plotDatasetId);

		this.instanceValidator.validate(studyId, new HashSet<>(Collections.singletonList(3)), false);
	}

}
