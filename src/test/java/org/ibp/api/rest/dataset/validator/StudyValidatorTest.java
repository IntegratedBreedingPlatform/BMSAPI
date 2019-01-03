package org.ibp.api.rest.dataset.validator;

import java.util.Random;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;

public class StudyValidatorTest {

	@Mock
	private HttpServletRequest request;

	@Mock
	private SecurityService securityService;

	@Mock
	private StudyDataManager studyDataManager;

	@InjectMocks
	private StudyValidator studyValidator;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);
	}

	@Test (expected = ResourceNotFoundException.class)
	public void testStudyDoesNotExist() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(null);
		studyValidator.validate(studyId, ran.nextBoolean());
	}

	@Test (expected = ForbiddenException.class)
	public void testStudyIsLocked() {
		final Random ran = new Random();
		final Integer studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(study);
		studyValidator.validate(studyId, true);
	}

}
