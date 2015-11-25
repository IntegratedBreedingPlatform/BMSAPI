package org.ibp.api.java.impl.middleware.security;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.study.StudySummary;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Lists;

public class SecurityServiceImplTest {

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	private SecurityServiceImpl securityServiceImpl = new SecurityServiceImpl();

	private User me;
	private User otherBreeder;
	private UsernamePasswordAuthenticationToken loggedInUser;

	private final String programUUID = "fb0783d2-dc82-4db6-a36e-7554d3740092";

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);

		this.me = new User();
		this.me.setName("Mr. Breeder");
		this.me.setUserid(1);

		this.otherBreeder = new User();
		this.otherBreeder.setName("Other Breeder");
		this.otherBreeder.setUserid(2);

		this.loggedInUser = new UsernamePasswordAuthenticationToken(this.me.getName(), this.me.getPassword());
		SecurityContextHolder.getContext().setAuthentication(this.loggedInUser);

		Mockito.when(this.workbenchDataManager.getUserById(this.me.getUserid())).thenReturn(this.me);
		Mockito.when(this.workbenchDataManager.getUserById(this.otherBreeder.getUserid())).thenReturn(this.otherBreeder);

		Mockito.when(this.workbenchDataManager.getUserByUsername(this.me.getName())).thenReturn(this.me);
		Mockito.when(this.workbenchDataManager.getUserByUsername(this.otherBreeder.getName())).thenReturn(this.otherBreeder);
	}

	@After
	public void afterEachTest() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	/**
	 * Case 1 Study in a program that logged in user is member of.
	 */
	@Test
	public void testIsAccessibleStudySummaryMine() {
		StudySummary summaryStudy = new StudySummary();
		summaryStudy.setProgramUUID(this.programUUID);

		final Project summaryStudyProgram = new Project();
		summaryStudyProgram.setProjectId(2L);
		summaryStudyProgram.setUniqueID(summaryStudy.getProgramUUID());

		Mockito.when(this.workbenchDataManager.getProjectByUuid(summaryStudy.getProgramUUID())).thenReturn(summaryStudyProgram);

		// Logged in user = me is a the member
		Mockito.when(this.workbenchDataManager.getUsersByProjectId(summaryStudyProgram.getProjectId())).thenReturn(
				Lists.newArrayList(this.me));

		// Hence accessible
		Assert.assertTrue(this.securityServiceImpl.isAccessible(summaryStudy));
	}

	/**
	 * Case 2 Study in a program that logged in user is NOT member of.
	 */
	@Test
	public void testIsAccessibleStudySummaryOthers() {
		StudySummary summaryStudy = new StudySummary();
		summaryStudy.setProgramUUID(this.programUUID);

		final Project summaryStudyProgram = new Project();
		summaryStudyProgram.setProjectId(2L);
		summaryStudyProgram.setUniqueID(summaryStudy.getProgramUUID());

		Mockito.when(this.workbenchDataManager.getProjectByUuid(summaryStudy.getProgramUUID())).thenReturn(summaryStudyProgram);

		// Logged in user = me is not the member, some other breeder is
		Mockito.when(this.workbenchDataManager.getUsersByProjectId(summaryStudyProgram.getProjectId())).thenReturn(
				Lists.newArrayList(this.otherBreeder));

		// Hence not accessible
		Assert.assertFalse(this.securityServiceImpl.isAccessible(summaryStudy));
	}

	/**
	 * Case 3 Nursery/Trial templates case where there is no program uuid
	 */
	@Test
	public void testIsAccessibleStudySummaryTemplates() {
		StudySummary summary = new StudySummary();
		summary.setProgramUUID(null);

		// Accessible to all
		Assert.assertTrue(this.securityServiceImpl.isAccessible(summary));
	}

	@Test
	public void testGetCurrentlyLoggedInUser() {
		// We setup authentication context in setup method.
		Assert.assertEquals(this.me, this.securityServiceImpl.getCurrentlyLoggedInUser());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentlyLoggedInUserErrorCase() {
		// We setup authentication context in setup method.
		// We want that cleared for this test case.
		SecurityContextHolder.getContext().setAuthentication(null);
		this.securityServiceImpl.getCurrentlyLoggedInUser();
	}
}
