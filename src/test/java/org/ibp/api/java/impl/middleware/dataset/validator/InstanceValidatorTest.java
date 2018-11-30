package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.Sets;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

public class InstanceValidatorTest extends ApiUnitTestBase {

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private StudyDataManager studyDataManager;

	@Test
	public void testValidateSuccess() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int instanceId = ran.nextInt();

		Mockito.when(studyDataManager.areAllInstancesExistInDataset(datasetId, Sets.newHashSet(instanceId))).thenReturn(true);
		instanceValidator.validate(datasetId, Sets.newHashSet(instanceId));

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateRejected() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int instanceId = ran.nextInt();

		Mockito.when(studyDataManager.areAllInstancesExistInDataset(datasetId, Sets.newHashSet(instanceId))).thenReturn(false);
		instanceValidator.validate(datasetId, Sets.newHashSet(instanceId));

	}

}
