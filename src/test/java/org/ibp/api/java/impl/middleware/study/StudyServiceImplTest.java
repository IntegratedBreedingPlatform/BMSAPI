
package org.ibp.api.java.impl.middleware.study;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.domain.common.ValidationUtil;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.domain.study.validators.ObservationValidator;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.ArrayList;
import java.util.List;

public class StudyServiceImplTest {

	private static final int TEST_STUDY_IDENTIFIER = 2013;

	private static final int TEST_OBSERVATION_IDENTIFIER = 5;

	private StudyServiceImpl studyServiceImpl;

	@Mock
	private StudyService mockMiddlewareStudyService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private ObservationValidator observationValidator;

	final PodamFactory factory = new PodamFactoryImpl();

	final Function<ObservationDto, Observation> observationTransformFunction =
		input -> StudyServiceImplTest.this.mapObservationDtoToObservation(input);


	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);

		this.studyServiceImpl = new StudyServiceImpl();
		this.studyServiceImpl.setMiddlewareStudyService(this.mockMiddlewareStudyService);
		this.studyServiceImpl.setStudyDataManager(this.studyDataManager);
		this.studyServiceImpl.setValidationUtil(new ValidationUtil());
		this.studyServiceImpl.setObservationValidator(this.observationValidator);
		this.studyServiceImpl.setStudyValidator(this.studyValidator);
	}

	@Test
	public void testGetObservations() {
		final List<ObservationDto> observationDtoTestData =
				Lists.newArrayList(this.factory.manufacturePojo(ObservationDto.class), this.factory.manufacturePojo(ObservationDto.class));
		Mockito.when(this.mockMiddlewareStudyService.getObservations(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, 1, 1, 100, null, null))
				.thenReturn(
				observationDtoTestData);

		final List<Observation> actualObservations =
				this.studyServiceImpl.getObservations(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, 1, 1, 100, null, null);

		Assert.assertEquals(Lists.transform(observationDtoTestData, this.observationTransformFunction), actualObservations);

	}

	@Test(expected = ApiRequestValidationException.class)
	public void testUpdateObservationWhichDoesNotExist() {
		final Integer studyIdentifier = new Integer(5);
		final Observation manufacturePojo = this.factory.manufacturePojo(Observation.class);

		try {
			this.studyServiceImpl.updateObservation(studyIdentifier, manufacturePojo);
		} catch (final ApiRequestValidationException apiRequestValidationException) {
			final List<ObjectError> errors = apiRequestValidationException.getErrors();
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
	public void testUpdateObservationsWhichDoesNotExist() {
		final Integer studyIdentifier = new Integer(5);
		final Observation manufacturePojo = this.factory.manufacturePojo(Observation.class);
		final Observation manufacturePojo1 = this.factory.manufacturePojo(Observation.class);
		final Observation manufacturePojo2 = this.factory.manufacturePojo(Observation.class);
		try {
			this.studyServiceImpl.updateObservations(studyIdentifier,
					Lists.newArrayList(manufacturePojo, manufacturePojo1, manufacturePojo2));
		} catch (final ApiRequestValidationException apiRequestValidationException) {
			final List<ObjectError> errors = apiRequestValidationException.getErrors();
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
	public void testUpdateAnAlreadyInsertedMeasurement() {
		final MeasurementDto databaseReturnedMeasurement =
			new MeasurementDto(new MeasurementVariableDto(1, "Plant Height"), 1, "123", Phenotype.ValueStatus.OUT_OF_SYNC);
		final ObservationDto databaseReturnedObservationValue =
				new ObservationDto(1, "1", "Test", 1, "CML123", "1", "CIMMYT Seed Bank", "1", "1", "2",
						Lists.newArrayList(databaseReturnedMeasurement));
		final List<ObservationDto> observationDtoTestData = Lists.newArrayList(databaseReturnedObservationValue);
		Mockito.when(
				this.mockMiddlewareStudyService.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						databaseReturnedObservationValue.getMeasurementId())).thenReturn(observationDtoTestData);
		try {
			final Observation observation = Lists.transform(observationDtoTestData, this.observationTransformFunction).get(0);
			observation.getMeasurements().get(0).getMeasurementIdentifier().setMeasurementId(null);
			this.studyServiceImpl.updateObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,observation);
		} catch (final ApiRequestValidationException apiRequestValidationException) {
			final List<ObjectError> errors = apiRequestValidationException.getErrors();
			// This is because we are just stopping at the first error
			Assert.assertEquals("We should only have one error", 1, errors.size());
			Assert.assertEquals("The error should have the code", "measurement.already.inserted", errors.get(0).getCode());
			throw apiRequestValidationException;
		}
	}

	@Test
	public void testEnsureValidationInvokedOnUpdateObservation() {
		final ObservationDto manufacturePojo = this.factory.manufacturePojo(ObservationDto.class);
		final List<ObservationDto> observationDtoTestData = Lists.newArrayList(manufacturePojo);
		Mockito.when(
				this.mockMiddlewareStudyService.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						manufacturePojo.getMeasurementId())).thenReturn(observationDtoTestData);
		Mockito.when(this.mockMiddlewareStudyService.updateObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, manufacturePojo))
				.thenReturn(manufacturePojo);

		final Observation observation = Lists.transform(observationDtoTestData, this.observationTransformFunction).get(0);
		this.studyServiceImpl.updateObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, observation);
		Mockito.verify(this.observationValidator, Mockito.times(1)).validate(Matchers.eq(observation), Matchers.any(Errors.class));
	}

	@Test
	public void testEnsureValidationInvokedOnUpdateObservations() {

		final ObservationDto manufacturePojo = this.factory.manufacturePojo(ObservationDto.class);
		Mockito.when(
				this.mockMiddlewareStudyService.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						manufacturePojo.getMeasurementId())).thenReturn(Lists.newArrayList(manufacturePojo));
		Mockito.when(this.mockMiddlewareStudyService.updateObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, manufacturePojo))
				.thenReturn(manufacturePojo);
		final List<Observation> observations =
				Lists.transform(Lists.newArrayList(manufacturePojo, manufacturePojo), this.observationTransformFunction);
		this.studyServiceImpl.updateObservations(StudyServiceImplTest.TEST_STUDY_IDENTIFIER, observations);

		observations.get(0).equals(observations.get(1));
		Mockito.verify(this.observationValidator, Mockito.times(2)).validate(Matchers.eq(observations.get(0)), Matchers.any(Errors.class));

	}

	@Test
	public void testGetSingleObservations() {
		final List<ObservationDto> observationDtoTestData = Lists.newArrayList(this.factory.manufacturePojo(ObservationDto.class));
		Mockito.when(
				this.mockMiddlewareStudyService.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						StudyServiceImplTest.TEST_OBSERVATION_IDENTIFIER)).thenReturn(observationDtoTestData);

		final Observation actualObservations =
				this.studyServiceImpl.getSingleObservation(StudyServiceImplTest.TEST_STUDY_IDENTIFIER,
						StudyServiceImplTest.TEST_OBSERVATION_IDENTIFIER);

		Assert.assertEquals(Lists.transform(observationDtoTestData, this.observationTransformFunction).get(0), actualObservations);

	}

	@Test
	public void testGetStudyReference() {
		int studyId = 101;
		this.studyServiceImpl.getStudyReference(studyId);
		Mockito.verify(this.studyDataManager).getStudyReference(studyId);
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
			observation.setEntryCode(measurement.getEntryCode());

			final List<MeasurementDto> measurementsDto = measurement.getVariableMeasurements();
			final List<Measurement> measurements = new ArrayList<>();
			for (final MeasurementDto measurementDto : measurementsDto) {
				measurements.add(new Measurement(
					new MeasurementIdentifier(measurementDto.getPhenotypeId(), new Trait(
						measurementDto.getMeasurementVariable().getId(),
						measurementDto.getMeasurementVariable().getName())),
					measurementDto.getVariableValue(),
					measurementDto.getValueStatus()));
			}

			observation.setMeasurements(measurements);
		}
		return observation;
	}

}
