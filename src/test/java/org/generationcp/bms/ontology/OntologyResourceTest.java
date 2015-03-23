package org.generationcp.bms.ontology;

import org.generationcp.bms.ApiUnitTestBase;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class OntologyResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyDataManager ontologyDataManager() {
            return Mockito.mock(OntologyDataManager.class);
        }
    }

    @Autowired
    private OntologyDataManager ontologyDataManager;

    @Before
    public void reset(){
        Mockito.reset(ontologyDataManager);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void listAllSummariesTest() throws Exception {

        Set<StandardVariable> vars = new HashSet<>();
        StandardVariable var1 = new StandardVariable();
        var1.setId(1);
        var1.setName("name");
        var1.setDescription("des");
        Term term = new Term();
        term.setId(1);
        term.setName("T1");
        term.setDefinition("T1");
        var1.setProperty(term);
        var1.setMethod(term);
        var1.setScale(term);
        vars.add(var1);

        Mockito.doReturn(vars).when(ontologyDataManager).getAllStandardVariables();

        mockMvc.perform(get("/ontology/var/list").contentType(contentType)).andExpect(status().isOk()).andDo(print());

        verify(ontologyDataManager, times(1)).getAllStandardVariables();
    }


    @Test
    public void getDetailsById() throws Exception{
        StandardVariable var1 = new StandardVariable();
        var1.setId(1);
        var1.setName("name");
        var1.setDescription("des");
        Term term = new Term();
        term.setId(1);
        term.setName("T1");
        term.setDefinition("T1");
        var1.setProperty(term);
        var1.setMethod(term);
        var1.setScale(term);

        Mockito.doReturn(var1).when(ontologyDataManager).getStandardVariable(1);

        mockMvc.perform(get("/ontology/var/1").contentType(contentType)).andExpect(status().isOk()).andDo(print());

        verify(ontologyDataManager, times(1)).getStandardVariable(1);

    }
}
