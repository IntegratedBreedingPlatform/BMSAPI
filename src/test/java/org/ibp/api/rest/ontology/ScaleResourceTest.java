
package org.ibp.api.rest.ontology;

import java.util.List;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.ScaleSummary;
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

/**
 * Tests to check Scale API Services Extended from {@link ApiUnitTestBase} for basic mock of services and common methods
 */
public class ScaleResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public TermDataManager termDataManager() {
			return Mockito.mock(TermDataManager.class);
		}

		@Bean
		@Primary
		public OntologyScaleDataManager ontologyScaleDataManager() {
			return Mockito.mock(OntologyScaleDataManager.class);
		}
	}

	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Before
	public void reset() {
		Mockito.reset(this.termDataManager);
		Mockito.reset(this.ontologyScaleDataManager);
	}

	/**
	 * List all scales with details
	 *
	 * @throws Exception
	 */
	@Test
	public void listAllScales() throws Exception {

		List<Scale> scaleList = TestDataProvider.getTestScales(3);

		Mockito.doReturn(scaleList).when(this.ontologyScaleDataManager).getAllScales();

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/ontology/{cropname}/scales", this.cropName).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(scaleList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(scaleList.get(0).getId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(scaleList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(scaleList.get(0).getDefinition())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].validValues.min", Matchers.is(StringUtil.parseInt(scaleList.get(0).getMinValue(), null))))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].validValues.max", Matchers.is(StringUtil.parseInt(scaleList.get(0).getMaxValue(), null))))
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

		Scale scale = TestDataProvider.getTestScale();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scale.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.get("/ontology/{cropname}/scales/{id}", this.cropName, scale.getId()).contentType(
								this.contentType))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(scale.getId()))))
		.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(scale.getName())))
		.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(scale.getDefinition())))
		.andExpect(MockMvcResultMatchers.jsonPath("$.validValues.min", Matchers.is(scale.getMinValue())))
		.andExpect(MockMvcResultMatchers.jsonPath("$.validValues.max", Matchers.is(scale.getMaxValue())))
		.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager, Mockito.times(1)).getScaleById(scale.getId());
	}

	/**
	 * Add new scale with provided data and return id of newly generated scale
	 *
	 * @throws Exception
	 */
	@Test
	public void addScale() throws Exception {

		final Scale scale = TestDataProvider.getTestScale();

		// Mock Scale Class and when addScale method called it will set id to 1 and return (self member alter if void is return type of
		// method)
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					Scale entity = (Scale) arguments[0];
					entity.setId(scale.getId());
				}
				return null;
			}
		}).when(this.ontologyScaleDataManager).addScale(org.mockito.Matchers.any(Scale.class));

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();
		scaleSummary.setId(null);

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.post("/ontology/{cropname}/scales", this.cropName).contentType(this.contentType)
								.content(this.convertObjectToByte(scaleSummary))).andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(scale.getId()))))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager).addScale(org.mockito.Matchers.any(Scale.class));
	}

	/**
	 * Update a scale if exist
	 *
	 * @throws Exception
	 */
	@Test
	public void updateScale() throws Exception {

		Scale scale = TestDataProvider.getTestScale();
		Term scaleTerm = TestDataProvider.getScaleTerm();

		ArgumentCaptor<Scale> captor = ArgumentCaptor.forClass(Scale.class);

		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scale.getId());
		Mockito.doNothing().when(this.ontologyScaleDataManager).updateScale(org.mockito.Matchers.any(Scale.class));

		ScaleSummary scaleSummary = TestDataProvider.getTestScaleSummary();

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.put("/ontology/{cropname}/scales/{id}", this.cropName, scale.getId())
								.contentType(this.contentType).content(this.convertObjectToByte(scaleSummary)))
				.andExpect(MockMvcResultMatchers.status().isNoContent()).andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager).updateScale(captor.capture());

		Scale captured = captor.getValue();

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

		Term scaleTerm = TestDataProvider.getScaleTerm();
		Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scale.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId());
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(scale.getId());
		Mockito.doNothing().when(this.ontologyScaleDataManager).deleteScale(scale.getId());

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.delete("/ontology/{cropname}/scales/{id}", this.cropName, scale.getId()).contentType(
								this.contentType)).andExpect(MockMvcResultMatchers.status().isNoContent())
		.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager, Mockito.times(1)).deleteScale(scale.getId());
	}
}
