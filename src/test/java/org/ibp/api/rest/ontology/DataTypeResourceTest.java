
package org.ibp.api.rest.ontology;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.oms.Term;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class DataTypeResourceTest extends ApiUnitTestBase {

	@Test
	public void listAllDataTypes() throws Exception {

		List<Term> termList = new ArrayList<>();
		Term term = new Term(DataType.CATEGORICAL_VARIABLE.getId(), "Categorical", "");
		termList.add(term);
		term = new Term(DataType.NUMERIC_VARIABLE.getId(), "Numeric", "");
		termList.add(term);
		term = new Term(DataType.CHARACTER_VARIABLE.getId(), "Character", "");
		termList.add(term);
		term = new Term(DataType.DATE_TIME_VARIABLE.getId(), "Date", "");
		termList.add(term);
		term = new Term(DataType.LOCATION.getId(), "Location", "");
		termList.add(term);
		term = new Term(DataType.PERSON.getId(), "Person", "");
		termList.add(term);
		term = new Term(DataType.STUDY.getId(), "Study", "");
		termList.add(term);
		term = new Term(DataType.DATASET.getId(), "Dataset", "");
		termList.add(term);
		term = new Term(DataType.GERMPLASM_LIST.getId(), "Germplasm", "");
		termList.add(term);
		term = new Term(DataType.BREEDING_METHOD.getId(), "Breeding Method", "");
		termList.add(term);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/datatypes").contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(termList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(termList.get(0).getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(termList.get(0).getName())))
				.andDo(MockMvcResultHandlers.print());
	}
}
