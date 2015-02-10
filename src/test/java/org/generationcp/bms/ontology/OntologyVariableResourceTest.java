package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class OntologyVariableResourceTest extends ApiUnitTestBase {

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
    public void listAllStandardVariables() throws Exception {

        Term scale = new Term();
        scale.setId(1);
        scale.setName("scale");
        scale.setDefinition("definition");
        Term property = new Term();
        property.setId(2);

        Set<StandardVariable> standardVariables = new HashSet<>();
        StandardVariable standardVariable = new StandardVariable();
        standardVariable.setId(1);
        standardVariable.setCropOntologyId("22");
        standardVariable.setScale(scale);
        standardVariable.setProperty(property);

        standardVariables.add(standardVariable);

        Mockito.doReturn(standardVariables).when(ontologyService).getAllStandardVariables();

        mockMvc.perform(get("/ontology/variables/").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(standardVariable.getId())))
                .andExpect(jsonPath("$[0].cropOntologyId", is(standardVariable.getCropOntologyId())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllStandardVariables();
    }

    @Test
    public void getStandardVariablesByPropertyId() throws Exception{

        Term scale = new Term();
        scale.setId(2);
        scale.setName("scale");
        scale.setDefinition("definition");
        Term property = new Term();
        property.setId(1);

        List<StandardVariable> standardVariables = new ArrayList<>();
        StandardVariable standardVariable = new StandardVariable();
        standardVariable.setId(11);
        standardVariable.setCropOntologyId("22");
        standardVariable.setScale(scale);
        standardVariable.setProperty(property);

        standardVariables.add(standardVariable);

        Mockito.doReturn(standardVariables).when(ontologyService).getStandardVariablesByProperty(1);

        mockMvc.perform(get("/ontology/variables/property/{id}", 1).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(standardVariable.getId())))
                .andExpect(jsonPath("$[0].cropOntologyId", is(standardVariable.getCropOntologyId())))
                .andDo(print());

        verify(ontologyService, times(1)).getStandardVariablesByProperty(1);
    }

    @Test
    public void getStandardVariablesByFilter() throws Exception{
        Term scale = new Term();
        scale.setId(2);
        scale.setName("scale");
        scale.setDefinition("definition");
        Term property = new Term();
        property.setId(1);

        List<StandardVariable> standardVariables = new ArrayList<>();
        StandardVariable standardVariable = new StandardVariable();
        standardVariable.setId(11);
        standardVariable.setCropOntologyId("22");
        standardVariable.setScale(scale);
        standardVariable.setProperty(property);

        standardVariables.add(standardVariable);

        Mockito.doReturn(standardVariables).when(ontologyService).getStandardVariables("scale");

        mockMvc.perform(get("/ontology/variables/filter/{text}", "scale").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(standardVariable.getId())))
                .andExpect(jsonPath("$[0].cropOntologyId", is(standardVariable.getCropOntologyId())))
                .andDo(print());

        verify(ontologyService, times(1)).getStandardVariables("scale");
    }
}
