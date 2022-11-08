
package org.ibp.api.java.user;

import org.generationcp.middleware.dao.workbench.ProgramEligibleUsersSearchRequest;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.user.UserProfileUpdateRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

	List<UserDto> getAllUsersSortedByLastName();

	Integer createUser(final UserDto user);

	Integer updateUser(final UserDto user);

	List<UserDto> getUsersByProjectUUID(final String projectUUID);

	UserDto getUserWithAuthorities(final String cropName, final String programUuid);

	void updateUserProfile(final UserProfileUpdateRequestDTO userProfileUpdateRequestDTO, final WorkbenchUser workbenchUser);

	List<UserDto> getMembersEligibleUsers(String programUUID, ProgramEligibleUsersSearchRequest searchRequest, Pageable pageable);

	long countAllMembersEligibleUsers(String programUUID, ProgramEligibleUsersSearchRequest searchRequest);

}
