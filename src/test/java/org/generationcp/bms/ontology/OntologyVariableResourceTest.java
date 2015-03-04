package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class OntologyVariableResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyService ontologyService() {
            return Mockito.mock(OntologyService.class);
        }
    }

    private final String scaleName = "scaleName";
    private final String scaleDefinition = "scaleDefinition";
    private final String propertyName = "Abiotic Stress";
    private final String propertyDescription = "Description";

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

        String cropName = "rice";

        Set<StandardVariable> standardVariables = new HashSet<>();
        StandardVariable standardVariable = new StandardVariableBuilder()
                .id(1)
                .name("standardVariable")
                .description("standardDescription")
                .cropOntologyId("C21")
                .setScale(10,scaleName, scaleDefinition)
                .setProperty(11, propertyName, propertyDescription)
                .build();

        standardVariables.add(standardVariable);

        Mockito.doReturn(standardVariables).when(ontologyService).getAllStandardVariables();

        mockMvc.perform(get("/ontology/{cropname}/variables/list",cropName).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(standardVariable.getId())))
                .andExpect(jsonPath("$[0].cropOntologyId", is(standardVariable.getCropOntologyId())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllStandardVariables();
    }

    @Test
    public void getStandardVariablesByPropertyId() throws Exception{

        String cropName = "maize";

        List<StandardVariable> standardVariables = new ArrayList<>();
        StandardVariable standardVariable = new StandardVariableBuilder()
                .id(1)
                .name("standardVariable")
                .description("standardDescription")
                .cropOntologyId("C21")
                .setScale(10,scaleName, scaleDefinition)
                .setProperty(11, propertyName, propertyDescription)
                .build();

        standardVariables.add(standardVariable);

        Mockito.doReturn(standardVariables).when(ontologyService).getStandardVariablesByProperty(11);

        mockMvc.perform(get("/ontology/{cropname}/variables/property/{id}", cropName, 11).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(standardVariable.getId())))
                .andExpect(jsonPath("$[0].cropOntologyId", is(standardVariable.getCropOntologyId())))
                .andDo(print());

        verify(ontologyService, times(1)).getStandardVariablesByProperty(11);
    }

    @Test
    public void getStandardVariablesByFilter() throws Exception{

        String cropName = "maize";

        List<StandardVariable> standardVariables = new ArrayList<>();
        StandardVariable standardVariable = new StandardVariableBuilder()
                .id(1)
                .name("standardVariable")
                .description("standardDescription")
                .cropOntologyId("C21")
                .setScale(10,scaleName, scaleDefinition)
                .setProperty(11, propertyName, propertyDescription)
                .build();

        standardVariables.add(standardVariable);

        Mockito.doReturn(standardVariables).when(ontologyService).getStandardVariables("standardVariable");

        mockMvc.perform(get("/ontology/{cropname}/variables/filter/{text}", cropName,"standardVariable").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(standardVariable.getId())))
                .andExpect(jsonPath("$[0].cropOntologyId", is(standardVariable.getCropOntologyId())))
                .andDo(print());

        verify(ontologyService, times(1)).getStandardVariables("standardVariable");
    }

}
