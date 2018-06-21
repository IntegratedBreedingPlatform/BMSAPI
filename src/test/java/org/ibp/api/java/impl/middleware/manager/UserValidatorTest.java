package org.ibp.api.java.impl.middleware.manager;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@RunWith(MockitoJUnitRunner.class)
public class UserValidatorTest {

	private UserValidator uservalidator;

	@Mock 
	protected WorkbenchDataManager workbenchDataManager;

	@Mock
	private SecurityService securityService;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.uservalidator = new UserValidator();
		this.uservalidator.setWorkbenchDataManager(this.workbenchDataManager);
		this.uservalidator.setSecurityService(this.securityService);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * Should validate all fields empty and with length exceed.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateFieldLength() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = new UserDetailDto();

		userDto.setLastName("zxcvbnmaaskjhdfsgeeqwfsafsafg6tk6kglkugt8oljhhlly11");
		userDto.setUsername("wertyuioiuytredsdfrtghjuklsl123");

		this.uservalidator.validate(userDto, bindingResult, true);

		assertThat(6, equalTo(bindingResult.getAllErrors().size()));
		assertThat(null, equalTo(bindingResult.getFieldError("firstName").getRejectedValue()));
		assertThat("signup.field.length.exceed", equalTo(bindingResult.getFieldError("lastName").getCode()));
		assertThat("30", equalTo(bindingResult.getFieldError("username").getArguments()[0]));
		assertThat("Username", equalTo(bindingResult.getFieldError("username").getArguments()[1]));
		assertThat(null, equalTo(bindingResult.getFieldError("email").getRejectedValue()));
		assertThat(null, equalTo(bindingResult.getFieldError("role").getRejectedValue()));
		assertThat("signup.field.required", equalTo(bindingResult.getFieldError("status").getCode()));
	}

	/**
	 * Should validate the Role allow.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateRoleWhenNull() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);

		userDto.setRole(null);
		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat(UserValidator.SIGNUP_FIELD_INVALID_ROLE, equalTo(bindingResult.getFieldError("role").getCode()));
	}
	
	@Test
	public void testValidateRole() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);

		userDto.getRole().setDescription("Breeder qwertyuioiuytredsdfrtghjuklsl123");
		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.length.exceed", equalTo(bindingResult.getFieldError("role").getCode()));
	}

	/**
	 * Should validate the Email Format.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateEmail() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		userDto.setEmail("cuenya.diego!@leafnode.io");

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.email.invalid", equalTo(bindingResult.getFieldError("email").getCode()));
	}

	/**
	 * Should validate the UserId.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateUserId() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(777);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.userId", equalTo(bindingResult.getFieldError("userId").getCode()));
	}

	/**
	 * Should validate the userId inexistent.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateUserIdInexistent() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(5);
		this.uservalidator.validate(userDto, bindingResult, false);

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.userId", equalTo(bindingResult.getFieldError("userId").getCode()));
	}

	/**
	 * Should validate the status allow.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateStatus() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(20);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20);
		
		userDto.setStatus("truee");
		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.status", equalTo(bindingResult.getFieldError("status").getCode()));
	}

	/**
	 * Should validate if username and email exists.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateUserAndPeronalEmailExists() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(0);

		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(true);
		this.uservalidator.validate(userDto, bindingResult, true);

		assertThat(2, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.username.exists", equalTo(bindingResult.getFieldError("username").getCode()));
		assertThat("signup.field.email.exists", equalTo(bindingResult.getFieldError("email").getCode()));
	}

	/**
	 * Should validate update user.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateUpdateUser() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20);

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	/**
	 * Should validate update user with diferent Email* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateUpdateUserEmailExists() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);

		user.getPerson().setEmail("user@leafnode.io");
		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(true);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.email.exists", equalTo(bindingResult.getFieldError("email").getCode()));

	}

	/**
	 * Should validate update user with diferent username* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateUpdateUserUsernameExists() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(10);
		user.setName("Nahuel");

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.username.exists", equalTo(bindingResult.getFieldError("username").getCode()));

	}

	/**
	 * Should validate update user.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateInvalidUserUpdate() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20);

		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userDto.getEmail())).thenReturn(false);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);
		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
	}

	/**
	 * Should validate create user.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateCreateUser() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(0);

		this.uservalidator.validate(userDto, bindingResult, true);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	@Test
	public void testValidateUserCannotAutoDeactivate() {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(20);

		userDto.setStatus("false");
		Mockito.when(this.workbenchDataManager.getUserById(userDto.getId())).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		this.uservalidator.validate(userDto, bindingResult, false);

		assertThat(0, not(equalTo(bindingResult.getGlobalErrorCount())));
		assertThat(UserValidator.USER_AUTO_DEACTIVATION, equalTo(bindingResult.getGlobalErrors().get(0).getCode()));
	}
}
