package org.ibp.api.rest.ontology;

import org.generationcp.middleware.domain.oms.Term;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OntologyDataTypeResourceTest extends ApiUnitTestBase {

    @Test
    public void listAllDataTypes() throws Exception {

        String cropName = "maize";

        List<Term> termList = new ArrayList<>();
        Term term = new Term(1048, "Categorical", "");
        termList.add(term);
        term = new Term(1110, "Numeric", "");
        termList.add(term);
        term = new Term(1117, "Character", "");
        termList.add(term);
        term = new Term(1120, "Date", "");
        termList.add(term);

        mockMvc.perform(get("/ontology/{cropname}/datatypes", cropName).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(termList.size())))
                .andExpect(jsonPath("$[0].id", is(termList.get(0).getId())))
                .andExpect(jsonPath("$[0].name", is(termList.get(0).getName())))
                .andDo(print());
    }
}
