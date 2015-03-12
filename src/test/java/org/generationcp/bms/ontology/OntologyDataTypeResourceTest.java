package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.ArrayList;

import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class OntologyDataTypeResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }
    }

    @Autowired
    private OntologyManagerService ontologyManagerService;

    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void listAllDataTypes() throws Exception {

        String cropName = "maize";

        List<Term> termList = new ArrayList<>();
        Term term = new Term(1, "Categorical", "");
        termList.add(term);
        term = new Term(2, "Numeric", "");
        termList.add(term);
        term = new Term(3, "Character", "");
        termList.add(term);
        term = new Term(4, "Date", "");
        termList.add(term);

        Mockito.doReturn(termList).when(ontologyManagerService).getDataTypes();

        mockMvc.perform(get("/ontology/{cropname}/datatypes", cropName).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(termList.size())))
                .andExpect(jsonPath("$[0].id", is(termList.get(0).getId())))
                .andExpect(jsonPath("$[0].name", is(termList.get(0).getName())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getDataTypes();
    }
}
