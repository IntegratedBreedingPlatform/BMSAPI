package org.ibp.api.rest.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.sample.SampleService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class SampleResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public SampleService sampleService() {
			return Mockito.mock(SampleService.class);
		}
	}

	@Autowired
	private SampleService sampleService;

	@Test
	public void testListSamplesNotFound() throws Exception {
		final String plotId = null;
		final Integer listId = null;

		final List<SampleDTO> list = new ArrayList<>();

		Mockito.when(this.sampleService.filter(null, null, null)).thenReturn(list);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/sample/maize/samples?plotId=" + plotId).contentType(this.contentType)) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(jsonPath("$", Matchers.empty())) //
		;
	}

	@Test
	public void testListSamples() throws Exception {
		final String plotId = randomAlphanumeric(13);
		final Integer listId = null;
		final Date samplingDate = new Date();
		final List<SampleDTO> list = new ArrayList<>();
		final SampleDTO sample =
			new SampleDTO(randomAlphanumeric(6), randomAlphanumeric(6), randomAlphanumeric(6), samplingDate, randomAlphanumeric(6),
				nextInt(), randomAlphanumeric(6), nextInt());
		list.add(sample);

		Mockito.when(this.sampleService
			.filter(anyString(), anyInt(), org.mockito.Matchers.any(Pageable.class)))
			.thenReturn(list);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/sample/maize/samples?plotId=" + plotId).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.status().isOk()) //
			.andExpect(jsonPath("$", IsCollectionWithSize.hasSize(list.size()))) //
			.andExpect(jsonPath("$[0].sampleName", is(sample.getSampleName()))) //
			.andExpect(jsonPath("$[0].sampleBusinessKey", is(sample.getSampleBusinessKey()))) //
			.andExpect(jsonPath("$[0].takenBy", is(sample.getTakenBy()))) //
			// FIXME Jackson use UTC as default timezone
			// .andExpect(MockMvcResultMatchers
			// 	.jsonPath("$[0].samplingDate", is(SampleListResourceTest.DATE_FORMAT.format(sample.getSamplingDate())))) //
			.andExpect(jsonPath("$[0].sampleList", is(sample.getSampleList()))) //
			.andExpect(jsonPath("$[0].plantNumber", is(sample.getPlantNumber()))) //
			.andExpect(jsonPath("$[0].plantBusinessKey", is(sample.getPlantBusinessKey()))) //
		;

		Mockito.verify(this.sampleService, Mockito.atLeastOnce()).filter(anyString(), anyInt(), isNull(Pageable.class));
	}
}
