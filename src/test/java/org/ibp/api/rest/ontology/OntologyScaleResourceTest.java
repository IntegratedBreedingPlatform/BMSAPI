package org.ibp.api.rest.ontology;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.OntologyScale;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.ScaleRequestBase;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.builders.ScaleBuilder;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doAnswer;

public class OntologyScaleResourceTest extends ApiUnitTestBase {

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
		public OntologyScaleDataManager ontologyScaleDataManager() {
			return Mockito.mock(OntologyScaleDataManager.class);
		}
	}

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private OntologyBasicDataManager ontologyBasicDataManager;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	private final String scaleName = "scaleName";
	private final String scaleDescription = "scaleDescription";
	private final Map<String, String> categories = new HashMap<>();

	@Before
	public void reset() {
		Mockito.reset(this.ontologyBasicDataManager);
		Mockito.reset(this.ontologyScaleDataManager);
	}

	@After
	public void validate() {
		Mockito.validateMockitoUsage();
	}

	/**
	 * List all scales with details
	 *
	 * @throws Exception
	 */
	@Test
	public void listAllScales() throws Exception {

		this.categories.put("label", "value");
		this.categories.put("label2", "value2");

		List<OntologyScale> scaleList = new ArrayList<>();
		scaleList.add(new ScaleBuilder().build(1, this.scaleName, this.scaleDescription,DataType.NUMERIC_VARIABLE, "10", "20", null));
		scaleList.add(new ScaleBuilder().build(2, this.scaleName + "2",this.scaleDescription + "2", DataType.NUMERIC_VARIABLE, "30", "40", null));
		scaleList.add(new ScaleBuilder().build(3, this.scaleName + "3", this.scaleDescription + "3", DataType.CATEGORICAL_VARIABLE, "", "", this.categories));

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(scaleList).when(this.ontologyScaleDataManager).getAllScales();

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/scales", cropName)
				.contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$",IsCollectionWithSize.hasSize(scaleList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is("1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(scaleList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(scaleList.get(0).getDefinition())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager, Mockito.times(1)).getAllScales();
	}

	/**
	 * Get a scale if exist using given scale id
	 *
	 * @throws Exception
	 */
	@Test
	public void getScaleById() throws Exception {

		OntologyScale scale = new ScaleBuilder().build(1, this.scaleName, this.scaleDescription, DataType.NUMERIC_VARIABLE, "10", "20", null);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(1);
		Mockito.doReturn(new Term(1, this.scaleName, this.scaleDescription, CvId.SCALES.getId(), false)).when(this.ontologyBasicDataManager).getTermById(1);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/scales/{id}", cropName, String.valueOf(1)).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is("1")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(scale.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(scale.getDefinition()))).andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager, Mockito.times(1)).getScaleById(1);
	}

	/**
	 * Add new scale with provided data and return id of newly generated scale
	 *
	 * @throws Exception
	 */
	@Test
	public void addScale() throws Exception {

		ValidValues validValues = new ValidValues();
		validValues.setMin("10");
		validValues.setMax("20");

		ScaleRequestBase scaleRequest = new ScaleRequestBase();
		scaleRequest.setName(this.scaleName);
		scaleRequest.setDescription(this.scaleDescription);
		scaleRequest.setDataTypeId("1110");
		scaleRequest.setValidValues(validValues);

		OntologyScale scale = new OntologyScale();
		scale.setName(this.scaleName);
		scale.setDefinition(this.scaleDescription);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);

		//Mock Scale Class and when addScale method called it will set id to 1 and return (self member alter if void is return type of method)
		doAnswer(new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					OntologyScale entity = (OntologyScale) arguments[0];
					entity.setId(1);
				}
				return null;
			}
		}).when(this.ontologyScaleDataManager).addScale(org.mockito.Matchers.any(OntologyScale.class));

		this.mockMvc.perform(MockMvcRequestBuilders.post("/ontology/{cropname}/scales", cropName)
				.contentType(this.contentType).content(this.convertObjectToByte(scaleRequest)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager).addScale(org.mockito.Matchers.any(OntologyScale.class));
	}

	/**
	 * Update a scale if exist
	 *
	 * @throws Exception
	 */
	@Test
	public void updateScale() throws Exception {

		ValidValues validValues = new ValidValues();
		validValues.setMin("10");
		validValues.setMax("20");

		ScaleRequestBase scaleRequest = new ScaleRequestBase();
		scaleRequest.setName(this.scaleName);
		scaleRequest.setDescription(this.scaleDescription);
		scaleRequest.setDataTypeId("1110");
		scaleRequest.setValidValues(validValues);

		OntologyScale scale = new OntologyScale(new Term(1, this.scaleName, this.scaleDescription));

		ArgumentCaptor<OntologyScale> captor = ArgumentCaptor.forClass(OntologyScale.class);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doNothing().when(this.ontologyScaleDataManager).updateScale(org.mockito.Matchers.any(OntologyScale.class));
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.put("/ontology/{cropname}/scales/{id}", cropName, scale.getId())
				.contentType(this.contentType)
				.content(this.convertObjectToByte(scaleRequest)))
				.andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager).updateScale(captor.capture());

		OntologyScale captured = captor.getValue();

		Assert.assertEquals(scale.getName(), captured.getName());
		Assert.assertEquals(scale.getDefinition(), captured.getDefinition());
	}

	/**
	 * Delete a scale if exist and not referred
	 *
	 * @throws Exception
	 */
	@Test
	public void deleteScale() throws Exception {

		Term term = new Term(10, "name", "", CvId.SCALES.getId(), false);
		OntologyScale scale = new OntologyScale(term);

		Mockito.doReturn(new CropType(cropName)).when(this.workbenchDataManager).getCropTypeByName(cropName);
		Mockito.doReturn(term).when(this.ontologyBasicDataManager).getTermById(scale.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(false).when(this.ontologyBasicDataManager).isTermReferred(scale.getId());
		Mockito.doNothing().when(this.ontologyScaleDataManager).deleteScale(scale.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/ontology/{cropname}/scales/{id}", cropName,scale.getId()).contentType(this.contentType))
						.andExpect(MockMvcResultMatchers.status().isNoContent())
						.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager, Mockito.times(1)).deleteScale(scale.getId());
	}
}
