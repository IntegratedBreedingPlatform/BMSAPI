
package org.ibp.api.brapi.v1.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

	private UserServiceImpl userServiceImpl;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	protected UserValidator userValidator;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.userServiceImpl = new UserServiceImpl();
		this.userServiceImpl.setWorkbenchDataManager(this.workbenchDataManager);
		this.userValidator.setWorkbenchDataManager(this.workbenchDataManager);
		this.userServiceImpl.setUserValidator(this.userValidator);
	}

	@Test
	public void testGetAllUsers() throws Exception {
		final List<UserDto> usersDto = getAllListUser();

		Mockito.when(this.workbenchDataManager.getAllUserDtosSorted()).thenReturn(usersDto);
		List<UserDetailDto> usersDtlsDto = this.userServiceImpl.getAllUserDtosSorted();
		assertThat(usersDto.get(0).getFirstName(), equalTo(usersDtlsDto.get(0).getFirstName()));
		assertThat(usersDto.get(0).getLastName(), equalTo(usersDtlsDto.get(0).getLastName()));
		assertThat(usersDto.get(0).getUsername(), equalTo(usersDtlsDto.get(0).getUsername()));
		assertThat(usersDto.get(0).getUserId(), equalTo(usersDtlsDto.get(0).getUserId()));
		assertThat(usersDto.get(0).getEmail(), equalTo(usersDtlsDto.get(0).getEmail()));
		assertThat("true", equalTo(usersDtlsDto.get(0).getStatus()));
		assertThat(usersDto.get(0).getRole(), equalTo(usersDtlsDto.get(0).getRole()));
	}

	@Test
	public void testCreateUser() throws Exception {
		final UserDetailsDto usrDtlsDto = inicializeUserDetailDto(0);
		final UserDto userDto = inicializeUserDto(0);
		Mockito.when(this.workbenchDataManager.createUser(userDto)).thenReturn(new Integer(7));
		GenericResponse response = this.userServiceImpl.createUser(usrDtlsDto);
		assertThat(response.getId(), equalTo("7"));
	}

	@Test
	public void testCreateUserValidateError() throws Exception {
		final UserDetailsDto usrDtlsDto = inicializeUserDetailDto(0);
		
		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlsDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlsDto.getEmail())).thenReturn(true);
		
		GenericResponse response = this.userServiceImpl.createUser(usrDtlsDto);
		assertThat(response.getId(), equalTo("0"));
	}

	@Test
	public void testUpdateUser() throws Exception {
		final UserDetailsDto usrDtlsDto = inicializeUserDetailDto(12);
		final UserDto userDto = inicializeUserDto(12);
		Mockito.when(this.workbenchDataManager.updateUser(userDto)).thenReturn(new Integer(8));
		GenericResponse response = this.userServiceImpl.updateUser(usrDtlsDto);
		assertThat(response.getId(), equalTo("8"));
	}

	@Test
	public void testUpdateUserValidateError() throws Exception {
		UserDetailsDto usrDtlsDto = inicializeUserDetailDto(10);

		GenericResponse response = this.userServiceImpl.updateUser(usrDtlsDto);
		assertThat(response.getId(), equalTo("0"));
	}

	/**
	 * Inicialize List all user
	 * 
	 * @return List<UserDto>
	 */
	public List<UserDto> getAllListUser() {
		UserDto user = inicializeUserDto(10);
		final List<UserDto> users = Lists.newArrayList(user);
		return users;
	}

	public UserDto inicializeUserDto(final Integer userId) {
		UserDto user = new UserDto();
		user.setFirstName("Diego");
		user.setLastName("Cuenya");
		user.setStatus(0);
		user.setRole("Breeder");
		user.setUserId(userId);
		user.setUsername("Cuenyad");
		user.setEmail("diego.cuenya@leafnode.io");
		return user;
	}

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
