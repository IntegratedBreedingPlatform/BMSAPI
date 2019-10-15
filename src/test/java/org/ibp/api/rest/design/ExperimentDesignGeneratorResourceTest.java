package org.ibp.api.rest.design;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.design.DesignLicenseService;
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

public class ExperimentDesignGeneratorResourceTest extends ApiUnitTestBase {

	@Autowired
	private DesignLicenseService designLicenseService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public DesignLicenseService designLicenseService() {
			return Mockito.mock(DesignLicenseService.class);
		}
	}

	@Test
	public void testRetrieveDesignTypes() throws Exception {
		final List<ExperimentDesignType> types = Arrays.asList(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK,
			ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK,
			ExperimentDesignType.ROW_COL,
			ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK,
			ExperimentDesignType.CUSTOM_IMPORT,
			ExperimentDesignType.ENTRY_LIST_ORDER,
			ExperimentDesignType.P_REP);

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/design/types")
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(types.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].termId", Matchers.is(types.get(0).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].termId", Matchers.is(types.get(1).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[2].termId", Matchers.is(types.get(2).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[3].termId", Matchers.is(types.get(3).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[4].termId", Matchers.is(types.get(4).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[5].termId", Matchers.is(types.get(5).getTermId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[6].termId", Matchers.is(types.get(6).getTermId())));

	}

	@Test
	public void testRetrieveCheckInsertionManners() throws Exception {

		this.mockMvc
			.perform(MockMvcRequestBuilders.get("/design/checks/insertionManners")
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(2)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is("8414")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is("1")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is("Insert each check in turn")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is("8415")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].name", Matchers.is("2")))
			.andExpect(MockMvcResultMatchers.jsonPath("$[1].description", Matchers.is("Insert all checks at each position")));
	}

	@Test
	public void testCountExpiryDays() throws Exception {
		final int expiryDays = 101;
		Mockito.when(this.designLicenseService.getExpiryDays()).thenReturn(expiryDays);

		this.mockMvc
			.perform(MockMvcRequestBuilders.head("/design/generator/license/expiryDays")
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(expiryDays)));
	}

}
