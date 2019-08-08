package org.ibp.api.java.impl.middleware.role;

import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.ApiUnitTestBase;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class RoleServiceImplTest extends ApiUnitTestBase {

	@Mock
	private UserService userService;

	@InjectMocks
	private RoleServiceImpl roleServiceImpl;
	
	private List<Role> allRoles;
	private Role restrictedRole;
	
	@Before 
	public void setup() {
		this.createTestRoles();
		final List<Role> assignableRoles = new ArrayList<>(this.allRoles);
		assignableRoles.remove(this.restrictedRole);
		Mockito.doReturn(assignableRoles).when(this.workbenchDataManager).getRoles(new RoleSearchDto(Boolean.TRUE, null, null));
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
