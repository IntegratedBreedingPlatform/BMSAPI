package org.ibp.api.rest.germplasm;

import java.util.List;

import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;


public class GermplasmResourceTest extends ApiUnitTestBase {
	
	@Configuration
	public static class TestConfiguration {
		@Bean
		@Primary
		public GermplasmService getGermplasmService() {
			return Mockito.mock(GermplasmService.class);
		}
	}
	
	@Autowired
	private GermplasmService germplasmService;
	
	@Test
	public void testSearchGermplasm() throws Exception {
		
		GermplasmSummary summary = createTestGermplasmSummary();
		
		List<GermplasmSummary> matchingGermplasm = Lists.newArrayList(summary);
		Mockito.when(this.germplasmService.searchGermplasm(Mockito.anyString())).thenReturn(matchingGermplasm);
		
		this.mockMvc.perform(MockMvcRequestBuilders.get("/germplasm/maize/search?q=CML")
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(matchingGermplasm.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].germplasmId", Matchers.is(summary.getGermplasmId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].location", Matchers.is(summary.getLocation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].parent1Id", Matchers.is(summary.getParent1Id())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].parent1Url", Matchers.containsString("/germplasm/maize/" + summary.getParent1Id())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].parent2Id", Matchers.is(summary.getParent2Id())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].parent2Url",  Matchers.containsString("/germplasm/maize/" + summary.getParent2Id())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].pedigreeString", Matchers.is(summary.getPedigreeString())));
		
	}
	
	@Test
	public void testGetGermplasmSummaryById() throws Exception {
		
		GermplasmSummary summary = createTestGermplasmSummary();
		
		Mockito.when(this.germplasmService.getGermplasm(Mockito.anyString())).thenReturn(summary);
		
		this.mockMvc.perform(MockMvcRequestBuilders.get("/germplasm/maize/85")
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.germplasmId", Matchers.is(summary.getGermplasmId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.location", Matchers.is(summary.getLocation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.parent1Id", Matchers.is(summary.getParent1Id())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.parent1Url", Matchers.containsString("/germplasm/maize/" + summary.getParent1Id())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.parent2Id", Matchers.is(summary.getParent2Id())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.parent2Url",  Matchers.containsString("/germplasm/maize/" + summary.getParent2Id())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pedigreeString", Matchers.is(summary.getPedigreeString())));
		
	}

	private GermplasmSummary createTestGermplasmSummary() {
		GermplasmSummary summary = new GermplasmSummary();
		summary.setGermplasmId("85");
		summary.setLocation("Mexico");
		summary.setParent1Id("1");
		summary.setParent2Id("2");
		summary.setPedigreeString("CML1/CML2");
		return summary;
	}
}
