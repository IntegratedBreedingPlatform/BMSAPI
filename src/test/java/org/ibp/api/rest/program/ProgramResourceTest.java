package org.ibp.api.rest.program;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.ibp.ApiUnitTestBase;
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


public class ProgramResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public WorkbenchDataManager workbenchDataManager() {
            return Mockito.mock(WorkbenchDataManager.class);
        }
    }

    @Autowired
    private WorkbenchDataManager workbenchDataManager;

    @Before
    public void reset(){
        Mockito.reset(workbenchDataManager);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void listAllMethods() throws Exception {

        CropType cropType = new CropType();
        cropType.setCropName("MAIZE");

        List<Project> projectList = new ArrayList<>();
        Project project = new Project();
        project.setProjectId(1L);
        project.setProjectName("projectName");
        project.setCropType(cropType);
        project.setUniqueID("123-456");
        project.setUserId(1);

        projectList.add(project);

        Mockito.doReturn(projectList).when(workbenchDataManager).getProjects();

        mockMvc.perform(get("/program/list").contentType(contentType)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectName", is(projectList.get(0).getProjectName())))
                .andExpect(jsonPath("$[0].uniqueID", is(projectList.get(0).getUniqueID())))
                .andExpect(jsonPath("$[0].userId", is(projectList.get(0).getUserId())))
                .andDo(print());

        verify(workbenchDataManager, times(1)).getProjects();
    }
}
