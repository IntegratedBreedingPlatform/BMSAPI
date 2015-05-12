package org.ibp.api.rest.ontology;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.ibp.api.domain.ontology.PropertyRequestBase;
import org.ibp.builders.PropertyBuilder;
import org.junit.After;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doAnswer;

public class OntologyPropertyResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public WorkbenchDataManager workbenchDataManager() {
			return Mockito.mock(WorkbenchDataManager.class);
		}

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

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.termDataManager);
		Mockito.reset(this.ontologyPropertyDataManager);
	}

	private static final String propertyName = "My Property";

	private static final String propertyDescription = "Description";

	private static final String className1 = "Study condition";
	private static final String className2 = "Biotic stress";

	private static final List<String> classes = new ArrayList<>(Arrays.asList(className1, className2));

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	@Test
	public void listAllProperties() throws Exception {

		List<Property> propertyList = new ArrayList<>();
		propertyList.add(new PropertyBuilder().build(1, "p1", "d1", "CO:000001", classes));
		propertyList.add(new PropertyBuilder().build(2, "p2", "d2", "CO:000002", classes));
		propertyList.add(new PropertyBuilder().build(3, "p3", "d3", "CO:000003", classes));

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(propertyList).when(this.ontologyPropertyDataManager).getAllProperties();

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/properties", cropName)
				.contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$",IsCollectionWithSize.hasSize(propertyList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is("1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(propertyList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description",Matchers.is(propertyList.get(0).getDefinition())));

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).getAllProperties();
	}

	/**
	 * Get a property with id. It should respond with 200 and property data. * *
	 * 
	 * @throws Exception
	 */
	@Test
	public void getPropertyById() throws Exception {

		Property property = new PropertyBuilder().build(1, "property", "description", "CO:000001", OntologyPropertyResourceTest.classes);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(new Term(1, property.getName(), property.getDefinition(), CvId.PROPERTIES.getId(),false)).when(this.termDataManager).getTermById(1);
		Mockito.doReturn(property).when(this.ontologyPropertyDataManager).getProperty(1);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/properties/{id}", cropName, property.getId())
				.contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(property.getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(property.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description",Matchers.is(property.getDefinition())));

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).getProperty(1);
	}

	/**
	 * This test should expect 400 if no Property Found * *
	 * 
	 * @throws Exception
	 */
	@Test
	public void getPropertyById_Should_Respond_With_400_For_Invalid_Id() throws Exception {

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(null).when(this.termDataManager).getTermById(1);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/properties/{id}", cropName, 1)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.termDataManager, Mockito.times(1)).getTermById(1);
	}

	/*
	 * This test should expect 201 : Created*
	 * 
	 * @throws Exception
	 */
	@Test
	public void addProperty() throws Exception {

		PropertyRequestBase propertyDTO = new PropertyRequestBase();
		propertyDTO.setName(OntologyPropertyResourceTest.propertyName);
		propertyDTO.setDescription(OntologyPropertyResourceTest.propertyDescription);
		propertyDTO.setCropOntologyId("CO:000001");
		propertyDTO.setClasses(new ArrayList<>(Collections.singletonList(OntologyPropertyResourceTest.className1)));

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(null).when(this.termDataManager).getTermByNameAndCvId(OntologyPropertyResourceTest.propertyName,
				CvId.PROPERTIES.getId());

		//Mock Property Class and when addProperty method called it will set id to 1 and return (self member alter if void is return type of method)
		doAnswer(new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					Property entity = (Property) arguments[0];
					entity.setId(1);
				}
				return null;
			}
		}).when(this.ontologyPropertyDataManager).addProperty(org.mockito.Matchers.any(Property.class));

		this.mockMvc.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/properties", cropName)
				.contentType(this.contentType)
				.content(this.convertObjectToByte(propertyDTO)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
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

		PropertyRequestBase propertyDTO = new PropertyRequestBase();
		propertyDTO.setName(OntologyPropertyResourceTest.propertyName);
		propertyDTO.setDescription(OntologyPropertyResourceTest.propertyDescription);
		propertyDTO.setCropOntologyId("CO:000001");
		propertyDTO.setClasses(OntologyPropertyResourceTest.classes);

		Property property = new PropertyBuilder().build(11, propertyDTO.getName(),propertyDTO.getDescription(), propertyDTO.getCropOntologyId(),OntologyPropertyResourceTest.classes);

		ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(new Term(11, OntologyPropertyResourceTest.propertyName,OntologyPropertyResourceTest.propertyDescription, CvId.PROPERTIES.getId(),false)).when(this.termDataManager).getTermByNameAndCvId(OntologyPropertyResourceTest.propertyName, CvId.PROPERTIES.getId());
		Mockito.doReturn(property).when(this.ontologyPropertyDataManager).getProperty(property.getId());
		Mockito.doNothing().when(this.ontologyPropertyDataManager).updateProperty(org.mockito.Matchers.any(Property.class));

		this.mockMvc.perform(MockMvcRequestBuilders.put("/ontology/{cropname}/properties/{id}", cropName, property.getId())
				.contentType(this.contentType).content(this.convertObjectToByte(propertyDTO)))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).updateProperty(captor.capture());
	}

	/**
	 * This test should expect 204 : No Content
	 * 
	 * @throws Exception
	 */
	@Test
	public void deleteProperty() throws Exception {

		PropertyRequest propertyDTO = new PropertyRequest();
		propertyDTO.setName(OntologyPropertyResourceTest.propertyName);
		propertyDTO.setDescription(OntologyPropertyResourceTest.propertyDescription);
		propertyDTO.setCropOntologyId("CO:000001");
		propertyDTO.setClasses(OntologyPropertyResourceTest.classes);

		Term term = new Term(10, propertyDTO.getName(), propertyDTO.getDescription(),CvId.PROPERTIES.getId(), false);

		Property property = new PropertyBuilder().build(10, propertyDTO.getName(),propertyDTO.getDescription(), propertyDTO.getCropOntologyId(),OntologyPropertyResourceTest.classes);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(term).when(this.termDataManager).getTermById(term.getId());
		Mockito.doReturn(property).when(this.ontologyPropertyDataManager).getProperty(property.getId());
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(property.getId());
		Mockito.doNothing().when(this.ontologyPropertyDataManager).deleteProperty(property.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/ontology/{cropname}/properties/{id}", cropName, property.getId())
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyPropertyDataManager, Mockito.times(1)).deleteProperty(property.getId());
	}
}
