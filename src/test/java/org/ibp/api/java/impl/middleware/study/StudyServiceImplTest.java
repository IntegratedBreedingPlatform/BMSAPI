
package org.ibp.api.java.impl.middleware.study;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.domain.study.validators.ObservationValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
