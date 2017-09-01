
package org.ibp.api.java.user;

import org.ibp.api.java.impl.middleware.user.UserDetailDto;

import java.util.List;
import java.util.Map;

public interface UserService {

	List<UserDetailDto> getAllUsersSortedByLastName();

	Map<String, Object> createUser(final UserDetailDto user);

	Map<String, Object> updateUser(final UserDetailDto user);

	List<UserDetailDto> getUsersByProjectUUID(final String projectUUID);
}
