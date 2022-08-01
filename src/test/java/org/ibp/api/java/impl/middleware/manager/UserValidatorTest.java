package org.ibp.api.java.impl.middleware.manager;

import liquibase.util.StringUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.workbench.CropDto;
import org.generationcp.middleware.api.role.RoleService;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.hamcrest.MatcherAssert;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UserValidatorTest {

	public static final String USER_NAME_VALID = "userName";
	public static final String FIRST_NAME_VALID = "firstName";
	public static final String LAST_NAME_VALID = "lastName";
	public static final String EMAIL_VALID = "user@test.com";
	public static final String STATUS_VALID = "true";

	@InjectMocks
	private UserValidator uservalidator;

	@Mock
	protected RoleService roleService;

	@Mock
	protected UserService userService;

	@Mock
	private SecurityService securityService;

	@Mock
	private CropService cropService;

	@After
	public void validate() {
		MockitoAnnotations.openMocks(this);
		Mockito.validateMockitoUsage();
	}

	@Test
	public void test_validateUsername_OK() {
		final UserDetailDto user1 = this.createDummyUserDetailDto("user-name");
		this.uservalidator.validate(user1, true);

		final UserDetailDto user2 = this.createDummyUserDetailDto("user.name");
		this.uservalidator.validate(user2, true);

		final UserDetailDto user3 = this.createDummyUserDetailDto("user_name.company");
		this.uservalidator.validate(user3, true);
	}

	@Test
	public void test_validateUserName_FAIL() {
		final UserDetailDto user1 = this.createDummyUserDetailDto("[]%&");
		this.assertValidateException(user1, true, "user.invalid.username", null);

		final UserDetailDto user2 = this.createDummyUserDetailDto("user_name.company.");
		this.assertValidateException(user2, true, "user.invalid.username", null);
	}

	@Test
	public void test_validateFieldLength_firstNameBlank_FAIL() {
		final String firstName = "            ";
		final UserDetailDto userDto = this.createDummyUserDetailDto(USER_NAME_VALID, firstName,
			LAST_NAME_VALID, EMAIL_VALID, STATUS_VALID);

		this.assertValidateException(userDto, true, UserValidator.SIGNUP_FIELD_REQUIRED,
			Arrays.asList(UserValidator.FIRST_NAME));
	}

	@Test
	public void test_validateFieldLength_lastNameBlank_FAIL() {
		final String lastName = "            ";
		final UserDetailDto userDto = this.createDummyUserDetailDto(USER_NAME_VALID, FIRST_NAME_VALID,
			lastName, EMAIL_VALID, STATUS_VALID);

		this.assertValidateException(userDto, true, UserValidator.SIGNUP_FIELD_REQUIRED,
			Arrays.asList(UserValidator.LAST_NAME));
	}

	@Test
	public void test_validateFieldLength_firstNameExceedLength_FAIL() {
		final String firstName = StringUtils.repeat("0", UserValidator.FIRST_NAME_MAX_LENGTH + 1);
		final UserDetailDto userDto = this.createDummyUserDetailDto(USER_NAME_VALID, firstName,
			LAST_NAME_VALID, EMAIL_VALID, STATUS_VALID);

		this.assertValidateException(userDto, true, UserValidator.SIGNUP_FIELD_LENGTH_EXCEED,
			Arrays.asList(UserValidator.FIRST_NAME, String.valueOf(UserValidator.FIRST_NAME_MAX_LENGTH)));
	}

	@Test
	public void test_validateFieldLength_lastNameExceedLength_FAIL() {
		final String lastName = StringUtils.repeat("0", UserValidator.LAST_NAME_MAX_LENGTH + 1);
		final UserDetailDto userDto = this.createDummyUserDetailDto(USER_NAME_VALID, FIRST_NAME_VALID,
			lastName, EMAIL_VALID, STATUS_VALID);

		this.assertValidateException(userDto, true, UserValidator.SIGNUP_FIELD_LENGTH_EXCEED,
			Arrays.asList(UserValidator.LAST_NAME, String.valueOf(UserValidator.LAST_NAME_MAX_LENGTH)));
	}

	@Test
	public void test_validateFieldLength_userNameExceedLength_FAIL() {
		final String username = StringUtils.repeat("0", UserValidator.USERNAME_MAX_LENGTH + 1);
		final UserDetailDto userDto = this.createDummyUserDetailDto(username, FIRST_NAME_VALID,
			LAST_NAME_VALID, EMAIL_VALID, STATUS_VALID);

		this.assertValidateException(userDto, true, UserValidator.SIGNUP_FIELD_LENGTH_EXCEED,
			Arrays.asList(UserValidator.USERNAME, String.valueOf(UserValidator.USERNAME_MAX_LENGTH)));
	}

	@Test
	public void test_validateFieldLength_emailExceedLength_FAIL() {
		final String email = RandomStringUtils.randomAlphabetic(247) + "@test.com";
		final UserDetailDto userDto = this.createDummyUserDetailDto(USER_NAME_VALID, FIRST_NAME_VALID,
			LAST_NAME_VALID, email, STATUS_VALID);

		this.assertValidateException(userDto, true, UserValidator.SIGNUP_FIELD_LENGTH_EXCEED,
			Arrays.asList(UserValidator.EMAIL, String.valueOf(UserValidator.EMAIL_MAX_LENGTH)));
	}


	/**
	 * Should validate the Email Format.* *
	 */
	@Test
	public void testValidateEmail_FAIL() {
		UserDetailDto userDto = this.createDummyUserDetailDto(USER_NAME_VALID, FIRST_NAME_VALID,
			LAST_NAME_VALID, "cuenya.diego!@leafnode.io", STATUS_VALID);

		this.assertValidateException(userDto, true, "signup.field.email.invalid", null);
	}
	/**
	 * Should validate the UserId.* *
	 */
	@Test
	public void test_validateUserId_notPresentForUpdate_FAIL() {
		final UserDetailDto userDetailDto = this.createDummyUserDetailDto();
		this.assertValidateException(userDetailDto, false, "signup.field.invalid.userId", null);

		Mockito.verifyZeroInteractions(this.userService);
	}

	/**
	 * Should validate the userId not exists.* *
	 */
	@Test
	public void test_validateUserId_notExistsForUpdate_FAIL() {
		Integer userId = new Random().nextInt(Integer.MAX_VALUE);

		final UserDetailDto userDetailDto = this.createDummyUserDetailDto();
		userDetailDto.setId(userId);

		Mockito.when(this.userService.getUserById(userId)).thenReturn(null);

		this.assertValidateException(userDetailDto, false, "signup.field.invalid.userId", null);

		Mockito.verify(this.userService).getUserById(userId);
	}

	/**
	 * Should validate the status allow.* *
	 */
	@Test
	public void testValidateStatus() {
		UserDetailDto userDto = this.createDummyUserDetailDto(USER_NAME_VALID, FIRST_NAME_VALID,
			LAST_NAME_VALID, EMAIL_VALID, "truee");

		this.assertValidateException(userDto, true, "signup.field.invalid.status", null);
	}

	/**
	 * Should validate if username exists.* *
	 */
	@Test
	public void test_validate_create_usernameExists_FAIL() {
		final UserDetailDto userDto = this.createDummyUserDetailDto();
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);

		this.assertValidateException(userDto, true, "signup.field.username.exists", null);

		Mockito.verify(this.userService).isUsernameExists(userDto.getUsername());
		Mockito.verify(this.userService).isPersonWithEmailExists(userDto.getEmail());
		Mockito.verifyNoMoreInteractions(this.userService);
	}

	/**
	 * Should validate if email exists.* *
	 */
	@Test
	public void test_validate_create_emailsExists_FAIL() {
		final UserDetailDto userDto = this.createDummyUserDetailDto();
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(true);

		this.assertValidateException(userDto, true, "signup.field.email.exists", null);

		Mockito.verify(this.userService).isUsernameExists(userDto.getUsername());
		Mockito.verify(this.userService).isPersonWithEmailExists(userDto.getEmail());
		Mockito.verifyNoMoreInteractions(this.userService);
	}

	/**
	 * Should validate update user.* *
	 */
	@Test
	public void test_validate_updateUser_OK() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20, UserTestDataGenerator.initializeUserRoleAdmin());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.roleService.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));

		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, false);

		Mockito.verify(this.userService).getUserById(userDto.getId());
		Mockito.verify(this.userService).isUsernameExists(userDto.getUsername());
		Mockito.verify(this.userService).isPersonWithEmailExists(userDto.getEmail());
		Mockito.verify(this.userService).getUsersByPersonIds(ArgumentMatchers.anyList());
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();
		Mockito.verifyNoMoreInteractions(this.userService);
		Mockito.verifyNoMoreInteractions(this.securityService);
	}

	/**
	 * Should validate update user with diferent Email* *
	 */
	@Test
	public void test_validate_updateUserEmailExists_FAIL() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleAdmin());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.roleService.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));

		user.getPerson().setEmail("user@leafnode.io");
		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(true);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.assertValidateException(userDto, false, "signup.field.email.exists", null);

		Mockito.verify(this.userService).getUserById(userDto.getId());
		Mockito.verify(this.userService).isUsernameExists(userDto.getUsername());
		Mockito.verify(this.userService).isPersonWithEmailExists(userDto.getEmail());
		Mockito.verify(this.userService).getUsersByPersonIds(ArgumentMatchers.anyList());
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();
		Mockito.verifyNoMoreInteractions(this.userService);
		Mockito.verifyNoMoreInteractions(this.securityService);
	}

	/**
	 * Should validate update user with diferent username* *
	 */
	@Test
	public void test_validate_UpdateUserUsernameExists_FAIL() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleAdmin());
		user.setName("Nahuel");
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.roleService.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));

		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.userService.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.userService.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.assertValidateException(userDto, false, "signup.field.username.exists", null);

		Mockito.verify(this.userService).getUserById(userDto.getId());
		Mockito.verify(this.userService).isUsernameExists(userDto.getUsername());
		Mockito.verify(this.userService).isPersonWithEmailExists(userDto.getEmail());
		Mockito.verify(this.userService).getUsersByPersonIds(ArgumentMatchers.anyList());
		Mockito.verify(this.securityService).getCurrentlyLoggedInUser();
		Mockito.verifyNoMoreInteractions(this.userService);
		Mockito.verifyNoMoreInteractions(this.securityService);
	}

	/**
	 * Should validate update user.* *
	 */
	@Test
	public void testValidateUpdateUserForExistingSuperAdminUser() {
		Integer userId = new Random().nextInt(Integer.MAX_VALUE);

		final UserDetailDto userDetailDto = this.createDummyUserDetailDto();
		userDetailDto.setId(userId);

		final WorkbenchUser workbenchUser = Mockito.mock(WorkbenchUser.class);
		Mockito.when(workbenchUser.isSuperAdmin()).thenReturn(true);
		Mockito.when(this.userService.getUserById(userId)).thenReturn(workbenchUser);

		this.assertValidateException(userDetailDto, false, "users.update.superadmin.not.allowed", null);

		Mockito.verify(this.userService).getUserById(userId);
	}

	/**
	 * Should validate create user.* *
	 */
	@Test
	public void test_validate_createUser_OK() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(0, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(0,UserTestDataGenerator.initializeUserRoleAdmin());
		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.roleService.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));
		this.uservalidator.validate(userDto, true);
	}

	@Test
	public void testValidateUserCannotAutoDeactivate() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20, UserTestDataGenerator.initializeUserRoleAdmin());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.roleService.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));

		userDto.setStatus("false");
		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.assertValidateException(userDto, false, "users.user.can.not.be.deactivated", null);
	}

	@Test
	public void testValidateSuperAdminUserCannotAssignable() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoSuperAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleSuperAdmin());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.roleService.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));

		this.assertValidateException(userDto, true, "user.not.assignable.roles", null);
	}

	@Test
	public void testValidateUserInvalidRole() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"), UserTestDataGenerator.initializeUserRoleDtoAdmin());
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleAdmin());
		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.assertValidateException(userDto, false, "user.invalid.roles", null);
	}

	@Test
	public void testValidateUserCropNotAssociated() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("wheat"),
			UserTestDataGenerator.initializeUserRoleDtoBreeder(new CropDto(new CropType("maize"))));
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleBreeder());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.roleService.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));

		Mockito.when(this.cropService.getInstalledCrops()).thenReturn(Arrays.asList("maize", "wheat"));

		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.assertValidateException(userDto, false, "user.crop.not.associated.to.user", null);
	}

	@Test
	public void testValidateUserCropNotExists() {
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10, new CropType("bean"),
			UserTestDataGenerator.initializeUserRoleDtoBreeder(new CropDto(new CropType("bean"))));
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10, UserTestDataGenerator.initializeUserRoleBreeder());

		final Set<Integer> roleIds = userDto.getUserRoles().stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());
		Mockito.when(this.roleService.getRoles(new RoleSearchDto(null, null, roleIds))).thenReturn(Arrays.asList(user.getRoles().get(0).getRole()));


		Mockito.when(this.userService.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.assertValidateException(userDto, false, "user.crop.not.exist", null);
	}

	@Test
	public void testValidateUserId_NullUserId() {
		final BindingResult result = Mockito.mock(BindingResult.class);
		this.uservalidator.validateUserId(result, null);
		Mockito.verify(result).reject("signup.field.missing.userId");
	}

	@Test
	public void testValidateUserId_NonDigitsUserId() {
		final BindingResult result = Mockito.mock(BindingResult.class);
		this.uservalidator.validateUserId(result, RandomStringUtils.randomAlphabetic(3));
		Mockito.verify(result).reject("signup.field.invalid.userId");
	}

	@Test
	public void testValidateUserId_ValidUserId() {
		final BindingResult result = Mockito.mock(BindingResult.class);
		final Integer userId = new Random().nextInt(100);
		Mockito.doReturn(UserTestDataGenerator.initializeWorkbenchUser(userId, UserTestDataGenerator.initializeUserRoleAdmin())).when(this.userService).getUserById(userId);
		this.uservalidator.validateUserId(result, userId.toString());
		Mockito.verifyZeroInteractions(result);
	}

	@Test
	public void testValidateUserId_NonExistentUserId() {
		final BindingResult result = Mockito.mock(BindingResult.class);
		final Integer userId = new Random().nextInt(100);
		Mockito.doReturn(null).when(this.userService).getUserById(userId);
		this.uservalidator.validateUserId(result, userId.toString());
		Mockito.verify(result).reject("signup.field.invalid.userId");
	}

	private void assertValidateException(final UserDetailDto userDto, final boolean createUser, final String message, final List<String> errorArgs) {
		try {
			this.uservalidator.validate(userDto, createUser);
			Assert.fail("Should has failed");
		} catch (Exception e) {
			MatcherAssert.assertThat(e, instanceOf(ApiRequestValidationException.class));
			final ApiRequestValidationException exception = (ApiRequestValidationException) e;
			assertThat(exception.getErrors(), hasSize(1));

			MatcherAssert.assertThat(Arrays.asList((exception).getErrors().get(0).getCodes()), hasItem(message));

			if (!CollectionUtils.isEmpty(errorArgs)) {
				final List<Object> arguments = Arrays.asList((exception).getErrors().get(0).getArguments());
				assertFalse(CollectionUtils.isEmpty(arguments));
				assertThat(errorArgs, hasSize(arguments.size()));
				IntStream.of(0, arguments.size() - 1).forEach(i -> assertThat(arguments.get(i), is(errorArgs.get(i))));
			}
		}
	}

	private UserDetailDto createDummyUserDetailDto() {
		return this.createDummyUserDetailDto(USER_NAME_VALID, FIRST_NAME_VALID, LAST_NAME_VALID, EMAIL_VALID, STATUS_VALID);
	}

	private UserDetailDto createDummyUserDetailDto(final String userName) {
		return this.createDummyUserDetailDto(userName, FIRST_NAME_VALID, LAST_NAME_VALID, EMAIL_VALID, STATUS_VALID);
	}

	private UserDetailDto createDummyUserDetailDto(final String userName, final String firstName, final String lastName, final String email,
		final String status) {
		final UserDetailDto userDto = new UserDetailDto();
		userDto.setUsername(userName);
		userDto.setFirstName(firstName);
		userDto.setLastName(lastName);
		userDto.setEmail(email);
		userDto.setStatus(status);

		userDto.setUserRoles(new ArrayList<>());

		return userDto;
	}
}
