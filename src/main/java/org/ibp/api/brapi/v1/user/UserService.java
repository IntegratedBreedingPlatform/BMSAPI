
package org.ibp.api.brapi.v1.user;

import java.util.List;
import java.util.Map;

public interface UserService {

	List<UserDetailDto> getAllUsersSortedByLastName();

	Map<String, Object> createUser(final UserDetailDto user);

	Map<String, Object> updateUser(final UserDetailDto user);

	List<UserDetailDto> getUsersByProjectUUID(final String projectUUID);
}
