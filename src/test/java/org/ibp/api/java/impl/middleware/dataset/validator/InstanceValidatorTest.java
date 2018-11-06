package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.base.Optional;
import org.generationcp.middleware.dao.dms.InstanceMetadata;
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
		final int studyId = ran.nextInt();
		final int instanceId = ran.nextInt();

		final Optional<InstanceMetadata> instanceMetadataOptional = Optional.of(new InstanceMetadata());
		Mockito.when(studyDataManager.getInstanceMetadataByInstanceId(studyId, instanceId)).thenReturn(instanceMetadataOptional);
		instanceValidator.validate(studyId, instanceId);

	}

	@Test(expected = ResourceNotFoundException.class)
	public void testValidateRejected() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final int instanceId = ran.nextInt();

		Mockito.when(studyDataManager.getInstanceMetadataByInstanceId(studyId, instanceId)).thenReturn(Optional.<InstanceMetadata>absent());
		instanceValidator.validate(studyId, instanceId);

	}

}
