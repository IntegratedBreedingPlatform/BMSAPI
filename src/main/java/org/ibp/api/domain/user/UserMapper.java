package org.ibp.api.domain.user;

import org.generationcp.middleware.domain.workbench.CropDto;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private static Converter<Integer, String> toStatusConvert = new AbstractConverter<Integer, String>() {

		protected String convert(final Integer statusId) {
			return statusId.equals(0) ? "true" : "false";

		}
	};

	private static AbstractConverter<List<CropType>, List<CropDto>> cropsConverter =
		new AbstractConverter<List<CropType>, List<CropDto>>() {

			@Override
			protected List<CropDto> convert(final List<CropType> source) {
				return source.stream().map(CropDto::new).collect(Collectors.toList());
			}
		};

	private static AbstractConverter<List<UserRole>, Set<String>> authoritiesConverter =
		new AbstractConverter<List<UserRole>, Set<String>>() {

			@Override
			protected Set<String> convert(final List<UserRole> source) {
				// TODO Use Permissions
				return source.stream().map(userRole -> userRole.getRole().getCapitalizedRole()).collect(Collectors.toSet());
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

		mapper.addMappings(new PropertyMap<UserDto, UserDetailDto>() {

			@Override
			protected void configure() {
				this.map().setFirstName(this.source.getFirstName());
				this.map().setLastName(this.source.getLastName());
				this.map().setId(this.source.getUserId());
				this.map().setUsername(this.source.getUsername());
				this.map().setRole(this.source.getRole());
				using(toStatusConvert).map().setStatus(this.source.getStatus().toString());
				this.map().setEmail(this.source.getEmail());
				this.map().setCrops(this.source.getCrops());
			}
		});

		mapper.addMappings(new PropertyMap<WorkbenchUser, UserDto>() {

			@Override
			protected void configure() {
				this.map().setUserId(this.source.getUserid());
				this.map().setUsername(this.source.getName());
				this.map().setFirstName(this.source.getPerson().getFirstName());
				this.map().setLastName(this.source.getPerson().getLastName());
				this.map().setStatus(this.source.getStatus());
				this.map().setEmail(this.source.getPerson().getEmail());
				using(authoritiesConverter).map(this.source.getRoles()).setAuthorities(null); // TODO use Permissions
				using(cropsConverter).map(this.source.getCrops()).setCrops(null);
			}
		});
	}
}
