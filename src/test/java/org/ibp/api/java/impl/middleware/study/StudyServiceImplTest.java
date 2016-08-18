
package org.ibp.api.java.impl.middleware.study;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
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
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.StudySummary;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.domain.study.validators.ObservationValidator;
import org.ibp.api.exception.ApiRequestValidationException;
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
		Mockito.when(this.mockMiddlewareStudyService.getObservations(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, 1, 1, 100))
				.thenReturn(
				observationDtoTestData);

		final List<Observation> actualObservations =
				this.studyServiceImpl.getObservations(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, 1, 1, 100);

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
}
