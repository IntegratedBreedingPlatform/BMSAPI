
package org.ibp.api.rest.ontology;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.OntologyVariableInfo;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.StringUtil;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;

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
	protected ProgramService programService;

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

		final List<Variable> variables = TestDataProvider.getTestVariables(4);

		final Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		Mockito.doReturn(project).when(this.programService).getProjectByUuidAndCrop(this.programUuid, this.cropName);
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.setProgramUuid(this.programUuid);
		Mockito.doReturn(variables).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{cropname}/variables?programUUID=" + this.programUuid, this.cropName)
			.contentType(this.contentType)).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(variables.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", is(String.valueOf(variables.get(0).getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", is(variables.get(0).getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", is(variables.get(0).getDefinition())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].alias", is(variables.get(0).getAlias())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].property.id", is(String.valueOf(variables.get(0).getProperty().getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].property.name", is(variables.get(0).getProperty().getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].method.id", is(String.valueOf(variables.get(0).getMethod().getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].method.name", is(variables.get(0).getMethod().getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].scale.id", is(String.valueOf(variables.get(0).getScale().getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].scale.name", is(variables.get(0).getScale().getName())))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$[0].scale.dataType.id", is(String.valueOf(variables.get(0).getScale().getDataType().getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].scale.dataType.name", is(variables.get(0).getScale().getDataType().getName())))
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

		final Variable ontologyVariable = TestDataProvider.getTestVariable();

		final Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		Mockito.doReturn(project).when(this.programService).getProjectByUuidAndCrop(this.programUuid, this.cropName);
		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(this.programUuid, ontologyVariable.getId(),
				true);
		Mockito.doReturn(
				new Term(ontologyVariable.getId(), ontologyVariable.getName(), ontologyVariable.getDefinition(), CvId.VARIABLES.getId(),
						false)).when(this.termDataManager).getTermById(ontologyVariable.getId());
		Mockito.doReturn(true).when(this.modelService).isNumericDataType(String.valueOf(ontologyVariable.getScale().getDataType().getId()));

		this.mockMvc
			.perform(
				MockMvcRequestBuilders.get("/crops/{cropname}/variables/{id}?programUUID=" + this.programUuid, this.cropName,
					ontologyVariable.getId()).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$.name", is(ontologyVariable.getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.description", is(ontologyVariable.getDefinition())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.alias", is(ontologyVariable.getAlias())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.method.id", is(String.valueOf(ontologyVariable.getMethod().getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.method.name", is(ontologyVariable.getMethod().getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.property.id", is(String.valueOf(ontologyVariable.getProperty().getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.property.name", is(ontologyVariable.getProperty().getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.scale.id", is(String.valueOf(ontologyVariable.getScale().getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.scale.name", is(ontologyVariable.getScale().getName())))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.scale.dataType.id", is(String.valueOf(ontologyVariable.getScale().getDataType().getId()))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.scale.dataType.name", is(ontologyVariable.getScale().getDataType().getName())))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.scale.validValues.min", is(StringUtil.parseDouble(ontologyVariable.getScale().getMinValue(), null))))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.scale.validValues.max", is(StringUtil.parseDouble(ontologyVariable.getScale().getMaxValue(), null))))
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.variableTypes", IsCollectionWithSize.hasSize(ontologyVariable.getVariableTypes().size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.formula.formulaId", is(ontologyVariable.getFormula().getFormulaId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.formula.target.id", is(ontologyVariable.getFormula().getTarget().getId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.formula.definition", is(ontologyVariable.getFormula().getDefinition())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.allowsFormula", is(ontologyVariable.isAllowsFormula())))
		;

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).getVariable(this.programUuid, ontologyVariable.getId(), true);

	}

	/**
	 * add new variable and return new generated variable Id with status code 201 : Created
	 *
	 * @throws Exception
	 */
	@Test
	public void addVariable() throws Exception {
		final Term variableTerm = TestDataProvider.getVariableTerm();
		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();

		final Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		// Set variable id to null for post request.
		variableDetails.setId(null);
		final Integer methodId = StringUtil.parseInt(variableDetails.getMethod().getId(), null);
		final Integer propertyId = StringUtil.parseInt(variableDetails.getProperty().getId(), null);
		final Integer scaleId = StringUtil.parseInt(variableDetails.getScale().getId(), null);

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addMethodId(methodId);
		variableFilter.addPropertyId(propertyId);
		variableFilter.addScaleId(scaleId);

		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(variableDetails.getName(), CvId.VARIABLES.getId());
		Mockito.doReturn(TestDataProvider.getTestScale()).when(this.ontologyScaleDataManager).getScaleById(scaleId, true);
		Mockito.doReturn(TestDataProvider.getPropertyTerm()).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(TestDataProvider.getMethodTerm()).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(TestDataProvider.getScaleTerm()).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);
		Mockito.doReturn(project).when(this.programService).getProjectByUuidAndCrop(this.programUuid, this.cropName);

		// Mock OntologyVariableInfo Class and when addDatasetVariable method called it will set id to 1 and return (self member alter if void is
		// return type of method)
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) {
				final Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					final OntologyVariableInfo entity = (OntologyVariableInfo) arguments[0];
					entity.setId(variableTerm.getId());
				}
				return null;
			}
		}).when(this.ontologyVariableDataManager).addVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.post("/crops/{cropname}/variables?programUUID=" + this.programUuid, this.cropName)
								.contentType(this.contentType).content(this.convertObjectToByte(variableDetails)))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", is(String.valueOf(variableTerm.getId()))));

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

		final VariableDetails variableDetails = TestDataProvider.getTestVariableDetails();

		final Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		final Term propertyTerm = TestDataProvider.getPropertyTerm();
		final Term methodTerm = TestDataProvider.getMethodTerm();
		final Term scaleTerm = TestDataProvider.getScaleTerm();
		final Term variableTerm = TestDataProvider.getVariableTerm();

		final Scale scale = TestDataProvider.getTestScale();
		final Variable variable = TestDataProvider.getTestVariable();
		variable.setMethod(new Method(methodTerm));
		variable.setProperty(new Property(propertyTerm));
		variable.setScale(scale);
	  	variable.setHasUsage(false);

		final Integer methodId = StringUtil.parseInt(variableDetails.getMethod().getId(), null);
		final Integer propertyId = StringUtil.parseInt(variableDetails.getProperty().getId(), null);
		final Integer scaleId = StringUtil.parseInt(variableDetails.getScale().getId(), null);

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addMethodId(methodId);
		variableFilter.addPropertyId(propertyId);
		variableFilter.addScaleId(scaleId);

		Mockito.doReturn(project).when(this.programService).getProjectByUuidAndCrop(this.programUuid, this.cropName);
		Mockito.doReturn(variableTerm).when(this.termDataManager).getTermById(variableTerm.getId());
		Mockito.doReturn(variableTerm).when(this.termDataManager).getTermByNameAndCvId(variable.getName(), CvId.VARIABLES.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId, true);
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(propertyId);
		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(methodId);
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scaleId);
		Mockito.doReturn(new ArrayList<>()).when(this.ontologyVariableDataManager).getWithFilter(variableFilter);
		Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(this.programUuid, variable.getId(), true);

		Mockito.doNothing().when(this.ontologyVariableDataManager).updateVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));

		this.mockMvc
				.perform(
						MockMvcRequestBuilders
								.put("/crops/{cropname}/variables/{id}?programUUID=" + this.programUuid, this.cropName,
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

		final Term term = TestDataProvider.getVariableTerm();

		final Variable ontologyVariable = TestDataProvider.getTestVariable();

		Mockito.doReturn(term).when(this.termDataManager).getTermById(ontologyVariable.getId());
		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(null, ontologyVariable.getId(),
				true);
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(ontologyVariable.getId());
		Mockito.doNothing().when(this.ontologyVariableDataManager).deleteVariable(ontologyVariable.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.delete("/crops/{cropname}/variables/{id}?programUUID=" + this.programUuid, this.cropName, ontologyVariable.getId())
								.contentType(this.contentType)).andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).deleteVariable(ontologyVariable.getId());
	}
}
