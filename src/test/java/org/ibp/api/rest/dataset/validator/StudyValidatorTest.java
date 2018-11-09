package org.ibp.api.rest.dataset.validator;

import java.util.Random;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class StudyValidatorTest {
	
	@Mock
	private StudyDataManager studyDataManager;

	@InjectMocks
	private StudyValidator studyValidator;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

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
