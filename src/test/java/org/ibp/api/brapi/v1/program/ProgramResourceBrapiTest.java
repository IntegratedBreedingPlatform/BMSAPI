package org.ibp.api.brapi.v1.program;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramFilters;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.program.ProgramService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ProgramResourceBrapiTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public ProgramService programService() {
			return Mockito.mock(ProgramService.class);
		}
	}

	@Autowired
	private ProgramService programService;


	@Autowired
	private WorkbenchDataManager workbenchDataManager;


	@Test
	public void testListProgramsBadCrop() throws Exception {
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(getAllCrops());

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize2/brapi/v1/programs?pageNumber=1&pageSize=10&programName=&abbreviation=").contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isNotFound()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.metadata.status.message", Matchers.is("the crop doesn't exist"))); //

	}

	@Test
	public void testListProgram() throws Exception {
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(getAllCrops());
		Mockito.when(this.programService.countProgramsByFilter(Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(2L);

		final List<ProgramDetailsDto> programDetailsDtoList = getProgramdetails();

		Mockito.when(this.programService.getProgramsByFilter(Mockito.anyInt(), Mockito.anyInt(),
			Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(programDetailsDtoList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/programs?pageNumber=1&pageSize=10&programName=&abbreviation=").contentType(this.contentType)) //
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
	public void testListProgramFilterByname() throws Exception {
		Mockito.when(this.workbenchDataManager.getInstalledCropDatabses()).thenReturn(getAllCrops());
		Mockito.when(this.programService.countProgramsByFilter(Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(1L);

		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		programDetailsDtoList.add(new ProgramDetailsDto(11, "Rice", null, null, null));

		Mockito.when(this.programService.getProgramsByFilter(Mockito.anyInt(), Mockito.anyInt(),
			Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(programDetailsDtoList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/programs?pageNumber=1&pageSize=10&programName=Rice&abbreviation=").contentType(this.contentType)) //
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
		Mockito.when(this.programService.countProgramsByFilter(Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(1L);

		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		programDetailsDtoList.add(new ProgramDetailsDto(11, "Rice", null, null, null));

		Mockito.when(this.programService.getProgramsByFilter(Mockito.anyInt(), Mockito.anyInt(),
			Mockito.anyMapOf(ProgramFilters.class, Object.class))).thenReturn(programDetailsDtoList);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/programs?pageNumber=1&pageSize=10&programName=&abbreviation=AAAB").contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isNotFound()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.metadata.status.message", Matchers.is("not found programs"))); //
	}

	private List<ProgramDetailsDto> getProgramdetails() {
		final List<ProgramDetailsDto> programDetailsDtoList = new ArrayList<>();
		programDetailsDtoList.add(new ProgramDetailsDto(10, "Wheat", null, null, null));
		programDetailsDtoList.add(new ProgramDetailsDto(11, "Rice", null, null, null));
		return programDetailsDtoList;
	}

	private List<CropType> getAllCrops() {
		List<CropType> cropTypes = new ArrayList<>();
		CropType cropTypeMaize = new CropType();
		cropTypeMaize.setCropName("maize");
		cropTypeMaize.setDbName("ibdbv2_maize_merged");
		cropTypeMaize.setVersion("4.0.0");
		cropTypes.add(cropTypeMaize);

		CropType cropTypeRice = new CropType();
		cropTypeRice.setCropName("rice");
		cropTypeRice.setDbName("ibdbv2_rice_merged");
		cropTypeRice.setVersion("4.0.0");
		cropTypes.add(cropTypeRice);
		return cropTypes;
	}

}
