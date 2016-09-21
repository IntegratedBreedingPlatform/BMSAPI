
package org.ibp.api.brapi.v1.user;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;

public interface UserService {

	public List<UserDetailDto> getAllUserDtosSorted();
	
	public GenericResponse createUser(final UserDetailDto user);
	
	public GenericResponse updateUser(final UserDetailDto user);

}
