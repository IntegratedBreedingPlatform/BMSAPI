
package org.ibp.api.brapi.v1.role;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.ibp.ApiUnitTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class RoleServiceImplTest extends ApiUnitTestBase {
	
	
	@Mock
	private WorkbenchDataManager workbenchManager;

	@InjectMocks
	private RoleServiceImpl roleServiceImpl;
	
	private List<Role> allRoles;
	private Role restrictedRole;
	
	@Before 
	public void setup() {
		this.createTestRoles();
		final List<Role> assignableRoles = new ArrayList<>(this.allRoles);
		assignableRoles.remove(this.restrictedRole);
		Mockito.doReturn(this.allRoles).when(this.workbenchDataManager).getAllRoles();
		Mockito.doReturn(assignableRoles).when(this.workbenchDataManager).getAssignableRoles();
	}
	
	@Test
	public void testGetAllRoles() throws Exception {
		final List<RoleDto> allRoles = this.roleServiceImpl.getAllRoles();
		Mockito.verify(this.workbenchDataManager).getAllRoles();
		Assert.assertEquals(this.allRoles.size(), allRoles.size());
	}
	
	@Test
	public void testGetAssignableRoles() throws Exception {
		final List<RoleDto> roles = this.roleServiceImpl.getAssignableRoles();
		Mockito.verify(this.workbenchDataManager).getAssignableRoles();
		Assert.assertEquals(this.allRoles.size() - 1, roles.size());
		Assert.assertFalse(roles.contains(this.restrictedRole));
	}

	private void createTestRoles() {
		this.allRoles = new ArrayList<>();
		Role admin = new Role(1, "ADMIN");
		Role breeder = new Role(2, "BREEDER");
		Role technician = new Role(3, "TECHNICIAN");
		this.restrictedRole = new Role(4, Role.SUPERADMIN);
		
		this.allRoles.add(admin);
		this.allRoles.add(breeder);
		this.allRoles.add(technician);
		this.allRoles.add(this.restrictedRole);
	}
}
