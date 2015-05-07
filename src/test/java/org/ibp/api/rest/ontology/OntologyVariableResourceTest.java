package org.ibp.api.rest.ontology;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.OntologyVariableInfo;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.AddVariableRequest;
import org.ibp.api.domain.ontology.ExpectedRange;
import org.ibp.api.domain.ontology.UpdateVariableRequest;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.builders.MethodBuilder;
import org.ibp.builders.PropertyBuilder;
import org.ibp.builders.ScaleBuilder;
import org.ibp.builders.VariableBuilder;
import org.junit.After;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doAnswer;

public class OntologyVariableResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public OntologyBasicDataManager ontologyBasicDataManager() {
			return Mockito.mock(OntologyBasicDataManager.class);
		}

		@Bean
		@Primary
		public WorkbenchDataManager workbenchDataManager() {
			return Mockito.mock(WorkbenchDataManager.class);
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
	private OntologyBasicDataManager ontologyBasicDataManager;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

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

	private static final String className1 = "Study condition";
	private static final String className2 = "Biotic stress";

	private static final List<String> classes = new ArrayList<>(Arrays.asList(className1, className2));

	@Before
	public void reset() {
		Mockito.reset(this.ontologyBasicDataManager);
		Mockito.reset(this.ontologyVariableDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * List all variables with details with status code 200 : Ok
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
		Mockito.doReturn(new Project()).when(this.workbenchDataManager).getProjectByUuid(programUuid);
		Mockito.doReturn(variableSummaries).when(this.ontologyVariableDataManager).getWithFilter(programUuid, null, null, null, null);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/variables?programId=" + programUuid, cropName)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(variableSummaries.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(variableSummaries.get(0).getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(variableSummaries.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(variableSummaries.get(0).getDescription())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).getWithFilter(programUuid, null, null, null, null);
	}

	/**
	 * get variable by given id with status code 200 : Ok
	 * @throws Exception
	 */
  	@Test
  	public void getVariableById() throws Exception{
		Term term = new Term(1, this.variableName, this.variableDescription);
		Variable ontologyVariable = new Variable(term);

	  	ontologyVariable.setProperty(new PropertyBuilder().build(10, this.propertyName, this.propertyDescription, "CO:000001", classes));
	  	ontologyVariable.setMethod(new MethodBuilder().build(11, this.methodName, this.methodDescription));
	  	ontologyVariable.setScale(new ScaleBuilder().build(12, this.scaleName, this.scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null));
	  	ontologyVariable.setMinValue("14");
	  	ontologyVariable.setMaxValue("16");
	  	ontologyVariable.setAlias("Variable Alias Name");
	  	ontologyVariable.addVariableType(VariableType.getById(1));

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(new Project()).when(this.workbenchDataManager).getProjectByUuid(programUuid);
		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(programUuid, ontologyVariable.getId());
	  	Mockito.doReturn(new Term(1, this.variableName, this.variableDescription, CvId.VARIABLES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(ontologyVariable.getId());

		this.mockMvc.perform(MockMvcRequestBuilders
				.get("/ontology/{cropname}/variables/{id}?programId=" + programUuid, cropName, ontologyVariable.getId())
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(ontologyVariable.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(ontologyVariable.getDefinition())))
				.andDo(MockMvcResultHandlers.print());

	  Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).getVariable(programUuid, ontologyVariable.getId());
	}

	/**
	 * add new variable and return new generated variable Id with status code 201 : Created
	 * @throws Exception
	 */
  	@Test
  	public void addVariable() throws Exception{

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		AddVariableRequest request = new AddVariableRequest();
		request.setName(this.variableName);
		request.setDescription(this.variableDescription);
		request.setPropertyId("10");
		request.setMethodId("11");
		request.setScaleId("12");
	  	request.setVariableTypeIds(new ArrayList<>(Collections.singletonList("1")));
	  	request.setExpectedRange(expectedRange);

		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(new Project()).when(this.workbenchDataManager).getProjectByUuid(programUuid);
		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermByNameAndCvId(request.getName(), CvId.VARIABLES.getId());
		Mockito.doReturn(new ScaleBuilder().build(12, this.scaleName, this.scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null)).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(new Term(10, this.propertyName, this.propertyDescription, CvId.PROPERTIES.getId(), null)).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(new Term(11, this.methodName, this.methodDescription, CvId.METHODS.getId(), null)).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(new Term(12, this.scaleName, this.scaleDescription, CvId.SCALES.getId(), null)).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null,methodId, propertyId, scaleId);

	  	//Mock OntologyVariableInfo Class and when addVariable method called it will set id to 1 and return (self member alter if void is return type of method)
		doAnswer(new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
				  	OntologyVariableInfo entity = (OntologyVariableInfo) arguments[0];
				  	entity.setId(1);
				}
				return null;
			}
		}).when(this.ontologyVariableDataManager).addVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));

		this.mockMvc.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/variables?programId=" + programUuid, cropName)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(request)))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)));


	  Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).addVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));
	}

	/**
	 * update variable using given Id and data if exist and return status code 204 : No Content
	 * @throws Exception
	 */
	@Test
	public void updateVariable() throws Exception{

		ExpectedRange expectedRange = new ExpectedRange();
		expectedRange.setMin("12");
		expectedRange.setMax("16");

		UpdateVariableRequest request = new UpdateVariableRequest();
		request.setAlias("Test");
		request.setName(this.variableName);
		request.setDescription(this.variableDescription);
		request.setPropertyId("10");
		request.setMethodId("11");
		request.setScaleId("12");
		request.setVariableTypeIds(new ArrayList<>(Collections.singletonList("1")));
		request.setExpectedRange(expectedRange);

		Term propertyTerm = new Term(10, this.propertyName, this.propertyDescription, CvId.PROPERTIES.getId(), null);
		Term methodTerm = new Term(11, this.methodName, this.methodDescription, CvId.METHODS.getId(), null);
		Term scaleTerm = new Term(12, this.scaleName, this.scaleDescription, CvId.SCALES.getId(), null);
		Term variableTerm = new Term(1, this.variableName, this.variableDescription, CvId.VARIABLES.getId(), null);

		Scale scale = new ScaleBuilder().build(scaleTerm.getId(), scaleTerm.getName(), scaleTerm.getDefinition(), DataType.NUMERIC_VARIABLE, "10", "20", null);
		Variable variable = new Variable(variableTerm);
		variable.setMethod(new Method(methodTerm));
		variable.setProperty(new Property(propertyTerm));
		variable.setScale(scale);

		Integer scaleId = CommonUtil.tryParseSafe(request.getScaleId());
		Integer methodId = CommonUtil.tryParseSafe(request.getMethodId());
		Integer propertyId = CommonUtil.tryParseSafe(request.getPropertyId());

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(new Project()).when(this.workbenchDataManager).getProjectByUuid(programUuid);
		Mockito.doReturn(variableTerm).when(this.ontologyBasicDataManager).getTermById(variableTerm.getId());
		Mockito.doReturn(variableTerm).when(this.ontologyBasicDataManager).getTermByNameAndCvId(request.getName(), CvId.VARIABLES.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scaleId);
		Mockito.doReturn(propertyTerm).when(this.ontologyBasicDataManager).getTermById(propertyId);
		Mockito.doReturn(methodTerm).when(this.ontologyBasicDataManager).getTermById(methodId);
		Mockito.doReturn(scaleTerm).when(this.ontologyBasicDataManager).getTermById(scaleId);
		Mockito.doReturn(new ArrayList<OntologyVariableSummary>()).when(this.ontologyVariableDataManager).getWithFilter(null, null, methodId, propertyId, scaleId);
		Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(programUuid, variable.getId());
		Mockito.doNothing().when(this.ontologyVariableDataManager).updateVariable(org.mockito.Matchers.any(OntologyVariableInfo.class));

		this.mockMvc.perform(MockMvcRequestBuilders.put("/ontology/{cropname}/variables/{id}?programId=" + programUuid, cropName, variableTerm.getId())
				.contentType(this.contentType)
				.content(this.convertObjectToByte(request)))
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

		Term term = new Term(10, variableName, variableDescription, CvId.VARIABLES.getId(), false);
		Variable ontologyVariable = new Variable(term);

		ontologyVariable.setProperty(new PropertyBuilder().build(10, this.propertyName, this.propertyDescription, "CO:000001", classes));
		ontologyVariable.setMethod(new MethodBuilder().build(11, this.methodName, this.methodDescription));
		ontologyVariable.setScale(new ScaleBuilder().build(12, this.scaleName, this.scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null));
		ontologyVariable.setMinValue("14");
		ontologyVariable.setMaxValue("16");
		ontologyVariable.setAlias("Variable Alias Name");
		ontologyVariable.addVariableType(VariableType.getById(1));

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(new Project()).when(this.workbenchDataManager).getProjectByUuid(programUuid);
		Mockito.doReturn(term).when(this.ontologyBasicDataManager).getTermById(ontologyVariable.getId());
		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(programUuid, ontologyVariable.getId());
		Mockito.doReturn(false).when(this.ontologyBasicDataManager).isTermReferred(ontologyVariable.getId());
		Mockito.doNothing().when(this.ontologyVariableDataManager).deleteVariable(ontologyVariable.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/ontology/{cropname}/variables/{id}", cropName, ontologyVariable.getId())
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyVariableDataManager, Mockito.times(1)).deleteVariable(ontologyVariable.getId());
	}
}
