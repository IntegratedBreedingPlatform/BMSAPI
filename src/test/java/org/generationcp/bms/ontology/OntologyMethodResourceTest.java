package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.builders.MethodBuilder;
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

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
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

        String cropName = "maize";

        List<Method> methodList = new ArrayList<>();
        methodList.add(new MethodBuilder().build(1, "m1", "d1"));
        methodList.add(new MethodBuilder().build(2, "m2", "d2"));
        methodList.add(new MethodBuilder().build(3, "m3", "d3"));

        Mockito.doReturn(methodList).when(ontologyService).getAllMethods();

        mockMvc.perform(get("/ontology/{cropname}/methods/list", cropName).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(methodList.size())))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(methodList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(methodList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getAllMethods();
    }

    /**
     * Get a method with id. It should respond with 200 and method data.
     * * *
     * @throws Exception
     */
    @Test
    public void getMethodById() throws Exception{

        String cropName = "maize";
        Method method = new MethodBuilder().build(1, "m1", "d1");

        Mockito.doReturn(method).when(ontologyService).getMethod(1);

        //TODO: check editable and deletable fields.
        mockMvc.perform(get("/ontology/{cropname}/methods/{id}",cropName, 1).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(method.getName())))
                .andExpect(jsonPath("$.description", is(method.getDefinition())))
                .andDo(print());

        verify(ontologyService, times(1)).getMethod(1);
    }

    /**
     * This test should expect 400
     * * *
     * @throws Exception
     */
    @Test
    public void getMethodById_Should_Respond_With_400_For_Invalid_Id() throws Exception{

        String cropName = "maize";

        mockMvc.perform(get("/ontology/{cropname}/methods/{id}",cropName, 1).contentType(contentType))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(ontologyService, times(1)).getMethod(1);
    }

}
