package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.domain.oms.TraitClassReference;
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

        List<TraitClassReference> referenceClassList = new ArrayList<>();
        TraitClassReference referenceClass = new TraitClassReference(1, "Abiotic Stress");
        referenceClassList.add(referenceClass);
        referenceClass = new TraitClassReference(2, "Agronomic");
        referenceClassList.add(referenceClass);
        referenceClass = new TraitClassReference(3, "Biotic Stress");
        referenceClassList.add(referenceClass);

        Mockito.doReturn(referenceClassList).when(ontologyService).getAllTraitClass();

        mockMvc.perform(get("/ontology/{cropname}/classes/list", cropName).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(referenceClassList.size())))
                .andExpect(jsonPath("$[0]", is(referenceClassList.get(0).getName())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllTraitClass();
    }
}
