
package org.ibp.api.brapi.v1.user;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.common.ErrorResponse;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

	private UserServiceImpl userServiceImpl;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	protected UserValidator userValidator;

	private static final String projectUUID = "d8d59d89-f4ca-4b83-90e2-be2d82407146";

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

		Mockito.when(this.workbenchDataManager.getAllUsersSortedByLastName()).thenReturn(usersDto);
		final List<UserDetailDto> usersDtlsDto = this.userServiceImpl.getAllUsersSortedByLastName();
		
		assertThat(usersDto.get(0).getFirstName(), equalTo(usersDtlsDto.get(0).getFirstName()));
		assertThat(usersDto.get(0).getLastName(), equalTo(usersDtlsDto.get(0).getLastName()));
		assertThat(usersDto.get(0).getUsername(), equalTo(usersDtlsDto.get(0).getUsername()));
		assertThat(usersDto.get(0).getUserId(), equalTo(usersDtlsDto.get(0).getId()));
		assertThat(usersDto.get(0).getEmail(), equalTo(usersDtlsDto.get(0).getEmail()));
		assertThat("true", equalTo(usersDtlsDto.get(0).getStatus()));
		assertThat(usersDto.get(0).getRole(), equalTo(usersDtlsDto.get(0).getRole()));
	}

	/**
	 * Should create the new user, and return the new userId.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUser() throws Exception {
		final UserDetailDto usrDtlDto = initializeUserDetailDto(0);
		final UserDto userDto = initializeUserDto(0);
		
		Mockito.when(this.workbenchDataManager.createUser(userDto)).thenReturn(new Integer(7));
		
		final Map<String, Object> mapResponse = this.userServiceImpl.createUser(usrDtlDto);
		assertThat((String) mapResponse.get("id"), equalTo("7"));
	}

	/**
	 * Should return the id 0, because happened a error during the creation user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUserError() throws Exception {
		final UserDetailDto usrDtlDto = initializeUserDetailDto(0);
		final UserDto userDto = initializeUserDto(0);

		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlDto.getEmail())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.createUser(userDto)).thenThrow(new MiddlewareQueryException("Error encountered while saving User: UserDataManager.addUser(user=" + userDto.getUsername() + "): "));
		
		final Map<String, Object> mapResponse = this.userServiceImpl.createUser(usrDtlDto);
		final ErrorResponse error = (ErrorResponse)mapResponse.get("ERROR");
		
		assertThat((String) mapResponse.get("id"), equalTo("0"));
		assertThat(error.getErrors().size(), equalTo(1));
		assertThat(error.getErrors().get(0).getFieldNames()[0], equalTo("userId"));
		assertThat(error.getErrors().get(0).getMessage(), 	equalTo("DB error"));
	}
	
	/**
	 * Should return the id 0, because happened a error during the creation user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUserValidateError() throws Exception {
		final UserDetailDto usrDtlDto = initializeUserDetailDto(0);

		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlDto.getEmail())).thenReturn(true);

		final Map<String, Object> mapResponse = this.userServiceImpl.createUser(usrDtlDto);
		final ErrorResponse error = (ErrorResponse)mapResponse.get("ERROR");
		
		assertThat((String) mapResponse.get("id"), equalTo("0"));
		assertThat(error.getErrors().size(), equalTo(2));
	}

	/**
	 * Should update the user, and return the same userId.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUser() throws Exception {
		final UserDetailDto usrDtlDto = initializeUserDetailDto(8);
		final UserDto userDto = initializeUserDto(8);
		final User user = initializeUser(8);
		
		Mockito.when(this.workbenchDataManager.updateUser(userDto)).thenReturn(new Integer(8));
		Mockito.when(this.workbenchDataManager.getUserById(8)).thenReturn(user);
		
		final Map<String, Object> mapResponse = this.userServiceImpl.updateUser(usrDtlDto);
		assertThat((String) mapResponse.get("id"), equalTo("8"));
	}

	/**
	 * Should return the id 0, because happened a error during the update user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserValidateError() throws Exception {
		final UserDetailDto usrDtlDto = initializeUserDetailDto(10);
		final User usr = initializeUser(10);
		usr.getPerson().setEmail("diego.nicolas.cuenya@leafnode.io");
		Mockito.when(this.workbenchDataManager.getUserById(usrDtlDto.getId())).thenReturn(usr);
		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlDto.getEmail())).thenReturn(true);
		
		final Map<String, Object> mapResponse = this.userServiceImpl.updateUser(usrDtlDto);
		final ErrorResponse error = (ErrorResponse)mapResponse.get("ERROR");
		
		assertThat((String) mapResponse.get("id"), equalTo("0"));
		assertThat(error.getErrors().size(), equalTo(1));
	}

	/**
	 * Should return the id 0, because happened a error during the update user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserError() throws Exception {
		final UserDetailDto usrDtlDto = initializeUserDetailDto(10);
		final User usr = initializeUser(10);
		final UserDto userDto = initializeUserDto(10);
		usr.getPerson().setEmail("diego.nicolas.cuenya@leafnode.io");
		Mockito.when(this.workbenchDataManager.getUserById(usrDtlDto.getId())).thenReturn(usr);
		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlDto.getEmail())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.updateUser(userDto)).thenThrow(new MiddlewareQueryException("Error encountered while saving User: UserDataManager.addUser(user=" + userDto.getUsername() + "): "));
		
		final Map<String, Object> mapResponse = this.userServiceImpl.updateUser(usrDtlDto);
		final ErrorResponse error = (ErrorResponse)mapResponse.get("ERROR");
		
		assertThat((String) mapResponse.get("id"), equalTo("0"));
		assertThat(error.getErrors().size(), equalTo(1));
		assertThat(error.getErrors().get(0).getFieldNames()[0], equalTo("userId"));
		assertThat(error.getErrors().get(0).getMessage(), 	equalTo("DB error"));
	}

	/**
	 * Should return a map with a List of UserDetailDto by the USER Key.
	 *
	 * @throws Exception
	 */
	@Test
	public void getUsersByProjectUUID() throws Exception {
		List<UserDto> usersDto = this.getAllListUser();

		Mockito.when(this.workbenchDataManager.getUsersByProjectUuid(projectUUID)).thenReturn(usersDto);
		final Map<String, Object> mapResults = this.userServiceImpl.getUsersByProjectUUID(projectUUID);
		final List<UserDetailDto> userDetailDtoList = (List<UserDetailDto>) mapResults.get("USERS");
		assertThat(usersDto.get(0).getFirstName(), equalTo(userDetailDtoList.get(0).getFirstName()));
		assertThat(usersDto.get(0).getLastName(), equalTo(userDetailDtoList.get(0).getLastName()));
		assertThat(usersDto.get(0).getUsername(), equalTo(userDetailDtoList.get(0).getUsername()));
		assertThat(usersDto.get(0).getUserId(), equalTo(userDetailDtoList.get(0).getId()));
		assertThat(usersDto.get(0).getEmail(), equalTo(userDetailDtoList.get(0).getEmail()));
		assertThat("true", equalTo(userDetailDtoList.get(0).getStatus()));
		assertThat(usersDto.get(0).getRole(), equalTo(userDetailDtoList.get(0).getRole()));
	}

	/**
	 * Should return a map with a description by the ERROR Key.
	 *
	 * @throws Exception
	 */
	@Test
	public void getUsersByProjectUUIDWithOutProjectUUID() throws Exception {
		final Map<String, Object> mapResults = this.userServiceImpl.getUsersByProjectUUID("");
		assertThat("The projectUUID must not be empty", equalTo(mapResults.get("ERROR")));

	}

	/**
	 * Should return a map with a description by the ERROR Key.
	 *
	 * @throws Exception
	 */
	@Test
	public void getUsersByProjectUUIDWithOutUsers() throws Exception {
		final Map<String, Object> mapResults = this.userServiceImpl.getUsersByProjectUUID(projectUUID);
		assertThat("don't exists users for this projectUUID", equalTo(mapResults.get("ERROR")));

	}

	/**
	 * Should return a map with a description by the ERROR Key.
	 *
	 * @throws Exception
	 */
	@Test
	public void getUsersByProjectUUIDErrorQuery() throws Exception {
		Mockito.when(this.workbenchDataManager.getUsersByProjectUuid(projectUUID))
			.thenThrow(new MiddlewareQueryException("Error in getUsersByProjectUUId()"));

		final Map<String, Object> mapResults = this.userServiceImpl.getUsersByProjectUUID(projectUUID);
		assertThat("An internal error occurred while trying to get the users", equalTo(mapResults.get("ERROR")));

	}

	/**
	 * initialize List all user
	 *
	 * @return List<UserDetailDto>
	 */
	public List<UserDetailDto> createUserDetailDto() {
		final UserDetailDto user = initializeUserDetailDto(10);
		final List<UserDetailDto> users = Lists.newArrayList(user);

		return users;
	}


	/**
	 * initialize List all user
	 * 
	 * @return List<UserDto>
	 */
	public List<UserDto> getAllListUser() {
		final UserDto user = initializeUserDto(10);
		final List<UserDto> users = Lists.newArrayList(user);
		
		return users;
	}

	/**
	 * initialize UserDto
	 * 
	 * @return user UserDto
	 */
	public UserDto initializeUserDto(final Integer userId) {
		final UserDto user = new UserDto();
		
		final String firstName = RandomStringUtils.randomAlphanumeric(20);
		user.setFirstName(firstName);
		
		final String lastName = RandomStringUtils.randomAlphanumeric(50);
		user.setLastName(lastName);
		
		user.setStatus(0);
		user.setRole("Breeder");
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
	 * @return UserDetailDto
	 */
	public UserDetailDto initializeUserDetailDto(final Integer userId) {
		final UserDetailDto user = new UserDetailDto();
		
		final String firstName = RandomStringUtils.randomAlphanumeric(20);
		user.setFirstName(firstName);
		
		final String lastName = RandomStringUtils.randomAlphanumeric(50);
		user.setLastName(lastName);
		
		user.setStatus("true");
		user.setRole("Breeder");
		user.setId(userId);
		
		final String username = RandomStringUtils.randomAlphanumeric(30);
		user.setUsername(username);
		
		final String email = RandomStringUtils.randomAlphanumeric(24);
		user.setEmail("test" + email + "@leafnode.io");
		
		return user;
	}
	
	/**
	 * initialize User
	 * 
	 * @param userId Integer
	 * @return user User
	 */
	public User initializeUser(final Integer userId) {
		final User user = new User();
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

		user.setPersonid(person.getId());
		user.setPerson(person);
		
		final String username = RandomStringUtils.randomAlphanumeric(30);
		user.setName(username);
		user.setAccess(0);
		user.setInstalid(0);
		user.setType(0);
		
		return user;
	}

}
