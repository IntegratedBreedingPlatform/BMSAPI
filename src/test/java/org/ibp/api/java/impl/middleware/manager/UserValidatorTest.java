package org.ibp.api.java.impl.middleware.manager;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class UserValidatorTest {

	@InjectMocks
	private UserValidator uservalidator;

	@Mock
	protected WorkbenchDataManager workbenchDataManager;

	@Mock
	protected UserService userService;

	@Mock
	private SecurityService securityService;

	@Before
	public void beforeEachTest() {
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses())
		.thenReturn(Arrays.asList(new CropType("maize"), new CropType("wheat")));

	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * Should validate all fields empty and with length exceed.* *
	 */
	@Test
	public void testValidateFieldLength() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = new UserDetailDto();

		userDto.setLastName("zxcvbnmaaskjhdfsgeeqwfsafsafg6tk6kglkugt8oljhhlly11");
		userDto.setUsername("wertyuioiuytredsdfrtghjuklsl123");
		userDto.setUserRoles(new ArrayList<>());

		this.uservalidator.validate(userDto, bindingResult, true);

		assertThat(5, equalTo(bindingResult.getAllErrors().size()));
		assertThat(null, equalTo(bindingResult.getFieldError("firstName").getRejectedValue()));
		assertThat("signup.field.length.exceed", equalTo(bindingResult.getFieldError("lastName").getCode()));
		assertThat("30", equalTo(bindingResult.getFieldError("username").getArguments()[0]));
		assertThat("Username", equalTo(bindingResult.getFieldError("username").getArguments()[1]));
		assertThat(null, equalTo(bindingResult.getFieldError("email").getRejectedValue()));
		assertThat("signup.field.required", equalTo(bindingResult.getFieldError("status").getCode()));
	}

	/**
	 * Should validate the Email Format.* *
	 */
	@Test
	public void testValidateEmail() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailWithAdminRoleDto(10, new CropType("wheat"));
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleAdmin());
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(UserTestDataGenerator.initializeUserRoleAdmin()));


		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		//Valid email
		userDto.setEmail("cuenya.diego@leafnode.io");

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

		//Valid email
		userDto.setEmail("cuenya.diego@leafnode.email-valid.io");

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

		//Invalid email
		userDto.setEmail("cuenya.diego!@leafnode.io");

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.email.invalid", equalTo(bindingResult.getFieldError("email").getCode()));
	}

	/**
	 * Should validate the UserId.* *
	 */
	@Test
	public void testValidateUserId() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailWithAdminRoleDto(777, new CropType("wheat"));
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(UserTestDataGenerator.initializeUserRoleAdmin()));

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.userId", equalTo(bindingResult.getFieldError("userId").getCode()));
	}

	/**
	 * Should validate the userId inexistent.* *
	 */
	@Test
	public void testValidateUserIdInexistent() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailWithAdminRoleDto(10, new CropType("wheat"));
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(UserTestDataGenerator.initializeUserRoleAdmin()));

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.userId", equalTo(bindingResult.getFieldError("userId").getCode()));
	}

	/**
	 * Should validate the status allow.* *
	 */
	@Test
	public void testValidateStatus() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto =
			UserTestDataGenerator.initializeUserDetailDto(20, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20, UserTestDataGenerator.initializeUserRoleAdmin());
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds)))
			.thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));

		userDto.setStatus("truee");
		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.status", equalTo(bindingResult.getFieldError("status").getCode()));
	}

	/**
	 * Should validate if username and email exists.* *
	 */
	@Test
	public void testValidateUserAndPeronalEmailExists() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto =
			UserTestDataGenerator.initializeUserDetailDto(20, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20, UserTestDataGenerator.initializeUserRoleAdmin());
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds)))
			.thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(true);
		this.uservalidator.validate(userDto, bindingResult, true);

		assertThat(2, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.username.exists", equalTo(bindingResult.getFieldError("username").getCode()));
		assertThat("signup.field.email.exists", equalTo(bindingResult.getFieldError("email").getCode()));
	}

	/**
	 * Should validate update user.* *
	 */
	@Test
	public void testValidateUpdateUser() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20, UserTestDataGenerator.initializeUserRoleAdmin());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));


		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	/**
	 * Should validate update user with diferent Email* *
	 */
	@Test
	public void testValidateUpdateUserEmailExists() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleAdmin());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));


		user.getPerson().setEmail("user@leafnode.io");
		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(true);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.email.exists", equalTo(bindingResult.getFieldError("email").getCode()));

	}

	/**
	 * Should validate update user with diferent username* *
	 */
	@Test
	public void testValidateUpdateUserUsernameExists() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleAdmin());
		user.setName("Nahuel");
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));


		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.username.exists", equalTo(bindingResult.getFieldError("username").getCode()));

	}

	/**
	 * Should validate update user.* *
	 */
	@Test
	public void testValidateUpdateUserForExistingSuperAdminUser() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(1, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoSuperAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20,UserTestDataGenerator.initializeUserRoleSuperAdmin());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));


		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);
		assertThat(2, equalTo(bindingResult.getAllErrors().size()));
	}

	/**
	 * Should validate create user.* *
	 */
	@Test
	public void testValidateCreateUser() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(0, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(0,UserTestDataGenerator.initializeUserRoleAdmin());
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));
		this.uservalidator.validate(userDto, bindingResult, true);
		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	@Test
	public void testValidateUserCannotAutoDeactivate() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20, UserTestDataGenerator.initializeUserRoleAdmin());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));

		userDto.setStatus("false");
		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, not(equalTo(bindingResult.getGlobalErrorCount())));
		assertThat(UserValidator.USER_AUTO_DEACTIVATION, equalTo(bindingResult.getGlobalErrors().get(0).getCode()));
	}

}
