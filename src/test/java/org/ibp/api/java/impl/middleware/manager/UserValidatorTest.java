
package org.ibp.api.java.impl.middleware.manager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.ibp.api.brapi.v1.user.UserDetailsDto;
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
		final UserDetailsDto userdto = new UserDetailsDto();

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
		final UserDetailsDto userdto = inicializeUserDetailDto(10);
		userdto.setRole("Breeeder");
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
		final UserDetailsDto userdto = inicializeUserDetailDto(10);
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
		final UserDetailsDto userdto = inicializeUserDetailDto(null);

		this.uservalidator.validate(userdto, bindingResult);

		assertThat(1, equalTo(bindingResult.getAllErrors().size()));
		assertThat("signup.field.required", equalTo(bindingResult.getFieldError("userId").getCode()));
	}

	/**
	 * Should validate the status allow.* *
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidateStatus() throws Exception {
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "User");
		final UserDetailsDto userdto = inicializeUserDetailDto(20);
		userdto.setStatus("truee");
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
		final UserDetailsDto userdto = inicializeUserDetailDto(0);

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
		final UserDetailsDto userdto = inicializeUserDetailDto(10);

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
		final UserDetailsDto userdto = inicializeUserDetailDto(0);

		this.uservalidator.validate(userdto, bindingResult);

		assertThat(0, equalTo(bindingResult.getAllErrors().size()));

	}

	/**
	 * Inicialize UserDetailsDto
	 * 
	 * @return UserDetailsDto
	 */
	public UserDetailsDto inicializeUserDetailDto(final Integer userId) {
		UserDetailsDto user = new UserDetailsDto();
		user.setFirstName("Diego");
		user.setLastName("Cuenya");
		user.setStatus("true");
		user.setRole("Breeder");
		user.setUserId(userId);
		user.setUsername("Cuenyad");
		user.setEmail("diego.cuenya@leafnode.io");
		user.setSendEmail(Boolean.FALSE);
		return user;
	}
}
