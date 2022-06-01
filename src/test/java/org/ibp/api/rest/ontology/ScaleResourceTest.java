package org.ibp.api.rest.ontology;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.util.StringUtil;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.ibp.api.java.ontology.ModelService;
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

import java.util.List;

/**
 * Tests to check Scale API Services Extended from {@link ApiUnitTestBase} for basic mock of services and common methods
 */
public class ScaleResourceTest extends ApiUnitTestBase {

	private static final String PROGRAM_UUID = "50a7e02e-db60-4240-bd64-417b34606e46";


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

		@Bean
		@Primary
		public ModelService modelService() {
			return Mockito.mock(ModelService.class);
		}

		@Bean
		@Primary
		public org.generationcp.middleware.api.program.ProgramService programService() {
			return Mockito.mock( org.generationcp.middleware.api.program.ProgramService.class);
		}
	}


	@Autowired
	protected ModelService modelService;

	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private OntologyScaleDataManager ontologyScaleDataManager;

	@Autowired
	private ProgramService programService;

	@Autowired
	private ContextUtil contextUtil;

	@Before
	public void reset() {
		Mockito.reset(this.modelService);
		Mockito.reset(this.termDataManager);
		Mockito.reset(this.ontologyScaleDataManager);

		final Project project = new Project();
		project.setUniqueID(PROGRAM_UUID);
		project.setProjectId(1l);
		Mockito.doReturn(project).when(this.programService).getLastOpenedProjectAnyUser();
		Mockito.doReturn(PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
		ContextHolder.setCurrentProgram(PROGRAM_UUID);

	}

	/**
	 * List all scales with details
	 *
	 * @throws Exception
	 */
	@Test
	public void listAllScales() throws Exception {

		final List<Scale> scaleList = TestDataProvider.getTestScales(3);

		Mockito.doReturn(scaleList).when(this.ontologyScaleDataManager).getAllScales();
		Mockito.doReturn(true).when(this.modelService).isNumericDataType(String.valueOf(scaleList.get(0).getDataType().getId()));

		this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{cropname}/scales?programUUID=" + this.programUuid, this.cropName).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(scaleList.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(String.valueOf(scaleList.get(0).getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(scaleList.get(0).getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(scaleList.get(0).getDefinition()))).andExpect(
				MockMvcResultMatchers
						.jsonPath("$[0].validValues.min", Matchers.is(StringUtil.parseDouble(scaleList.get(0).getMinValue(), null))))
				.andExpect(MockMvcResultMatchers
						.jsonPath("$[0].validValues.max", Matchers.is(StringUtil.parseDouble(scaleList.get(0).getMaxValue(), null))))
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

		final Scale scale = TestDataProvider.getTestScale();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scale.getId());
		Mockito.doReturn(true).when(this.modelService).isNumericDataType(String.valueOf(scale.getDataType().getId()));

		this.mockMvc.perform(
				MockMvcRequestBuilders.get("/crops/{cropname}/scales/{id}?programUUID=" + this.programUuid, this.cropName, scale.getId()).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(scale.getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(scale.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(scale.getDefinition()))).andExpect(
				MockMvcResultMatchers.jsonPath("$.validValues.min", Matchers.is(StringUtil.parseDouble(scale.getMinValue(), null))))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.validValues.max", Matchers.is(StringUtil.parseDouble(scale.getMaxValue(), null))))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager, Mockito.times(1)).getScaleById(scale.getId(), true);
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
			public Void answer(final InvocationOnMock invocation) {
				final Object[] arguments = invocation.getArguments();
				if (arguments != null && arguments.length > 0 && arguments[0] != null) {
					final Scale entity = (Scale) arguments[0];
					entity.setId(scale.getId());
				}
				return null;
			}
		}).when(this.ontologyScaleDataManager).addScale(org.mockito.Matchers.any(Scale.class));

		final ScaleDetails scaleDetails = TestDataProvider.getTestScaleDetails();
		scaleDetails.setId(null);

		Mockito.doReturn(true).when(this.modelService).isNumericDataType(String.valueOf(scaleDetails.getDataType().getId()));

		this.mockMvc.perform(MockMvcRequestBuilders.post("/crops/{cropname}/scales?programUUID=" + this.programUuid, this.cropName).contentType(this.contentType)
				.content(this.convertObjectToByte(scaleDetails))).andExpect(MockMvcResultMatchers.status().isCreated())
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

		final Scale scale = TestDataProvider.getTestScale();
		final Term scaleTerm = TestDataProvider.getScaleTerm();

		final ArgumentCaptor<Scale> captor = ArgumentCaptor.forClass(Scale.class);

		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scale.getId());
		Mockito.doNothing().when(this.ontologyScaleDataManager).updateScale(org.mockito.Matchers.any(Scale.class));

		final ScaleDetails scaleDetails = TestDataProvider.getTestScaleDetails();

		Mockito.doReturn(true).when(this.modelService).isNumericDataType(String.valueOf(scaleDetails.getDataType().getId()));

		this.mockMvc.perform(
				MockMvcRequestBuilders.put("/crops/{cropname}/scales/{id}?programUUID=" + this.programUuid, this.cropName, scale.getId()).contentType(this.contentType)
						.content(this.convertObjectToByte(scaleDetails))).andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager).updateScale(captor.capture());

		final Scale captured = captor.getValue();

		Assert.assertEquals(scale.getName(), captured.getName());
		Assert.assertEquals(scale.getDefinition(), captured.getDefinition());
		Assert.assertEquals(PROGRAM_UUID, ContextHolder.getCurrentProgram());

	}

	/**
	 * Delete a scale if exist and not referred
	 *
	 * @throws Exception
	 */
	@Test
	public void deleteScale() throws Exception {

		final Term scaleTerm = TestDataProvider.getScaleTerm();
		final Scale scale = TestDataProvider.getTestScale();

		Mockito.doReturn(scaleTerm).when(this.termDataManager).getTermById(scale.getId());
		Mockito.doReturn(scale).when(this.ontologyScaleDataManager).getScaleById(scale.getId(), true);
		Mockito.doReturn(false).when(this.termDataManager).isTermReferred(scale.getId());
		Mockito.doNothing().when(this.ontologyScaleDataManager).deleteScale(scale.getId());

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/crops/{cropname}/scales/{id}?programUUID=" + this.programUuid, this.cropName, scale.getId())
				.contentType(this.contentType)).andExpect(MockMvcResultMatchers.status().isNoContent())
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.ontologyScaleDataManager, Mockito.times(1)).deleteScale(scale.getId());
	}
}
