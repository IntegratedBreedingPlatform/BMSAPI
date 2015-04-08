package org.ibp.api.rest.ontology;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.ibp.builders.PropertyBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.*;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

    private static final String cropName = "maize";

    private static final String propertyName = "My Property";

    private static final String propertyDescription = "Description";

    private static final String className1 = "Study condition";
    private static final String className2 = "Biotic stress";

    private static final List<String> classes = new ArrayList<>(Arrays.asList(className1, className2));

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void listAllProperties() throws Exception {

        List<Property> propertyList = new ArrayList<>();
        propertyList.add(new PropertyBuilder().build(1, "p1", "d1", "CO:000001", classes));
        propertyList.add(new PropertyBuilder().build(2, "p2", "d2", "CO:000002", classes));
        propertyList.add(new PropertyBuilder().build(3, "p3", "d3", "CO:000003", classes));

        Mockito.doReturn(propertyList).when(ontologyManagerService).getAllProperties();

        mockMvc.perform(get("/ontology/{cropname}/properties", cropName).contentType(contentType))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(propertyList.size())))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(propertyList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(propertyList.get(0).getDefinition())));


        verify(ontologyManagerService, times(1)).getAllProperties();
    }

    /**
     * Get a property with id. It should respond with 200 and property data.
     * * *
     * @throws Exception
     */
    @Test
    public void getPropertyById() throws Exception{

        Property property = new PropertyBuilder().build(1, "property", "description", "CO:000001" , classes);

        Mockito.doReturn(new Term(1, property.getName(), property.getDefinition(), CvId.PROPERTIES.getId(), false)).when(ontologyManagerService).getTermById(1);
        Mockito.doReturn(property).when(ontologyManagerService).getProperty(1);

        mockMvc.perform(get("/ontology/{cropname}/properties/{id}",cropName, property.getId()).contentType(contentType))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(property.getId())))
                .andExpect(jsonPath("$.name", is(property.getName())))
                .andExpect(jsonPath("$.description", is(property.getDefinition())));


        verify(ontologyManagerService, times(1)).getProperty(1);
    }

    /**
     * This test should expect 400 if no Property Found
     * * *
     * @throws Exception
     */
    @Test
    public void getPropertyById_Should_Respond_With_400_For_Invalid_Id() throws Exception{

        Mockito.doReturn(null).when(ontologyManagerService).getTermById(1);

        mockMvc.perform(get("/ontology/{cropname}/properties/{id}",cropName, 1).contentType(contentType))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(ontologyManagerService, times(1)).getTermById(1);
    }

    /*
     * This test should expect 201 : Created*
     * @throws Exception
     */
    @Test
    public void addProperty() throws Exception {

        PropertyRequest propertyDTO = new PropertyRequest();
        propertyDTO.setName(propertyName);
        propertyDTO.setDescription(propertyDescription);
        propertyDTO.setCropOntologyId("CO:000001");
        propertyDTO.setClasses(new ArrayList<>(Collections.singletonList(className1)));

        ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(propertyName, CvId.PROPERTIES.getId());
        Mockito.doNothing().when(ontologyManagerService).addProperty(any(Property.class));

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

        PropertyRequest propertyDTO = new PropertyRequest();
        propertyDTO.setName(propertyName);
        propertyDTO.setDescription(propertyDescription);
        propertyDTO.setCropOntologyId("CO:000001");
        propertyDTO.setClasses(classes);

        Property property = new PropertyBuilder().build(11, propertyDTO.getName(), propertyDTO.getDescription(), propertyDTO.getCropOntologyId() , classes);

        ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);

        Mockito.doReturn(new Term(11, propertyName, propertyDescription, CvId.PROPERTIES.getId(), false))
                .when(ontologyManagerService).getTermByNameAndCvId(propertyName, CvId.PROPERTIES.getId());
        Mockito.doReturn(property).when(ontologyManagerService).getProperty(property.getId());
        Mockito.doNothing().when(ontologyManagerService).updateProperty(any(Property.class));

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

        PropertyRequest propertyDTO = new PropertyRequest();
        propertyDTO.setName(propertyName);
        propertyDTO.setDescription(propertyDescription);
        propertyDTO.setCropOntologyId("CO:000001");
        propertyDTO.setClasses(classes);

        Term term = new Term(10, propertyDTO.getName(), propertyDTO.getDescription(), CvId.PROPERTIES.getId(), false);

        Property property = new PropertyBuilder().build(10, propertyDTO.getName(), propertyDTO.getDescription(), propertyDTO.getCropOntologyId() , classes);

        Mockito.doReturn(term).when(ontologyManagerService).getTermById(term.getId());
        Mockito.doReturn(property).when(ontologyManagerService).getProperty(property.getId());
        Mockito.doReturn(false).when(ontologyManagerService).isTermReferred(property.getId());
        Mockito.doNothing().when(ontologyManagerService).deleteProperty(property.getId());

        mockMvc.perform(delete("/ontology/{cropname}/properties/{id}", cropName, property.getId())
                .contentType(contentType))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(ontologyManagerService, times(1)).deleteProperty(property.getId());
    }
}
