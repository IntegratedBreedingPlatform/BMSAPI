package org.ibp.api.brapi.v1.germplasm;

import org.generationcp.middleware.domain.germplasm.ParentType;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class GermplasmResourceBrapiTest extends ApiUnitTestBase {

	private static Locale locale = Locale.getDefault();


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public GermplasmService germplasmService() {
			return Mockito.mock(GermplasmService.class);
		}

	}


	@Autowired
	private GermplasmService germplasmService;

	@Test
	public void testGetPedigree() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);

		final PedigreeDTO pedigreeDTO = new PedigreeDTO();
		pedigreeDTO.setGermplasmDbId(gid);
		pedigreeDTO.setPedigree(randomAlphanumeric(255));

		when(this.germplasmService.getPedigree(gid, null, true)).thenReturn(pedigreeDTO);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/germplasm/" + germplasmDbId + "/pedigree") //
			.contentType(this.contentType) //
			.locale(locale)) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.result.germplasmDbId", is(pedigreeDTO.getGermplasmDbId()))) //
			.andExpect(jsonPath("$.result.pedigree", is(pedigreeDTO.getPedigree()))) //
		;
	}

	@Test
	public void testGetProgeny() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);

		final ProgenyDTO progenyDTO= new ProgenyDTO();
		progenyDTO.setGermplasmDbId(gid);
		final List<ProgenyDTO.Progeny> progenies = new ArrayList<>();
		final ProgenyDTO.Progeny progeny = new ProgenyDTO.Progeny();
		progeny.setGermplasmDbId(nextInt());
		progeny.setParentType(ParentType.MALE.name());
		progenies.add(progeny);
		progenyDTO.setProgeny(progenies);

		when(this.germplasmService.getProgeny(gid)).thenReturn(progenyDTO);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/germplasm/" + germplasmDbId + "/progeny") //
			.contentType(this.contentType) //
			.locale(locale)) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$.result.germplasmDbId", is(gid))) //
			.andExpect(jsonPath("$.result.progeny[0].germplasmDbId", is(progenyDTO.getProgeny().get(0).getGermplasmDbId()))) //
			.andExpect(jsonPath("$.result.progeny[0].parentType", is(progenyDTO.getProgeny().get(0).getParentType()))) //
		;
	}
}
