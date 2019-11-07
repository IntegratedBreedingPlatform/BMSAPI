package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Sets;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class InstanceValidatorTest extends ApiUnitTestBase {

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private StudyDataManager studyDataManager;

	private final Random random = new Random();

	@Test
	public void testValidateSuccess() {
		final int datasetId = this.random.nextInt();
		final int instanceId = this.random.nextInt();

		when(this.studyDataManager.areAllInstancesExistInDataset(datasetId, Sets.newHashSet(instanceId))).thenReturn(true);
		when(this.studyDataManager.existInstances(Sets.newHashSet(instanceId))).thenReturn(true);
		this.instanceValidator.validate(datasetId, Sets.newHashSet(instanceId));

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateRejected() {
		final int datasetId = this.random.nextInt();
		final int instanceId = this.random.nextInt();

		when(this.studyDataManager.areAllInstancesExistInDataset(datasetId, Sets.newHashSet(instanceId))).thenReturn(false);
		this.instanceValidator.validate(datasetId, Sets.newHashSet(instanceId));

	}

	@Test
	public void testCheckStudyInstanceAlreadyExistsSuccess() {

		final int studyId = this.random.nextInt();
		final int instanceNumber = this.random.nextInt();

		final Map<String, Integer> instanceNumberGeolocationIdsMap = new HashMap<>();
		when(this.studyDataManager.getInstanceGeolocationIdsMap(studyId)).thenReturn(instanceNumberGeolocationIdsMap);

		try {
			this.instanceValidator.checkStudyInstanceAlreadyExists(studyId, instanceNumber);
		} catch (ApiRequestValidationException e) {
			fail("Method should not throw an exception");
		}

	}

	@Test
	public void testCheckStudyInstanceAlreadyExistsFail() {

		final int studyId = this.random.nextInt();
		final int instanceNumber = this.random.nextInt();

		final Map<String, Integer> instanceNumberGeolocationIdsMap = new HashMap<>();
		instanceNumberGeolocationIdsMap.put(String.valueOf(instanceNumber), this.random.nextInt());
		when(this.studyDataManager.getInstanceGeolocationIdsMap(studyId)).thenReturn(instanceNumberGeolocationIdsMap);

		try {
			this.instanceValidator.checkStudyInstanceAlreadyExists(studyId, instanceNumber);
			fail("Method should throw an exception");
		} catch (ApiRequestValidationException e) {
			assertEquals("instance.already.exists", e.getErrors().get(0).getCode());
		}
	}

}
