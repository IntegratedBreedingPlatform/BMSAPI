
package org.ibp.api.java.user;

import org.generationcp.middleware.dao.workbench.ProgramEligibleUsersSearchRequest;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.domain.user.UserProfileUpdateRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

	List<UserDetailDto> getAllUsersSortedByLastName();

	Integer createUser(final UserDetailDto user);

	Integer updateUser(final UserDetailDto user);

	List<UserDetailDto> getUsersByProjectUUID(final String projectUUID);

	UserDetailDto getUserWithAuthorities(final String cropName, final String programUuid);

	void updateUserProfile(final UserProfileUpdateRequestDTO userProfileUpdateRequestDTO, final WorkbenchUser workbenchUser);

	List<UserDetailDto> getMembersEligibleUsers(String programUUID, ProgramEligibleUsersSearchRequest searchRequest, Pageable pageable);

	long countAllMembersEligibleUsers(String programUUID, ProgramEligibleUsersSearchRequest searchRequest);

}
