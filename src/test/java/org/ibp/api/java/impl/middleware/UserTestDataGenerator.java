package org.ibp.api.java.impl.middleware;

import com.google.common.collect.Lists;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.workbench.CropDto;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Permission;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.RoleType;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserRoleDto;
import org.ibp.api.domain.user.UserDetailDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public abstract class UserTestDataGenerator {

	/**
	 * initialize List all user
	 *
	 * @return List<UserDto>
	 */
	public static List<UserDto> getAllListUser() {
		final UserDto user = initializeUserDto(10);
		final List<UserDto> users = Lists.newArrayList(user);

		return users;
	}

	/**
	 * initialize Workbench User
	 *
	 * @param userId Integer
	 * @param role Role
	 * @return user WorkbenchUser
	 */
	public static WorkbenchUser initializeWorkbenchUser(final Integer userId, final Role role) {
		final WorkbenchUser user = new WorkbenchUser();
		final Person person = new Person();

		person.setId(2);

		final String firstName = RandomStringUtils.randomAlphanumeric(20);
		person.setFirstName(firstName);

		person.setMiddleName("");
		final String lastName = RandomStringUtils.randomAlphanumeric(50);
		person.setLastName(lastName);

		final String email = RandomStringUtils.randomAlphanumeric(24);
		person.setEmail("test" + email + "@leafnode.io");

		person.setTitle("-");
		person.setContact("-");
		person.setExtension("-");
		person.setFax("-");
		person.setInstituteId(0);
		person.setLanguage(0);
		person.setNotes("-");
		person.setPositionName("-");
		person.setPhone("-");

		user.setPerson(person);
		user.setUserid(userId);
		user.setPerson(person);
		user.setStatus(0);

		final String username = RandomStringUtils.randomAlphanumeric(30);
		user.setName(username);
		user.setAccess(0);
		user.setInstalid(0);
		user.setType(0);

		user.setRoles(Arrays.asList(new UserRole(user, role)));

		return user;
	}

	/**
	 * initialize UserDto
	 * @param userId Integer
	 * @return user UserDto
	 */
	public static UserDto initializeUserDto(final Integer userId) {
		final UserDto user = new UserDto();

		final String firstName = RandomStringUtils.randomAlphanumeric(20);
		user.setFirstName(firstName);

		final String lastName = RandomStringUtils.randomAlphanumeric(50);
		user.setLastName(lastName);

		user.setStatus(0);
		final List<UserRoleDto> userRoleDtos = new ArrayList<>();
		final UserRoleDto userRoleDto = new UserRoleDto();
		userRoleDto.setId(1);
		final RoleDto roleDto = new RoleDto();
		roleDto.setName("Breeder");
		userRoleDto.setRole(roleDto);
		userRoleDto.setCrop(new CropDto(new CropType("maize")));
		userRoleDtos.add(userRoleDto);

		user.setUserRoles(userRoleDtos);
		user.setUserId(userId);

		final String username = RandomStringUtils.randomAlphanumeric(30);
		user.setUsername(username);

		final String email = RandomStringUtils.randomAlphanumeric(24);
		user.setEmail("test" + email + "@leafnode.io");

		return user;
	}

	/**
	 * Initialize UserDetailDto
	 *
	 * @param userId Integer
	 * @param cropType CropType
	 * @param userRoleDto UserRoleDto

	 * @return UserDetailDto
	 */
	public static UserDetailDto initializeUserDetailDto(final Integer userId, final CropType cropType, final UserRoleDto userRoleDto) {
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailDto();
		user.setId(userId);

		final List<UserRoleDto> userRoleDtoList = new ArrayList<>();
		userRoleDtoList.add(userRoleDto);
		final Set<CropDto> cropDtoList = new HashSet<>();
		cropDtoList.add(new CropDto(cropType));
		user.setCrops(cropDtoList);
		user.setUserRoles(userRoleDtoList);

		return user;
	}

	/**
	 * Initialize UserDetailDto with Role Admin
	 *
	 * @param userId Integer
	 * @param cropType CropType
	 * @return UserDetailDto
	 */
	public static UserDetailDto initializeUserDetailWithAdminRoleDto(final Integer userId, final CropType cropType) {
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailDto();
		user.setId(userId);

		final List<UserRoleDto> userRoleDtoList = new ArrayList<>();
		userRoleDtoList.add(UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final Set<CropDto> cropDtoList = new HashSet<>();
		cropDtoList.add(new CropDto(cropType));
		user.setCrops(cropDtoList);
		user.setUserRoles(userRoleDtoList);

		return user;
	}

	/**
	 * initialize UserDetailDto
	 *
	 * @return UserDetailDto
	 */
	public static UserDetailDto initializeUserDetailWithAdminRoleDto() {
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailDto();
		user.setUserRoles(Arrays.asList(UserTestDataGenerator.initializeUserRoleDtoAdmin()));
		return user;
	}

	private static UserDetailDto initializeUserDetailDto() {
		final UserDetailDto user = new UserDetailDto();
		final Integer userId = ThreadLocalRandom.current().nextInt();

		final String firstName = RandomStringUtils.randomAlphanumeric(20);
		user.setFirstName(firstName);

		final String lastName = RandomStringUtils.randomAlphanumeric(50);
		user.setLastName(lastName);
		user.setId(userId);

		final String username = RandomStringUtils.randomAlphanumeric(30);
		user.setUsername(username);

		final String email = RandomStringUtils.randomAlphanumeric(24);
		user.setEmail("test" + email + "@leafnode.io");
		user.setStatus("true");
		user.setId(userId);
		return user;
	}

	/**
	 * initialize List UserDetailDto
	 *
	 * @return List<UserDetailDto>
	 */
	public static List<UserDetailDto> initializeListUserDetailDto() {
		final List<UserDetailDto> users = Lists.newArrayList(UserTestDataGenerator.initializeUserDetailWithAdminRoleDto());
		return users;
	}

	public static UserRoleDto initializeUserRoleDtoSuperAdmin() {
		final RoleDto role = new RoleDto(5,"SuperAdmin","","1",true,false,false);
		final UserRoleDto userRoleDto = new UserRoleDto(RandomUtils.nextInt(), role, null, null, 1);
		userRoleDto.setRole(role);
		return userRoleDto;
	}

	public static UserRoleDto initializeUserRoleDtoAdmin() {
		final RoleDto role = new RoleDto(1,"Admin","","1",true,true,true);
		final UserRoleDto userRoleDto = new UserRoleDto(RandomUtils.nextInt(), role, null, null, 1);
		userRoleDto.setRole(role);
		return userRoleDto;
	}

	public static UserRoleDto initializeUserRoleDtoBreeder(final CropDto cropDto) {
		final RoleDto role = new RoleDto(2,"Breeder","","2",true,true,true);
		final UserRoleDto userRoleDto = new UserRoleDto(RandomUtils.nextInt(), role, cropDto, null, 1);
		userRoleDto.setRole(role);
		return userRoleDto;
	}

	public static Role initializeUserRoleSuperAdmin() {
		final Role role = new Role("", "SuperAdmin");
		role.setActive(true);
		role.setId(5);
		role.setAssignable(false);
		role.setEditable(false);
		final RoleType roleType = new RoleType("INSTANCE");
		roleType.setId(1);
		role.setRoleType(roleType);
		List<Permission> permissions = new ArrayList<>();
		final Permission permission = new Permission();
		permission.setPermissionId(1);
		permission.setName("ADMIN");
		permission.setDescription("Full");
		permissions.add(permission);
		role.setPermissions(permissions);
		return role;
	}

	public static Role initializeUserRoleAdmin() {
		final Role role = new Role("", "Admin");
		role.setId(1);
		role.setActive(true);
		role.setAssignable(true);
		role.setEditable(true);
		final RoleType roleType = new RoleType("INSTANCE");
		roleType.setId(1);
		role.setRoleType(roleType);
		List<Permission> permissions = new ArrayList<>();
		final Permission permission = new Permission();
		permission.setPermissionId(1);
		permission.setName("ADMIN");
		permission.setDescription("Full");
		permissions.add(permission);
		role.setPermissions(permissions);
		return role;
	}

	public static Role initializeUserRoleBreeder() {
		final Role role = new Role("", "Breeder");
		role.setId(2);
		role.setActive(true);
		role.setAssignable(true);
		role.setEditable(true);
		final RoleType roleType = new RoleType("CROP");
		roleType.setId(2);
		role.setRoleType(roleType);
		List<Permission> permissions = new ArrayList<>();
		final Permission permission = new Permission();
		final Permission parent = new Permission();
		parent.setPermissionId(1);
		parent.setName("ADMIN");
		parent.setDescription("Full");

		permission.setPermissionId(4);
		permission.setName("GERMPLASM");
		permission.setDescription("Germplasm");
		permission.setParent(parent);
		permissions.add(permission);
		role.setPermissions(permissions);
		return role;
	}

}
