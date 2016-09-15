
package org.ibp.api.brapi.v1.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.common.GenericResponse;
import org.jfree.util.Log;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	public static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	// @Autowired
	// private UserDataManager userDataManager;
	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<UserDetailDto> getAllUserDtosSorted() {
		final List<UserDetailDto> result = new ArrayList<>();
		final List<UserDto> users = this.workbenchDataManager.getAllUserDtosSorted();

		final PropertyMap<UserDto, UserDetailDto> userMapper = new PropertyMap<UserDto, UserDetailDto>() {

			@Override
			protected void configure() {
				this.map(this.source.getFirstName(), this.destination.getFirstName());
				this.map(this.source.getLastName(), this.destination.getLastName());
				this.map(this.source.getUserId(), this.destination.getUserId());
				this.map(this.source.getUsername(), this.destination.getUsername());
				this.map(this.source.getRole(), this.destination.getRole());
				this.map(this.source.getStatus() == 0 ? "true" : "false", this.destination.getStatus());
				this.map(this.source.getEmail(), this.destination.getEmail());
			}
		};

		final ModelMapper modelMapper = new ModelMapper();
		modelMapper.addMappings(userMapper);

		for (final Iterator<UserDto> iterator = users.iterator(); iterator.hasNext();) {
			final UserDto userDto = iterator.next();
			final UserDetailDto userInfo = modelMapper.map(userDto, UserDetailDto.class);

			if (userDto.getStatus() == 0) {
				userInfo.setStatus("true");
			} else {
				userInfo.setStatus("false");
			}
			userInfo.setRole(WordUtils.capitalize(userInfo.getRole().toLowerCase()));
			result.add(userInfo);
		}

		return result;
	}

	@Override
	public GenericResponse createUser(final UserDetailsDto user) {
		LOGGER.info(user.toString());
		final UserDto userdto = new UserDto();
		final List<String> Errors = new ArrayList<String>();
		final GenericResponse gResponse = new GenericResponse(String.valueOf(0));

		Integer newUserId = null;
		userdto.setUsername(user.getUsername());
		userdto.setFirstName(user.getFirstName());
		userdto.setLastName(user.getLastName());
		userdto.setRole(user.getRole());
		userdto.setEmail(user.getEmail());
		userdto.setStatus(user.getStatus().equals("true") ? 0 : 1);

		if (ValidatorUserHelper.validate(userdto, Errors)) {

			try {
				newUserId = this.workbenchDataManager.addNewUser(userdto);
				gResponse.setId(String.valueOf(newUserId));
			} catch (MiddlewareQueryException e) {
				Log.info("Error on createUser");
			}
			LOGGER.info("Validación OK");
			return gResponse;
		}
		LOGGER.info("Validación NOK");
		return gResponse;
	}

	@Override
	public GenericResponse updateUser(final UserDetailsDto user) {
		LOGGER.info(user.toString());
		final UserDto userdto = new UserDto();
		final List<String> Errors = new ArrayList<String>();
		final GenericResponse gResponse = new GenericResponse(String.valueOf(0));
		Integer updateUserId = null;
		userdto.setUsername(user.getUsername());
		userdto.setFirstName(user.getFirstName());
		userdto.setLastName(user.getLastName());
		userdto.setRole(user.getRole());
		userdto.setEmail(user.getEmail());
		userdto.setStatus(user.getStatus().equals("true") ? 0 : 1);
		if (ValidatorUserHelper.validate(userdto, Errors)) {
			LOGGER.info("Validación OK");
			try {
				updateUserId = this.workbenchDataManager.updateUser(userdto);
				gResponse.setId(String.valueOf(updateUserId));
			} catch (MiddlewareQueryException e) {
				Log.info("Error on createUser");
			}
			return gResponse;
		}
		LOGGER.info("Validación NOK");
		return gResponse;
	}
}
