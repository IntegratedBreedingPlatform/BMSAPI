package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.builders.ScaleBuilder;
import org.generationcp.bms.ontology.dto.ValidValues;
import org.generationcp.bms.ontology.dto.ScaleRequest;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class OntologyScaleResourceTest extends ApiUnitTestBase {

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

    private final String scaleName = "scaleName";
    private final String scaleDescription = "scaleDescription";
    private Map<String, String> categories = new HashMap<>();

    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    /**
     * List all scales with details
     * @throws Exception
     */
    @Test
    public void listAllScales() throws Exception {

        String cropName = "maize";

        categories.put("label", "value");
        categories.put("label2", "value2");

        List<Scale> scaleList = new ArrayList<>();
        scaleList.add(new ScaleBuilder().build(1, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null));
        scaleList.add(new ScaleBuilder().build(1, scaleName + "2", scaleDescription + "2", DataType.NUMERIC_VARIABLE, "30", "40", null));
        scaleList.add(new ScaleBuilder().build(1, scaleName + "3", scaleDescription + "3", DataType.CATEGORICAL_VARIABLE, "", "", categories));

        Mockito.doReturn(scaleList).when(ontologyManagerService).getAllScales();

        mockMvc.perform(get("/ontology/{cropname}/scales", cropName).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(scaleList.size())))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is(scaleList.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(scaleList.get(0).getDefinition())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getAllScales();
    }

    /**
     * Get a scale if exist using given scale id
     * @throws Exception
     */
    @Test
    public void getScaleById() throws Exception{

        String cropName = "maize";

        Scale scale = new ScaleBuilder().build(1, scaleName, scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

        Mockito.doReturn(scale).when(ontologyManagerService).getScaleById(1);
        Mockito.doReturn(new Term(1, scaleName, scaleDescription, CvId.SCALES.getId(), false)).when(ontologyManagerService).getTermById(1);

        mockMvc.perform(get("/ontology/{cropname}/scales/{id}",cropName, String.valueOf(1)).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(scale.getName())))
                .andExpect(jsonPath("$.description", is(scale.getDefinition())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getScaleById(1);
    }

    /**
     * Add new scale with provided data and return id of newly generated scale
     * @throws Exception
     */
    @Test
    public void addScale() throws Exception{

        String cropName = "maize";

        ValidValues validValues = new ValidValues();
        validValues.setMin("10");
        validValues.setMax("20");

        ScaleRequest scaleRequest = new ScaleRequest();
        scaleRequest.setName(scaleName);
        scaleRequest.setDescription(scaleDescription);
        scaleRequest.setDataTypeId(1110);
        scaleRequest.setValidValues(validValues);

        Scale scale = new Scale();
        scale.setName(scaleName);
        scale.setDefinition(scaleDescription);

        ArgumentCaptor<Scale> captor = ArgumentCaptor.forClass(Scale.class);

        Mockito.doNothing().when(ontologyManagerService).addScale(any(Scale.class));

        mockMvc.perform(post("/ontology/{cropname}/scales", cropName)
                .contentType(contentType).content(convertObjectToByte(scaleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(0)))
                .andDo(print());

        verify(ontologyManagerService).addScale(captor.capture());
    }

    /**
     * Update a scale if exist
     * @throws Exception
     */
    @Test
    public void updateScale() throws Exception{

        String cropName = "maize";

        ValidValues validValues = new ValidValues();
        validValues.setMin("10");
        validValues.setMax("20");

        ScaleRequest scaleRequest = new ScaleRequest();
        scaleRequest.setName(scaleName);
        scaleRequest.setDescription(scaleDescription);
        scaleRequest.setDataTypeId(1110);
        scaleRequest.setValidValues(validValues);

        Scale scale = new Scale(new Term(1, scaleName, scaleDescription));

        ArgumentCaptor<Scale> captor = ArgumentCaptor.forClass(Scale.class);

        Mockito.doNothing().when(ontologyManagerService).updateScale(any(Scale.class));
        Mockito.doReturn(scale).when(ontologyManagerService).getScaleById(scale.getId());

        mockMvc.perform(put("/ontology/{cropname}/scales/{id}", cropName, scale.getId())
                .contentType(contentType).content(convertObjectToByte(scaleRequest)))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(ontologyManagerService).updateScale(captor.capture());

        Scale captured = captor.getValue();

        assertEquals(scale.getName(), captured.getName());
        assertEquals(scale.getDefinition(), captured.getDefinition());
    }

    /**
     * Delete a scale if exist and not referred
     * @throws Exception
     */
    @Test
    public void deleteScale() throws Exception{

        String cropName = "maize";

        Term term = new Term(10, "name", "", CvId.SCALES.getId(), false);
        Scale scale = new Scale(term);

        Mockito.doReturn(term).when(ontologyManagerService).getTermById(scale.getId());
        Mockito.doReturn(scale).when(ontologyManagerService).getScaleById(scale.getId());
        Mockito.doReturn(false).when(ontologyManagerService).isTermReferred(scale.getId());
        Mockito.doNothing().when(ontologyManagerService).deleteScale(scale.getId());

        mockMvc.perform(delete("/ontology/{cropname}/scales/{id}", cropName, scale.getId())
                .contentType(contentType))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(ontologyManagerService, times(1)).deleteScale(scale.getId());
    }
}
