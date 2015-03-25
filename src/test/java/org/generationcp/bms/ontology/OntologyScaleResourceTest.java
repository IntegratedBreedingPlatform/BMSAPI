package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.builders.ScaleBuilder;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;
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

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
}
