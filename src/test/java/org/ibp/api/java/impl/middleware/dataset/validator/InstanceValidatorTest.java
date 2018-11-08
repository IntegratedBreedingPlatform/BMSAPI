package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ResourceNotFoundException;
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

		Mockito.when(studyDataManager.isInstanceExistsInDataset(datasetId, instanceId)).thenReturn(true);
		instanceValidator.validate(datasetId, instanceId);

	}

	@Test(expected = ResourceNotFoundException.class)
	public void testValidateRejected() {
		final Random ran = new Random();
		final int datasetId = ran.nextInt();
		final int instanceId = ran.nextInt();

		Mockito.when(studyDataManager.isInstanceExistsInDataset(datasetId, instanceId)).thenReturn(false);
		instanceValidator.validate(datasetId, instanceId);

	}

}
