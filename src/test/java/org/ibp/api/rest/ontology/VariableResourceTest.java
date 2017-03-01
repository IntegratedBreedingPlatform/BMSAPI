
package org.ibp.api.rest.ontology;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.manager.ontology.daoElements.OntologyVariableInfo;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.StringUtil;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.ibp.api.java.ontology.ModelService;
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
		public OntologyVariableDataManager ontologyVariableDataManager() {
			return Mockito.mock(OntologyVariableDataManager.class);
		}

		@Bean
		@Primary
		public OntologyScaleDataManager ontologyScaleDataManager() {
			return Mockito.mock(OntologyScaleDataManager.class);
		}

		@Bean
		@Primary
		public ModelService modelService(){
			return Mockito.mock(ModelService.class);
		}
	}

	@Autowired
	protected ModelService modelService;

	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.modelService);
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

		List<Variable> variables = TestDataProvider.getTestVariables(4);

		Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(this.programUuid, this.cropName);
		VariableFilter variableFilter = new VariableFilter();
		variableFilter.setProgramUuid(this.programUuid);
		Mockito.doReturn(variables).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/variables?programId=" + this.programUuid, this.cropName)
						.contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(variables.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(String.valueOf(variables.get(0).getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(variables.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(variables.get(0).getDefinition())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].alias", Matchers.is(variables.get(0).getAlias())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].property.id",
								Matchers.is(String.valueOf(variables.get(0).getProperty().getId()))))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].property.name",
								Matchers.is(variables.get(0).getProperty().getName())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].method.id",
								Matchers.is(String.valueOf(variables.get(0).getMethod().getId()))))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].method.name",
								Matchers.is(variables.get(0).getMethod().getName())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].scale.id",
								Matchers.is(String.valueOf(variables.get(0).getScale().getId()))))
				.andExpect(MockMvcResultMatchers
								.jsonPath("$[0].scale.name", Matchers.is(variables.get(0).getScale().getName())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].scale.dataType.id",
								Matchers.is(String.valueOf(variables.get(0).getScale().getDataType().getId()))))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].scale.dataType.name",
								Matchers.is(variables.get(0).getScale().getDataType().getName())))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$[0].variableTypes", IsCollectionWithSize.hasSize(variables.get(0).getVariableTypes().size())));

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).getWithFilter(variableFilter);
	}

	/**
	 * get variable by given id with status code 200 : Ok
	 * 
	 * @throws Exception
	 */
	@Test
	public void getVariableById() throws Exception {

		Variable ontologyVariable = TestDataProvider.getTestVariable();

		Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(this.programUuid, this.cropName);
		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(this.programUuid, ontologyVariable.getId(),
				true, true);
		Mockito.doReturn(
				new Term(ontologyVariable.getId(), ontologyVariable.getName(), ontologyVariable.getDefinition(), CvId.VARIABLES.getId(),
						false)).when(this.termDataManager).getTermById(ontologyVariable.getId());
		Mockito.doReturn(true).when(this.modelService).isNumericDataType(String.valueOf(ontologyVariable.getScale().getDataType().getId()));

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.get("/ontology/{cropname}/variables/{id}?programId=" + this.programUuid, this.cropName,
								ontologyVariable.getId()).contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(ontologyVariable.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(ontologyVariable.getDefinition())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.alias", Matchers.is(ontologyVariable.getAlias())))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$.method.id", Matchers.is(String.valueOf(ontologyVariable.getMethod().getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.method.name", Matchers.is(ontologyVariable.getMethod().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.property.id",
						Matchers.is(String.valueOf(ontologyVariable.getProperty().getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.property.name", Matchers.is(ontologyVariable.getProperty().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.id", Matchers.is(String.valueOf(ontologyVariable.getScale().getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.name", Matchers.is(ontologyVariable.getScale().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.dataType.id",Matchers.is(String.valueOf(
						ontologyVariable.getScale().getDataType().getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.dataType.name",Matchers.is(ontologyVariable.getScale().getDataType().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.validValues.min",Matchers.is(StringUtil.parseDouble(ontologyVariable.getScale().getMinValue(), null))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.scale.validValues.max",Matchers.is(StringUtil.parseDouble(ontologyVariable.getScale().getMaxValue(), null))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.variableTypes",
						IsCollectionWithSize.hasSize(ontologyVariable.getVariableTypes().size())));

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).getVariable(this.programUuid, ontologyVariable.getId(), true, true);
	}

	/**
	 * add new variable and return new generated variable Id with status code 201 : Created
	 * 
	 * @throws Exception
	 */
	@Test
	public void addVariable() throws Exception {
		final Term variableTerm = TestDataProvider.getVariableTerm();
		VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();

		Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		// Set variable id to null for post request.
		variableDetails.setId(null);
		Integer methodId = StringUtil.parseInt(variableDetails.getMethod().getId(), null);
		Integer propertyId = StringUtil.parseInt(variableDetails.getProperty().getId(), null);
		Integer scaleId = StringUtil.parseInt(variableDetails.getScale().getId(), null);

		VariableFilter variableFilter = new VariableFilter();
		variableFilter.addMethodId(methodId);
		variableFilter.addPropertyId(propertyId);
		variableFilter.addScaleId(scaleId);

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(variableDetails.getName(), CvId.VARIABLES.getId());
		Mockito.doReturn(TestDataProvider.getTestScale()).when(this.ontologyScaleDataManager).getScaleById(scaleId, true);
		Mockito.doReturn(TestDataProvider.getPropertyTerm()).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(TestDataProvider.getMethodTerm()).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(TestDataProvider.getScaleTerm()).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);
		Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(this.programUuid, this.cropName);

		// Mock OntologyVariableInfo Class and when addVariable method called it will set id to 1 and return (self member alter if void is
		// return type of method)
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					OntologyVariableInfo entity = (OntologyVariableInfo) arguments[0];
					entity.setId(variableTerm.getId());
				}
				return null;
			}
		}).when(this.ontologyVariableDataManager).addVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.post("/ontology/{cropname}/variables?programId=" + this.programUuid, this.cropName)
								.contentType(this.contentType).content(this.convertObjectToByte(variableDetails)))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(variableTerm.getId()))));

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1))
				.addVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));
	}

	/**
	 * update variable using given Id and data if exist and return status code 204 : No Content
	 * 
	 * @throws Exception
	 */
	@Test
	public void updateVariable() throws Exception {

		VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();

		Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		Term propertyTerm = TestDataProvider.getPropertyTerm();
		Term methodTerm = TestDataProvider.getMethodTerm();
		Term scaleTerm = TestDataProvider.getScaleTerm();
		Term variableTerm = TestDataProvider.getVariableTerm();

		Scale scale = TestDataProvider.getTestScale();
		Variable variable = TestDataProvider.getTestVariable();
		variable.setMethod(new Method(methodTerm));
		variable.setProperty(new Property(propertyTerm));
		variable.setScale(scale);
	  	variable.setHasUsage(false);

		Integer methodId = StringUtil.parseInt(variableDetails.getMethod().getId(), null);
		Integer propertyId = StringUtil.parseInt(variableDetails.getProperty().getId(), null);
		Integer scaleId = StringUtil.parseInt(variableDetails.getScale().getId(), null);

		VariableFilter variableFilter = new VariableFilter();
		variableFilter.addMethodId(methodId);
		variableFilter.addPropertyId(propertyId);
		variableFilter.addScaleId(scaleId);

		Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(this.programUuid, this.cropName);
		Mockito.doReturn(variableTerm).when(this.termDataManager).getTermById(variableTerm.getId());
		Mockito.doReturn(variableTerm).when(this.termDataManager).getTermByNameAndCvId(variable.getName(), CvId.VARIABLES.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId, true);
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);
		Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(this.programUuid, variable.getId(), true, true);
		Mockito.doNothing().when(this.ontologyVariableDataManager).updateVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));

		this.mockMvc
				.perform(
						MockMvcRequestBuilders
								.put("/ontology/{cropname}/variables/{id}?programId=" + this.programUuid, this.cropName,
										variableTerm.getId()).contentType(this.contentType)
								.content(this.convertObjectToByte(variableDetails))).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isNoContent());

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).updateVariable(
				org.mockito.Matchers.any(OntologyVariableInfo.class));
	}

	/**
	 * delete variable using given Id if exist and return status code 204 : No Content
	 * 
	 * @throws Exception
	 */
	@Test
	public void deleteVariable() throws Exception {

		Term term = TestDataProvider.getVariableTerm();

		Variable ontologyVariable = TestDataProvider.getTestVariable();

		Mockito.doReturn(term).when(this.termDataManager).getTermById(ontologyVariable.getId());
		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(this.programUuid, ontologyVariable.getId(),
				true, true);
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(ontologyVariable.getId());
		Mockito.doNothing().when(this.ontologyVariableDataManager).deleteVariable(ontologyVariable.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.delete("/ontology/{cropname}/variables/{id}", this.cropName, ontologyVariable.getId())
								.contentType(this.contentType)).andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).deleteVariable(ontologyVariable.getId());
	}
}
