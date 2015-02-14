package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;
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

        String cropName = "rice";

        List<Method> methodList = new ArrayList<>();
        Method method = new Builder()
                .id(1)
                .name("methodName")
                .definition("methodName")
                .buildMethod();

        methodList.add(method);

        Mockito.doReturn(methodList).when(ontologyService).getAllMethods();

        mockMvc.perform(get("/ontology/{cropname}/methods/list", cropName).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(methodList.get(0).getName())))
                .andExpect(jsonPath("$[0].definition", is(methodList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllMethods();
    }

    @Test
    public void getMethodById() throws Exception{

        String cropName = "rice";
        Method method = new Builder()
                .id(1)
                .name("methodName")
                .definition("methodName")
                .buildMethod();

        Mockito.doReturn(method).when(ontologyService).getMethod(1);

        mockMvc.perform(get("/ontology/{cropname}/methods/{id}",cropName, 1).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(method.getName())))
                .andExpect(jsonPath("$.definition", is(method.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getMethod(1);
    }

    @Test
    public void getMethodByName() throws Exception{

        String cropName = "rice";
        Method method = new Builder()
                .id(1)
                .name("methodName")
                .definition("methodName")
                .buildMethod();

        Mockito.doReturn(method).when(ontologyService).getMethod("methodName");

        mockMvc.perform(get("/ontology/{cropname}/methods/filter/{name}",cropName, "methodName").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(method.getName())))
                .andExpect(jsonPath("$.definition", is(method.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getMethod("methodName");
    }
}
