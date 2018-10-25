package org.ibp.api.rest.dataset.validator;

import org.generationcp.middleware.domain.dms.Study;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

public class StudyValidatorTest extends ApiUnitTestBase {

	@Autowired
	private StudyValidator studyValidator;

	@Test (expected = ResourceNotFoundException.class)
	public void testStudyDoesNotExist() throws Exception {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(null);
		studyValidator.validate(studyId, ran.nextBoolean());
	}

	@Test (expected = ForbiddenException.class)
	public void testStudyIsLocked() throws Exception {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(study);
		studyValidator.validate(studyId, true);
	}

}
