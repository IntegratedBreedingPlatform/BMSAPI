
package org.ibp.api.rest.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.MeasurementImportDTO;
import org.ibp.api.domain.study.ObservationImportDTO;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.Trait;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class StudyResourceImportTest extends ApiUnitTestBase {

	@Autowired
	private FieldbookService fieldbookService;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private DataImportService dataImportService;

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public FieldbookService fieldbookService() {
			return Mockito.mock(FieldbookService.class);
		}

		@Bean
		@Primary
		public GermplasmListManager germplasmListManager() {
			return Mockito.mock(GermplasmListManager.class);
		}

		@Bean
		@Primary
		public DataImportService dataImportService() {
			return Mockito.mock(DataImportService.class);
		}
	}

	@Test
	public void testImportNursery() throws Exception {

		final StudyImportDTO inputDTO = new StudyImportDTO();
		inputDTO.setStudyType(StudyTypeDto.NURSERY_NAME);
		inputDTO.setName("Maize Nursery");
		inputDTO.setObjective("Grow more seeds");
		inputDTO.setDescription("Maize Nursery title");
		inputDTO.setStartDate("20150101");
		inputDTO.setEndDate("20151201");
		inputDTO.setUserId(1);
		inputDTO.setFolderId(1L);
		inputDTO.setSiteName("CIMMYT");
		inputDTO.setCreatedBy("1");

		final Trait trait1 = new Trait(1, "Plant Height");
		final Trait trait2 = new Trait(2, "Grain Yield");
		inputDTO.setTraits(Lists.newArrayList(trait1, trait2));

		final StudyGermplasm g1 = new StudyGermplasm();
		g1.setEntryNumber(1);
		g1.setEntryType("Test");
		g1.setPosition("1");
		final GermplasmListEntrySummary g1Summary = new GermplasmListEntrySummary();
		g1Summary.setGid(1);
		g1Summary.setEntryCode("Entry Code 1");
		g1Summary.setSeedSource("Seed Source 1");
		g1Summary.setDesignation("Designation 1");
		g1Summary.setCross("Cross 1");
		g1.setGermplasmListEntrySummary(g1Summary);

		final StudyGermplasm g2 = new StudyGermplasm();
		g2.setEntryNumber(2);
		g2.setEntryType("Test");
		g2.setPosition("2");
		final GermplasmListEntrySummary g2Summary = new GermplasmListEntrySummary();
		g2Summary.setGid(2);
		g2Summary.setEntryCode("Entry Code 2");
		g2Summary.setSeedSource("Seed Source 2");
		g2Summary.setDesignation("Designation 2");
		g2Summary.setCross("Cross 2");
		g2.setGermplasmListEntrySummary(g2Summary);

		inputDTO.setGermplasm(Lists.newArrayList(g1, g2));

		final ObservationImportDTO observationUnit1 = new ObservationImportDTO();
		observationUnit1.setGid(g1.getGermplasmListEntrySummary().getGid());
		observationUnit1.setEntryNumber(g1.getEntryNumber());
		observationUnit1.setPlotNumber(1);

		final MeasurementImportDTO measurement11 = new MeasurementImportDTO();
		measurement11.setTraitId(trait1.getTraitId());
		measurement11.setTraitValue("11");

		final MeasurementImportDTO measurement12 = new MeasurementImportDTO();
		measurement12.setTraitId(trait2.getTraitId());
		measurement12.setTraitValue("12");

		observationUnit1.setMeasurements(Lists.newArrayList(measurement11, measurement12));

		final ObservationImportDTO observationUnit2 = new ObservationImportDTO();
		observationUnit2.setGid(g2.getGermplasmListEntrySummary().getGid());
		observationUnit2.setEntryNumber(g2.getEntryNumber());
		observationUnit2.setPlotNumber(2);

		final MeasurementImportDTO measurement21 = new MeasurementImportDTO();
		measurement21.setTraitId(trait1.getTraitId());
		measurement21.setTraitValue("21");

		final MeasurementImportDTO measurement22 = new MeasurementImportDTO();
		measurement22.setTraitId(trait2.getTraitId());
		measurement22.setTraitValue("22");

		observationUnit2.setMeasurements(Lists.newArrayList(measurement21, measurement22));

		inputDTO.setObservations(Lists.newArrayList(observationUnit1, observationUnit2));

		final String inptJSON = new ObjectMapper().writeValueAsString(inputDTO);

		this.mockMvc.perform(
				MockMvcRequestBuilders.post("/study/{cropName}/import?programUUID={programUUID}", this.cropName, this.programUuid) //
				.contentType(this.contentType).content(inptJSON)) //
				.andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()));
	}
}
