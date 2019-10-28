package org.ibp.api.rest.design;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.hamcrest.Matchers;
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

import java.util.Arrays;
import java.util.List;

public class ExperimentalDesignTypeResourceTest extends ApiUnitTestBase {

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

	@Test
	public void testRetrieveDesignTypes() throws Exception {
		final List<ExperimentDesignType> types = Arrays.asList(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK,
			ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK,
			ExperimentDesignType.ROW_COL,
			ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK,
			ExperimentDesignType.ENTRY_LIST_ORDER,
			ExperimentDesignType.P_REP);
		Mockito.doReturn(types).when(this.experimentalDesignService).getExperimentalDesignTypes();

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/crops/{crop}/experimental-design-types", "maize")
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(types.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].termId", Matchers.is(types.get(0).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].termId", Matchers.is(types.get(1).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[2].termId", Matchers.is(types.get(2).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[3].termId", Matchers.is(types.get(3).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[4].termId", Matchers.is(types.get(4).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[5].termId", Matchers.is(types.get(5).getTermId())));

	}

}
