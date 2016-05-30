package org.ibp.api.rest.ontology;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class VariableFilterResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyVariableDataManager ontologyVariableDataManager() {
            return Mockito.mock(OntologyVariableDataManager.class);
        }

    }

    @Autowired
    private OntologyVariableDataManager ontologyVariableDataManager;


    @Test
    public void testVariableWithFilter() throws Exception{

        Project project = new Project();
        project.setCropType(new CropType(this.cropName));
        project.setUniqueID(this.programUuid);
        project.setProjectName("project_name");

        Set<Integer> propertySet = new HashSet<>();
        propertySet.add(3);
        propertySet.add(2);

        List<Variable> variables = TestDataProvider.getTestVariables(1);

        VariableFilter variableFilter = new VariableFilter();
        variableFilter.setProgramUuid(this.programUuid);
        variableFilter.setFetchAll(true);
        variableFilter.setFavoritesOnly(false);
        variableFilter.addVariableId(variables.get(0).getId());
        variableFilter.addVariableType(VariableType.ANALYSIS);
        variableFilter.addDataType(DataType.NUMERIC_VARIABLE);
        variableFilter.addExcludedVariableId(1000);
        variableFilter.addMethodId(1);
        variableFilter.addPropertyId(propertySet.iterator().next());
        variableFilter.addPropertyClass("Property Class");
        variableFilter.addScaleId(1030);


        Mockito.when(this.ontologyVariableDataManager.getWithFilter(variableFilter)).thenReturn(variables);

        Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(this.programUuid);
        this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/filtervariables?programId="+variableFilter.getProgramUuid()+"&propertyIds=201,202&methodIds=111,112&scaleIds=99&variableIds=221&exclusionVariableIds=2&dataTypeIds=332&variableTypeIds=998&propertyClasses='Triat Class'", this.cropName ).contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }
}
