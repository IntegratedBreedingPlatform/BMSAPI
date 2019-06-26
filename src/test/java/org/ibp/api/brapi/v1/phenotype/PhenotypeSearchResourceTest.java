package org.ibp.api.brapi.v1.phenotype;

 import java.util.List;

 import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchRequestDTO;
import org.generationcp.middleware.service.api.phenotype.PhenotypeSearchDTO;
import org.ibp.api.java.study.StudyService;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

 import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

 import java.util.ArrayList;

 public class PhenotypeSearchResourceTest extends ApiUnitTestBase {

 	@Autowired
	private StudyService studyService;

 	@Test
	public void testSearchPhenotypes() throws Exception {

 		final PhenotypeSearchRequestDTO phenotypeSearchRequestDTO = new PhenotypeSearchRequestDTO();
		phenotypeSearchRequestDTO.setObservationLevel("plot");
		phenotypeSearchRequestDTO.setPage(0);
		phenotypeSearchRequestDTO.setPageSize(2);
		final List<PhenotypeSearchDTO> phenotypeList = this.getPhenotypeList();

 		final UriComponents uriComponents = UriComponentsBuilder.newInstance().path("/maize/brapi/v1/phenotypes-search").build().encode();

 		Mockito.when(this.studyService.countPhenotypes(phenotypeSearchRequestDTO)).thenReturn(1L);

 		Mockito.when(this.studyService.searchPhenotypes(org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(),
			org.mockito.Matchers.any(PhenotypeSearchRequestDTO.class))).thenReturn(phenotypeList);

 		this.mockMvc.perform(MockMvcRequestBuilders.post(uriComponents.toUriString()).contentType(this.contentType)
			.content(this.convertObjectToByte(phenotypeSearchRequestDTO))).andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(phenotypeList.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].observationLevel", Matchers.is("PLOT")))
			.andExpect(
				MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmDbId", Matchers.is(phenotypeList.get(0).getGermplasmDbId())))
			.andDo(MockMvcResultHandlers.print());
	}

 	private PhenotypeSearchDTO getPhenotype(final String germplasmDbId, final String observationLevel) {
		final PhenotypeSearchDTO phenotype = new PhenotypeSearchDTO();

 		phenotype.setGermplasmDbId(germplasmDbId);
		phenotype.setObservationLevel(observationLevel);
		return phenotype;
	}

 	private List<PhenotypeSearchDTO> getPhenotypeList() {
		final List<PhenotypeSearchDTO> phenotypeList = new ArrayList<>();
		phenotypeList.add(this.getPhenotype("1", "PLOT"));
		phenotypeList.add(this.getPhenotype("1", "PLOT"));
		return phenotypeList;
	}

 }