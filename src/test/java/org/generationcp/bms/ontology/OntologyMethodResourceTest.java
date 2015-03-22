package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.builders.MethodBuilder;
import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class OntologyMethodResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }
    }

    @Autowired
    private OntologyManagerService ontologyManagerService;

    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
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

        Mockito.doReturn(methodList).when(ontologyManagerService).getAllMethods();

        mockMvc.perform(get("/ontology/{cropname}/methods", cropName).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(methodList.size())))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(methodList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(methodList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getAllMethods();
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

        Mockito.doReturn(method).when(ontologyManagerService).getMethod(1);

        //TODO: check editable and deletable fields.
        mockMvc.perform(get("/ontology/{cropname}/methods/{id}",cropName, 1).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(method.getName())))
                .andExpect(jsonPath("$.description", is(method.getDefinition())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getMethod(1);
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

        verify(ontologyManagerService, times(1)).getMethod(1);
    }

    /**
     * This test should expect 201 : Created*
     * @throws Exception
     */
    @Test
    public void addMethod() throws Exception {

        String cropName = "maize";

        MethodRequest methodDTO = new MethodRequest();
        methodDTO.setName("methodName");
        methodDTO.setDescription("methodDescription");

        Method method = new Method();
        method.setName(methodDTO.getName());
        method.setDefinition(methodDTO.getDescription());

        ArgumentCaptor<Method> captor = ArgumentCaptor.forClass(Method.class);

        Mockito.doNothing().when(ontologyManagerService).addMethod(any(Method.class));

        mockMvc.perform(post("/ontology/{cropname}/methods",cropName)
                .contentType(contentType).content(convertObjectToByte(methodDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(0)))
                .andDo(print());


        verify(ontologyManagerService).addMethod(captor.capture());

    }

    /**
     * This test should expect 204 : No Content
     * @throws Exception
     */
    @Test
    public void updateMethod() throws Exception {

        String cropName = "maize";

        MethodRequest methodDTO = new MethodRequest();
        methodDTO.setName("methodName");
        methodDTO.setDescription("methodDescription");

        Method method = new Method(new Term(10, methodDTO.getName(), methodDTO.getDescription()));

        /**
         * We Need equals method inside Method (Middleware) because it throws hashcode matching error.
         * So Added ArgumentCaptor that will implement equals()
         */
        ArgumentCaptor<Method> captor = ArgumentCaptor.forClass(Method.class);

        Mockito.doNothing().when(ontologyManagerService).updateMethod(any(Method.class));
        Mockito.doReturn(method).when(ontologyManagerService).getMethod(method.getId());

        mockMvc.perform(put("/ontology/{cropname}/methods/{id}", cropName, method.getId())
                .contentType(contentType).content(convertObjectToByte(methodDTO)))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(ontologyManagerService).updateMethod(captor.capture());

        Method captured = captor.getValue();

        assertEquals(method.getName(), captured.getName());
        assertEquals(method.getDefinition(), captured.getDefinition());
    }

    /**
     * This test should expect 204 : No Content
     * @throws Exception
     */
    @Test
    public void deleteMethod() throws Exception {

        String cropName = "maize";

        Term term = new Term(10, "name", "", CvId.METHODS.getId(), false);
        Method method = new Method(term);
        Mockito.doReturn(term).when(ontologyManagerService).getTermById(method.getId());
        Mockito.doReturn(method).when(ontologyManagerService).getMethod(method.getId());
        Mockito.doReturn(false).when(ontologyManagerService).isTermReferred(method.getId());

        Mockito.doNothing().when(ontologyManagerService).deleteMethod(method.getId());

        mockMvc.perform(delete("/ontology/{cropname}/methods/{id}", cropName, method.getId())
                .contentType(contentType))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(ontologyManagerService, times(1)).deleteMethod(method.getId());
    }
}
