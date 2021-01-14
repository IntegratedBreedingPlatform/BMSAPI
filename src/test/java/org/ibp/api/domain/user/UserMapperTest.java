package org.ibp.api.domain.user;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.impl.middleware.user.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserMapperTest {

	private UserServiceImpl userServiceImpl;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private SecurityService securityService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private UserService middlewareUserService;

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

	@InjectMocks
	protected UserValidator userValidator;

	@Before
	public void beforeEachTest() {
		// We cannot mock resourceBundleMessageSource.getMessage because the method is marked as final.
		// So as a workaround, use a real class instance and set setUseCodeAsDefaultMessage to true
		this.messageSource.setUseCodeAsDefaultMessage(true);
		MockitoAnnotations.initMocks(this);
		this.userServiceImpl = new UserServiceImpl();
		this.userValidator.setWorkbenchDataManager(this.workbenchDataManager);
		this.userServiceImpl.setUserValidator(this.userValidator);
		this.userServiceImpl.setSecurityService(this.securityService);
		this.userServiceImpl.setUserService(this.middlewareUserService);

		final CropType cropType = new CropType("maize");
		final Project project = new Project();
		project.setUniqueID("");
		project.setProjectId(1L);
		project.setCropType(cropType);
		Mockito.doReturn(project).when(this.contextUtil).getProjectInContext();

	}

	@Test
	public void userDetailsMapperTest() {
		final ModelMapper mapper = UserMapper.getInstance();
		final UserDto userDto = UserTestDataGenerator.initializeUserDto(1);
		final UserDetailDto userDetailDto = mapper.map(userDto, UserDetailDto.class);
		System.out.println(userDetailDto);

		assertThat(userDto.getFirstName(), equalTo(userDetailDto.getFirstName()));
		assertThat(userDto.getLastName(), equalTo(userDetailDto.getLastName()));
		assertThat(userDto.getUserId(), equalTo(userDetailDto.getId()));
		assertThat(userDto.getUsername(), equalTo(userDetailDto.getUsername()));
		assertThat(userDto.getUserRoles().get(0).getId(), equalTo(userDetailDto.getUserRoles().get(0).getId()));
		assertThat((userDto.getStatus() == 0 ? "true" : "false"), equalTo(userDetailDto.getStatus()));
		assertThat(userDto.getEmail(), equalTo(userDetailDto.getEmail()));

	}
}
