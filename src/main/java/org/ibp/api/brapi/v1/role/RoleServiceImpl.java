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
	public List<RoleDto> getAllRoles() {
		List<RoleDto> roles = new ArrayList<RoleDto>();

		RoleDto admin = new RoleDto(1, ADMIN);
		RoleDto breeder = new RoleDto(2, BREEDER);
		RoleDto technician = new RoleDto(3, TECHNICIAN);

		roles.add(admin);
		roles.add(breeder);
		roles.add(technician);
		return roles;
	}

}
