package org.ibp.api.rest.program;

import org.ibp.ApiUnitTestBase;
import org.junit.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ProgramResourceIntegrationTest extends ApiUnitTestBase {

    @Test
    public void listAllSummariesTest() throws Exception {

        mockMvc.perform(get("/program/list").contentType(contentType)).andExpect(status().isOk()).andDo(print());
    }

}
