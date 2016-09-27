
package org.ibp.api.brapi.v1.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
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

	/**
	 * Should return the list of users.
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAllUsers() throws Exception {
		final List<UserDto> usersDto = getAllListUser();

		Mockito.when(this.workbenchDataManager.getAllUserDtosSorted()).thenReturn(usersDto);
		List<UserDetailDto> usersDtlsDto = this.userServiceImpl.getAllUserDtosSorted();
		assertThat(usersDto.get(0).getFirstName(), equalTo(usersDtlsDto.get(0).getFirstName()));
		assertThat(usersDto.get(0).getLastName(), equalTo(usersDtlsDto.get(0).getLastName()));
		assertThat(usersDto.get(0).getUsername(), equalTo(usersDtlsDto.get(0).getUsername()));
		assertThat(usersDto.get(0).getUserId(), equalTo(usersDtlsDto.get(0).getId()));
		assertThat(usersDto.get(0).getEmail(), equalTo(usersDtlsDto.get(0).getEmail()));
		assertThat("true", equalTo(usersDtlsDto.get(0).getStatus()));
		assertThat(usersDto.get(0).getRole(), equalTo(usersDtlsDto.get(0).getRole()));
	}

	/**
	 * Should create the new user, 
	 * and return the new userId.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUser() throws Exception {
		final UserDetailDto usrDtlsDto = initializeUserDetailDto(0);
		final UserDto userDto = initializeUserDto(0);
		Mockito.when(this.workbenchDataManager.createUser(userDto)).thenReturn(new Integer(7));
		GenericResponse response = this.userServiceImpl.createUser(usrDtlsDto);
		assertThat(response.getId(), equalTo("7"));
	}

	/**
	 * Should return the id 0, 
	 * because happened a error during the creation user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUserValidateError() throws Exception {
		final UserDetailDto usrDtlsDto = initializeUserDetailDto(0);
		
		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlsDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlsDto.getEmail())).thenReturn(true);
		
		GenericResponse response = this.userServiceImpl.createUser(usrDtlsDto);
		assertThat(response.getId(), equalTo("0"));
	}

	/**
	 * Should update the user, 
	 * and return the same userId.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUser() throws Exception {
		final UserDetailDto usrDtlsDto = initializeUserDetailDto(8);
		final UserDto userDto = initializeUserDto(8);
		final User user = initializeUser(8);
		Mockito.when(this.workbenchDataManager.updateUser(userDto)).thenReturn(new Integer(8));
		Mockito.when(this.workbenchDataManager.getUserById(8)).thenReturn(user);
		GenericResponse response = this.userServiceImpl.updateUser(usrDtlsDto);
		assertThat(response.getId(), equalTo("8"));
	}

	/**
	 * Should return the id 0, 
	 * because happened a error during the update user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserValidateError() throws Exception {
		UserDetailDto usrDtlsDto = initializeUserDetailDto(10);

		GenericResponse response = this.userServiceImpl.updateUser(usrDtlsDto);
		assertThat(response.getId(), equalTo("0"));
	}

	/**
	 * initialize List all user
	 * 
	 * @return List<UserDto>
	 */
	public List<UserDto> getAllListUser() {
		UserDto user = initializeUserDto(10);
		final List<UserDto> users = Lists.newArrayList(user);
		return users;
	}

	/**
	 * initialize UserDto
	 * 
	 * @return user UserDto
	 */
	public UserDto initializeUserDto(final Integer userId) {
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


	/**
	 * initialize UserDetailDto
	 * 
	 * @param userId Integer
	 * @return userDetailDto UserDetailDto
	 */
	public UserDetailDto initializeUserDetailDto(final Integer userId) {
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
	
	/**
	 * initialize User
	 * 
	 * @param userId Integer
	 * @return user User
	 */
	public User initializeUser(final Integer userId) {
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
