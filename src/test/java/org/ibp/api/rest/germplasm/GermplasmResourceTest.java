
package org.ibp.api.rest.germplasm;

import java.util.List;

import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Before;
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
		public GermplasmDataManager germplasmDataManager() {
			return Mockito.mock(GermplasmDataManager.class);
		}

		@Bean
		@Primary
		public PedigreeService pedigreeService() {
			return Mockito.mock(PedigreeService.class);
		}

		@Bean
		@Primary
		public LocationDataManager locationDataManager() {
			return Mockito.mock(LocationDataManager.class);
		}
	}

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private LocationDataManager locationDataManger;

	private Germplasm germplasm = new Germplasm();
	private String pedigreeString = "Test1/Test2";
	private Method method = new Method();
	private Location location = new Location();

	@Before
	public void beforeEachTest() {
		this.germplasm = new Germplasm();
		this.germplasm.setGid(3);
		this.germplasm.setGpid1(1);
		this.germplasm.setGpid2(2);

		Name n = new Name();
		n.setNval("Test Germplasm");
		this.germplasm.setPreferredName(n);

		this.germplasm.setLocationId(1);
		this.germplasm.setMethodId(1);

		Mockito.when(this.germplasmDataManager.getGermplasmByGID(this.germplasm.getGid())).thenReturn(this.germplasm);

		Mockito.when(this.pedigreeService.getCrossExpansion(Mockito.anyInt(), Mockito.any(CrossExpansionProperties.class))).thenReturn(
				this.pedigreeString);

		Mockito.when(this.germplasmDataManager.getNamesByGID(this.germplasm.getGid(), null, null)).thenReturn(
				Lists.newArrayList(this.germplasm.getPreferredName()));

		this.method.setMname("Test Breeding Method");
		Mockito.when(this.germplasmDataManager.getMethodByID(this.germplasm.getMethodId())).thenReturn(this.method);

		this.location.setLname("Test Location");
		Mockito.when(this.locationDataManger.getLocationByID(this.germplasm.getLocationId())).thenReturn(this.location);
	}

	@Test
	public void testSearchGermplasm() throws Exception {

		String searchString = "Test";
		List<Germplasm> matchingGermplasm = Lists.newArrayList(this.germplasm);
		Mockito.when(this.germplasmDataManager.searchForGermplasm(searchString, Operation.LIKE, false, false))
		.thenReturn(matchingGermplasm);

		this.mockMvc
		.perform(MockMvcRequestBuilders.get("/germplasm/maize/search?q={searchString}", searchString).contentType(this.contentType))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andDo(MockMvcResultHandlers.print())
		.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(matchingGermplasm.size())))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].germplasmId", Matchers.is(this.germplasm.getGid().toString())))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].location", Matchers.is(this.location.getLname())))
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].parent1Id", Matchers.is(this.germplasm.getGpid1().toString())))
		.andExpect(
				MockMvcResultMatchers.jsonPath("$[0].parent1Url",
						Matchers.containsString("/germplasm/maize/" + this.germplasm.getGpid1().toString())))
						.andExpect(MockMvcResultMatchers.jsonPath("$[0].parent2Id", Matchers.is(this.germplasm.getGpid2().toString())))
						.andExpect(
								MockMvcResultMatchers.jsonPath("$[0].parent2Url",
										Matchers.containsString("/germplasm/maize/" + this.germplasm.getGpid2().toString())))
										.andExpect(MockMvcResultMatchers.jsonPath("$[0].pedigreeString", Matchers.is(this.pedigreeString)));

	}

	@Test
	public void testGetGermplasmSummaryById() throws Exception {
		this.mockMvc
				.perform(
						MockMvcRequestBuilders.get("/germplasm/{cropName}/{gid}", this.cropName, this.germplasm.getGid()).contentType(
								this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.germplasmId", Matchers.is(this.germplasm.getGid().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.names", Matchers.contains(this.germplasm.getPreferredName().getNval())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.location", Matchers.is(this.location.getLname())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.breedingMethod", Matchers.is(this.method.getMname())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.parent1Id", Matchers.is(this.germplasm.getGpid1().toString())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.parent1Url",
								Matchers.containsString("/germplasm/maize/" + this.germplasm.getGpid1().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.parent2Id", Matchers.is(this.germplasm.getGpid2().toString())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.parent2Url",
								Matchers.containsString("/germplasm/maize/" + this.germplasm.getGpid2().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pedigreeString", Matchers.is(this.pedigreeString)));

	}
}
