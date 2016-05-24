
package org.ibp.api.rest.study;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudySearchParameters;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.validators.ObservationValidationDataExtractorTest;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class StudyResourceTest extends ApiUnitTestBase {

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public org.generationcp.middleware.service.api.study.StudyService getStudyServiceMW() {
			return Mockito.mock(org.generationcp.middleware.service.api.study.StudyService.class);
		}

		@Bean
		@Primary
		public StudyDataManager studyDataManager() {
			return Mockito.mock(StudyDataManager.class);
		}
	}

	private static final String ENTITY_TYPE = "Test";
	private static final String DESIGNATION = "CML123";
	private static final String ENTRY_NUMBER = "1";
	private static final String SEED_SOURCE = "CIMMYT Seed Bank";
	private static final String PLOT_NUMBER = "1";
	private static final String REP_NUMBER = "2";
	private static final String ENV_NUMBER = "2";
	private static final Integer GERMPLASM_ID= 1;

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService studyServiceMW;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

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
		MeasurementDto measurement = new MeasurementDto(new TraitDto(1, "Plant Height"), 1, "123");
		ObservationDto obsDto =
				new ObservationDto(1, "1", "Test", 1, "CML123", "1", "CIMMYT Seed Bank", "1", "1", Lists.newArrayList(measurement));

		Mockito.when(this.studyServiceMW.getObservations(org.mockito.Matchers.anyInt())).thenReturn(Lists.newArrayList(obsDto));
		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/study/{cropname}/{studyId}/observations", "maize", "1").contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['uniqueIdentifier']", Matchers.is(obsDto.getMeasurementId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['germplasmId']", Matchers.is(obsDto.getGid())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['germplasmDesignation']", Matchers.is(obsDto.getDesignation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['entryNumber']", Matchers.is(obsDto.getEntryNo())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['entryType']", Matchers.is(obsDto.getEntryType())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['plotNumber']", Matchers.is(obsDto.getPlotNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['replicationNumber']", Matchers.is(obsDto.getRepitionNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['environmentNumber']", Matchers.is(obsDto.getTrialInstance())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['seedSource']", Matchers.is(obsDto.getSeedSource())))
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]['measurements']", IsCollectionWithSize.hasSize(1)))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0]['measurements'][0].measurementIdentifier.measurementId",
								Matchers.is(measurement.getPhenotypeId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0]['measurements'][0].measurementIdentifier.trait.traitId",
								Matchers.is(measurement.getTrait().getTraitId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0]['measurements'][0].measurementIdentifier.trait.traitName",
								Matchers.is(measurement.getTrait().getTraitName())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$[0]['measurements'][0].measurementValue", Matchers.is(measurement.getTriatValue())))
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void testGetSingleObservation() throws Exception {
		MeasurementDto measurement = new MeasurementDto(new TraitDto(1, "Plant Height"), 1, "123");
		ObservationDto obsDto =
				new ObservationDto(1, "1", StudyResourceTest.ENTITY_TYPE, StudyResourceTest.GERMPLASM_ID, StudyResourceTest.DESIGNATION,
						StudyResourceTest.ENTRY_NUMBER, StudyResourceTest.SEED_SOURCE, "1", StudyResourceTest.PLOT_NUMBER,
						Lists.newArrayList(measurement));

		Mockito.when(this.studyServiceMW.getSingleObservation(org.mockito.Matchers.anyInt(),
				org.mockito.Matchers.anyInt())).thenReturn(Lists.newArrayList(obsDto));
		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/study/{cropname}/{studyId}/observations/{observationId}",
						"maize", "1", "10").contentType(this.contentType))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$['uniqueIdentifier']", Matchers.is(obsDto.getMeasurementId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['germplasmId']", Matchers.is(obsDto.getGid())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['germplasmDesignation']", Matchers.is(obsDto.getDesignation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['entryNumber']", Matchers.is(obsDto.getEntryNo())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['entryType']", Matchers.is(obsDto.getEntryType())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['plotNumber']", Matchers.is(obsDto.getPlotNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['replicationNumber']", Matchers.is(obsDto.getRepitionNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['environmentNumber']", Matchers.is(obsDto.getTrialInstance())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['seedSource']", Matchers.is(obsDto.getSeedSource())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['measurements']", IsCollectionWithSize.hasSize(1)))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$['measurements'][0].measurementIdentifier.measurementId",
								Matchers.is(measurement.getPhenotypeId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$['measurements'][0].measurementIdentifier.trait.traitId",
								Matchers.is(measurement.getTrait().getTraitId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$['measurements'][0].measurementIdentifier.trait.traitName",
								Matchers.is(measurement.getTrait().getTraitName())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$['measurements'][0].measurementValue", Matchers.is(measurement.getTriatValue())))
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void testUpdateObservation() throws Exception{

		org.ibp.api.domain.study.Observation observation = new org.ibp.api.domain.study.Observation();
		observation.setUniqueIdentifier(1);
		observation.setGermplasmId(StudyResourceTest.GERMPLASM_ID);
		observation.setGermplasmDesignation(StudyResourceTest.DESIGNATION);
		observation.setEntryNumber(StudyResourceTest.ENTRY_NUMBER);
		observation.setEntryType(StudyResourceTest.ENTITY_TYPE);
		observation.setPlotNumber(StudyResourceTest.PLOT_NUMBER);
		observation.setReplicationNumber(StudyResourceTest.REP_NUMBER);
		observation.setEnvironmentNumber(StudyResourceTest.ENV_NUMBER);
		observation.setSeedSource(StudyResourceTest.SEED_SOURCE);

		List<Measurement> measurements = new ArrayList<>();
		Measurement measurement = ObservationValidationDataExtractorTest.getTestMeasurementData();
		measurement.setMeasurementValue("100");
		measurements.add(measurement);
		observation.setMeasurements(measurements);

		MeasurementDto measurementDTO = new MeasurementDto(new TraitDto(measurement.getMeasurementIdentifier().getTrait().getTraitId(),
				measurement.getMeasurementIdentifier().getTrait().getTraitName()),
				measurement.getMeasurementIdentifier().getMeasurementId(), measurement.getMeasurementValue());
		ObservationDto obsDto =
				new ObservationDto(measurement.getMeasurementIdentifier().getMeasurementId(), "1", StudyResourceTest.ENTITY_TYPE,
						StudyResourceTest.GERMPLASM_ID, StudyResourceTest.DESIGNATION, StudyResourceTest.ENTRY_NUMBER,
						StudyResourceTest.SEED_SOURCE, "1",
						StudyResourceTest.PLOT_NUMBER, Lists.newArrayList(measurementDTO));

		Project project = new Project();
		project.setCropType(new CropType(this.cropName));
		project.setUniqueID(this.programUuid);
		project.setProjectName("project_name");

		Variable ontologyVariable = TestDataProvider.getTestVariable();

		Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(Mockito.anyString());
		Mockito.doReturn(
				new Term(measurement.getMeasurementIdentifier().getTrait().getTraitId(),
						measurement.getMeasurementIdentifier().getTrait().getTraitName(), "Description", CvId.VARIABLES.getId(),
						false)).when(this.termDataManager).getTermById(Mockito.anyInt());
		Mockito.doReturn(ontologyVariable).when(this.ontologyVariableDataManager).getVariable(Mockito.anyString(), Mockito.anyInt(),
				Mockito.anyBoolean(), Mockito.anyBoolean());
		Mockito.when(this.studyServiceMW.getSingleObservation(org.mockito.Matchers.anyInt(),
				org.mockito.Matchers.anyInt())).thenReturn(Lists.newArrayList(obsDto));
		Mockito.when(this.studyServiceMW.updataObservation(org.mockito.Matchers.anyInt(),
				Mockito.any(ObservationDto.class))).thenReturn(obsDto);

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.put("/study/{cropname}/{studyId}/observations/{observationId}", this.cropName, "1", "1")
								.contentType(this.contentType).content(this.convertObjectToByte(observation)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$['uniqueIdentifier']", Matchers.is(obsDto.getMeasurementId())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['germplasmId']", Matchers.is(obsDto.getGid())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['germplasmDesignation']", Matchers.is(obsDto.getDesignation())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['entryNumber']", Matchers.is(obsDto.getEntryNo())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['entryType']", Matchers.is(obsDto.getEntryType())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['plotNumber']", Matchers.is(obsDto.getPlotNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['replicationNumber']", Matchers.is(obsDto.getRepitionNumber())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['environmentNumber']", Matchers.is(obsDto.getTrialInstance())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['seedSource']", Matchers.is(obsDto.getSeedSource())))
				.andExpect(MockMvcResultMatchers.jsonPath("$['measurements']", IsCollectionWithSize.hasSize(1)))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$['measurements'][0].measurementIdentifier.measurementId",
								Matchers.is(measurementDTO.getPhenotypeId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$['measurements'][0].measurementIdentifier.trait.traitId",
								Matchers.is(measurementDTO.getTrait().getTraitId())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$['measurements'][0].measurementIdentifier.trait.traitName",
								Matchers.is(measurementDTO.getTrait().getTraitName())))
				.andExpect(
						MockMvcResultMatchers.jsonPath("$['measurements'][0].measurementValue", Matchers.is(measurementDTO.getTriatValue())))
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void testUpdateObservationThrowsException() throws Exception{

		org.ibp.api.domain.study.Observation observation = new org.ibp.api.domain.study.Observation();
		observation.setUniqueIdentifier(10);
		observation.setGermplasmId(StudyResourceTest.GERMPLASM_ID);
		observation.setGermplasmDesignation(StudyResourceTest.DESIGNATION);
		observation.setEntryNumber(StudyResourceTest.ENTRY_NUMBER);
		observation.setEntryType(StudyResourceTest.ENTITY_TYPE);
		observation.setPlotNumber(StudyResourceTest.PLOT_NUMBER);
		observation.setReplicationNumber(StudyResourceTest.REP_NUMBER);
		observation.setEnvironmentNumber(StudyResourceTest.ENV_NUMBER);
		observation.setSeedSource(StudyResourceTest.SEED_SOURCE);

		List<Measurement> measurements = new ArrayList<>();
		Measurement measurement = ObservationValidationDataExtractorTest.getTestMeasurementData();
		measurement.setMeasurementValue("100");
		measurements.add(measurement);
		observation.setMeasurements(measurements);

		this.mockMvc
				.perform(
						MockMvcRequestBuilders.put("/study/{cropname}/{studyId}/observations/{observationId}", this.cropName, "1", "1")
								.contentType(this.contentType).content(this.convertObjectToByte(observation)))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());
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
}
