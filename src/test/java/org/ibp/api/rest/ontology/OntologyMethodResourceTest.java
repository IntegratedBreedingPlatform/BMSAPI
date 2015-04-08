package org.ibp.api.rest.ontology;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.MethodRequest;
import org.ibp.builders.MethodBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

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
		public OntologyManagerService ontologyManagerService() {
			return Mockito.mock(OntologyManagerService.class);
		}
	}

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private OntologyManagerService ontologyManagerService;

	@Before
	public void reset() {
		Mockito.reset(this.ontologyManagerService);
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
		Mockito.doReturn(methodList).when(this.ontologyManagerService).getAllMethods();

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/methods", cropName)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$",IsCollectionWithSize.hasSize(methodList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(methodList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(methodList.get(0).getDefinition())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyManagerService, Mockito.times(1)).getAllMethods();
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
		Mockito.doReturn(new Term(1, method.getName(), method.getDefinition(), CvId.METHODS.getId(), false)).when(this.ontologyManagerService).getTermById(1);
		Mockito.doReturn(method).when(this.ontologyManagerService).getMethod(1);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/methods/{id}", cropName, 1)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(method.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(method.getDefinition())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyManagerService, Mockito.times(1)).getMethod(1);
	}

	/**
	 * This test should expect 400 * *
	 *
	 * @throws Exception
	 */
	@Test
	public void getMethodById_Should_Respond_With_400_For_Invalid_Id() throws Exception {

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(null).when(this.ontologyManagerService).getTermById(1);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/methods/{id}", cropName, 1)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyManagerService, Mockito.times(1)).getTermById(1);
	}

	/**
	 * This test should expect 201 : Created*
	 *
	 * @throws Exception
	 */
	@Test
	public void addMethod() throws Exception {

		MethodRequest methodDTO = new MethodRequest();
		methodDTO.setName("methodName");
		methodDTO.setDescription("methodDescription");

		Method method = new Method();
		method.setName(methodDTO.getName());
		method.setDefinition(methodDTO.getDescription());

		ArgumentCaptor<Method> captor = ArgumentCaptor.forClass(Method.class);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doNothing().when(this.ontologyManagerService).addMethod(org.mockito.Matchers.any(Method.class));

		this.mockMvc.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/methods", cropName)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(methodDTO)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(0)))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyManagerService).addMethod(captor.capture());

	}

	/**
	 * This test should expect 204 : No Content
	 *
	 * @throws Exception
	 */
	@Test
	public void updateMethod() throws Exception {

		MethodRequest methodDTO = new MethodRequest();
		methodDTO.setName("methodName");
		methodDTO.setDescription("methodDescription");

		Method method = new Method(new Term(10, methodDTO.getName(), methodDTO.getDescription()));

		/**
		 * We Need equals method inside Method (Middleware) because it throws
		 * hashcode matching error. So Added ArgumentCaptor that will implement
		 * equals()
		 */
		ArgumentCaptor<Method> captor = ArgumentCaptor.forClass(Method.class);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doNothing().when(this.ontologyManagerService).updateMethod(org.mockito.Matchers.any(Method.class));
		Mockito.doReturn(method).when(this.ontologyManagerService).getMethod(method.getId());

		this.mockMvc.perform(MockMvcRequestBuilders
				.put("/ontology/{cropname}/methods/{id}", cropName, method.getId())
				.contentType(this.contentType)
				.content(this.convertObjectToByte(methodDTO)))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyManagerService).updateMethod(captor.capture());

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
		Mockito.doReturn(term).when(this.ontologyManagerService).getTermById(method.getId());
		Mockito.doReturn(method).when(this.ontologyManagerService).getMethod(method.getId());
		Mockito.doReturn(false).when(this.ontologyManagerService).isTermReferred(method.getId());
		Mockito.doNothing().when(this.ontologyManagerService).deleteMethod(method.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/ontology/{cropname}/methods/{id}",cropName, method.getId())
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyManagerService, Mockito.times(1)).deleteMethod(method.getId());
	}
}
