package org.ibp.api.brapi.v1.program;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramFilters;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ProgramResourceBrapiTest extends ApiUnitTestBase {

	@Test
	public void testListProgramsBadCrop() throws Exception {
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(getAllCrops());

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize2/brapi/v1/programs?pageNumber=1&pageSize=10&programName=&abbreviation=")
			.contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isNotFound()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.metadata.status.message", Matchers.is("crop maize2 doesn't exist"))); //

	}

	@Test
	public void testListProgram() throws Exception {
		final List<ProgramDetailsDto> programDetailsDtoList = getProgramDetails();
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(getAllCrops());
		Mockito.when(this.workbenchDataManager.countProjectsByFilter(Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(2L);
		final List<Project> projectList = getProjectList();
		Mockito.when(
			this.workbenchDataManager.getProjects(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyMapOf(ProgramFilters.class, Object.class)))
			.thenReturn(projectList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/programs?pageNumber=1&pageSize=10&programName=&abbreviation=")
			.contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(programDetailsDtoList.size()))) //
			.andExpect(jsonPath("$.result.data[0].programDbId", Matchers.is(10))) //
			.andExpect(jsonPath("$.result.data[0].name", Matchers.is("Wheat"))) //
			.andExpect(jsonPath("$.result.data[1].programDbId", Matchers.is(11))) //
			.andExpect(jsonPath("$.result.data[1].name", Matchers.is("Rice"))) //
		;
	}

	@Test
	public void testListProgramFilterByName() throws Exception {
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(getAllCrops());
		Mockito.when(this.workbenchDataManager.countProjectsByFilter(Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(1L);

		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		programDetailsDtoList.add(new ProgramDetailsDto(11, "Rice", null, null, null));
		final List<Project> projectList = new ArrayList<>();
		projectList.add(getProject(11L, "Rice"));

		Mockito.when(
			this.workbenchDataManager.getProjects(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyMapOf(ProgramFilters.class, Object.class)))
			.thenReturn(projectList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/programs?pageNumber=1&pageSize=10&programName=Rice&abbreviation=")
			.contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.result.data", IsCollectionWithSize.hasSize(programDetailsDtoList.size()))) //
			.andExpect(jsonPath("$.result.data[0].programDbId", Matchers.is(11))) //
			.andExpect(jsonPath("$.result.data[0].name", Matchers.is("Rice"))) //
		;
	}

	@Test
	public void testListProgramFilterByAbbreviation() throws Exception {
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(getAllCrops());
		Mockito.when(this.workbenchDataManager.countProjectsByFilter(Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(1L);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/programs?pageNumber=1&pageSize=10&programName=&abbreviation=AAAB")
			.contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isNotFound()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.metadata.status.message", Matchers.is("program not found."))); //
	}

	private List<ProgramDetailsDto> getProgramDetails() {
		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		programDetailsDtoList.add(new ProgramDetailsDto(10, "Wheat", null, null, null));
		programDetailsDtoList.add(new ProgramDetailsDto(11, "Rice", null, null, null));
		return programDetailsDtoList;
	}

	private List<CropType> getAllCrops() {
		List<CropType> cropTypes = new ArrayList<>();
		cropTypes.add(getCropType("maize","ibdbv2_maize_merged","4.0.0"));
		cropTypes.add(getCropType("rice","ibdbv2_rice_merged","4.0.0"));
		return cropTypes;
	}

	private CropType getCropType(final String cropName, final String dbName, final String version) {
		final CropType cropType = new CropType();
		cropType.setCropName(cropName);
		cropType.setDbName(dbName);
		cropType.setVersion(version);
		return cropType;
	}

	private List<Project> getProjectList() {
		final List<Project> projectList = new ArrayList<>();
		projectList.add(getProject(10L, "Wheat"));
		projectList.add(getProject(11L, "Rice"));
		return projectList;
	}

	private Project getProject(final Long id, final String projectName) {
		final Project project = new Project();
		project.setProjectId(id);
		project.setProjectName(projectName);
		return project;
	}
}
