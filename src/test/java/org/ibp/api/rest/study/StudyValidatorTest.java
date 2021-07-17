package org.ibp.api.rest.study;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudyInstanceService;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StudyValidatorTest {

	private static final Integer USER_ID = ThreadLocalRandom.current().nextInt();
	private static final String PROGRAM_UUID = UUID.randomUUID().toString();

	@Mock
	private SecurityService securityService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private StudyInstanceService studyInstanceService;

	@Mock
	private StudyService studyService;

	@Mock
	private UserService userService;

	@InjectMocks
	private StudyValidator studyValidator;

	@Test
	public void testvalidateDeleteStudy_ThrowsException_WhenStudynotExists() {
		try {
			final Integer studyId = RandomUtils.nextInt();
			this.studyValidator.validateDeleteStudy(studyId);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.id.not.exists"));
		}
	}

	@Test
	public void testvalidateDeleteStudy_ThrowsException_WhenStudyHasProgramIUUD() {
		try {
			final Integer studyId = RandomUtils.nextInt();
			final Study study = new Study();
			study.setId(studyId);
			study.setCreatedBy(USER_ID.toString());
			Mockito.doReturn(study).when(studyDataManager).getStudy(studyId, false);
			this.studyValidator.validateDeleteStudy(studyId);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.template.delete.not.permitted"));
		}
	}

	@Test
	public void testvalidateDeleteStudy_ThrowsException_WhenTheUserIsNotOwnerOfStudy() {
		try {
			final Integer studyId = RandomUtils.nextInt();
			final Study study = new Study();
			final WorkbenchUser userLogged = new WorkbenchUser();
			final WorkbenchUser userStudy = new WorkbenchUser();

			final Person person = new Person();
			person.setFirstName(RandomStringUtils.randomAlphanumeric(8));
			person.setMiddleName(RandomStringUtils.randomAlphanumeric(1));
			person.setLastName(RandomStringUtils.randomAlphanumeric(8));

			userStudy.setPerson(person);
			userLogged.setUserid(RandomUtils.nextInt());
			study.setId(studyId);
			study.setProgramUUID(StudyValidatorTest.PROGRAM_UUID);
			study.setCreatedBy(USER_ID.toString());
			Mockito.doReturn(study).when(studyDataManager).getStudy(studyId, false);
			Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(userLogged);
			Mockito.when(this.userService.getUserById(study.getUser())).thenReturn(userStudy);

			this.studyValidator.validateDeleteStudy(studyId);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("study.delete.not.permitted"));
		}
	}

}
