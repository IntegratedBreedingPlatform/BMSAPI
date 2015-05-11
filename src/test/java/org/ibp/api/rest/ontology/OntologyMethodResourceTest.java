package org.ibp.api.rest.ontology;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.builders.MethodBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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

import static org.mockito.Mockito.doAnswer;

public class OntologyMethodResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public WorkbenchDataManager workbenchDataManager() {
			return Mockito.mock(WorkbenchDataManager.class);
		}

		@Bean
		@Primary
		public OntologyBasicDataManager ontologyBasicDataManager() {
			return Mockito.mock(OntologyBasicDataManager.class);
		}

		@Bean
		@Primary
		public OntologyMethodDataManager ontologyMethodDataManager(){
			return Mockito.mock(OntologyMethodDataManager.class);
		}
	}

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private OntologyBasicDataManager ontologyBasicDataManager;

	@Autowired
	private OntologyMethodDataManager ontologyMethodDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.ontologyBasicDataManager);
		Mockito.reset(this.ontologyMethodDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void listAllMethods() throws Exception {

		List<Method> methodList = new ArrayList<>();
		methodList.add(new MethodBuilder().build(1, "m1", "d1"));
		methodList.add(new MethodBuilder().build(2, "m2", "d2"));
		methodList.add(new MethodBuilder().build(3, "m3", "d3"));

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(methodList).when(this.ontologyMethodDataManager).getAllMethods();

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/methods", cropName)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$",IsCollectionWithSize.hasSize(methodList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is("1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(methodList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(methodList.get(0).getDefinition())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyMethodDataManager, Mockito.times(1)).getAllMethods();
	}

	/**
	 * Get a method with id. It should respond with 200 and method data. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void getMethodById() throws Exception {

		Method method = new MethodBuilder().build(1, "m1", "d1");

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(new Term(1, method.getName(), method.getDefinition(), CvId.METHODS.getId(), false)).when(this.ontologyBasicDataManager).getTermById(1);
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(1);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/methods/{id}", cropName, 1)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is("1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(method.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(method.getDefinition())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyMethodDataManager, Mockito.times(1)).getMethod(1);
	}

	/**
	 * This test should expect 400 * *
	 *
	 * @throws Exception
	 */
	@Test
	public void getMethodById_Should_Respond_With_400_For_Invalid_Id() throws Exception {

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(null).when(this.ontologyBasicDataManager).getTermById(1);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/methods/{id}", cropName, 1)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyBasicDataManager, Mockito.times(1)).getTermById(1);
	}

	/**
	 * This test should expect 201 : Created*
	 *
	 * @throws Exception
	 */
	@Test
	public void addMethod() throws Exception {

		MethodSummary methodDTO = new MethodSummary();
		methodDTO.setName("methodName");
		methodDTO.setDescription("methodDescription");

		Method method = new Method();
		method.setName(methodDTO.getName());
		method.setDefinition(methodDTO.getDescription());

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);

		//Mock Method Class and when addMethod method called it will set id to 1 and return (self member alter if void is return type of method)
		doAnswer(new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					Method entity = (Method) arguments[0];
					entity.setId(1);
				}
				return null;
			}
		}).when(this.ontologyMethodDataManager).addMethod(org.mockito.Matchers.any(Method.class));

		this.mockMvc.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/methods", cropName)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(methodDTO)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyMethodDataManager).addMethod(org.mockito.Matchers.any(Method.class));
	}

	/**
	 * This test should expect 204 : No Content
	 *
	 * @throws Exception
	 */
	@Test
	public void updateMethod() throws Exception {

		MethodSummary methodDTO = new MethodSummary();
		methodDTO.setName("methodName");
		methodDTO.setDescription("methodDescription");

		Method method = new Method(new Term(10, methodDTO.getName(), methodDTO.getDescription()));

		/**
		 * We Need equals method inside Method (Middleware) because it throws
		 * hashcode matching error. So Added ArgumentCaptor that will implement
		 * equals()
		 */
		ArgumentCaptor<Method> captor = ArgumentCaptor.forClass(Method.class);

		Term term = new Term(10, "method name", "methodDescription");
		term.setVocabularyId(1020);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(term).when(this.ontologyBasicDataManager).getTermById(method.getId());
		Mockito.doReturn(new Term(11, "method name", "methodDescription", CvId.METHODS.getId(),false)).when(this.ontologyBasicDataManager).getTermByNameAndCvId("method name", CvId.METHODS.getId());
		Mockito.doNothing().when(this.ontologyMethodDataManager).updateMethod(org.mockito.Matchers.any(Method.class));
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(method.getId());

		this.mockMvc.perform(MockMvcRequestBuilders
				.put("/ontology/{cropname}/methods/{id}", cropName, method.getId())
				.contentType(this.contentType)
				.content(this.convertObjectToByte(methodDTO)))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyMethodDataManager).updateMethod(captor.capture());

		Method captured = captor.getValue();

		Assert.assertEquals(method.getName(), captured.getName());
		Assert.assertEquals(method.getDefinition(), captured.getDefinition());
	}

	/**
	 * This test should expect 204 : No Content
	 *
	 * @throws Exception
	 */
	@Test
	public void deleteMethod() throws Exception {

		Term term = new Term(10, "name", "", CvId.METHODS.getId(), false);
		Method method = new Method(term);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(term).when(this.ontologyBasicDataManager).getTermById(method.getId());
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(method.getId());
		Mockito.doReturn(false).when(this.ontologyBasicDataManager).isTermReferred(method.getId());
		Mockito.doNothing().when(this.ontologyMethodDataManager).deleteMethod(method.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/ontology/{cropname}/methods/{id}",cropName, method.getId())
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyMethodDataManager, Mockito.times(1)).deleteMethod(method.getId());
	}
}
