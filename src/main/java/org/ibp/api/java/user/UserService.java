
package org.ibp.api.java.user;

import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.user.UserDetailDto;

import java.util.List;
import java.util.Map;

public interface UserService {

	List<UserDetailDto> getAllUsersSortedByLastName();

	Map<String, Object> createUser(final UserDetailDto user);

	Map<String, Object> updateUser(final UserDetailDto user);

	List<UserDetailDto> getUsersByProjectUUID(final String projectUUID);

	UserDto getUserWithAuthorities(final String cropName, final String programUuid);
}
