
package org.ibp.api.rest.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudySearchParameters;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.FieldMapMetaData;
import org.ibp.api.domain.study.FieldMapPlantingDetails;
import org.ibp.api.domain.study.FieldMapStudySummary;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.MeasurementImportDTO;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.ObservationImportDTO;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.domain.study.validators.ObservationValidator;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.ibp.api.java.impl.middleware.study.FieldMapService;
import org.ibp.api.java.study.StudyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.common.collect.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

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

        @Bean
        @Primary
        public ObservationValidator observationValidator() {
            return Mockito.mock(ObservationValidator.class);
        }

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
        public OntologyVariableDataManager ontologyVariableDataManager() {
            return Mockito.mock(OntologyVariableDataManager.class);
        }

    }

	@Autowired
	private org.generationcp.middleware.service.api.study.StudyService studyServiceMW;

	@Autowired
	private StudyDataManager studyDataManager;

    @Autowired
    private CrossExpansionProperties crossExpansionProperties;

    @Mock
    private FieldMapService mapService;

    @Autowired
    private TermDataManager termDataManager;

    @Autowired
    private OntologyVariableDataManager ontologyVariableDataManager;

    @Before
    public void reset() {
        Mockito.reset(this.termDataManager);
        Mockito.reset(this.ontologyVariableDataManager);
        Mockito.mock(FieldMapService.class);
        Mockito.mock(StudyService.class);
    }


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
    public void testGetSingleObservation() throws Exception{

        MeasurementDto measurement = new MeasurementDto(new TraitDto(1, "Plant Height"), 1, "9");
        ObservationDto observationDto =
                new ObservationDto(1, "2", "Test", 3, "CML123", "4", "CIMMYT Seed Bank", "5", "6", Lists.newArrayList(measurement));

        List<ObservationDto> observationDtos = new ArrayList<>();
        observationDtos.add(observationDto);
        System.out.println("Repilication number"+ observationDto.getRepitionNumber());

        int studyId = 9;

        Mockito.when(this.studyServiceMW.getSingleObservation(studyId , 1)).thenReturn(observationDtos);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/{studyId}/observations/{observationId}" , this.cropName , studyId , 1).contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.uniqueIdentifier" , Matchers.is(observationDto.getMeasurementId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmId" , Matchers.is(observationDto.getGid())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmDesignation" , Matchers.is(observationDto.getDesignation())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.entryNumber" , Matchers.is(observationDto.getEntryNo())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.entryType" , Matchers.is(observationDto.getEntryType())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.plotNumber" , Matchers.is(observationDto.getPlotNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.replicationNumber" , Matchers.is(observationDto.getRepitionNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.environmentNumber" , Matchers.is(observationDto.getTrialInstance())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.seedSource" , Matchers.is(observationDto.getSeedSource())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.measurements[0].measurementIdentifier.measurementId" , Matchers.is(measurement.getPhenotypeId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.measurements[0].measurementIdentifier.trait.traitId" , Matchers.is(measurement.getTrait().getTraitId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.measurements[0].measurementIdentifier.trait.traitName" , Matchers.is(measurement.getTrait().getTraitName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.measurements[0].measurementValue" , Matchers.is(measurement.getTriatValue())));
    }

    @Test
    public void testUpdateObservation() throws Exception{

        int studyId = 9;

        Trait trait = new Trait();
        trait.setTraitId(15);
        trait.setTraitName("Plant height");

        MeasurementIdentifier newMeasurementIdentifier = new MeasurementIdentifier();
        newMeasurementIdentifier.setMeasurementId(1);
        newMeasurementIdentifier.setTrait(trait);

        Measurement newMeasurements = new Measurement();
        newMeasurements.setMeasurementValue("96");
        newMeasurements.setMeasurementIdentifier(newMeasurementIdentifier);

        MeasurementDto measurement = new MeasurementDto(new TraitDto(15, "Plant Height"), 1, "9");
        ObservationDto observationDto =
                new ObservationDto(1, "e1", "Test", 1, "CML123", "01", "CIMMYT Seed Bank", "R1", "p1", Lists.newArrayList(measurement));
        final List<MeasurementDto> traits = observationDto.getTraitMeasurements();


        final Observation observation = new Observation();
        observation.setUniqueIdentifier(observationDto.getMeasurementId());
        observation.setEntryNumber(observationDto.getEntryNo());
        observation.setEntryType(observationDto.getEntryType());
        observation.setEnvironmentNumber(observationDto.getTrialInstance());
        observation.setGermplasmDesignation(observationDto.getDesignation());
        observation.setGermplasmId(observationDto.getGid());
        observation.setPlotNumber(observationDto.getPlotNumber());
        observation.setReplicationNumber(observationDto.getRepitionNumber());
        observation.setSeedSource(observationDto.getSeedSource());

        final List<Measurement> measurements = new ArrayList<Measurement>();
        for (final MeasurementDto traitValue : traits) {
            measurements.add(new Measurement(new MeasurementIdentifier(traitValue.getPhenotypeId(), new Trait(traitValue.getTrait().getTraitId(),
                    traitValue.getTrait().getTraitName())), traitValue.getTriatValue()));
        }

        observation.setMeasurements(measurements);

        Project project = new Project();
        project.setCropType(new CropType(this.cropName));
        project.setUniqueID(this.programUuid);
        project.setProjectName("project_name");

        Term propertyTerm = TestDataProvider.getPropertyTerm();
        Term methodTerm = TestDataProvider.getMethodTerm();

        Scale scale = TestDataProvider.getTestScale();
        Variable variable = TestDataProvider.getTestVariable();
        variable.setMethod(new Method(methodTerm));
        variable.setProperty(new Property(propertyTerm));
        variable.setScale(scale);

        Term variableTerm = TestDataProvider.getVariableTerm();

        Mockito.when(this.studyServiceMW.getProgramUUID(studyId)).thenReturn(this.programUuid);
        Mockito.when(this.termDataManager.getTermById(variableTerm.getId())).thenReturn(variableTerm);
        Mockito.doReturn(variableTerm).when(this.termDataManager).getTermByNameAndCvId(variable.getName(), CvId.VARIABLES.getId());
        Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(this.programUuid);
        Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(this.programUuid, variable.getId(), true, true);
        Mockito.when(this.studyServiceMW.getSingleObservation(studyId , newMeasurementIdentifier.getMeasurementId())).thenReturn(Lists.newArrayList(observationDto));
        Mockito.when(this.studyServiceMW.updataObservation(studyId , observationDto)).thenReturn(observationDto);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/study/{cropname}/{studyId}/observations/{observationId}" , this.cropName , studyId , 1)
                .contentType(this.contentType)
                .content(this.convertObjectToByte(observation)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.uniqueIdentifier" ,Matchers.is(observation.getUniqueIdentifier())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmId" ,Matchers.is(observation.getGermplasmId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.germplasmDesignation" ,Matchers.is(observation.getGermplasmDesignation())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.entryNumber" ,Matchers.is(observation.getEntryNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.entryType" ,Matchers.is(observation.getEntryType())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.plotNumber" ,Matchers.is(observation.getPlotNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.replicationNumber" , Matchers.is(observation.getReplicationNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.environmentNumber" , Matchers.is(observation.getEnvironmentNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.seedSource" , Matchers.is(observation.getSeedSource())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.measurements[0].measurementIdentifier.measurementId" ,Matchers.is(measurement.getPhenotypeId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.measurements[0].measurementIdentifier.trait.traitId" ,Matchers.is(measurement.getTrait().getTraitId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.measurements[0].measurementIdentifier.trait.traitName" ,Matchers.is(measurement.getTrait().getTraitName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.measurements[0].measurementValue" ,Matchers.is(measurement.getTriatValue())));
    }

    @Test
    public void testUpdateObservationWithException() throws Exception{

        final Observation observation = new Observation();
        observation.setUniqueIdentifier(null);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/study/{cropname}/{studyId}/observations/{observationId}" , this.cropName , 1 , 1)
                .contentType(this.contentType)
                .content(this.convertObjectToByte(observation)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", Matchers.is("The observation identifier must be populated and have the same value in the object and the url")));
    }

    @Test
    public void testAddOrUpdateMultipleObservations() throws Exception{
        final int studyId = 9;

        Trait trait = new Trait();
        trait.setTraitId(15);
        trait.setTraitName("Plant height");

        MeasurementIdentifier measurementIdentifier = new MeasurementIdentifier();
        measurementIdentifier.setMeasurementId(1);
        measurementIdentifier.setTrait(trait);

        Measurement measurementValue = new Measurement();
        measurementValue.setMeasurementValue("1");
        measurementValue.setMeasurementIdentifier(measurementIdentifier);

        MeasurementDto measurement = new MeasurementDto(new TraitDto(15, "Plant Height"), 1, "9");
        ObservationDto observationDto =
                new ObservationDto(1, "e1", "Test", 1, "CML123", "01", "CIMMYT Seed Bank", "R1", "p1", Lists.newArrayList(measurement));

        List<MeasurementDto> traits = observationDto.getTraitMeasurements();

        Observation observation = new Observation();
        observation.setUniqueIdentifier(observationDto.getMeasurementId());
        observation.setEntryNumber(observationDto.getEntryNo());
        observation.setEntryType(observationDto.getEntryType());
        observation.setEnvironmentNumber(observationDto.getTrialInstance());
        observation.setGermplasmDesignation(observationDto.getDesignation());
        observation.setGermplasmId(observationDto.getGid());
        observation.setPlotNumber(observationDto.getPlotNumber());
        observation.setReplicationNumber(observationDto.getRepitionNumber());
        observation.setSeedSource(observationDto.getSeedSource());

        List<Measurement> measurements = new ArrayList<Measurement>();
        for (final MeasurementDto traitValue : traits) {
            measurements.add(new Measurement(new MeasurementIdentifier(traitValue.getPhenotypeId(), new Trait(traitValue.getTrait().getTraitId(),
                    traitValue.getTrait().getTraitName())), traitValue.getTriatValue()));
        }

        observation.setMeasurements(measurements);

        Project project = new Project();
        project.setCropType(new CropType(this.cropName));
        project.setUniqueID(this.programUuid);
        project.setProjectName("project_name");

        List<Observation> observations = new ArrayList<>();
        observations.add(observation);

        Term propertyTerm = TestDataProvider.getPropertyTerm();
        Term methodTerm = TestDataProvider.getMethodTerm();

        Scale scale = TestDataProvider.getTestScale();
        Variable variable = TestDataProvider.getTestVariable();
        variable.setMethod(new Method(methodTerm));
        variable.setProperty(new Property(propertyTerm));
        variable.setScale(scale);

        Term variableTerm = TestDataProvider.getVariableTerm();

        Mockito.when(this.studyServiceMW.getProgramUUID(studyId)).thenReturn(this.programUuid);
        Mockito.when(this.termDataManager.getTermById(variableTerm.getId())).thenReturn(variableTerm);
        Mockito.doReturn(variableTerm).when(this.termDataManager).getTermByNameAndCvId(variable.getName(), CvId.VARIABLES.getId());
        Mockito.doReturn(project).when(this.workbenchDataManager).getProjectByUuid(this.programUuid);
        Mockito.doReturn(variable).when(this.ontologyVariableDataManager).getVariable(this.programUuid, variable.getId(), true, true);
        Mockito.when(this.studyServiceMW.getSingleObservation(studyId , measurementIdentifier.getMeasurementId())).thenReturn(Lists.newArrayList(observationDto));
        Mockito.when(this.studyServiceMW.updataObservation(studyId , observationDto)).thenReturn(observationDto);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/study/{cropname}/{studyId}/observations" , this.cropName , studyId)
                .contentType(this.contentType)
                .content(this.convertObjectToByte(observations)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniqueIdentifier" ,Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].germplasmId" ,Matchers.is(observation.getGermplasmId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].germplasmDesignation" ,Matchers.is(observation.getGermplasmDesignation())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entryNumber" ,Matchers.is(observation.getEntryNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entryType" ,Matchers.is(observation.getEntryType())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].plotNumber" ,Matchers.is(observation.getPlotNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].replicationNumber" , Matchers.is(observation.getReplicationNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].environmentNumber" , Matchers.is(observation.getEnvironmentNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].seedSource" , Matchers.is(observation.getSeedSource())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].measurements[0].measurementIdentifier.measurementId" ,Matchers.is(measurement.getPhenotypeId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].measurements[0].measurementIdentifier.trait.traitId" ,Matchers.is(measurement.getTrait().getTraitId())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].measurements[0].measurementIdentifier.trait.traitName" ,Matchers.is(measurement.getTrait().getTraitName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].measurements[0].measurementValue" ,Matchers.is(measurement.getTriatValue())));
    }

    @Test
    public void testGetStudyGermplasm() throws Exception{

        int studyId = 9;

        this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/{studyId}/germplasm" , this.cropName , studyId).contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetFieldMap() throws Exception{

        List<Integer> studyList = new ArrayList<>();
        studyList.add(1);

        FieldMapLabel fieldMapLabel = new FieldMapLabel();
        fieldMapLabel.setExperimentId(1);
        fieldMapLabel.setEntryNumber(2);
        fieldMapLabel.setGermplasmName("Germplasm Name");
        fieldMapLabel.setRep(1);
        fieldMapLabel.setBlockNo(9);
        fieldMapLabel.setPlotNo(1);
        fieldMapLabel.setColumn(1);
        fieldMapLabel.setRange(2);
        fieldMapLabel.setStudyName("Study Name");
        fieldMapLabel.setDatasetId(1);
        fieldMapLabel.setGeolocationId(9);
        fieldMapLabel.setSiteName("Site name");
        fieldMapLabel.setGid(1);
        fieldMapLabel.setSeason(Season.DRY);
        fieldMapLabel.setStartYear("2015");

        List<String> deletedPlots = new ArrayList<>();

        Map<Integer, String> labelHeaders  = new HashMap<>();
        labelHeaders.put(1 , "Label1");

        FieldMapTrialInstanceInfo trialInstanceInfo = new FieldMapTrialInstanceInfo();
        trialInstanceInfo.setGeolocationId(9);
        trialInstanceInfo.setSiteName("Site name");
        trialInstanceInfo.setTrialInstanceNo("TI1");
        trialInstanceInfo.setFieldMapLabels(Lists.newArrayList(fieldMapLabel));
        trialInstanceInfo.setLabelHeaders(labelHeaders);
        trialInstanceInfo.setBlockName("Block Name");
        trialInstanceInfo.setFieldName("Field Name");
        trialInstanceInfo.setLocationName("Location");
        trialInstanceInfo.setFieldmapUUID(UUID.randomUUID().toString());
        trialInstanceInfo.setRowsInBlock(2);
        trialInstanceInfo.setRangesInBlock(3);
        trialInstanceInfo.setPlantingOrder(2);
        trialInstanceInfo.setStartColumn(0);
        trialInstanceInfo.setStartRange(2);
        trialInstanceInfo.setRowsPerPlot(1);
        trialInstanceInfo.setMachineRowCapacity(1);
        trialInstanceInfo.setOrder(3);
        trialInstanceInfo.setLocationId(9);
        trialInstanceInfo.setFieldId(1);
        trialInstanceInfo.setBlockId(1);
        trialInstanceInfo.setEntryCount(1);
        trialInstanceInfo.setLabelsNeeded(1);
        trialInstanceInfo.setDeletedPlots(deletedPlots);


        FieldMapDatasetInfo datasetInfo = new FieldMapDatasetInfo();
        datasetInfo.setDatasetId(1);
        datasetInfo.setDatasetName("Dataset Name");
        datasetInfo.setTrialInstances(Lists.newArrayList(trialInstanceInfo));

        FieldMapInfo fieldMapInfo = new FieldMapInfo();
        fieldMapInfo.setFieldbookId(1);
        fieldMapInfo.setFieldbookName("FieldBookName");
        fieldMapInfo.setTrial(true);
        fieldMapInfo.setDatasets(Lists.newArrayList(datasetInfo));

        List<FieldMapInfo> fieldMapInfos = new ArrayList<>();
        fieldMapInfos.add(fieldMapInfo);

        FieldMapPlantingDetails plantingDetails = new FieldMapPlantingDetails();
        plantingDetails.setBlockCapacity("2");
        plantingDetails.setColumns(2);
        plantingDetails.setFieldLocation("Field Location");
        plantingDetails.setPlotLayout("Plot Layout");
        plantingDetails.setFieldName("Field Name");
        plantingDetails.setRowCapacityOfPlantingMachine(2);
        plantingDetails.setRowsPerPlot(1);
        plantingDetails.setStartingCoordinates("0");

        FieldMapStudySummary studySummary = new FieldMapStudySummary();
        studySummary.setDataset("Dataset");
        studySummary.setEnvironment(9);
        studySummary.setNumbeOfReps(2L);
        studySummary.setNumberOfEntries(3L);
        studySummary.setOrder(2);
        studySummary.setPlotsNeeded(1L);
        studySummary.setStudyName("Study Name");
        studySummary.setTotalNumberOfPlots(1L);
        studySummary.setType("Type");


        FieldMapMetaData metaData = new FieldMapMetaData();
        metaData.setFieldPlantingDetails(plantingDetails);
        metaData.setRelevantStudies(Lists.newArrayList(studySummary));

        FieldMap fieldMap = new FieldMap();
        fieldMap.setBlockId(1);
        fieldMap.setBlockName("Block name");
        fieldMap.setFieldMapMetaData(metaData);

        List<FieldMap> fieldMapList = new ArrayList<>();
        fieldMapList.add(fieldMap);


        Map<Integer , FieldMap> fieldMapValue = new HashMap<>();
        for(FieldMap mapInfo: fieldMapList){
            fieldMapValue.put(mapInfo.getBlockId() , mapInfo);
        }

        Mockito.when(this.mapService.getFieldMap("9")).thenReturn(fieldMapValue);
        Mockito.when(this.studyDataManager.getFieldMapInfoOfStudy(studyList, StudyType.T , crossExpansionProperties, true)).thenReturn(fieldMapInfos);
        Mockito.when(this.studyDataManager.getStudyType(1)).thenReturn(StudyType.T);

         this.mockMvc.perform(MockMvcRequestBuilders.get("/study/{cropname}/fieldmaps/{studyId}" , this.cropName , 1).contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testImportStudy() throws Exception{

        GermplasmListEntrySummary summary = new GermplasmListEntrySummary();
        summary.setGid(1);
        summary.setCross("Cross");
        summary.setDesignation("Designation");
        summary.setEntryCode("Entry Code");
        summary.setSeedSource("Seed source");

        StudyGermplasm studyGermplasm = new StudyGermplasm();
        studyGermplasm.setEntryNumber(1);
        studyGermplasm.setEntryType("Entry type");
        studyGermplasm.setGermplasmListEntrySummary(summary);
        studyGermplasm.setPosition("1");

        MeasurementImportDTO measurementImportDTO = new MeasurementImportDTO();
        measurementImportDTO.setTraitId(1);
        measurementImportDTO.setTraitValue("Plant height");

        ObservationImportDTO observationImportDTO = new ObservationImportDTO();
        observationImportDTO.setGid(1);
        observationImportDTO.setEntryNumber(1);
        observationImportDTO.setEnvironmentNumber(1);
        observationImportDTO.setMeasurements(Lists.newArrayList(measurementImportDTO));
        observationImportDTO.setPlotNumber(11);
        observationImportDTO.setReplicationNumber(9);

        Trait trait = new Trait();
        trait.setTraitId(1);
        trait.setTraitName("Trait name");

        StudyImportDTO studyImportDTO = new StudyImportDTO();
        studyImportDTO.setName("Study Name");
        studyImportDTO.setTitle("Study Title");
        studyImportDTO.setStartDate("20160420");
        studyImportDTO.setEndDate("20160423");
        studyImportDTO.setStudyType(StudyType.N.getName());
        studyImportDTO.setSiteName("Site Name");
        studyImportDTO.setStudyInstitute("Study Institute");
        studyImportDTO.setGermplasm(Lists.newArrayList(studyGermplasm));
        studyImportDTO.setObservations(Lists.newArrayList(observationImportDTO));
        studyImportDTO.setUserId(99);
        studyImportDTO.setTraits(Lists.newArrayList(trait));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/study/{cropname}/import?programUUID=" +this.programUuid , this.cropName )
                .contentType(this.contentType)
                .content(this.convertObjectToByte(studyImportDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testBindingResultError() throws Exception{

        StudyImportDTO studyImportDTO = new StudyImportDTO();

        this.mockMvc.perform(MockMvcRequestBuilders.post("/study/{cropname}/import?programUUID=" +this.programUuid , this.cropName )
                .contentType(this.contentType)
                .content(this.convertObjectToByte(studyImportDTO)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());

    }
}
