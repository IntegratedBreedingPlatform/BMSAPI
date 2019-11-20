package org.ibp.api.rest.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.java.study.StudyInstanceService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Random;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class StudyInstanceResourceTest extends ApiUnitTestBase {

	public static final int BOUND = 10;

	@Autowired
	private StudyInstanceService studyInstanceService;

	private final Random random = new Random();

	@Test
	public void testCreateStudyInstance() throws Exception {

		final int studyId = new Random().nextInt();
		final int instanceNumber = 2;

		final StudyInstance studyInstance = new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
			RandomStringUtils.random(BOUND),
			RandomStringUtils.random(
				BOUND),
			instanceNumber,
			RandomStringUtils.random(BOUND), false);
		when(this.studyInstanceService.createStudyInstance(CropType.CropEnum.MAIZE.name().toLowerCase(), studyId))
			.thenReturn(studyInstance);

		this.mockMvc.perform(MockMvcRequestBuilders
			.post("/crops/{cropname}/studies/{studyId}/instances/generation", CropType.CropEnum.MAIZE.name().toLowerCase(), studyId)
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$.instanceDbId", Matchers.is(studyInstance.getInstanceDbId())))
			.andExpect(jsonPath("$.experimentId", Matchers.is(studyInstance.getExperimentId())))
			.andExpect(jsonPath("$.instanceNumber", Matchers.is(studyInstance.getInstanceNumber())))
			.andExpect(jsonPath("$.locationName", Matchers.is(studyInstance.getLocationName())))
			.andExpect(jsonPath("$.locationAbbreviation", Matchers.is(studyInstance.getLocationAbbreviation())))
			.andExpect(jsonPath("$.hasFieldmap", Matchers.is(studyInstance.getHasFieldmap())))
			.andExpect(jsonPath("$.customLocationAbbreviation", Matchers.is(studyInstance.getCustomLocationAbbreviation())));

	}

	@Test
	public void testGetStudyInstances() throws Exception {

		final int studyId = new Random().nextInt();
		final int instanceNumber = 1;

		final StudyInstance studyInstance = new StudyInstance(this.random.nextInt(BOUND), this.random.nextInt(BOUND),
			RandomStringUtils.random(BOUND),
			RandomStringUtils.random(
				BOUND),
			instanceNumber,
			RandomStringUtils.random(BOUND), false);
		when(this.studyInstanceService.getStudyInstances(studyId))
			.thenReturn(Arrays.asList(studyInstance));

		this.mockMvc.perform(MockMvcRequestBuilders
			.get("/crops/{cropname}/studies/{studyId}/instances", CropType.CropEnum.MAIZE.name().toLowerCase(), studyId)
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$[0].instanceDbId", Matchers.is(studyInstance.getInstanceDbId())))
			.andExpect(jsonPath("$[0].experimentId", Matchers.is(studyInstance.getExperimentId())))
			.andExpect(jsonPath("$[0].instanceNumber", Matchers.is(studyInstance.getInstanceNumber())))
			.andExpect(jsonPath("$[0].locationName", Matchers.is(studyInstance.getLocationName())))
			.andExpect(jsonPath("$[0].locationAbbreviation", Matchers.is(studyInstance.getLocationAbbreviation())))
			.andExpect(jsonPath("$[0].hasFieldmap", Matchers.is(studyInstance.getHasFieldmap())))
			.andExpect(jsonPath("$[0].customLocationAbbreviation", Matchers.is(studyInstance.getCustomLocationAbbreviation())));

	}

	@Test
	public void testDeleteStudyInstance() throws Exception {

		final Random random = new Random();
		final int studyId = random.nextInt();
		final int instanceId = random.nextInt();

		this.mockMvc.perform(MockMvcRequestBuilders
			.delete("/crops/{cropname}/studies/{studyId}/instances/{instanceId}", CropType.CropEnum.MAIZE.name().toLowerCase(), studyId, instanceId)
			.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());
		Mockito.verify(this.studyInstanceService).deleteStudyInstance(studyId, instanceId);

	}

}
