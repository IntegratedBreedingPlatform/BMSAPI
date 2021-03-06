package org.ibp.api.brapi.v2.program;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.service.api.user.UserService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v2.validation.CropValidator;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.program.ProgramService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.ObjectError;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProgramResourceBrapiv2Test extends ApiUnitTestBase {

	private static final String WHEAT = "Wheat";
	private static final String RICE = "Rice";
	private static final String MAIZE = "Maize";
	private static final String INVALID_CROP = "maize2";
	private static final String BRAPI_V2_PROGRAMS = "/brapi/v2/programs";
	final static String PROGRAM_UUID_RICE = "92c47f83-4427-44c9-982f-b611b8917a2d";
	final static String PROGRAM_UUID_WHEAT = "2ca55832-5c5d-404f-b05c-bc6e305c8b76";
	final static String PROGRAM_UUID_MAIZE = "d1a052d0-65eb-4d0e-8813-6c770d10f253";

	private List<CropType> crops = new ArrayList<>();

	@Autowired
	private UserService userService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ProgramService programService;

	@Autowired
	private CropValidator cropValidator;

	final WorkbenchUser user = new WorkbenchUser();

    @Before
    public void setup() {
        this.crops = this.getAllCrops();
        Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(this.crops);

		Mockito.when(this.workbenchDataManager.getAvailableCropsForUser(Mockito.anyInt())).thenReturn(this.crops);

		this.user.setName(RandomStringUtils.randomAlphabetic(10));
		this.user.setUserid(Integer.parseInt(RandomStringUtils.randomNumeric(5)));
		Mockito.when(this.userService.getUserById(Mockito.anyInt())).thenReturn(this.user);

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(this.user);

		this.cropValidator = Mockito.mock(CropValidator.class);

	}

	@Test
	@Ignore
    public void testListProgramsBadCrop() throws Exception {
        Mockito
			.doThrow(new ApiRequestValidationException(Arrays.asList(
				new ObjectError("", new String[] {"crop.does.not.exist"}, new String[] {ProgramResourceBrapiv2Test.INVALID_CROP}, ""))))
			.when(this.cropValidator).validateCrop(Mockito.anyString());

		final UriComponents uriComponents =
			UriComponentsBuilder.newInstance().path(ProgramResourceBrapiv2Test.BRAPI_V2_PROGRAMS)
				.queryParam("cropName", ProgramResourceBrapiv2Test.INVALID_CROP).build().encode();

		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isBadRequest()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message",
				Matchers.is("Crop " + ProgramResourceBrapiv2Test.INVALID_CROP + " doesn't exist."))); //

	}

	@Test
	public void testListProgramsNoAdditionalInfo() throws Exception {
		final List<ProgramDetailsDto> programDetailsDtoList = this.getProgramDetails();
		Mockito.when(this.programService.countProgramsByFilter(org.mockito.Matchers.any(ProgramSearchRequest.class)))
			.thenReturn(new Long(this.crops.size()));
		final List<ProgramDetailsDto> projectList = this.getProgramDetailsList();
		Mockito.when(this.programService.getProgramDetailsByFilter(org.mockito.Mockito.any(Pageable.class),
			org.mockito.Matchers.any(ProgramSearchRequest.class))).thenReturn(projectList);

		final UriComponents uriComponents =
			UriComponentsBuilder.newInstance().path(ProgramResourceBrapiv2Test.BRAPI_V2_PROGRAMS).build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(programDetailsDtoList.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId",
				Matchers.is(ProgramResourceBrapiv2Test.PROGRAM_UUID_MAIZE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programName", Matchers.is(ProgramResourceBrapiv2Test.MAIZE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].programDbId",
				Matchers.is(ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].programName", Matchers.is(ProgramResourceBrapiv2Test.RICE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2].programDbId",
				Matchers.is(ProgramResourceBrapiv2Test.PROGRAM_UUID_WHEAT))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2].programName", Matchers.is(ProgramResourceBrapiv2Test.WHEAT)))
			// Default starting page index is 0
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage",
				Matchers.is(BrapiPagedResult.DEFAULT_PAGE_NUMBER)))
			// Default page size is 1000
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(BrapiPagedResult.DEFAULT_PAGE_SIZE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(this.crops.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(1)));
	}

	@Test
	public void testListProgramsWithPaging() throws Exception {
		final List<ProgramDetailsDto> programDetailsDtoList = this.getProgramDetails();
		Mockito.when(this.programService.countProgramsByFilter(org.mockito.Matchers.any(ProgramSearchRequest.class)))
			.thenReturn(new Long(this.crops.size()));
		final List<ProgramDetailsDto> projectList = this.getProgramDetailsList();
		Mockito.when(this.programService.getProgramDetailsByFilter(org.mockito.Mockito.any(Pageable.class),
			org.mockito.Matchers.any(ProgramSearchRequest.class))).thenReturn(projectList);

		final int page = 1;
		final int pageSize = 2;
		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(ProgramResourceBrapiv2Test.BRAPI_V2_PROGRAMS)
			.queryParam("page", page).queryParam("pageSize", pageSize).build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(programDetailsDtoList.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId",
				Matchers.is(ProgramResourceBrapiv2Test.PROGRAM_UUID_MAIZE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programName", Matchers.is(ProgramResourceBrapiv2Test.MAIZE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].leadPersonDbId",
				Matchers.is(String.valueOf(this.user.getUserid()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].commonCropName",
				Matchers.is(ProgramResourceBrapiv2Test.MAIZE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].programDbId",
				Matchers.is(ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].leadPersonDbId",
				Matchers.is(String.valueOf(this.user.getUserid()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].commonCropName",
                        Matchers.is(ProgramResourceBrapiv2Test.RICE)))//
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.data[1].programName", Matchers.is(ProgramResourceBrapiv2Test.RICE)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2].programDbId",
                        Matchers.is(ProgramResourceBrapiv2Test.PROGRAM_UUID_WHEAT)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2].leadPersonDbId",
                        Matchers.is(String.valueOf(this.user.getUserid()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2].commonCropName",
				Matchers.is(ProgramResourceBrapiv2Test.WHEAT)))//
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[2].programName", Matchers.is(ProgramResourceBrapiv2Test.WHEAT)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.currentPage", Matchers.is(page)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.pageSize", Matchers.is(pageSize)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalCount", Matchers.is(this.crops.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.pagination.totalPages", Matchers.is(2)));
	}

	@Test
	public void testListProgramFilterByName() throws Exception {
		Mockito.when(this.programService.countProgramsByFilter(org.mockito.Matchers.any(ProgramSearchRequest.class)))
			.thenReturn(1L);

		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		programDetailsDtoList
			.add(
				new ProgramDetailsDto(ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE, ProgramResourceBrapiv2Test.RICE, null, null, null, null,
					null, null));
		final List<ProgramDetailsDto> projectList = new ArrayList<>();
        projectList.add(
            new ProgramDetailsDto(PROGRAM_UUID_RICE, RICE, null, null, null, null, null,
                null));

		Mockito.when(this.programService.getProgramDetailsByFilter(org.mockito.Mockito.any(Pageable.class),
			org.mockito.Matchers.any(ProgramSearchRequest.class))).thenReturn(projectList);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(ProgramResourceBrapiv2Test.BRAPI_V2_PROGRAMS)
			.queryParam("programName", ProgramResourceBrapiv2Test.RICE).build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(programDetailsDtoList.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId",
				Matchers.is(ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programName", Matchers.is(ProgramResourceBrapiv2Test.RICE))) //
		;
	}

	@Test
	public void testListProgramFilterByAbbreviation() throws Exception {
		Mockito.when(this.workbenchDataManager.countProjectsByFilter(org.mockito.Matchers.any(ProgramSearchRequest.class)))
			.thenReturn(1L);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(ProgramResourceBrapiv2Test.BRAPI_V2_PROGRAMS)
			.queryParam("abbreviation", "AAAB").build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isNotImplemented()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0].message", Matchers.is("Abbreviation is not yet supported")));

	}

	@Test
	public void testListProgramFilterByProgramId() throws Exception {
		Mockito.when(this.programService.countProgramsByFilter(org.mockito.Matchers.any(ProgramSearchRequest.class)))
			.thenReturn(1L);

		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		programDetailsDtoList
			.add(
				new ProgramDetailsDto(ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE, ProgramResourceBrapiv2Test.RICE, null, null, null, null,
					null, null));
		final List<Project> projectList = new ArrayList<>();
		projectList.add(this.getProject(11L, ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE, ProgramResourceBrapiv2Test.RICE,
			ProgramResourceBrapiv2Test.RICE));

		Mockito.when(this.programService.getProgramDetailsByFilter(org.mockito.Mockito.any(Pageable.class),
			org.mockito.Matchers.any(ProgramSearchRequest.class))).thenReturn(programDetailsDtoList);

		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path(ProgramResourceBrapiv2Test.BRAPI_V2_PROGRAMS)
			.queryParam("programDbId", ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE).build().encode();
		this.mockMvc.perform(MockMvcRequestBuilders.get(uriComponents.toString()).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(programDetailsDtoList.size()))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programDbId",
				Matchers.is(ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].programName", Matchers.is(ProgramResourceBrapiv2Test.RICE))) //
		;
	}

	private List<ProgramDetailsDto> getProgramDetails() {
		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		programDetailsDtoList
			.add(new ProgramDetailsDto(ProgramResourceBrapiv2Test.PROGRAM_UUID_MAIZE, ProgramResourceBrapiv2Test.MAIZE, null, null, null,
				this.user.getUserid().toString(), this.user.getName(), ProgramResourceBrapiv2Test.MAIZE));
		programDetailsDtoList
			.add(
				new ProgramDetailsDto(ProgramResourceBrapiv2Test.PROGRAM_UUID_RICE, ProgramResourceBrapiv2Test.RICE, null, null, null, this.user.getUserid().toString(), this.user.getName(), ProgramResourceBrapiv2Test.RICE));
		programDetailsDtoList
			.add(new ProgramDetailsDto(ProgramResourceBrapiv2Test.PROGRAM_UUID_WHEAT, ProgramResourceBrapiv2Test.WHEAT, null, null, null,
				this.user.getUserid().toString(), this.user.getName(), ProgramResourceBrapiv2Test.WHEAT));
		return programDetailsDtoList;
	}

	private List<CropType> getAllCrops() {
		final List<CropType> cropTypes = new ArrayList<>();
		cropTypes.add(this.getCropType("maize", "ibdbv2_maize_merged", "4.0.0"));
		cropTypes.add(this.getCropType("rice", "ibdbv2_rice_merged", "4.0.0"));
		cropTypes.add(this.getCropType("wheat", "ibdbv2_wheat_merged", "4.0.0"));
		return cropTypes;
	}

	private CropType getCropType(final String cropName, final String dbName, final String version) {
		final CropType cropType = new CropType();
		cropType.setCropName(cropName);
		cropType.setDbName(dbName);
		cropType.setVersion(version);
		return cropType;
	}

	private Project getProject(final Long id, final String programUniqueID, final String projectName, final String cropName) {
		final Project project = new Project();
		project.setProjectId(id);
		project.setProjectName(projectName);
		project.setUniqueID(programUniqueID);
		project.setCropType(this.getCropType(cropName, "ibdbv2_" + cropName + "_merged", "4.0.0"));
		project.setUserId(this.user.getUserid());
		return project;
	}

    private List<ProgramDetailsDto> getProgramDetailsList() {
        final ProgramDetailsDto p1 =
            new ProgramDetailsDto(PROGRAM_UUID_MAIZE, MAIZE, null, null,
                null, this.user.getUserid().toString(), this.user.getName(), ProgramResourceBrapiv2Test.MAIZE);
        final ProgramDetailsDto p2 =
            new ProgramDetailsDto(PROGRAM_UUID_RICE, RICE, null, null,
                null, this.user.getUserid().toString(), this.user.getName(), ProgramResourceBrapiv2Test.RICE);
        final ProgramDetailsDto p3 =
            new ProgramDetailsDto(PROGRAM_UUID_WHEAT, WHEAT, null, null,
                null, this.user.getUserid().toString(), this.user.getName(), ProgramResourceBrapiv2Test.WHEAT);
        return Arrays.asList(p1, p2, p3);
    }
}
