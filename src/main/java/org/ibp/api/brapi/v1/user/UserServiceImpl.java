
package org.ibp.api.brapi.v1.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	private static final String USER_NAME = "User";

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	protected UserValidator userValidator;

	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
		LOG.debug(user.toString());
		final UserDto userdto = new UserDto();
		GenericResponse gResponse = new GenericResponse(String.valueOf(0));
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), UserServiceImpl.USER_NAME);

		Integer newUserId = null;
		userdto.setUserId(0);
		userdto.setUsername(user.getUsername());
		userdto.setFirstName(user.getFirstName());
		userdto.setLastName(user.getLastName());
		userdto.setRole(user.getRole());
		userdto.setEmail(user.getEmail());
		userdto.setStatus(user.getStatus().equals("true") ? 0 : 1);
		userdto.setPassword(passwordEncoder.encode(userdto.getUsername()));
		userValidator.validate(userdto, errors);
		if (errors.hasErrors()) {
			LOG.debug("UserValidator returns errors");
			return gResponse;
		}
		try {
			newUserId = this.workbenchDataManager.addNewUser(userdto);
			gResponse.setId(String.valueOf(newUserId));
		} catch (MiddlewareQueryException e) {
			LOG.info("Error on workbenchDataManager.addNewUser " + e.getMessage());
		}

		return gResponse;
	}

	@Override
	public GenericResponse updateUser(final UserDetailsDto user) {
		LOG.debug(user.toString());
		final UserDto userdto = new UserDto();
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), UserServiceImpl.USER_NAME);

		GenericResponse gResponse = new GenericResponse(String.valueOf(0));
		Integer updateUserId = null;
		userdto.setUserId(user.getUserId());
		userdto.setUsername(user.getUsername());
		userdto.setFirstName(user.getFirstName());
		userdto.setLastName(user.getLastName());
		userdto.setRole(user.getRole());
		userdto.setEmail(user.getEmail());
		userdto.setStatus(user.getStatus().equals("true") ? 0 : 1);
		userValidator.validate(userdto, errors);
		if (errors.hasErrors()) {
			LOG.debug("UserValidator returns errors");
			return gResponse;
		}
		try {
			updateUserId = this.workbenchDataManager.updateUser(userdto);
			gResponse.setId(String.valueOf(updateUserId));
		} catch (MiddlewareQueryException e) {
			LOG.info("Error on workbenchDataManager.updateUser" + e.getMessage());
		}
		return gResponse;
	}
}
