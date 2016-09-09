
package org.ibp.api.brapi.v1.sample;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.generationcp.middleware.service.api.SampleService;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class SampleResourceBrapiTest extends ApiUnitTestBase {

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
	public void testGetSampleDetails() throws Exception {
		String sampleId = "SampleID-1469128772922";

		final org.generationcp.middleware.domain.sample.Sample sample = new org.generationcp.middleware.domain.sample.Sample();
		sample.setStudyId(1);
		sample.setStudyName("Draught Resistance Trial");
		sample.setLocationId(2);
		sample.setSampleId(sampleId);
		sample.setPlotId(100);
		sample.setPlantId("Plant-A1");
		sample.setTakenBy("Naymesh");
		sample.setSampleDate("2016-07-22");
		sample.setNotes("Infected leaf cut");
		sample.setSeason("Summer");
		sample.setLocationName("Kenya");
		sample.setEntryNumber(3);
		sample.setPlotNumber(5);
		sample.setPlantingDate("2016-06-01");
		sample.setHarvestDate("2016-12-31");
		sample.setSeedSource("SeedSource");
		sample.setGermplasmId(6);
		sample.setPedigree("A//B");
		sample.setFieldId(7);
		sample.setFieldName("CountrySideField");

		Mockito.when(this.sampleService.getSample(sampleId)).thenReturn(sample);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v1/sample/" + sampleId).contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isOk()) //
				.andDo(MockMvcResultHandlers.print()) //
				.andExpect(jsonPath("$.studyId", Matchers.is(sample.getStudyId()))) //
				.andExpect(jsonPath("$.locationId", Matchers.is(sample.getLocationId()))) //
				.andExpect(jsonPath("$.plotId", Matchers.is(sample.getPlotId()))) //
				.andExpect(jsonPath("$.sampleId", Matchers.is(sample.getSampleId()))) //
				.andExpect(jsonPath("$.takenBy", Matchers.is(sample.getTakenBy()))) //
				.andExpect(jsonPath("$.sampleDate", Matchers.is(sample.getSampleDate()))) //
				.andExpect(jsonPath("$.notes", Matchers.is(sample.getNotes()))) //
				.andExpect(jsonPath("$.studyName", Matchers.is(sample.getStudyName()))) //
				.andExpect(jsonPath("$.season", Matchers.is(sample.getSeason()))) //
				.andExpect(jsonPath("$.locationName", Matchers.is(sample.getLocationName()))) //
				.andExpect(jsonPath("$.plotNumber", Matchers.is(sample.getPlotNumber()))) //
				.andExpect(jsonPath("$.germplasmId", Matchers.is(sample.getGermplasmId()))) //
				.andExpect(jsonPath("$.plantingDate", Matchers.is(sample.getPlantingDate()))) //
				.andExpect(jsonPath("$.harvestDate", Matchers.is(sample.getHarvestDate()))) //
				.andExpect(jsonPath("$.seedSource", Matchers.is(sample.getSeedSource()))) //
				.andExpect(jsonPath("$.pedigree", Matchers.is(sample.getPedigree()))) //
				.andExpect(jsonPath("$.fieldId", Matchers.is(sample.getFieldId()))) //
				.andExpect(jsonPath("$.fieldName", Matchers.is(sample.getFieldName()))) //
		;
	}

	@Test
	public void testCreateSample() throws Exception {

		StringBuilder bodyBuilder = new StringBuilder();
		bodyBuilder.append("{\n" + "  \"plotId\": 503,\n" + "  \"plantId\" : \"P1\",\n" + "  \"takenBy\": \"Naymesh\",\n"
				+ "  \"sampleDate\": \"2016-07-19\",\n" + "  \"notes\": \"Flower petal\"\n" + "}");

		final String sampleId = "SampleID-123";
		Mockito.when(this.sampleService.createSample(Mockito.any(org.generationcp.middleware.domain.sample.Sample.class)))
				.thenReturn(sampleId);

		this.mockMvc
				.perform(MockMvcRequestBuilders.put("/maize/brapi/v1/sample").content(bodyBuilder.toString().getBytes())
						.contentType(this.contentType)) //
				.andExpect(MockMvcResultMatchers.status().isCreated()) //
				.andDo(MockMvcResultHandlers.print())//
				.andExpect(content().string(Matchers.is(sampleId)))
		;
	}
}
