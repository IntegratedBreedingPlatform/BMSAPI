package org.ibp.api.brapi.v1.role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

	public static final String ADMIN = "Admin";
	public static final String BREEDER = "Breeder";
	public static final String TECHNICIAN = "Technician";

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
