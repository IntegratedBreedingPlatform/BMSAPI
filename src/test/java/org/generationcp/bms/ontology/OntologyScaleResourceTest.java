package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Scale;
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

        Term term = new Term();
        term.setId(1);

        List<Scale> scaleList = new ArrayList<>();
        Scale scale = new Scale(term);
        scale.setId(1);
        scale.setName("scaleName");
        scale.setDefinition("scaleDefinition");

        scaleList.add(scale);

        Mockito.doReturn(scaleList).when(ontologyService).getAllScales();

        mockMvc.perform(get("/ontology/scales/").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(scaleList.get(0).getId())))
                .andExpect(jsonPath("$[0].name", is(scaleList.get(0).getName())))
                .andExpect(jsonPath("$[0].definition", is(scaleList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllScales();
    }

    @Test
    public void getScaleById() throws Exception{

        Term term = new Term();
        term.setId(1);

        Scale scale = new Scale(term);
        scale.setId(1);
        scale.setName("scaleName");
        scale.setDefinition("scaleDefinition");

        Mockito.doReturn(scale).when(ontologyService).getScale(1);

        mockMvc.perform(get("/ontology/scales/{id}", 1).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(scale.getId())))
                .andExpect(jsonPath("$.name", is(scale.getName())))
                .andExpect(jsonPath("$.definition", is(scale.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getScale(1);
    }
}
