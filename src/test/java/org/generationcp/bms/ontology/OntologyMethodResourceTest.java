package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.oms.Method;
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


public class OntologyMethodResourceTest extends ApiUnitTestBase {

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
    public void listAllMethods() throws Exception {

        Term term = new Term();
        term.setId(1);

        List<Method> methodList = new ArrayList<>();
        Method method = new Method(term);
        method.setId(1);
        method.setName("methodName");
        method.setDefinition("methodDefinition");

        methodList.add(method);

        Mockito.doReturn(methodList).when(ontologyService).getAllMethods();

        mockMvc.perform(get("/ontology/methods/list").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(methodList.get(0).getName())))
                .andExpect(jsonPath("$[0].definition", is(methodList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllMethods();
    }

    @Test
    public void getMethodById() throws Exception{

        Term term = new Term();
        term.setId(1);

        Method method = new Method(term);
        method.setId(1);
        method.setName("methodName");
        method.setDefinition("methodDefinition");

        Mockito.doReturn(method).when(ontologyService).getMethod(1);

        mockMvc.perform(get("/ontology/methods/{id}", 1).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(method.getName())))
                .andExpect(jsonPath("$.definition", is(method.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getMethod(1);
    }

    @Test
    public void getMethodByName() throws Exception{

        Term term = new Term();
        term.setId(1);

        Method method = new Method(term);
        method.setId(1);
        method.setName("methodName");
        method.setDefinition("methodDefinition");

        Mockito.doReturn(method).when(ontologyService).getMethod("methodName");

        mockMvc.perform(get("/ontology/methods/name/{name}", "methodName").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(method.getName())))
                .andExpect(jsonPath("$.definition", is(method.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getMethod("methodName");
    }
}
