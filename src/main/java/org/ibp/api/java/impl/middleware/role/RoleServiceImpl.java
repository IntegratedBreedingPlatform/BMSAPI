package org.ibp.api.java.impl.middleware.role;

import java.util.ArrayList;
import java.util.List;

import org.ibp.api.java.role.RoleData;
import org.ibp.api.java.role.RoleService;

public class RoleServiceImpl implements RoleService {

	public static final String ADMIN = "ADMIN";
	public static final String BREEDER = "BREEDER";
	public static final String TECHNICIAN = "TECHNICIAN";

	@Override
	public List<RoleData> getAllRoles() {
		List<RoleData> roles = new ArrayList<RoleData>();

		RoleData admin = new RoleData(1, ADMIN);
		RoleData breeder = new RoleData(2, BREEDER);
		RoleData technician = new RoleData(3, TECHNICIAN);

		roles.add(admin);
		roles.add(breeder);
		roles.add(technician);
		return roles;
	}

}
