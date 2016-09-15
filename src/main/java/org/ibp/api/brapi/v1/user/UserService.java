
package org.ibp.api.brapi.v1.user;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;

public interface UserService {

	public List<UserDetailDto> getAllUserDtosSorted();
	
	public GenericResponse createUser(final UserDetailsDto user);
	
	public GenericResponse updateUser(final UserDetailsDto user);

}
