package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.oms.Term;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class OntologyClassResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyService ontologyService() {
            return Mockito.mock(OntologyService.class);
        }
    }

    @Autowired
    private OntologyService ontologyService;

    @Before
    public void reset(){
        Mockito.reset(ontologyService);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void listAllClasses() throws Exception {

        String cropName = "maize";

        List<Term> termList = new ArrayList<>();
        Term term = new Term(1, "Abiotic Stress","");
        termList.add(term);
        term = new Term(2, "Agronomic","");
        termList.add(term);
        term = new Term(3, "Biotic Stress","");
        termList.add(term);

        Mockito.doReturn(termList).when(ontologyService).getAllTraitClass();

        mockMvc.perform(get("/ontology/{cropname}/classes", cropName).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(termList.size())))
                .andExpect(jsonPath("$[0]", is(termList.get(0).getName())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllTraitClass();
    }
}
