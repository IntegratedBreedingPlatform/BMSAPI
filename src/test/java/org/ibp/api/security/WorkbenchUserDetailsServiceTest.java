
package org.ibp.api.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class WorkbenchUserDetailsServiceTest {

	private static final String TEST_USER = "testUser";
	private WorkbenchDataManager workbenchDataManager;
	private WorkbenchUserDetailsService service;

	@Before
	public void setUpPerTest() {
		this.workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);
		this.service = new WorkbenchUserDetailsService();
		this.service.setWorkbenchDataManager(this.workbenchDataManager);
	}

	@Test
	public void testLoadUserByUserName() {
		try {
			List<WorkbenchUser> matchingUsers = new ArrayList<WorkbenchUser>();
			WorkbenchUser testUserWorkbench = new WorkbenchUser();
			testUserWorkbench.setName(WorkbenchUserDetailsServiceTest.TEST_USER);
			testUserWorkbench.setPassword("password");
			UserRole testUserRole = new UserRole(testUserWorkbench, new Role(1, "ADMIN"));
			testUserWorkbench.setRoles(Arrays.asList(testUserRole));
			matchingUsers.add(testUserWorkbench);

			Mockito.when(this.workbenchDataManager.getUserByName(WorkbenchUserDetailsServiceTest.TEST_USER, 0, 1, Operation.EQUAL))
					.thenReturn(matchingUsers);

			UserDetails userDetails = this.service.loadUserByUsername(WorkbenchUserDetailsServiceTest.TEST_USER);
			Assert.assertEquals(testUserWorkbench.getName(), userDetails.getUsername());
			Assert.assertEquals(testUserWorkbench.getPassword(), userDetails.getPassword());
			Assert.assertEquals(1, userDetails.getAuthorities().size());
			Assert.assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(testUserRole.getRole().getCapitalizedName())));
		} catch (MiddlewareQueryException e) {
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test
	public void testLoadUserByUsernameUTF8Support() throws Exception {
		String htmlEscaptedUTF8Username = "&#28900;&#29482;";
		String rawUTF8Username = "烤猪";

		List<WorkbenchUser> matchingUsers = new ArrayList<WorkbenchUser>();
		WorkbenchUser testUserWorkbench = new WorkbenchUser();
		testUserWorkbench.setName(rawUTF8Username);
		testUserWorkbench.setPassword("password");
		UserRole testUserRole = new UserRole(testUserWorkbench, new Role(1, "ADMIN"));
		testUserWorkbench.setRoles(Arrays.asList(testUserRole));
		matchingUsers.add(testUserWorkbench);

		Mockito.when(this.workbenchDataManager.getUserByName(rawUTF8Username, 0, 1, Operation.EQUAL)).thenReturn(matchingUsers);

		UserDetails userDetails = this.service.loadUserByUsername(htmlEscaptedUTF8Username);
		Assert.assertEquals(testUserWorkbench.getName(), userDetails.getUsername());
	}

	@Test(expected = UsernameNotFoundException.class)
	public void testLoadUserByNonExistantUserName() throws MiddlewareQueryException {
		Mockito.when(this.workbenchDataManager.getUserByName(WorkbenchUserDetailsServiceTest.TEST_USER, 0, 1, Operation.EQUAL)).thenReturn(
				Collections.<WorkbenchUser>emptyList());
		this.service.loadUserByUsername(WorkbenchUserDetailsServiceTest.TEST_USER);
	}

	@Test(expected = AuthenticationServiceException.class)
	public void testLoadUserDataAccessError() throws MiddlewareQueryException {
		Mockito.when(this.workbenchDataManager.getUserByName(WorkbenchUserDetailsServiceTest.TEST_USER, 0, 1, Operation.EQUAL)).thenThrow(
				new MiddlewareQueryException("Boom!"));
		this.service.loadUserByUsername(WorkbenchUserDetailsServiceTest.TEST_USER);
	}
}
