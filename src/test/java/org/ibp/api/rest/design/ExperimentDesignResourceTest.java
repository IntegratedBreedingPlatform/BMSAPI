package org.ibp.api.rest.design;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.design.ExperimentDesignService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class ExperimentDesignResourceTest extends ApiUnitTestBase {


	@Autowired
	private ExperimentDesignService experimentDesignService;
	@Configuration
	public static class TestConfiguration {


		@Bean
		@Primary
		public ExperimentDesignService experimentDesignService() {
			return Mockito.mock(ExperimentDesignService.class);
		}

	}

	protected final String cropName = "maize";

	@Test
	public void testDeleteExperimentDesign() throws Exception {
		final int studyId = 111;
		this.mockMvc
			.perform(MockMvcRequestBuilders.delete("/crops/{crop}/studies/{studyId}/design", this.cropName, studyId)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.experimentDesignService).deleteDesign(studyId);
	}

	@Test
	public void testGenerateExperimentDesign() throws Exception {
		final int studyId = 111;
		final ExperimentDesignInput experimentDesignInput = new ExperimentDesignInput();
		experimentDesignInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		this.mockMvc
			.perform(MockMvcRequestBuilders.post("/crops/{crop}/studies/{studyId}/design", this.cropName, studyId)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(experimentDesignInput)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(this.experimentDesignService).generateAndSaveDesign(this.cropName, studyId, experimentDesignInput);
	}

}
