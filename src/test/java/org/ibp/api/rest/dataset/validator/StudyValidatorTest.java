package org.ibp.api.rest.dataset.validator;

import java.util.Random;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class StudyValidatorTest {

	public static final int USER_ID = 10;

	@Mock
	private HttpServletRequest request;

	@Mock
	private SecurityService securityService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private StudyValidator studyValidator;
	
	@Before
	public void setup() {
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(USER_ID);
		doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();
		doReturn(USER_ID).when(this.contextUtil).getIbdbUserId(ArgumentMatchers.anyInt());
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

	@Test
	public void testStudyIsLockedButUserIsOwner() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		study.setCreatedBy(String.valueOf(USER_ID));
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(study);
		studyValidator.validate(studyId, true);
		// no exceptions thrown
	}

	@Test
	public void testStudyIsLockedButUserIsSuperAdmin() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		doReturn(true).when(this.request).isUserInRole(Role.SUPERADMIN);
		Mockito.when(studyDataManager.getStudy(studyId)).thenReturn(study);
		studyValidator.validate(studyId, true);
		// no exceptions thrown
	}

}
