package org.ibp.api.java.impl.middleware.permission.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.RoleType;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.ibp.api.java.permission.PermissionService;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.java.study.StudyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class BrapiPermissionValidatorTest {

	public static final String DEFAULT_ID = "1";
	private final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(16);
	private final String CROP_NAME = "maize";
	private final String PERMISSIONS = "STUDIES";
	private static final Integer USER_ID = 1;

	@Mock
	private UserService userService;

	@Mock
	private SecurityService securityService;

	@Mock
	private PermissionService permissionService;

	@Mock
	private ProgramService programService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private StudyService studyService;

	@Mock
	private ObservationUnitService observationUnitService;

	@InjectMocks
	private BrapiPermissionValidator brapiPermissionValidator;

	@Before
	public void setUp() {
		WorkbenchUser user = new WorkbenchUser();
		user.setUserid(USER_ID);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		final Role role = new Role(USER_ID, PERMISSIONS);
		final RoleType roleType = new RoleType();
		roleType.setId(org.generationcp.middleware.domain.workbench.RoleType.PROGRAM.getId());
		role.setRoleType(roleType);
		final UserRole testUserRole = new UserRole(user, role);
		testUserRole.setCropType(new CropType(CROP_NAME));
		user.setRoles(Collections.singletonList(testUserRole));
	}

	@Test
	public void testValidatePermissions() {
		final PermissionDto permissionDto = new PermissionDto();
		permissionDto.setName(PERMISSIONS);
		final List<PermissionDto> permissions = Lists.newArrayList(permissionDto);
		Mockito.when(this.permissionService.getPermissions(USER_ID, CROP_NAME, null, true)).thenReturn(
			permissions);
		this.brapiPermissionValidator.validatePermissions(CROP_NAME, PERMISSIONS);
	}

	@Test(expected = AccessDeniedException.class)
	public void testValidatePermissions_error() {
		final PermissionDto permissionDto = new PermissionDto();
		permissionDto.setName("OTHER");
		final List<PermissionDto> permissions = Lists.newArrayList(permissionDto);
		Mockito.when(this.permissionService.getPermissions(USER_ID, CROP_NAME, null, true)).thenReturn(
			permissions);
		this.brapiPermissionValidator.validatePermissions(CROP_NAME, PERMISSIONS);
	}

	@Test
	public void testValidateProgramByProgramDbId() {
		this.mockFilterPrograms();
		List<String> validProgramIds = this.brapiPermissionValidator.validateProgramByProgramDbId(CROP_NAME, PROGRAM_UUID);
		Assert.assertNotEquals(0, validProgramIds.size());
		Assert.assertEquals(PROGRAM_UUID, validProgramIds.get(0));
	}

	private void mockFilterPrograms() {
		final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
		programSearchRequest.setLoggedInUserId(USER_ID);
		programSearchRequest.setCommonCropName(CROP_NAME);

		ProgramDTO programDTO = new ProgramDTO();
		programDTO.setUniqueID(PROGRAM_UUID);

		Mockito.when(this.programService.getFilteredPrograms(null, programSearchRequest)).thenReturn(
			Arrays.asList(programDTO));
	}

	@Test(expected = AccessDeniedException.class)
	public void testValidateProgramByProgramDbId_error() {
		final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
		programSearchRequest.setLoggedInUserId(USER_ID);
		programSearchRequest.setCommonCropName(CROP_NAME);

		ProgramDTO otherProgramDTO = new ProgramDTO();
		otherProgramDTO.setUniqueID(RandomStringUtils.randomAlphabetic(16));

		Mockito.when(this.programService.getFilteredPrograms(null, programSearchRequest)).thenReturn(
			Arrays.asList(otherProgramDTO));
		List<String> validProgramIds = this.brapiPermissionValidator.validateProgramByProgramDbId(CROP_NAME, PROGRAM_UUID);
	}

	@Test
	public void testValidateProgramByStudyDbId() {
		this.mockFilterPrograms();
		Mockito.when(this.studyDataManager.getProjectIdByStudyDbId(1)).thenReturn(1);

		DmsProject project = new DmsProject();
		project.setProgramUUID(PROGRAM_UUID);
		Mockito.when(this.studyService.getDmSProjectByStudyId(1)).thenReturn(project);
		List<String> validProgramIds = this.brapiPermissionValidator.validateProgramByStudyDbId(CROP_NAME, DEFAULT_ID);
		Assert.assertNotEquals(0, validProgramIds.size());
		Assert.assertEquals(PROGRAM_UUID, validProgramIds.get(0));
	}

	@Test
	public void testValidateProgramByObservationUnitDbId() {
		this.mockFilterPrograms();

		final ObservationUnitSearchRequestDTO obsRequestDto = new ObservationUnitSearchRequestDTO();
		obsRequestDto.setObservationUnitDbIds(Arrays.asList(DEFAULT_ID));
		final ObservationUnitDto observationUnitDto = new ObservationUnitDto();
		observationUnitDto.setObservationUnitDbId(DEFAULT_ID);
		observationUnitDto.setProgramDbId(PROGRAM_UUID);
		Mockito.when(this.observationUnitService.searchObservationUnits(null, null, obsRequestDto))
			.thenReturn(Arrays.asList(observationUnitDto));

		List<String> validProgramIds = this.brapiPermissionValidator.validateProgramByObservationUnitDbId(CROP_NAME, DEFAULT_ID);
		Assert.assertNotEquals(0, validProgramIds.size());
		Assert.assertEquals(PROGRAM_UUID, validProgramIds.get(0));
	}
}
