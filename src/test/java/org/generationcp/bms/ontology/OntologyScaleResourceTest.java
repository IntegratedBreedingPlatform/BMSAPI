package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.oms.Scale;
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


public class OntologyScaleResourceTest extends ApiUnitTestBase {

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
    public void listAllScales() throws Exception {

        String cropName = "rice";

        List<Scale> scaleList = new ArrayList<>();

        Scale scale = new Builder()
                .id(1)
                .name("scaleName")
                .definition("scaleDefinition")
                .buildScale();

        scaleList.add(scale);

        Mockito.doReturn(scaleList).when(ontologyService).getAllScales();

        mockMvc.perform(get("/ontology/{cropname}/scales/list", cropName).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(scale.getId())))
                .andExpect(jsonPath("$[0].name", is(scale.getName())))
                .andExpect(jsonPath("$[0].definition", is(scale.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllScales();
    }

    @Test
    public void getScaleById() throws Exception{

        String cropName = "rice";
        Scale scale = new Builder()
                .id(1)
                .name("scaleName")
                .definition("scaleDefinition")
                .buildScale();

        Mockito.doReturn(scale).when(ontologyService).getScale(1);

        mockMvc.perform(get("/ontology/{cropname}/scales/{id}",cropName, 1).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(scale.getId())))
                .andExpect(jsonPath("$.name", is(scale.getName())))
                .andExpect(jsonPath("$.definition", is(scale.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getScale(1);
    }

    @Test
    public void getScaleByFilter() throws Exception{

        String cropName = "rice";
        Scale scale = new Builder()
                .id(1)
                .name("scaleName")
                .definition("scaleDefinition")
                .buildScale();

        Mockito.doReturn(scale).when(ontologyService).getScale("scaleName");

        mockMvc.perform(get("/ontology/{cropname}/scales/filter/{text}",cropName, "scaleName").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(scale.getId())))
                .andExpect(jsonPath("$.name", is(scale.getName())))
                .andExpect(jsonPath("$.definition", is(scale.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getScale("scaleName");
    }
}
