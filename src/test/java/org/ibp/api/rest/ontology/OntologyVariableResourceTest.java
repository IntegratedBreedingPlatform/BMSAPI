package org.ibp.api.rest.ontology;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.builders.VariableBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

	private final String programUuid = UUID.randomUUID().toString();

	private final String variableName = "Variable Name";
	private final String variableDescription = "Variable Description";

	private final String methodName = "Method Name";
	private final String methodDescription = "Method Description";

	private final String propertyName = "Property Name";
	private final String propertyDescription = "Property Description";

	private final String scaleName = "Scale Name";
	private final String scaleDescription = "Scale Description";

	@Before
	public void reset() {
		Mockito.reset(this.ontologyManagerService);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * List all variables with details
	 *
	 * @throws Exception
	 */
	@Test
	public void listAllVariables() throws Exception {

		List<OntologyVariableSummary> variableSummaries = new ArrayList<>();
		OntologyVariableSummary variableSummary = new VariableBuilder().build(1, this.variableName, this.variableDescription,
				new TermSummary(11, this.methodName, this.methodDescription),
				new TermSummary(10, this.propertyName, this.propertyDescription),
				new TermSummary(12, this.scaleName, this.scaleDescription));

		variableSummaries.add(variableSummary);

	  	Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(variableSummaries).when(this.ontologyManagerService).getWithFilter(programUuid, null, null, null, null);
		//Mockito.doReturn(project).when(this.workbenchDataManager).getProjectById(Long.valueOf(programId));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/variables?programId=" + programUuid,cropName)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(variableSummaries.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(variableSummaries.get(0).getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(variableSummaries.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(variableSummaries.get(0).getDescription())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyManagerService, Mockito.times(1)).getWithFilter(programUuid, null, null, null, null);
	}
}
