
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.StudySearchParameters;
import org.generationcp.middleware.service.api.study.StudyService;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.ibp.api.domain.common.ValidationUtil;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.FieldMapMetaData;
import org.ibp.api.domain.study.FieldMapPlantingDetails;
import org.ibp.api.domain.study.FieldMapStudySummary;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyFolder;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.domain.study.validators.ObservationValidator;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.ontology.TestDataProvider;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

public class StudyServiceImplTest {

	private static final int TEST_STUDY_IDENTIFIER = 2013;

	private static final int TEST_OBSERVATION_IDENTIIFER = 5;

	private StudyServiceImpl studyServiceImpl;

	@Mock
	private StudyService mockMiddlewareStudyService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private ConversionService conversionService;

	@Mock
	private DataImportService dataImportService;

	@Mock
	private SecurityService securityService;

	@Mock
	private ObservationValidator observationValidator;

    @Mock
    private FieldMapService mapService;


	private final String programUID = UUID.randomUUID().toString();

	final PodamFactory factory = new PodamFactoryImpl();

	final Function<ObservationDto, Observation> observationTransformFunction = new Function<ObservationDto, Observation>() {

		@Override
		public Observation apply(final ObservationDto input) {
			return StudyServiceImplTest.this.mapObservationDtoToObservation(input);
		}

	};

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.studyServiceImpl = new StudyServiceImpl();
		this.studyServiceImpl.setMiddlewareStudyService(this.mockMiddlewareStudyService);
		this.studyServiceImpl.setConversionService(this.conversionService);
		this.studyServiceImpl.setFieldbookService(this.fieldbookService);
		this.studyServiceImpl.setGermplasmListManager(this.germplasmListManager);
		this.studyServiceImpl.setStudyDataManager(this.studyDataManager);
		this.studyServiceImpl.setDataImportService(this.dataImportService);
		this.studyServiceImpl.setSecurityService(this.securityService);
		this.studyServiceImpl.setValidationUtil(new ValidationUtil());
		this.studyServiceImpl.setObservationValidator(this.observationValidator);
        Mockito.mock(FieldMapService.class);
		// Make all test data accessible
		Mockito.when(this.securityService.isAccessible(Mockito.any(org.generationcp.middleware.service.api.study.StudySummary.class)))
				.thenReturn(true);
	}

	@Test
	public void listAllStudies() throws MiddlewareQueryException {

		final List<org.generationcp.middleware.service.api.study.StudySummary> mockResult = new ArrayList<>();
		final org.generationcp.middleware.service.api.study.StudySummary studySummary =
				new org.generationcp.middleware.service.api.study.StudySummary();
		studySummary.setId(1);
		studySummary.setName("Study Name");
		studySummary.setObjective("Study Objective");
		studySummary.setTitle("Study Title");
		studySummary.setProgramUUID(this.programUID);
		studySummary.setStartDate("2015-01-01");
		studySummary.setEndDate("2015-12-31");
		studySummary.setType(StudyType.T);

		mockResult.add(studySummary);

		Mockito.when(this.mockMiddlewareStudyService.search(Mockito.any(StudySearchParameters.class))).thenReturn(mockResult);

		final List<StudySummary> studySummaries = this.studyServiceImpl.search(this.programUID, null, null, null);
		Assert.assertEquals(mockResult.size(), studySummaries.size());
		Assert.assertEquals(studySummary.getId().toString(), studySummaries.get(0).getId());
		Assert.assertEquals(studySummary.getName(), studySummaries.get(0).getName());
		Assert.assertEquals(studySummary.getTitle(), studySummaries.get(0).getTitle());
		Assert.assertEquals(studySummary.getObjective(), studySummaries.get(0).getObjective());
		Assert.assertEquals(studySummary.getStartDate(), studySummaries.get(0).getStartDate());
		Assert.assertEquals(studySummary.getEndDate(), studySummaries.get(0).getEndDate());
		Assert.assertEquals(studySummary.getType().toString(), studySummaries.get(0).getType());

	}

    @Test
    public void testSearch() throws Exception{
        final List<org.generationcp.middleware.service.api.study.StudySummary> mockResult = new ArrayList<>();
        final org.generationcp.middleware.service.api.study.StudySummary studySummary =
                new org.generationcp.middleware.service.api.study.StudySummary();
        studySummary.setId(1);
        studySummary.setPrincipalInvestigator("PITest");
        studySummary.setType(StudyType.S);
        studySummary.setLocation("Loc");
        studySummary.setSeason("sea");


        mockResult.add(studySummary);
        Mockito.when(this.securityService.isAccessible(studySummary))
                .thenReturn(true);

        StudySearchParameters searchParameters = new StudySearchParameters();
        searchParameters.setProgramUniqueId(this.programUID);

        Mockito.when(this.mockMiddlewareStudyService.search(searchParameters)).thenReturn(mockResult);

        List<StudySummary> studySummaries = this.studyServiceImpl.search(this.programUID, null , null, null);

        Assert.assertNotNull(studySummaries);
        Assert.assertEquals(studySummaries.get(0).getId() , String.valueOf(studySummary.getId()));
        Assert.assertEquals(studySummaries.get(0).getPrincipalInvestigator() , studySummary.getPrincipalInvestigator());
        Assert.assertEquals(studySummaries.get(0).getLocation() , studySummary.getLocation());
        Assert.assertEquals(studySummaries.get(0).getSeason() , studySummary.getSeason());

    }

    @Test
    public void testSearchWithAccess() throws Exception{
        final List<org.generationcp.middleware.service.api.study.StudySummary> mockResult = new ArrayList<>();
        final org.generationcp.middleware.service.api.study.StudySummary studySummary =
                new org.generationcp.middleware.service.api.study.StudySummary();
        studySummary.setId(1);

        mockResult.add(studySummary);
        Mockito.when(this.securityService.isAccessible(studySummary))
                .thenReturn(false);

        StudySearchParameters searchParameters = new StudySearchParameters();
        searchParameters.setProgramUniqueId(this.programUID);

        Mockito.when(this.mockMiddlewareStudyService.search(searchParameters)).thenReturn(mockResult);

        List<StudySummary> searchStudySummary = this.studyServiceImpl.search(this.programUID, null , null, null);
        Assert.assertNotNull(searchStudySummary);

    }

    @Test(expected = ApiRuntimeException.class)
    public void testSearchWithMiddlewareException() throws Exception{
        final List<org.generationcp.middleware.service.api.study.StudySummary> mockResult = new ArrayList<>();
        final org.generationcp.middleware.service.api.study.StudySummary studySummary =
                new org.generationcp.middleware.service.api.study.StudySummary();
        studySummary.setId(1);

        mockResult.add(studySummary);

        StudySearchParameters searchParameters = new StudySearchParameters();
        searchParameters.setProgramUniqueId(this.programUID);

        Mockito.when(this.mockMiddlewareStudyService.search(searchParameters)).thenThrow(new MiddlewareException("Middleware Exception"));

        this.studyServiceImpl.search(this.programUID, null , null, null);

    }

	@Test
	public void getStudyGermplasmList() throws MiddlewareQueryException {

		final List<StudyGermplasmDto> studyGermplasmTestData =
				Lists.newArrayList(this.factory.manufacturePojo(StudyGermplasmDto.class),
						this.factory.manufacturePojo(StudyGermplasmDto.class));
		Mockito.when(this.mockMiddlewareStudyService.getStudyGermplasmList(StudyServiceImplTest.TEST_STUDY_IDENTIFIER)).thenReturn(
				studyGermplasmTestData);

		final Function<StudyGermplasmDto, StudyGermplasm> transformFunction = new Function<StudyGermplasmDto, StudyGermplasm>() {

			@Override
			public StudyGermplasm apply(final StudyGermplasmDto studyGermplasmDto) {
				final StudyGermplasm studyGermplasm = new StudyGermplasm();
				studyGermplasm.setEntryNumber(studyGermplasmDto.getEntryNumber());
				studyGermplasm.setEntryType(studyGermplasmDto.getEntryType());
				studyGermplasm.setPosition(studyGermplasmDto.getPosition());
				studyGermplasm.setGermplasmListEntrySummary(new GermplasmListEntrySummary(studyGermplasmDto.getGermplasmId(),
						studyGermplasmDto.getDesignation(), studyGermplasmDto.getSeedSource(), studyGermplasmDto.getEntryCode(),
						studyGermplasmDto.getCross()));
				return studyGermplasm;
			}
		};

		final List<StudyGermplasm> studyGermplasmList = this.studyServiceImpl.getStudyGermplasmList(2013);

		final List<StudyGermplasm> expectedResults = Lists.transform(studyGermplasmTestData, transformFunction);
		Assert.assertEquals(expectedResults, studyGermplasmList);
	}

	@Test
	public void getObservations() {
		final List<ObservationDto> observationDtoTestData =
				Lists.newArrayList(this.factory.manufacturePojo(ObservationDto.class), this.factory.manufacturePojo(ObservationDto.class));
		Mockito.when(this.mockMiddlewareStudyService.getObservations(StudyServiceImplTest.TEST_STUDY_IDENTIFIER)).thenReturn(
				observationDtoTestData);

		final List<Observation> actualObservations = this.studyServiceImpl.getObservations(StudyServiceImplTest.TEST_STUDY_IDENTIFIER);

		Assert.assertEquals(Lists.transform(observationDtoTestData, this.observationTransformFunction), actualObservations);

	}

	@Test(expected = ApiRequestValidationException.class)
	public void updateObservationWhichDoesNotExist() {
		final Integer studyIdentifier = new Integer(5);
		Observation manufacturePojo = this.factory.manufacturePojo(Observation.class);

		try {
			this.studyServiceImpl.updateObservation(studyIdentifier, manufacturePojo);
		} catch (final ApiRequestValidationException apiRequestValidationException) {
			List<ObjectError> errors = apiRequestValidationException.getErrors();
			Assert.assertEquals("We should only have one error", 1, errors.size());
			Assert.assertEquals("The error should have the code", "no.observation.found", errors.get(0).getCode());
			Assert.assertEquals("The error have the study identifier as its first parameter", studyIdentifier,
					errors.get(0).getArguments()[0]);
			Assert.assertEquals("The error have the observation unique identifier as its second parameter",
					manufacturePojo.getUniqueIdentifier(), errors.get(0).getArguments()[1]);
			throw apiRequestValidationException;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void updateObservationsWhichDoesNotExist() {
		final Integer studyIdentifier = new Integer(5);
		Observation manufacturePojo = this.factory.manufacturePojo(Observation.class);
		Observation manufacturePojo1 = this.factory.manufacturePojo(Observation.class);
		Observation manufacturePojo2 = this.factory.manufacturePojo(Observation.class);
		try {
			this.studyServiceImpl.updateObservations(studyIdentifier,
					Lists.newArrayList(manufacturePojo, manufacturePojo1, manufacturePojo2));
		} catch (final ApiRequestValidationException apiRequestValidationException) {
			List<ObjectError> errors = apiRequestValidationException.getErrors();
			// This is because we are just stopping at the first error
			Assert.assertEquals("We should only have one error", 1, errors.size());
			Assert.assertEquals("The error should have the code", "no.observation.found", errors.get(0).getCode());
			Assert.assertEquals("The error have the study identifier as its first parameter", studyIdentifier,
					errors.get(0).getArguments()[0]);
			Assert.assertEquals("The error have the observation unique identifier as its second parameter",
					manufacturePojo.getUniqueIdentifier(), errors.get(0).getArguments()[1]);
			throw apiRequestValidationException;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void updateAnAlreadyInsertedMeasurement() {
		final MeasurementDto databaseReturnedMeasurement = new MeasurementDto(new TraitDto(1, "Plant Height"), 1, "123");
		final ObservationDto databaseReturnedObservationValue =
				new ObservationDto(1, "1", "Test", 1, "CML123", "1", "CIMMYT Seed Bank", "1", "1", Lists.newArrayList(databaseReturnedMeasurement));
		final List<ObservationDto> observationDtoTestData = Lists.newArrayList(databaseReturnedObservationValue);
		Mockito.when(
				this.mockMiddlewareStudyService.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						databaseReturnedObservationValue.getMeasurementId())).thenReturn(observationDtoTestData);
		try {
			final Observation observation = Lists.transform(observationDtoTestData, this.observationTransformFunction).get(0);
			observation.getMeasurements().get(0).getMeasurementIdentifier().setMeasurementId(null);
			this.studyServiceImpl.updateObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,observation);
		} catch (final ApiRequestValidationException apiRequestValidationException) {
			List<ObjectError> errors = apiRequestValidationException.getErrors();
			// This is because we are just stopping at the first error
			Assert.assertEquals("We should only have one error", 1, errors.size());
			Assert.assertEquals("The error should have the code", "measurement.already.inserted", errors.get(0).getCode());
			throw apiRequestValidationException;
		}
	}

	@Test
	public void ensureValidationInvokedOnUpdateObservation() {
		final ObservationDto manufacturePojo = this.factory.manufacturePojo(ObservationDto.class);
		final List<ObservationDto> observationDtoTestData = Lists.newArrayList(manufacturePojo);
		Mockito.when(
				this.mockMiddlewareStudyService.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						manufacturePojo.getMeasurementId())).thenReturn(observationDtoTestData);
		Mockito.when(this.mockMiddlewareStudyService.updataObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, manufacturePojo))
				.thenReturn(manufacturePojo);

		final Observation observation = Lists.transform(observationDtoTestData, this.observationTransformFunction).get(0);
		this.studyServiceImpl.updateObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, observation);
		Mockito.verify(observationValidator, Mockito.times(1)).validate(Mockito.eq(observation), Mockito.any(Errors.class));
	}

	@Test
	public void ensureValidationInvokedOnUpdateObservations() {

		final ObservationDto manufacturePojo = this.factory.manufacturePojo(ObservationDto.class);
		Mockito.when(
				this.mockMiddlewareStudyService.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						manufacturePojo.getMeasurementId())).thenReturn(Lists.newArrayList(manufacturePojo));
		Mockito.when(this.mockMiddlewareStudyService.updataObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, manufacturePojo))
				.thenReturn(manufacturePojo);
		final List<Observation> observations =
				Lists.transform(Lists.newArrayList(manufacturePojo, manufacturePojo), this.observationTransformFunction);
		this.studyServiceImpl.updateObservations(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, observations);

		observations.get(0).equals(observations.get(1));
		Mockito.verify(observationValidator, Mockito.times(2)).validate(Mockito.eq(observations.get(0)), Mockito.any(Errors.class));

	}

	@Test
	public void getSingleObservations() {
		final List<ObservationDto> observationDtoTestData = Lists.newArrayList(this.factory.manufacturePojo(ObservationDto.class));
		Mockito.when(
				this.mockMiddlewareStudyService.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						StudyServiceImplTest.TEST_OBSERVATION_IDENTIIFER)).thenReturn(observationDtoTestData);

		final Observation actualObservations =
				this.studyServiceImpl.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						StudyServiceImplTest.TEST_OBSERVATION_IDENTIIFER);

		Assert.assertEquals(Lists.transform(observationDtoTestData, this.observationTransformFunction).get(0), actualObservations);

	}

	private Observation mapObservationDtoToObservation(final ObservationDto measurement) {
		final Observation observation = new Observation();
		if (measurement != null) {
			observation.setUniqueIdentifier(measurement.getMeasurementId());
			observation.setEntryNumber(measurement.getEntryNo());
			observation.setEntryType(measurement.getEntryType());
			observation.setEnvironmentNumber(measurement.getTrialInstance());
			observation.setGermplasmDesignation(measurement.getDesignation());
			observation.setGermplasmId(measurement.getGid());
			observation.setPlotNumber(measurement.getPlotNumber());
			observation.setReplicationNumber(measurement.getRepitionNumber());
			observation.setSeedSource(measurement.getSeedSource());

			final List<MeasurementDto> traits = measurement.getTraitMeasurements();
			final List<Measurement> measurements = new ArrayList<Measurement>();
			for (final MeasurementDto trait : traits) {
				measurements.add(new Measurement(new MeasurementIdentifier(trait.getPhenotypeId(), new Trait(trait.getTrait().getTraitId(),
						trait.getTrait().getTraitName())), trait.getTriatValue()));
			}

			observation.setMeasurements(measurements);
		}
		return observation;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void importStudy() {

		// Minimal setup
		final StudyImportDTO studyImportDTO = new StudyImportDTO();
		studyImportDTO.setStudyType("N");
		studyImportDTO.setUserId(1);

		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		workbook.setStudyDetails(studyDetails);

		Mockito.when(this.conversionService.convert(studyImportDTO, Workbook.class)).thenReturn(workbook);
		this.studyServiceImpl.importStudy(studyImportDTO, this.programUID);

		// Only asserting interactions with key collaborators
		Mockito.verify(this.conversionService).convert(studyImportDTO, Workbook.class);
		Mockito.verify(this.dataImportService).saveDataset(workbook, true, false, this.programUID);
		Mockito.verify(this.conversionService).convert(studyImportDTO, GermplasmList.class);
		Mockito.verify(this.germplasmListManager).addGermplasmList(Mockito.any(GermplasmList.class));
		Mockito.verify(this.germplasmListManager).addGermplasmListData(Mockito.anyList());
		Mockito.verify(this.fieldbookService).saveOrUpdateListDataProject(Mockito.anyInt(), Mockito.any(GermplasmListType.class),
				Mockito.anyInt(), Mockito.anyList(), Mockito.anyInt());
	}


    @SuppressWarnings("unchecked")
    @Test
    public void importStudyGermplasmListData() {

        // Minimal setup
        final StudyImportDTO studyImportDTO = new StudyImportDTO();
        studyImportDTO.setStudyType("N");
        studyImportDTO.setUserId(1);

        StudyGermplasm studyGermplasm = new StudyGermplasm();
        studyGermplasm.setEntryNumber(1);
        studyGermplasm.setEntryType("Entry Type");
        studyGermplasm.setPosition("Position");

        //GermplasmListEntrySummary Data
        GermplasmListEntrySummary listEntrySummary = new GermplasmListEntrySummary();
        listEntrySummary.setGid(1);
        listEntrySummary.setCross("Cross");
        listEntrySummary.setDesignation("Designation");
        listEntrySummary.setSeedSource("Seed source");
        listEntrySummary.setEntryCode("Entry code");

        //Setting germplasmListEntrySummary to studyGermplasm
        studyGermplasm.setGermplasmListEntrySummary(listEntrySummary);

        //Setting studyGermplasm in studyImportDto
        studyImportDTO.setGermplasm(Lists.newArrayList(studyGermplasm));

        //Setting values for GermplasmListData
        GermplasmListData listData = new GermplasmListData();
        listData.setGid(1);
        listData.setId(2);
        listData.setEntryCode("Entry Code");
        listData.setSeedSource("Seed source");
        listData.setDesignation("Designation");
        listData.setFgid(1);
        listData.setFemaleParent("Female Parent");

        //Setting values for Germplasm
        Germplasm germplasm = new Germplasm();
        germplasm.setGid(1);
        germplasm.setGnpgs(1);
        germplasm.setGrplce(2);

        //Setting germplasm into GermplasmListData
        listData.setGermplasm(germplasm);

        listData.setGroupId(9);
        listData.setGroupName("GroupInfo");

        //Setting listDataInventoryInfo values
        ListDataInventory dataInventory = new ListDataInventory(1 , 1);

        //Setting inventoryInfo to GermplasmListdata
        listData.setInventoryInfo(dataInventory);

        Mockito.when(this.conversionService.convert(studyGermplasm , GermplasmListData.class))
                .thenReturn(listData);

        final Workbook workbook = new Workbook();
        final StudyDetails studyDetails = new StudyDetails();
        workbook.setStudyDetails(studyDetails);

        //Setting values for germplasm list
        GermplasmList germplasmList = new GermplasmList();
        germplasmList.setId(1);
        germplasmList.setDescription("Description");
        germplasmList.setDate(20150301L);
        germplasmList.setListData(Lists.newArrayList(listData));
        germplasmList.seteDate(20150320);
        germplasmList.setListLocation(1);
        germplasmList.setListRef(1);
        germplasmList.setName("List Name");
        germplasmList.setNotes("Notes");
        germplasmList.setParent(germplasmList);
        germplasmList.setListData((Lists.newArrayList(listData)));

        //Setting list in germplasmListData
        listData.setList(germplasmList);

        Mockito.when(this.conversionService.convert(studyImportDTO , GermplasmList.class))
                .thenReturn(germplasmList);

        Mockito.when(this.conversionService.convert(studyImportDTO, Workbook.class))
                .thenReturn(workbook);

        this.studyServiceImpl.importStudy(studyImportDTO, this.programUID);

        // Only asserting interactions with key collaborators
        Mockito.verify(this.conversionService).convert(studyImportDTO, Workbook.class);
        Mockito.verify(this.conversionService).convert(studyImportDTO, GermplasmList.class);
        Mockito.verify(this.dataImportService).saveDataset(workbook, true, false, this.programUID);
        Mockito.verify(this.conversionService).convert(studyImportDTO, GermplasmList.class);
        Mockito.verify(this.germplasmListManager).addGermplasmList(Mockito.any(GermplasmList.class));
        Mockito.verify(this.germplasmListManager).addGermplasmListData(Mockito.anyList());
        Mockito.verify(this.fieldbookService).saveOrUpdateListDataProject(Mockito.anyInt(), Mockito.any(GermplasmListType.class),
                Mockito.anyInt(), Mockito.anyList(), Mockito.anyInt());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void importStudyWithTrialStudyType() {

        // Minimal setup
        final StudyImportDTO studyImportDTO = new StudyImportDTO();
        studyImportDTO.setStudyType("T");
        studyImportDTO.setUserId(1);

        final Workbook workbook = new Workbook();
        final StudyDetails studyDetails = new StudyDetails();
        workbook.setStudyDetails(studyDetails);

        Mockito.when(this.conversionService.convert(studyImportDTO, Workbook.class)).thenReturn(workbook);
        this.studyServiceImpl.importStudy(studyImportDTO, this.programUID);

        // Only asserting interactions with key collaborators
        Mockito.verify(this.conversionService).convert(studyImportDTO, Workbook.class);
        Mockito.verify(this.dataImportService).saveDataset(workbook, true, false, this.programUID);
        Mockito.verify(this.conversionService).convert(studyImportDTO, GermplasmList.class);
        Mockito.verify(this.germplasmListManager).addGermplasmList(Mockito.any(GermplasmList.class));
        Mockito.verify(this.germplasmListManager).addGermplasmListData(Mockito.anyList());
        Mockito.verify(this.fieldbookService).saveOrUpdateListDataProject(Mockito.anyInt(), Mockito.any(GermplasmListType.class),
                Mockito.anyInt(), Mockito.anyList(), Mockito.anyInt());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ApiRuntimeException.class)
    public void importStudyException() {

        // Minimal setup
        final StudyImportDTO studyImportDTO = new StudyImportDTO();
        studyImportDTO.setStudyType("T");
        studyImportDTO.setUserId(1);

        final Workbook workbook = new Workbook();
        final StudyDetails studyDetails = new StudyDetails();
        workbook.setStudyDetails(studyDetails);

        Mockito.when(this.conversionService.convert(studyImportDTO, Workbook.class)).thenThrow(new MiddlewareQueryException("Exception"));
        this.studyServiceImpl.importStudy(studyImportDTO, this.programUID);
    }

        @Test
    public void testGetStudyDetails() throws Exception{
        int studyId = 9;

        //Setting values for VariableConstraints
        VariableConstraints constraints = new VariableConstraints();
        constraints.setMinValueId(2);
        constraints.setMinValue(9.0);
        constraints.setMaxValueId(3);
        constraints.setMaxValue(19.0);

        //Setting values for term
        Term term = new Term();
        term.setId(4);
        term.setName("Term name");
        term.setDefinition("Definition");
        term.setObsolete(false);
        term.setVocabularyId(5);

        Term methodTerm = TestDataProvider.getMethodTerm();

        //Setting values for standardVariables
        StandardVariable standardVariable = new StandardVariable();
        standardVariable.setId(6);
        standardVariable.setConstraints(constraints);
        standardVariable.setCropOntologyId("CO:001");
        standardVariable.setDataType(term);
        standardVariable.setName("Standard Variable");
        standardVariable.setDescription("Description");
        standardVariable.setMethod(methodTerm);
        standardVariable.setPhenotypicType(PhenotypicType.getPhenotypicTypeById(term.getId()));

        //Setting values for DMSVariableType
        DMSVariableType variableType = new DMSVariableType();
        variableType.setLocalDescription("Local Description");
        variableType.setLocalName("Local Name");
        variableType.setRank(7);
        variableType.setStandardVariable(standardVariable);

        //Setting variable values
        Variable variable = new Variable();
        variable.setValue("8");
        variable.setPhenotypeId(9);
        variable.setVariableType(variableType);

        Variable variableValue = new Variable();
        variableValue.setValue("10");
        variableValue.setPhenotypeId(11);
        variableValue.setVariableType(variableType);

        //Setting variableList values
        VariableList list = new VariableList();
        list.setVariables(Lists.newArrayList(variable));
        list.add(variable);
        list.add(variableValue);

        //Setting study values
        Study study = Mockito.mock(Study.class);
        study.setId(studyId);
        Mockito.when(study.getName()).thenReturn("Maizing Trial");
        Mockito.when(study.getTitle()).thenReturn("Title");
        Mockito.when(study.getObjective()).thenReturn("Objective");
        Mockito.when(study.getType()).thenReturn(StudyType.T);
        Mockito.when(study.getStartDate()).thenReturn(20150101);
        Mockito.when(study.getEndDate()).thenReturn(20151231);
        Mockito.when(study.getConditions()).thenReturn(list);
        study.setProgramUUID(this.programUID);

        //Setting variableTypeList Values
        VariableTypeList typeList = new VariableTypeList();
        typeList.setVariableTypes(Lists.newArrayList(variableType));

        //Setting datasetreference values
        DatasetReference reference = new DatasetReference(studyId , "DatasetReference");
        reference.setId(12);
        reference.setDescription("Description");
        reference.setName("Name-ENVIRONMENT");
        reference.setProgramUUID(this.programUID);

        //Setting experiment values
        Experiment experiment = new Experiment();
        experiment.setId(13);
        experiment.setFactors(list);
        experiment.setLocationId(14);
        experiment.setVariates(list);

        Mockito.when(this.studyDataManager.getAllStudyFactors(studyId))
                .thenReturn(typeList);
        Mockito.when(this.studyDataManager.getAllStudyVariates(studyId))
                .thenReturn(typeList);
        Mockito.when(this.studyDataManager.getDatasetReferences(studyId))
                .thenReturn(Lists.newArrayList(reference));
        Mockito.when(this.studyDataManager.getExperiments(reference.getId() ,  0, Integer.MAX_VALUE))
                .thenReturn(Lists.newArrayList(experiment));
        Mockito.when(this.studyDataManager.getStudy(studyId))
                .thenReturn(study);

        org.ibp.api.domain.study.StudyDetails studyDetails = studyServiceImpl.getStudyDetails(String.valueOf(studyId));

        Assert.assertNotNull(studyDetails);
        Assert.assertEquals(studyDetails.getGeneralInfo().iterator().next().getId() , String.valueOf(variableType.getId()));
        Assert.assertEquals(studyDetails.getGeneralInfo().iterator().next().getName() , variableType.getLocalName());
        Assert.assertEquals(studyDetails.getGeneralInfo().iterator().next().getDescription() , variableType.getLocalDescription());
        Assert.assertEquals(studyDetails.getEnvironments().iterator().next().getEnvironmentDetails().iterator().next().getId() , String.valueOf(standardVariable.getId()));
        Assert.assertEquals(studyDetails.getEnvironments().iterator().next().getEnvironmentDetails().iterator().next().getName() , variableType.getLocalName());
        Assert.assertEquals(studyDetails.getEnvironments().iterator().next().getEnvironmentDetails().iterator().next().getValue() , variable.getValue());
        Assert.assertEquals(studyDetails.getEnvironments().iterator().next().getEnvironmentDetails().iterator().next().getDescription() , variableType.getLocalDescription());
        Assert.assertEquals(studyDetails.getTraits().iterator().next().getId(), String.valueOf(standardVariable.getId()));
        Assert.assertEquals(studyDetails.getTraits().iterator().next().getName(), standardVariable.getName());
        Assert.assertEquals(studyDetails.getTraits().iterator().next().getDescription(), standardVariable.getDescription());
        Assert.assertEquals(studyDetails.getDatasets().iterator().next().getId(), String.valueOf(reference.getId()));
        Assert.assertEquals(studyDetails.getDatasets().iterator().next().getName(), reference.getName());
        Assert.assertEquals(studyDetails.getDatasets().iterator().next().getDescription(), reference.getDescription());
        Assert.assertEquals(studyDetails.getName(), study.getName());
        Assert.assertEquals(studyDetails.getTitle(), study.getTitle());
        Assert.assertEquals(studyDetails.getObjective(), study.getObjective());
        Assert.assertEquals(studyDetails.getType(), study.getType().getName());
        Assert.assertEquals(studyDetails.getStartDate(), study.getStartDate().toString());
        Assert.assertEquals(studyDetails.getEndDate(), study.getEndDate().toString());
    }

    @Test(expected = ApiRuntimeException.class)
    public void testGetStudyDetailsWithWrongNonNumericId() throws Exception{
        String nonNumericId = RandomStringUtils.randomAlphabetic(1);

        Study study = new Study();
        study.setId(TEST_STUDY_IDENTIFIER);

        Mockito.when(this.studyDataManager.getStudy(TEST_STUDY_IDENTIFIER)).thenReturn(study);


        studyServiceImpl.getStudyDetails(nonNumericId);
    }

    @Test(expected = ApiRuntimeException.class)
    public void testGetStudyDetailsWithWrongId() throws Exception{
        int studyId =10;

        Study study = new Study();
        study.setId(studyId);

        Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
        String intValue = RandomStringUtils.randomNumeric(1);

        studyServiceImpl.getStudyDetails(intValue);
    }

    @Test(expected = ApiRuntimeException.class)
    public void testGetStudyDetailsWithException() throws Exception{
        int studyId =10;

        Study study = new Study();
        study.setId(studyId);

        Mockito.when(this.studyDataManager.getStudy(studyId)).thenThrow(new MiddlewareException("Middleware Exception"));

        studyServiceImpl.getStudyDetails(String.valueOf(studyId));
    }

    @Test
    public void testGetFieldMap() throws Exception{

        List<Integer> studyList = new ArrayList<>();
        studyList.add(9);

        //Setting fieldMapLabel Values
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

        //Setting FieldMapTrialInstanceInfo values
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


        //Setting FieldMapDatasetInfo values
        FieldMapDatasetInfo datasetInfo = new FieldMapDatasetInfo();
        datasetInfo.setDatasetId(1);
        datasetInfo.setDatasetName("Dataset Name");
        datasetInfo.setTrialInstances(Lists.newArrayList(trialInstanceInfo));

        //Setting FielMapInfo Values
        FieldMapInfo fieldMapInfo = new FieldMapInfo();
        fieldMapInfo.setFieldbookId(1);
        fieldMapInfo.setFieldbookName("FieldBookName");
        fieldMapInfo.setTrial(true);
        fieldMapInfo.setDatasets(Lists.newArrayList(datasetInfo));

        //Setting FieldMapPlantingDetails values
        FieldMapPlantingDetails plantingDetails = new FieldMapPlantingDetails();
        plantingDetails.setBlockCapacity("2");
        plantingDetails.setColumns(2);
        plantingDetails.setFieldLocation("Field Location");
        plantingDetails.setPlotLayout("Plot Layout");
        plantingDetails.setFieldName("Field Name");
        plantingDetails.setRowCapacityOfPlantingMachine(2);
        plantingDetails.setRowsPerPlot(1);
        plantingDetails.setStartingCoordinates("0");

        //Setting FieldMapStudySummary values
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

        //Setting FieldMapMetaData values
        FieldMapMetaData metaData = new FieldMapMetaData();
        metaData.setFieldPlantingDetails(plantingDetails);
        metaData.setRelevantStudies(Lists.newArrayList(studySummary));

        //Setting FieldMap Values
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

        Mockito.when(this.mapService.getFieldMap("9"))
                .thenReturn(fieldMapValue);
        Mockito.doReturn(Lists.newArrayList(fieldMapInfo)).when(this.studyDataManager).getFieldMapInfoOfStudy(studyList, StudyType.T , null, true);
        Mockito.when(this.studyDataManager.getStudyType(9))
                .thenReturn(StudyType.T);

        Map<Integer, FieldMap> fieldMapValues =  studyServiceImpl.getFieldMap(String.valueOf(9));

        Assert.assertNotNull(fieldMapValues);
    }

    @Test
    public void testGetAllStudyFolders() throws Exception{

        FolderReference folderReference = new FolderReference(1, 2, "My Folder", "My Folder Description");

        Mockito.when(this.studyDataManager.getAllFolders())
                .thenReturn(Lists.newArrayList(folderReference));

        List<StudyFolder> studyFolders = studyServiceImpl.getAllStudyFolders();

        Assert.assertNotNull(studyFolders);
    }

    @Test
    public void testGetProgramUUID() throws Exception{

        int studyId = 10;

        Mockito.when(this.mockMiddlewareStudyService.getProgramUUID(studyId)).thenReturn(this.programUID);

        String programUUID = studyServiceImpl.getProgramUUID(studyId);

        Assert.assertNotNull(programUUID);
    }
}
