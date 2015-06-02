package org.ibp.api.rest.ontology;

import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.OntologyVariableInfo;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.StringUtil;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.VariableSummary;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class VariableResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public TermDataManager termDataManager() {
			return Mockito.mock(TermDataManager.class);
		}

		@Bean
		@Primary
		public OntologyVariableDataManager ontologyVariableDataManager(){
			return Mockito.mock(OntologyVariableDataManager.class);
		}

		@Bean
		@Primary
		public OntologyScaleDataManager ontologyScaleDataManager(){
			return Mockito.mock(OntologyScaleDataManager.class);
		}

	}

	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.termDataManager);
		Mockito.reset(this.ontologyVariableDataManager);
	}
	/**
	 * List all variables with details with status code 200 : Ok
	 *
	 * @throws Exception
	 */
	@Test
	public void listAllVariables() throws Exception {

		List<OntologyVariableSummary> variableSummaries = TestDataProvider.getTestVariables(4);

		Mockito.doReturn(new Project()).when(this.workbenchDataManager).getProjectByUuid(programUuid);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(programUuid, null, null, null, null);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/variables?programId=" + programUuid, cropName)
				.contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(variableSummaries.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(variableSummaries.get(0).getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(variableSummaries.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(variableSummaries.get(0).getDescription())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].alias", Matchers.is(variableSummaries.get(0).getAlias())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].propertySummary.id", Matchers.is(variableSummaries.get(0).getPropertySummary().getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].propertySummary.name", Matchers.is(variableSummaries.get(0).getPropertySummary().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].methodSummary.id", Matchers.is(variableSummaries.get(0).getMethodSummary().getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].methodSummary.name", Matchers.is(variableSummaries.get(0).getMethodSummary().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].scaleSummary.id", Matchers.is(String.valueOf(variableSummaries.get(0).getScaleSummary().getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].scaleSummary.name", Matchers.is(variableSummaries.get(0).getScaleSummary().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].scaleSummary.dataType.id", Matchers.is(variableSummaries.get(0).getScaleSummary().getDataType().getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].scaleSummary.dataType.name", Matchers.is(variableSummaries.get(0).getScaleSummary().getDataType().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].variableTypes", IsCollectionWithSize.hasSize(variableSummaries.get(0).getVariableTypes().size())));

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).getWithFilter(programUuid, null, null, null, null);
	}

	/**
	 * get variable by given id with status code 200 : Ok
	 * @throws Exception
	 */
	@Test
	public void getVariableById() throws Exception{

		Variable ontologyVariable = TestDataProvider.getTestVariable();

		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(programUuid, ontologyVariable.getId());
		Mockito.doReturn(new Term(ontologyVariable.getId(), ontologyVariable.getName(), ontologyVariable.getDefinition(), CvId.VARIABLES.getId(), false)).when(this.termDataManager).getTermById(ontologyVariable.getId());

		this.mockMvc.perform(MockMvcRequestBuilders
				.get("/ontology/{cropname}/variables/{id}?programId=" + programUuid, cropName, ontologyVariable.getId())
				.contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(ontologyVariable.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(ontologyVariable.getDefinition())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.alias", Matchers.is(ontologyVariable.getAlias())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.methodSummary.id", Matchers.is(String.valueOf(ontologyVariable.getMethod().getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.methodSummary.name", Matchers.is(ontologyVariable.getMethod().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.propertySummary.id", Matchers.is(String.valueOf(ontologyVariable.getProperty().getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.propertySummary.name", Matchers.is(ontologyVariable.getProperty().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.id", Matchers.is(String.valueOf(ontologyVariable.getScale().getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.name", Matchers.is(ontologyVariable.getScale().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.dataType.id", Matchers.is(ontologyVariable.getScale().getDataType().getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.dataType.name", Matchers.is(ontologyVariable.getScale().getDataType().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.validValues.min", Matchers.is(StringUtil.parseInt(ontologyVariable.getScale().getMinValue(), null))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.validValues.max", Matchers.is(StringUtil.parseInt(ontologyVariable.getScale().getMaxValue(), null))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.variableTypes", IsCollectionWithSize.hasSize(ontologyVariable.getVariableTypes().size())));

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).getVariable(programUuid, ontologyVariable.getId());
	}

	/**
	 * add new variable and return new generated variable Id with status code 201 : Created
	 * @throws Exception
	 */
	@Test
	public void addVariable() throws Exception{
		final Term variableTerm = TestDataProvider.getVariableTerm();
		VariableSummary variableSummary = TestDataProvider.getTestVariableSummary();
		//Set variable id to null for post request.
		variableSummary.setId(null);
		Integer methodId = StringUtil.parseInt(variableSummary.getMethodSummary().getId(), null);
		Integer propertyId = StringUtil.parseInt(variableSummary.getPropertySummary().getId(), null);
		Integer scaleId = StringUtil.parseInt(variableSummary.getScaleSummary().getId(), null);

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(variableSummary.getName(), CvId.VARIABLES.getId());
		Mockito.doReturn(TestDataProvider.getTestScale()).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(TestDataProvider.getPropertyTerm()).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(TestDataProvider.getMethodTerm()).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(TestDataProvider.getScaleTerm()).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);

		//Mock OntologyVariableInfo Class and when addVariable method called it will set id to 1 and return (self member alter if void is return type of method)
		doAnswer(new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					OntologyVariableInfo entity = (OntologyVariableInfo) arguments[0];
					entity.setId(variableTerm.getId());
				}
				return null;
			}
		}).when(this.ontologyVariableDataManager).addVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));

		this.mockMvc.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/variables?programId=" + programUuid, cropName)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(variableSummary)))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(variableTerm.getId()))));


		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).addVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));
	}

	/**
	 * update variable using given Id and data if exist and return status code 204 : No Content
	 * @throws Exception
	 */
	@Test
	public void updateVariable() throws Exception{

		VariableSummary variableSummary = TestDataProvider.getTestVariableSummary();

		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();
		Term variableTerm = TestDataProvider.getVariableTerm();

		Scale scale = TestDataProvider.getTestScale();
		Variable variable = TestDataProvider.getTestVariable();
		variable.setMethod(new Method(methodTerm));
		variable.setProperty(new Property(propertyTerm));
		variable.setScale(scale);

		Integer methodId = StringUtil.parseInt(variableSummary.getMethodSummary().getId(), null);
		Integer propertyId = StringUtil.parseInt(variableSummary.getPropertySummary().getId(), null);
		Integer scaleId = StringUtil.parseInt(variableSummary.getScaleSummary().getId(), null);

		Mockito.doReturn(new Project()).when(this.workbenchDataManager).getProjectByUuid(programUuid);
		Mockito.doReturn(variableTerm).when(this.termDataManager).getTermById(variableTerm.getId());
		Mockito.doReturn(variableTerm).when(this.termDataManager).getTermByNameAndCvId(variable.getName(), CvId.VARIABLES.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(programUuid, variable.getId());
		Mockito.doNothing().when(this.ontologyVariableDataManager).updateVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));

		this.mockMvc.perform(MockMvcRequestBuilders.put("/ontology/{cropname}/variables/{id}?programId=" + programUuid, cropName, variableTerm.getId())
				.contentType(this.contentType)
				.content(this.convertObjectToByte(variableSummary)))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isNoContent());

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).updateVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));
	}


	/**
	 * delete variable using given Id if exist and return status code 204 : No Content
	 * @throws Exception
	 */
	@Test
	public void deleteVariable() throws Exception{

		Term term = TestDataProvider.getVariableTerm();

		Variable ontologyVariable = TestDataProvider.getTestVariable();

		Mockito.doReturn(term).when(this.termDataManager).getTermById(ontologyVariable.getId());
		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(programUuid, ontologyVariable.getId());
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(ontologyVariable.getId());
		Mockito.doNothing().when(this.ontologyVariableDataManager).deleteVariable(ontologyVariable.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/ontology/{cropname}/variables/{id}", cropName, ontologyVariable.getId())
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).deleteVariable(ontologyVariable.getId());
	}
}
