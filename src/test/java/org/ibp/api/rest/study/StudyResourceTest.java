
package org.ibp.api.rest.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudySearchParameters;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

public class StudyResourceTest extends ApiUnitTestBase {

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService studyServiceMW;

	@Autowired
	private StudyDataManager studyDataManager;

	@Test
	public void testListAllStudies() throws Exception {

		List<org.generationcp.middleware.service.api.study.StudySummary> summariesMW = new ArrayList<>();
		org.generationcp.middleware.service.api.study.StudySummary summaryMW =
				new org.generationcp.middleware.service.api.study.StudySummary();
		summaryMW.setId(1);
		summaryMW.setName("A Maizing Trial");
		summaryMW.setTitle("A Maizing Trial Title");
		summaryMW.setObjective("A Maize the world with new Maize variety.");
		summaryMW.setType(StudyType.T);
		summaryMW.setStartDate("01012015");
		summaryMW.setEndDate("01012015");
		summaryMW.setPrincipalInvestigator("Mr. Breeder");
		summaryMW.setLocation("Auckland");
		summaryMW.setSeason("Summer");
		summariesMW.add(summaryMW);

		Mockito.when(this.studyServiceMW.search(org.mockito.Matchers.any(StudySearchParameters.class))).thenReturn(summariesMW);

		this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/search", "maize").contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(summariesMW.size())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['id']", Matchers.is(summaryMW.getId().toString())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['name']", Matchers.is(summaryMW.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['title']", Matchers.is(summaryMW.getTitle())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['objective']", Matchers.is(summaryMW.getObjective())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['type']", Matchers.is(summaryMW.getType().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['startDate']", Matchers.is(summaryMW.getStartDate())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['endDate']", Matchers.is(summaryMW.getEndDate())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['principalInvestigator']", Matchers.is(summaryMW.getPrincipalInvestigator())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['location']", Matchers.is(summaryMW.getLocation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['season']", Matchers.is(summaryMW.getSeason())))
				.andDo(MockMvcResultHandlers.print());

		Mockito.verify(this.studyServiceMW).search(org.mockito.Matchers.any(StudySearchParameters.class));
	}

	@Test
	public void testGetObservations() throws Exception {
		MeasurementDto measurement = new MeasurementDto(new MeasurementVariableDto(1, "Plant Height"), 1, "123");
		ObservationDto obsDto =
				new ObservationDto(1, "1", "Test", 1, "CML123", "1", "CIMMYT Seed Bank", "1", "1", "2", Lists.newArrayList(measurement));

		obsDto.setColumnNumber("11");
		obsDto.setRowNumber("22");
		obsDto.setPlotId("CHMEPwuxU2Yr6");
		obsDto.additionalGermplasmDescriptor("StockID", "Stck-123");

		Mockito.when(this.studyServiceMW.countTotalObservationUnits(org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt()))
				.thenReturn(100);
		Mockito.when(this.studyServiceMW.getObservations(org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(),
				org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyString(),
				org.mockito.Matchers.anyString())).thenReturn(Lists.newArrayList(obsDto));

		this.mockMvc
				.perform(MockMvcRequestBuilders
						.get("/study/{cropname}/{studyId}/observations?instanceId=1", "maize", "1")
						.contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageSize", Matchers.is(100)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalResults", Matchers.is(100)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.is(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.firstPage", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.lastPage", Matchers.is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.hasNextPage", Matchers.is(false)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.hasPreviousPage", Matchers.is(false)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults", IsCollectionWithSize.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['uniqueIdentifier']", Matchers.is(obsDto.getMeasurementId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['germplasmId']", Matchers.is(obsDto.getGid())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['germplasmDesignation']", Matchers.is(obsDto.getDesignation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['entryNumber']", Matchers.is(obsDto.getEntryNo())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['entryType']", Matchers.is(obsDto.getEntryType())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['plotNumber']", Matchers.is(obsDto.getPlotNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['blockNumber']", Matchers.is(obsDto.getBlockNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['replicationNumber']", Matchers.is(obsDto.getRepitionNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['environmentNumber']", Matchers.is(obsDto.getTrialInstance())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['entryCode']", Matchers.is(obsDto.getEntryCode())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['rowNumber']", Matchers.is(obsDto.getRowNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['columnNumber']", Matchers.is(obsDto.getColumnNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['plotId']", Matchers.is(obsDto.getPlotId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['additionalGermplasmDescriptors'][0]['StockID']",
						Matchers.is("Stck-123")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['measurements']", IsCollectionWithSize.hasSize(1)))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0]['measurements'][0].measurementIdentifier.measurementId",
								Matchers.is(measurement.getPhenotypeId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0]['measurements'][0].measurementIdentifier.trait.traitId",
								Matchers.is(measurement.getMeasurementVariable().getId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.pageResults[0]['measurements'][0].measurementIdentifier.trait.traitName",
								Matchers.is(measurement.getMeasurementVariable().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.pageResults[0]['measurements'][0].measurementValue",
						Matchers.is(measurement.getVariableValue())));
	}

	@Test
	public void testGetStudyDetailsBasic() throws Exception {

		int studyId = 123;

		// Study object is Middleware is quite complex to setup so chosing to just mock it instead
		// so that test does not need too much structural knowledge of Middleware data objects.
		Study study = Mockito.mock(Study.class);
		Mockito.when(study.getId()).thenReturn(studyId);
		Mockito.when(study.getName()).thenReturn("Maizing Trial");
		Mockito.when(study.getTitle()).thenReturn("Title");
		Mockito.when(study.getObjective()).thenReturn("Objective");
		Mockito.when(study.getType()).thenReturn(StudyType.T);
		Mockito.when(study.getStartDate()).thenReturn(20150101);
		Mockito.when(study.getEndDate()).thenReturn(20151231);

		Mockito.when(study.getConditions()).thenReturn(new VariableList());

		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		Mockito.when(this.studyDataManager.getAllStudyFactors(studyId)).thenReturn(new VariableTypeList());
		Mockito.when(this.studyDataManager.getAllStudyVariates(studyId)).thenReturn(new VariableTypeList());

		this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/{studyId}", "maize", studyId).contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(String.valueOf(study.getId()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(study.getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is(study.getTitle())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.objective", Matchers.is(study.getObjective())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is(study.getType().getName())))
				.andExpect(MockMvcResultMatchers.jsonPath("$.startDate", Matchers.is(String.valueOf(study.getStartDate()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.endDate", Matchers.is(String.valueOf(study.getEndDate()))))
				.andExpect(MockMvcResultMatchers.jsonPath("$.generalInfo", Matchers.empty()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.environments", Matchers.empty()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.traits", Matchers.empty()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.datasets", Matchers.empty()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.germplasm", Matchers.empty()));
	}
	
	@Test
	public void testListAllFolders() throws Exception {
		
		FolderReference folderRef = new FolderReference(1, 2, "My Folder", "My Folder Description");
		Mockito.when(this.studyDataManager.getAllFolders()).thenReturn(Lists.newArrayList(folderRef));
		
		this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/folders", "maize").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].folderId", Matchers.is(folderRef.getId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].name", Matchers.is(folderRef.getName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(folderRef.getDescription())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].parentFolderId", Matchers.is(folderRef.getParentFolderId())));
	}

	@Test
	public void testListStudyInstances() throws Exception {

		final StudyInstance studyInstance = new StudyInstance(1, "Gujarat, India", "GUJ", 1);
		Mockito.when(this.studyServiceMW.getStudyInstances(Mockito.anyInt()))
				.thenReturn(Lists.newArrayList(studyInstance));

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/study/{cropname}/{studyId}/instances", "maize", "1")
				.contentType(this.contentType))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].instanceDbId", Matchers.is(studyInstance.getInstanceDbId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].locationName", Matchers.is(studyInstance.getLocationName())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0].locationAbbreviation", Matchers.is(studyInstance.getLocationAbbreviation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].instanceNumber", Matchers.is(studyInstance.getInstanceNumber())));
	}
}
