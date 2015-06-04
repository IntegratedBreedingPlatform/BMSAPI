
package org.ibp.api.rest.ontology;

import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.util.ISO8601DateParser;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.core.Is;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
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

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class MethodResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public TermDataManager termDataManager() {
			return Mockito.mock(TermDataManager.class);
		}

		@Bean
		@Primary
		public OntologyMethodDataManager ontologyMethodDataManager() {
			return Mockito.mock(OntologyMethodDataManager.class);
		}
	}

	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private OntologyMethodDataManager ontologyMethodDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.termDataManager);
		Mockito.reset(this.ontologyMethodDataManager);
	}

	@Test
	public void listAllMethods() throws Exception {

		List<Method> methodList = TestDataProvider.getTestMethodList(3);

		Mockito.doReturn(methodList).when(this.ontologyMethodDataManager).getAllMethods();

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/methods", this.cropName).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(methodList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Is.is(String.valueOf(methodList.get(0).getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Is.is(methodList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Is.is(methodList.get(0).getDefinition())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].metadata.dateCreated",
								Is.is(ISO8601DateParser.toString(methodList.get(0).getDateCreated()))))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].metadata.dateLastModified",
								Is.is(ISO8601DateParser.toString(methodList.get(0).getDateLastModified()))))
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

		Method method = TestDataProvider.getTestMethod();

		Mockito.doReturn(TestDataProvider.getMethodTerm()).when(this.termDataManager).getTermById(method.getId());
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(method.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.get("/ontology/{cropname}/methods/{id}", this.cropName, method.getId()).contentType(
								this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Is.is(String.valueOf(method.getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Is.is(method.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Is.is(method.getDefinition())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.metadata.dateCreated", Is.is(ISO8601DateParser.toString(method.getDateCreated()))))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.metadata.dateLastModified",
								Is.is(ISO8601DateParser.toString(method.getDateLastModified()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.deletable", Is.is(true)))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.metadata.editableFields",
								IsIterableContainingInOrder.contains("name", "description")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.usage.variables", Matchers.empty()))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyMethodDataManager, Mockito.times(1)).getMethod(method.getId());
	}

	/**
	 * This test should expect 400 * *
	 *
	 * @throws Exception
	 */
	@Test
	public void getMethodById_Should_Respond_With_400_For_Invalid_Id() throws Exception {

		Mockito.doReturn(null).when(this.termDataManager).getTermById(1);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/methods/{id}", this.cropName, 1).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isBadRequest()).andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.termDataManager, Mockito.times(1)).getTermById(1);
	}

	/**
	 * This test should expect 201 : Created*
	 *
	 * @throws Exception
	 */
	@Test
	public void addMethod() throws Exception {

		final MethodSummary methodSummary = TestDataProvider.getTestMethodSummary();

		// Setting id as null to ignore checking editable field validation.
		methodSummary.setId(null);

		final Method method = TestDataProvider.getTestMethod();

		// Mock Method Class and when addMethod method called it will set id to 1 and return (self member alter if void is return type of
		// method)
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					Method entity = (Method) arguments[0];
					entity.setId(method.getId());
				}
				return null;
			}
		}).when(this.ontologyMethodDataManager).addMethod(org.mockito.Matchers.any(Method.class));

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.post("/ontology/{cropname}/methods", this.cropName).contentType(this.contentType)
								.content(this.convertObjectToByte(methodSummary))).andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Is.is(String.valueOf(method.getId()))))
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

		MethodSummary methodSummary = TestDataProvider.getTestMethodSummary();
		Method method = TestDataProvider.getTestMethod();

		/**
		 * We Need equals method inside Method (Middleware) because it throws hashcode matching error. So Added ArgumentCaptor that will
		 * implement equals()
		 */
		ArgumentCaptor<Method> captor = ArgumentCaptor.forClass(Method.class);

		Term methodTerm = TestDataProvider.getMethodTerm();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(method.getId());
		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermByNameAndCvId("method name", CvId.METHODS.getId());
		Mockito.doNothing().when(this.ontologyMethodDataManager).updateMethod(org.mockito.Matchers.any(Method.class));
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(method.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.put("/ontology/{cropname}/methods/{id}", this.cropName, method.getId())
								.contentType(this.contentType).content(this.convertObjectToByte(methodSummary)))
				.andExpect(MockMvcResultMatchers.status().isNoContent()).andDo(MockMvcResultHandlers.print());

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

		Term methodTerm = TestDataProvider.getMethodTerm();
		Method method = TestDataProvider.getTestMethod();

		Mockito.doReturn(methodTerm).when(this.termDataManager).getTermById(method.getId());
		Mockito.doReturn(method).when(this.ontologyMethodDataManager).getMethod(method.getId());
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(method.getId());
		Mockito.doNothing().when(this.ontologyMethodDataManager).deleteMethod(method.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.delete("/ontology/{cropname}/methods/{id}", this.cropName, method.getId()).contentType(
								this.contentType)).andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyMethodDataManager, Mockito.times(1)).deleteMethod(method.getId());
	}
}
