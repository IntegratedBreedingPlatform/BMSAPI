package org.ibp.api.domain.user;

import org.generationcp.middleware.domain.workbench.CropDto;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserRoleDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private static final Converter<Integer, String> toStatusConvert = new AbstractConverter<Integer, String>() {

		protected String convert(final Integer statusId) {
			return statusId.equals(0) ? "true" : "false";

		}
	};

	private static final AbstractConverter<Set<CropType>, Set<CropDto>> cropsConverter =
		new AbstractConverter<Set<CropType>, Set<CropDto>>() {

			@Override
			protected Set<CropDto> convert(final Set<CropType> source) {
				return source.stream().map(CropDto::new).collect(Collectors.toSet());
			}
		};

	private static final AbstractConverter<List<UserRole>, List<UserRoleDto>> userRolesConverter =
		new AbstractConverter<List<UserRole>, List<UserRoleDto>>() {

			@Override
			protected List<UserRoleDto> convert(final List<UserRole> source) {
				return source.stream().map(UserRoleDto::new).collect(Collectors.toList());
			}
		};

	private static final AbstractConverter<List<PermissionDto>, Set<String>> authoritiesConverter =
		new AbstractConverter<List<PermissionDto>, Set<String>>() {

			@Override
			protected Set<String> convert(final List<PermissionDto> source) {
				return source.stream().map(PermissionDto::getName).collect(Collectors.toSet());
			}
		};

	private UserMapper() {

	}

	static {
		UserMapper.addUserDetailsDataMapping(UserMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return UserMapper.applicationWideModelMapper;
	}

	private static void addUserDetailsDataMapping(final ModelMapper mapper) {


		mapper.addMappings(new PropertyMap<WorkbenchUser, UserDto>() {

			@Override
			protected void configure() {
				this.map().setId(this.source.getUserid());
				this.map().setUsername(this.source.getName());
				this.map().setFirstName(this.source.getPerson().getFirstName());
				this.map().setLastName(this.source.getPerson().getLastName());
				this.using(toStatusConvert).map().setStatus(this.source.getStatus().toString());
				this.map().setEmail(this.source.getPerson().getEmail());
				this.using(userRolesConverter).map(this.source.getRoles()).setUserRoles(null);
				this.using(authoritiesConverter).map(this.source.getPermissions()).setAuthorities(null);
				this.using(cropsConverter).map(this.source.getPerson().getCrops()).setCrops(null);
			}
		});
	}
}
