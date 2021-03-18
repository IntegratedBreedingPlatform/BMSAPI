
package org.ibp.api.java.impl.middleware.study;

import com.google.common.base.Function;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.ibp.api.domain.study.Measurement;
import org.ibp.api.domain.study.MeasurementIdentifier;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudyServiceImplTest {

	private StudyServiceImpl studyServiceImpl;

	@Mock
	private StudyService mockMiddlewareStudyService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private GermplasmValidator germplasmValidator;



	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);

		this.studyServiceImpl = new StudyServiceImpl();
		this.studyServiceImpl.setMiddlewareStudyService(this.mockMiddlewareStudyService);
		this.studyServiceImpl.setStudyDataManager(this.studyDataManager);
		this.studyServiceImpl.setStudyValidator(this.studyValidator);
		this.studyServiceImpl.setGermplasmValidator(this.germplasmValidator);
	}

	@Test
	public void testGetStudyReference() {
		int studyId = 101;
		this.studyServiceImpl.getStudyReference(studyId);
		Mockito.verify(this.studyDataManager).getStudyReference(studyId);
	}

	@Test
	public void testGetGermplasmStudies_Success() {
		final Integer gid = 1;
		this.studyServiceImpl.getGermplasmStudies(gid);
		Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
			ArgumentMatchers.eq(Collections.singletonList(gid)));
		Mockito.verify(this.mockMiddlewareStudyService).getGermplasmStudies(gid);
	}


	@Test
	public void testGetGermplasmStudies_ThrowsException_WhenGIDIsInvalid() {
		final Integer gid = 999;
		try {
			Mockito.doThrow(new ApiRequestValidationException(Collections.EMPTY_LIST)).when(this.germplasmValidator)
				.validateGids(ArgumentMatchers.any(BindingResult.class), ArgumentMatchers.eq(Collections.singletonList(gid)));
			this.studyServiceImpl.getGermplasmStudies(gid);
			Assert.fail("should throw an exception");
		} catch (ApiRequestValidationException e) {
			Mockito.verify(this.germplasmValidator).validateGids(ArgumentMatchers.any(BindingResult.class),
				ArgumentMatchers.eq(Collections.singletonList(gid)));
			Mockito.verify(this.mockMiddlewareStudyService, Mockito.never()).getGermplasmStudies(gid);
		}
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
