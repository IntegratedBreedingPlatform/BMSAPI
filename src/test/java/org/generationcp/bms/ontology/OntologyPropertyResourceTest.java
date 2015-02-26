package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.builders.PropertyBuilder;
import org.generationcp.middleware.domain.oms.Property;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;


public class OntologyPropertyResourceTest extends ApiUnitTestBase {

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
    public void listAllProperties() throws Exception {

        String cropName = "maize";

        List<Property> propertyList = new ArrayList<>();
        propertyList.add(new PropertyBuilder().build(1, "p1", "d1"));
        propertyList.add(new PropertyBuilder().build(2, "p2", "d2"));
        propertyList.add(new PropertyBuilder().build(3, "p3", "d3"));

        Mockito.doReturn(propertyList).when(ontologyService).getAllProperties();

        mockMvc.perform(get("/ontology/{cropname}/properties/list", cropName).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(propertyList.size())))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(propertyList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(propertyList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllProperties();
    }


}
