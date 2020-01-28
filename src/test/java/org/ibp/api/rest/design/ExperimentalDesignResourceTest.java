package org.ibp.api.rest.design;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.design.ExperimentalDesignService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class ExperimentalDesignResourceTest extends ApiUnitTestBase {


	@Autowired
	private ExperimentalDesignService experimentalDesignService;
	@Configuration
	public static class TestConfiguration {


		@Bean
		@Primary
		public ExperimentalDesignService experimentDesignService() {
			return Mockito.mock(ExperimentalDesignService.class);
		}

	}

	protected final String cropName = "maize";

	@Test
	public void testDeleteExperimentDesign() throws Exception {
		final int studyId = 111;
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/crops/{crop}/programs/{programUUID}/studies/{studyId}/experimental-designs", this.cropName, this.programUuid, studyId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.experimentalDesignService).deleteDesign(studyId);
	}

	@Test
	public void testGenerateExperimentDesign() throws Exception {
		final int studyId = 111;
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/crops/{crop}/programs/{programUUID}/studies/{studyId}/experimental-designs/generation", this.cropName, this.programUuid, studyId)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(experimentalDesignInput)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.experimentalDesignService).generateAndSaveDesign(this.cropName, studyId, experimentalDesignInput);
	}

}
