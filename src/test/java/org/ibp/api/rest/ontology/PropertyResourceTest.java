
package org.ibp.api.rest.ontology;

import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
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

/**
 * Tests to check property API Services Extended from {@link ApiUnitTestBase} for basic mock of services and common methods
 */
public class PropertyResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public TermDataManager termDataManager() {
			return Mockito.mock(TermDataManager.class);
		}

		@Bean
		@Primary
		public OntologyPropertyDataManager ontologyManagerService() {
			return Mockito.mock(OntologyPropertyDataManager.class);
		}
	}

	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private OntologyPropertyDataManager ontologyPropertyDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.termDataManager);
		Mockito.reset(this.ontologyPropertyDataManager);
	}

	/**
	 * Get All properties. It should respond with 200.
	 * 
	 * @throws Exception
	 */
	@Test
	public void listAllProperties() throws Exception {

		List<Property> propertyList = TestDataProvider.getTestProperties(3);

		Mockito.doReturn(propertyList).when(this.ontologyPropertyDataManager).getAllProperties();

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/properties", this.cropName).contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(propertyList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(String.valueOf(propertyList.get(0).getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(propertyList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(propertyList.get(0).getDefinition())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].cropOntologyId", Matchers.is(propertyList.get(0).getCropOntologyId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].classes",
								IsCollectionWithSize.hasSize(propertyList.get(0).getClasses().size())));

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).getAllProperties();
	}

	/**
	 * Get All properties that have given class. It should respond with 200.
	 * 
	 * @throws Exception
	 */
	@Test
	public void listAllPropertiesUsingClass() throws Exception {

		String className = "Agronomic";

		List<Property> propertyList = TestDataProvider.getTestProperties(3);

		Mockito.doReturn(propertyList).when(this.ontologyPropertyDataManager).getAllPropertiesWithClass(className);

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.get("/ontology/{cropname}/properties?class=" + className, this.cropName).contentType(
								this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(propertyList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(String.valueOf(propertyList.get(0).getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(propertyList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(propertyList.get(0).getDefinition())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].cropOntologyId", Matchers.is(propertyList.get(0).getCropOntologyId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].classes",
								IsCollectionWithSize.hasSize(propertyList.get(0).getClasses().size())));

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).getAllPropertiesWithClass(className);
	}

	/**
	 * Get a property with id. It should respond with 200 and property data. * *
	 *
	 * @throws Exception
	 */
	@Test
	public void getPropertyById() throws Exception {

		Property property = TestDataProvider.getTestProperty();
		Term propertyTerm = TestDataProvider.getPropertyTerm();

		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(property.getId());
		Mockito.doReturn(property).when(this.ontologyPropertyDataManager).getProperty(property.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.get("/ontology/{cropname}/properties/{id}", this.cropName, property.getId()).contentType(
								this.contentType)).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(property.getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(property.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(property.getDefinition())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.cropOntologyId", Matchers.is(property.getCropOntologyId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.classes", IsCollectionWithSize.hasSize(property.getClasses().size())));

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).getProperty(property.getId());
	}

	/**
	 * This test should expect 400 if no Property Found * *
	 *
	 * @throws Exception
	 */
	@Test
	public void getPropertyById_Should_Respond_With_400_For_Invalid_Id() throws Exception {

		Mockito.doReturn(null).when(this.termDataManager).getTermById(1);

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/properties/{id}", this.cropName, 1).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isBadRequest()).andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.termDataManager, Mockito.times(1)).getTermById(1);
	}

	/*
	 * This test should expect 201 : Created*
	 * 
	 * @throws Exception
	 */
	@Test
	public void addProperty() throws Exception {

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();
		// Setting id as null to ignore checking editable field validation.
		propertyDetails.setId(null);

		final Property property = TestDataProvider.getTestProperty();

		// Mock Property Class and when addProperty method called it will set id to 1 and return (self member alter if void is return type
		// of method)
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					Property entity = (Property) arguments[0];
					entity.setId(property.getId());
				}
				return null;
			}
		}).when(this.ontologyPropertyDataManager).addProperty(org.mockito.Matchers.any(Property.class));

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.post("/ontology/{cropname}/properties", this.cropName).contentType(this.contentType)
								.content(this.convertObjectToByte(propertyDetails))).andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(property.getId()))))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).addProperty(org.mockito.Matchers.any(Property.class));
	}

	/*
	 * This test should expect 204 : No Content*
	 * 
	 * @throws Exception
	 */
	@Test
	public void updateProperty() throws Exception {

		PropertyDetails propertyDetails = TestDataProvider.getTestPropertyDetails();
		Property property = TestDataProvider.getTestProperty();
		Term propertyTerm = TestDataProvider.getPropertyTerm();

		ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);

		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermByNameAndCvId(property.getName(), CvId.PROPERTIES.getId());
		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(property.getId());
		Mockito.doReturn(property).when(this.ontologyPropertyDataManager).getProperty(property.getId());
		Mockito.doNothing().when(this.ontologyPropertyDataManager).updateProperty(org.mockito.Matchers.any(Property.class));

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.put("/ontology/{cropname}/properties/{id}", this.cropName, property.getId())
								.contentType(this.contentType).content(this.convertObjectToByte(propertyDetails)))
				.andExpect(MockMvcResultMatchers.status().isNoContent()).andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).updateProperty(captor.capture());
	}

	/**
	 * This test should expect 204 : No Content
	 *
	 * @throws Exception
	 */
	@Test
	public void deleteProperty() throws Exception {

		Property property = TestDataProvider.getTestProperty();
		Term propertyTerm = TestDataProvider.getPropertyTerm();

		Mockito.doReturn(propertyTerm).when(this.termDataManager).getTermById(property.getId());
		Mockito.doReturn(property).when(this.ontologyPropertyDataManager).getProperty(property.getId());
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(property.getId());
		Mockito.doNothing().when(this.ontologyPropertyDataManager).deleteProperty(property.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.delete("/ontology/{cropname}/properties/{id}", this.cropName, property.getId()).contentType(
								this.contentType)).andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).deleteProperty(property.getId());
	}
}
