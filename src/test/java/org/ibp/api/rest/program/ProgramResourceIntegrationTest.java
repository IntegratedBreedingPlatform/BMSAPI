package org.ibp.api.rest.program;

import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class ProgramResourceIntegrationTest extends ApiUnitTestBase {

	@Test
	public void listAllSummariesTest() throws Exception {

		this.mockMvc
		.perform(MockMvcRequestBuilders.get("/program/list").contentType(this.contentType))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andDo(MockMvcResultHandlers.print());
	}

}
