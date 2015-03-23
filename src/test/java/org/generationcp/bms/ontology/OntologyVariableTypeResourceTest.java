package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class OntologyVariableTypeResourceTest extends ApiUnitTestBase {

    @Before
    public void reset(){

    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void listAllVariableTypes() throws Exception {

        String cropName = "maize";

        mockMvc.perform(get("/ontology/{cropname}/variableTypes", cropName).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[0].name", is("Analysis")))
                .andDo(print());

    }
}
