package org.ibp.api.java.impl.middleware;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.workbench.CropDto;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.RoleDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserRoleDto;
import org.ibp.api.domain.user.UserDetailDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class UserTestDataGenerator {

	/**
	 * initialize Workbench User
	 *
	 * @param userId Integer
	 * @return user WorkbenchUser
	 */
	public static WorkbenchUser initializeWorkbenchUser(final Integer userId) {
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
		user.setPersonid(person.getId());
		user.setPerson(person);

		final String username = RandomStringUtils.randomAlphanumeric(30);
		user.setName(username);
		user.setAccess(0);
		user.setInstalid(0);
		user.setType(0);
		
		user.setRoles(Arrays.asList(new UserRole(user, new Role(2, "Breeder"))));

		return user;
	}
	
	/**
	 * initialize Workbench User
	 *
	 * @param userId Integer
	 * @return user User
	 */
	public static User initializeUser(final Integer userId) {
		final WorkbenchUser workbenchUser = UserTestDataGenerator.initializeWorkbenchUser(userId);
		final User user = workbenchUser.copyToUser();
		user.setUserid(userId);
		
		return user;
	}

	/**
	 * initialize UserDto
	 *
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
	 * Initialize UserDetailDto
	 *
	 * @param userId Integer
	 * @return UserDetailDto
	 */
	public static UserDetailDto initializeUserDetailDto(final Integer userId) {
		final UserDetailDto user = UserTestDataGenerator.initializeUserDetailDto();

		final String firstName = RandomStringUtils.randomAlphanumeric(20);
		user.setFirstName(firstName);

		final String lastName = RandomStringUtils.randomAlphanumeric(50);
		user.setLastName(lastName);
		user.setId(userId);

		final String username = RandomStringUtils.randomAlphanumeric(30);
		user.setUsername(username);

		final String email = RandomStringUtils.randomAlphanumeric(24);
		user.setEmail("test" + email + "@leafnode.io");

		final List<UserRoleDto> userRoleDtos = new ArrayList<>();
		final UserRoleDto userRoleDto = new UserRoleDto();
		userRoleDto.setId(1);
		final RoleDto roleDto = new RoleDto();
		roleDto.setName("Breeder");
		userRoleDto.setRole(roleDto);
		userRoleDto.setCrop(new CropDto(new CropType("maize")));
		userRoleDtos.add(userRoleDto);
		List<CropDto> cropDtos = new ArrayList<>();
		cropDtos.add(new CropDto(new CropType("wheat")));
		user.setCrops(cropDtos);
		user.setUserRoles(userRoleDtos);

		return user;
	}

	/**
	 * initialize UserDetailDto
	 *
	 * @return UserDetailDto
	 */
	public static UserDetailDto initializeUserDetailDto() {
		final UserDetailDto user = new UserDetailDto();
		final String firstName = RandomStringUtils.randomAlphabetic(5);
		final String lastName = RandomStringUtils.randomAlphabetic(5);
		final Integer userId = ThreadLocalRandom.current().nextInt();
		final String username = RandomStringUtils.randomAlphabetic(5);

		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setStatus("true");

		final List<UserRoleDto> userRoleDtos = new ArrayList<>();
		final UserRoleDto userRoleDto = new UserRoleDto();
		userRoleDto.setId(1);
		final RoleDto roleDto = new RoleDto();
		roleDto.setName("Breeder");
		userRoleDto.setRole(roleDto);
		userRoleDto.setCrop(new CropDto(new CropType("maize")));
		userRoleDtos.add(userRoleDto);

		user.setUserRoles(userRoleDtos);

		user.setId(userId);
		user.setUsername(username);
		return user;
	}

	/**
	 * initialize List UserDetailDto
	 *
	 * @return List<UserDetailDto>
	 */
	public static List<UserDetailDto> initializeListUserDetailDto() {
		final List<UserDetailDto> users = Lists.newArrayList(UserTestDataGenerator.initializeUserDetailDto());
		return users;
	}
}
