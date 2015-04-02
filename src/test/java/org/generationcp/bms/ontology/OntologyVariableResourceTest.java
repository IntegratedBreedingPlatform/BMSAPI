package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.bms.ontology.builders.VariableBuilder;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.OntologyManagerService;
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

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OntologyVariableResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }

        @Bean
        @Primary
        public WorkbenchDataManager workbenchDataManager() {
            return Mockito.mock(WorkbenchDataManager.class);
        }
    }

    @Autowired
    private OntologyManagerService ontologyManagerService;

    @Autowired
    private WorkbenchDataManager workbenchDataManager;

    private final String variableName = "Variable Name";
    private final String variableDescription = "Variable Description";

    private final String methodName = "Method Name";
    private final String methodDescription = "Method Description";

    private final String propertyName = "Property Name";
    private final String propertyDescription = "Property Description";

    private final String scaleName = "Scale Name";
    private final String scaleDescription = "Scale Description";

    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    /**
     * List all variables with details
     * @throws Exception
     */
    @Test
    public void listAllVariables() throws Exception {

        String cropName = "maize";
        Integer programId = 1;

        Project project = new Project();
        project.setProjectId(Long.valueOf(programId));
        project.setProjectName("Maize Test");


        List<OntologyVariableSummary> variableSummaries = new ArrayList<>();
        OntologyVariableSummary variableSummary = new VariableBuilder().build(1, variableName, variableDescription, new TermSummary(11, methodName, methodDescription),new TermSummary(10, propertyName, propertyDescription),new TermSummary(12, scaleName, scaleDescription));
        variableSummaries.add(variableSummary);

        Mockito.doReturn(variableSummaries).when(ontologyManagerService).getWithFilter(programId, null, null, null, null);
        Mockito.doReturn(project).when(workbenchDataManager).getProjectById(Long.valueOf(programId));

        mockMvc.perform(get("/ontology/{cropname}/variables?programId=" + programId.toString(), cropName).contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(variableSummaries.size())))
                .andExpect(jsonPath("$[0].id", is(variableSummaries.get(0).getId())))
                .andExpect(jsonPath("$[0].name", is(variableSummaries.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(variableSummaries.get(0).getDescription())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getWithFilter(programId, null, null, null, null);
    }
}
