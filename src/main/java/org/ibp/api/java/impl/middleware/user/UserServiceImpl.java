
package org.ibp.api.java.impl.middleware.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.java.user.UserData;
import org.ibp.api.java.user.UserService;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	@Override
	public List<UserData> getAllUserDtosSorted() {
		final List<UserData> result = new ArrayList<>();
		final List<UserDto> users = this.workbenchDataManager.getAllUserDtosSorted();

		final PropertyMap<UserDto, UserData> userMapper = new PropertyMap<UserDto, UserData>() {

			@Override
			protected void configure() {
				this.map(this.source.getFirstName(), this.destination.getFirstName());
				this.map(this.source.getLastName(), this.destination.getLastName());
				this.map(this.source.getUserId(), this.destination.getUserId());
				this.map(this.source.getUsername(), this.destination.getUsername());
				this.map(this.source.getRole(), this.destination.getRole());
				this.map(this.source.getStatus() == 0 ? "true" : "false", this.destination.getStatus());
			}
		};

		final ModelMapper modelMapper = new ModelMapper();
		modelMapper.addMappings(userMapper);

		for (final Iterator<UserDto> iterator = users.iterator(); iterator.hasNext();) {
			final UserDto userDto = iterator.next();
			final UserData userInfo = modelMapper.map(userDto, UserData.class);

			result.add(userInfo);
		}

		return result;
	}
}
