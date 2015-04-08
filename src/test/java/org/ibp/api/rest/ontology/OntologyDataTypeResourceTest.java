package org.ibp.api.rest.ontology;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.Term;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class OntologyDataTypeResourceTest extends ApiUnitTestBase {

	@Test
	public void listAllDataTypes() throws Exception {

		List<Term> termList = new ArrayList<>();
		Term term = new Term(1048, "Categorical", "");
		termList.add(term);
		term = new Term(1110, "Numeric", "");
		termList.add(term);
		term = new Term(1117, "Character", "");
		termList.add(term);
		term = new Term(1120, "Date", "");
		termList.add(term);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/datatypes")
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(termList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(termList.get(0).getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(termList.get(0).getName())))
				.andDo(MockMvcResultHandlers.print());
	}
}
