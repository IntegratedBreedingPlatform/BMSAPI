
package org.ibp.api.java.user;

import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.user.UserDetailDto;

import java.util.List;

public interface UserService {

	List<UserDetailDto> getAllUsersSortedByLastName();

	Integer createUser(final UserDetailDto user);

	Integer updateUser(final UserDetailDto user);

	List<UserDetailDto> getUsersByProjectUUID(final String projectUUID);

	UserDto getUserWithAuthorities(final String cropName, final String programUuid);
}
