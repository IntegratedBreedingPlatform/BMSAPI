package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.dto.PropertyRequest;
import org.generationcp.bms.ontology.builders.PropertyBuilder;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class OntologyPropertyResourceTest extends ApiUnitTestBase {


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

    private final String propertyName = "My Property";

    private final String propertyDescription = "Description";

    private final String className = "Study condition";

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void listAllProperties() throws Exception {

        String cropName = "maize";

        List<Term> classList = new ArrayList<>();
        Term term = new Term(10, propertyName, propertyDescription);
        classList.add(term);

        List<Property> propertyList = new ArrayList<>();
        propertyList.add(new PropertyBuilder().build(1, "p1", "d1", "CO:000001", classList));
        propertyList.add(new PropertyBuilder().build(2, "p2", "d2", "CO:000002", classList));
        propertyList.add(new PropertyBuilder().build(3, "p3", "d3", "CO:000003", classList));

        Mockito.doReturn(propertyList).when(ontologyManagerService).getAllProperties();

        mockMvc.perform(get("/ontology/{cropname}/properties", cropName).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(propertyList.size())))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(propertyList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(propertyList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getAllProperties();
    }

    /**
     * Get a property with id. It should respond with 200 and property data.
     * * *
     * @throws Exception
     */
    @Test
    public void getPropertyById() throws Exception{

        String cropName = "maize";

        List<Term> classList = new ArrayList<>();
        Term term = new Term(10, propertyName, propertyDescription);
        classList.add(term);

        Property property = new PropertyBuilder().build(1, "property", "description", "CO:000001" , classList);

        Mockito.doReturn(property).when(ontologyManagerService).getProperty(1);

        mockMvc.perform(get("/ontology/{cropname}/properties/{id}",cropName, property.getId()).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(property.getId())))
                .andExpect(jsonPath("$.name", is(property.getName())))
                .andExpect(jsonPath("$.description", is(property.getDefinition())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getProperty(1);
    }

    /**
     * This test should expect 400 if no Property Found
     * * *
     * @throws Exception
     */
    @Test
    public void getPropertyById_Should_Respond_With_400_For_Invalid_Id() throws Exception{

        String cropName = "maize";

        mockMvc.perform(get("/ontology/{cropname}/properties/{id}",cropName, 1).contentType(contentType))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(ontologyManagerService, times(1)).getProperty(1);
    }

    /*
     * This test should expect 201 : Created*
     * @throws Exception
     */
    @Test
    public void addProperty() throws Exception {

        String cropName = "maize";

        PropertyRequest propertyDTO = new PropertyRequest();
        propertyDTO.setName(propertyName);
        propertyDTO.setDescription(propertyDescription);
        propertyDTO.setCropOntologyId("CO:000001");
        propertyDTO.setClasses(new ArrayList<>(Arrays.asList(className)));

        List<Term> classList = new ArrayList<>();
        Term term = new Term(1, className, propertyDescription);
        classList.add(term);

        List<Property> propertyList = new ArrayList<>();
        propertyList.add(new PropertyBuilder().build(0, propertyDTO.getName(), propertyDTO.getDescription(), propertyDTO.getCropOntologyId() , classList));

        ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);

        Mockito.doNothing().when(ontologyManagerService).addProperty(any(Property.class));
        Mockito.doReturn(propertyList).when(ontologyManagerService).getAllPropertiesWithClass(propertyDTO.getClasses().get(0));

        mockMvc.perform(post("/ontology/{cropname}/properties", cropName)
                .contentType(contentType).content(convertObjectToByte(propertyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(0)))
                .andDo(print());

        verify(ontologyManagerService, times(1)).addProperty(captor.capture());
    }

    /*
    * This test should expect 204 : No Content*
    * @throws Exception
    */
    @Test
    public void updateProperty() throws Exception{
        String cropName = "maize";

        PropertyRequest propertyDTO = new PropertyRequest();
        propertyDTO.setName(propertyName);
        propertyDTO.setDescription(propertyDescription);
        propertyDTO.setCropOntologyId("CO:000001");
        propertyDTO.setClasses(new ArrayList<>(Arrays.asList(className)));

        List<Term> classList = new ArrayList<>();
        Term term = new Term(1, className, propertyDescription);
        classList.add(term);

        Property property = new PropertyBuilder().build(11, propertyDTO.getName(), propertyDTO.getDescription(), propertyDTO.getCropOntologyId() , classList);
        List<Property> propertyList = new ArrayList<>();
        propertyList.add(property);

        ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);

        Mockito.doNothing().when(ontologyManagerService).updateProperty(any(Property.class));
        Mockito.doReturn(property).when(ontologyManagerService).getProperty(property.getId());
        Mockito.doReturn(propertyList).when(ontologyManagerService).getAllPropertiesWithClass(propertyDTO.getClasses().get(0));

        mockMvc.perform(put("/ontology/{cropname}/properties/{id}", cropName, property.getId())
                .contentType(contentType).content(convertObjectToByte(propertyDTO)))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(ontologyManagerService, times(1)).updateProperty(captor.capture());
    }

    /**
     * This test should expect 204 : No Content
     * @throws Exception
     */
    @Test
    public void deleteProperty() throws Exception{
        String cropName = "maize";

        PropertyRequest propertyDTO = new PropertyRequest();
        propertyDTO.setName(propertyName);
        propertyDTO.setDescription(propertyDescription);
        propertyDTO.setCropOntologyId("CO:000001");
        propertyDTO.setClasses(new ArrayList<>(Arrays.asList(className)));

        List<Term> classList = new ArrayList<>();
        Term term = new Term(1, className, propertyDescription);
        classList.add(term);

        Property property = new PropertyBuilder().build(11, propertyDTO.getName(), propertyDTO.getDescription(), propertyDTO.getCropOntologyId() , classList);

        Mockito.doNothing().when(ontologyManagerService).deleteProperty(property.getId());

        mockMvc.perform(delete("/ontology/{cropname}/properties/{id}", cropName, property.getId())
                .contentType(contentType))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(ontologyManagerService, times(1)).deleteProperty(property.getId());
    }
}
