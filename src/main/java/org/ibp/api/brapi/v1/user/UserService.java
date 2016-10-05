
package org.ibp.api.brapi.v1.user;

import java.util.List;
import java.util.Map;

public interface UserService {

	public List<UserDetailDto> getAllUsersSortedByLastName();

	public Map<String, Object> createUser(final UserDetailDto user);

	public Map<String, Object> updateUser(final UserDetailDto user);

}
