
package org.ibp.api.java.impl.middleware.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.ibp.api.brapi.v1.user.UserDetailDto;
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

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.uservalidator = new UserValidator();
		this.uservalidator.setWorkbenchDataManager(this.workbenchDataManager);
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
		final UserDetailDto userdto = new UserDetailDto();

		userdto.setLastName("zxcvbnmaaskjhdfsgeeqwfsafsafg6tk6kglkugt8oljhhlly11");
		userdto.setUsername("wertyuioiuytredsdfrtghjuklsl123");
		this.uservalidator.validate(userdto, bindingResult);

		assertThat(7, equalTo(bindingResult.getAllErrors().size()));
		assertThat(null, equalTo(bindingResult.getFieldError("firstName").getRejectedValue()));
		assertThat("signup.field.length.exceed", equalTo(bindingResult.getFieldError("lastName").getCode()));
		assertThat("30", equalTo(bindingResult.getFieldError("username").getArguments()[0]));
		assertThat("Username", equalTo(bindingResult.getFieldError("username").getArguments()[1]));
		assertThat(null, equalTo(bindingResult.getFieldError("email").getRejectedValue()));
		assertThat(null, equalTo(bindingResult.getFieldError("role").getRejectedValue()));
		assertThat("signup.field.required", equalTo(bindingResult.getFieldError("userId").getCode()));
		assertThat("signup.field.required", equalTo(bindingResult.getFieldError("status").getCode()));
	}

	/**
	 * Should validate the Role allow.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateRole() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userdto = inicializeUserDetailDto(10);
		final User user = inicializeUser(10);

		userdto.setRole("Breeeder");
		Mockito.when(this.workbenchDataManager.getUserById(userdto.getId())).thenReturn(user);

		this.uservalidator.validate(userdto, bindingResult);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.invalid.role", equalTo(bindingResult.getFieldError("role").getCode()));
	}

	/**
	 * Should validate the Email Format.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateEmail() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userdto = inicializeUserDetailDto(10);
		final User user = inicializeUser(10);

		Mockito.when(this.workbenchDataManager.getUserById(userdto.getId())).thenReturn(user);
		userdto.setEmail("cuenya.diego!@leafnode.io");

		this.uservalidator.validate(userdto, bindingResult);

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
		final UserDetailDto userdto = inicializeUserDetailDto(null);

		this.uservalidator.validate(userdto, bindingResult);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.required", equalTo(bindingResult.getFieldError("userId").getCode()));
	}

	/**
	 * Should validate the userId inexistent.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateUserIdInexistent() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userdto = inicializeUserDetailDto(10);
		final User user = inicializeUser(5);
		this.uservalidator.validate(userdto, bindingResult);
		Mockito.when(this.workbenchDataManager.getUserById(userdto.getId())).thenReturn(user);
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
		final UserDetailDto userdto = inicializeUserDetailDto(20);
		final User user = inicializeUser(20);
		userdto.setStatus("truee");
		Mockito.when(this.workbenchDataManager.getUserById(userdto.getId())).thenReturn(user);
		this.uservalidator.validate(userdto, bindingResult);

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
		final UserDetailDto userdto = inicializeUserDetailDto(0);

		Mockito.when(this.workbenchDataManager.isUsernameExists(userdto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userdto.getEmail())).thenReturn(true);
		this.uservalidator.validate(userdto, bindingResult);

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
		final UserDetailDto userdto = inicializeUserDetailDto(10);
		final User user = inicializeUser(20);

		Mockito.when(this.workbenchDataManager.getUserById(userdto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userdto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userdto.getEmail())).thenReturn(false);
		this.uservalidator.validate(userdto, bindingResult);

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
		final UserDetailDto userdto = inicializeUserDetailDto(10);
		final User user = inicializeUser(10);
		user.getPerson().setEmail("user@leafnode.io");
		Mockito.when(this.workbenchDataManager.getUserById(userdto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userdto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userdto.getEmail())).thenReturn(true);
		this.uservalidator.validate(userdto, bindingResult);

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
		final UserDetailDto userdto = inicializeUserDetailDto(10);
		final User user = inicializeUser(10);
		user.setName("Nahuel");
		Mockito.when(this.workbenchDataManager.getUserById(userdto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userdto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userdto.getEmail())).thenReturn(false);
		this.uservalidator.validate(userdto, bindingResult);

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
		final UserDetailDto userdto = inicializeUserDetailDto(10);
		final User user = inicializeUser(20);

		Mockito.when(this.workbenchDataManager.getUserById(userdto.getId())).thenReturn(user);
		Mockito.when(this.workbenchDataManager.isUsernameExists(userdto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(userdto.getEmail())).thenReturn(false);
		this.uservalidator.validate(userdto, bindingResult);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	/**
	 * Should validate create user.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateCreateUser() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailDto userdto = inicializeUserDetailDto(0);

		this.uservalidator.validate(userdto, bindingResult);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	/**
	 * Inicialize UserDetailDto
	 * 
	 * @return UserDetailDto
	 */
	public UserDetailDto inicializeUserDetailDto(final Integer userId) {
		UserDetailDto user = new UserDetailDto();
		user.setFirstName("Diego");
		user.setLastName("Cuenya");
		user.setStatus("true");
		user.setRole("Breeder");
		user.setId(userId);
		user.setUsername("Cuenyad");
		user.setEmail("diego.cuenya@leafnode.io");
		return user;
	}

	public User inicializeUser(final Integer userId) {
		User user = new User();
		Person person = new Person();
		person.setId(2);
		person.setFirstName("Diego");
		person.setMiddleName("");
		person.setLastName("Cuenya");
		person.setEmail("diego.cuenya@leafnode.io");
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

		user.setPersonid(person.getId());
		user.setPerson(person);
		user.setName("Cuenyad");
		user.setAccess(0);
		user.setInstalid(0);
		user.setType(0);
		return user;
	}
}
