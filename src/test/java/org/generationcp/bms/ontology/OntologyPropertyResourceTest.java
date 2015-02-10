package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

        Term term = new Term();
        term.setId(1);

        List<Property> propertyList = new ArrayList<>();
        Property property = new Property(term);
        property.setId(1);
        property.setName("propertyName");
        property.setDefinition("propertyDefinition");

        propertyList.add(property);

        Mockito.doReturn(propertyList).when(ontologyService).getAllProperties();

        mockMvc.perform(get("/ontology/properties/list").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(propertyList.get(0).getName())))
                .andExpect(jsonPath("$[0].definition", is(propertyList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllProperties();
    }

    @Test
    public void getPropertyById() throws Exception{

        Term term = new Term();
        term.setId(1);

        Property property = new Property(term);
        property.setId(1);
        property.setName("propertyName");
        property.setDefinition("propertyDefinition");

        Mockito.doReturn(property).when(ontologyService).getProperty(1);

        mockMvc.perform(get("/ontology/properties/{id}", 1).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(property.getName())))
                .andExpect(jsonPath("$.definition", is(property.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getProperty(1);
    }

    @Test
    public void getPropertyByFilter() throws Exception{

        Term term = new Term();
        term.setId(1);

        Property property = new Property(term);
        property.setId(1);
        property.setName("propertyName");
        property.setDefinition("propertyDefinition");

        Mockito.doReturn(property).when(ontologyService).getProperty("propertyName");

        mockMvc.perform(get("/ontology/properties/filter/{text}", "propertyName").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(property.getName())))
                .andExpect(jsonPath("$.definition", is(property.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getProperty("propertyName");
    }
}
