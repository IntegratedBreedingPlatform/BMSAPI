
package org.ibp.api.rest.germplasm;

import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.germplasm.GermplasmName;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.java.germplasm.GermplasmService;
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

		@Bean
		@Primary
		public GermplasmService germplasmService() {
			return Mockito.mock(GermplasmService.class);
		}
	}

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private LocationDataManager locationDataManger;

	@Autowired
	private GermplasmService germplasmService;

	private Germplasm germplasm = new Germplasm();
	private String pedigreeString = "Test1/Test2";
	private Method method = new Method();
	private Location location = new Location();
	private UserDefinedField nameType = new UserDefinedField(0);

	@Before
	public void beforeEachTest() {
		this.germplasm = new Germplasm();
		this.germplasm.setGid(3);
		this.germplasm.setGpid1(1);
		this.germplasm.setGpid2(2);

		Name n = new Name();
		n.setTypeId(1);
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

		this.nameType.setFcode("SELHIS");
		this.nameType.setFname("Selection History");
		Mockito.when(this.germplasmDataManager.getUserDefinedFieldByID(n.getTypeId())).thenReturn(this.nameType);

	}

	@Test
	public void testSearchGermplasm() throws Exception {

		String searchString = "Test";
		Mockito.doReturn(1).when(this.germplasmService).searchGermplasmCount(searchString);
		Mockito.doReturn(Lists.newArrayList(this.createGermplasmSummary())).when(this.germplasmService).searchGermplasm(searchString, 1,
			PagedResult.DEFAULT_PAGE_SIZE);
		this.mockMvc
		.perform(MockMvcRequestBuilders.get("/germplasm/maize/search?q={searchString}", searchString).contentType(this.contentType))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults", IsCollectionWithSize.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageSize", Matchers.is(PagedResult.DEFAULT_PAGE_SIZE)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalResults", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.firstPage", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.lastPage", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.hasNextPage", Matchers.is(false)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.hasPreviousPage", Matchers.is(false)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].germplasmId", Matchers.is(this.germplasm.getGid().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].names[0].name",
						Matchers.is(this.germplasm.getPreferredName().getNval())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].names[0].nameTypeCode", Matchers.is(this.nameType.getFcode())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].names[0].nameTypeDescription",
						Matchers.is(this.nameType.getFname())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].location", Matchers.is(this.location.getLname())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].parent1Id", Matchers.is(this.germplasm.getGpid1().toString())))
		.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0].parent1Url",
						Matchers.containsString("/germplasm/maize/" + this.germplasm.getGpid1().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].parent2Id", Matchers.is(this.germplasm.getGpid2().toString())))
						.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0].parent2Url",
										Matchers.containsString("/germplasm/maize/" + this.germplasm.getGpid2().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0].pedigreeString", Matchers.is(this.pedigreeString)));

	}

	@Test
	public void testGetGermplasmSummaryById() throws Exception {
		final GermplasmSummary summary = createGermplasmSummary();

		Mockito.doReturn(summary).when(this.germplasmService).getGermplasm(germplasm.getGid().toString());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.get("/germplasm/{cropName}/{gid}", this.cropName, this.germplasm.getGid()).contentType(
								this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.germplasmId", Matchers.is(this.germplasm.getGid().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.names[0].name", Matchers.is(this.germplasm.getPreferredName().getNval())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.names[0].nameTypeCode", Matchers.is(this.nameType.getFcode())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.names[0].nameTypeDescription", Matchers.is(this.nameType.getFname())))
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

	private GermplasmSummary createGermplasmSummary() {
		final GermplasmSummary summary = new GermplasmSummary();
		summary.setGermplasmId(this.germplasm.getGid().toString());
		summary.setLocation(this.location.getLname());
		summary.setBreedingMethod(this.method.getMname());
		summary.setParent1Id(this.germplasm.getGpid1().toString());
		summary.setParent2Id(this.germplasm.getGpid2().toString());
		summary.setPedigreeString(this.pedigreeString);

		final GermplasmName name = new GermplasmName();
		name.setName(this.germplasm.getPreferredName().getNval());
		name.setNameTypeCode(this.nameType.getFcode());
		name.setNameTypeDescription(this.nameType.getFname());
		summary.addNames(Lists.newArrayList(name));
		return summary;
	}
}
