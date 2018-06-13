
package org.ibp.api.brapi.v1.role;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.ibp.ApiUnitTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class RoleServiceImplTest extends ApiUnitTestBase {
	
	
	@Mock
	private WorkbenchDataManager workbenchManager;

	@InjectMocks
	private RoleServiceImpl roleServiceImpl;
	
	@Test
	public void testGetAllUsers() throws Exception {
		
		List<Role> roles = new ArrayList<>();
		Role admin = new Role(1, "ADMIN");
		Role breeder = new Role(2, "BREEDER");
		Role technician = new Role(3, "TECHNICIAN");
		roles.add(admin);
		roles.add(breeder);
		roles.add(technician);
		Mockito.doReturn(roles).when(this.workbenchDataManager).getAssignableRoles();

		final List<RoleDto> allRoles = this.roleServiceImpl.getAllRoles();
		Mockito.verify(this.workbenchDataManager).getAssignableRoles();
		Assert.assertEquals(roles.size(), allRoles.size());
		for (int i = 0; i < allRoles.size(); i++) {
			final RoleDto roleDto = allRoles.get(i);
			final Role expectedRole = roles.get(i);
		}
	}
}
