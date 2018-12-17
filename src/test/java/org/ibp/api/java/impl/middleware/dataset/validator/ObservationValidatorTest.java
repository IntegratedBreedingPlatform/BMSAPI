package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Random;

import static org.mockito.Mockito.when;

public class ObservationValidatorTest {

	@InjectMocks
	private ObservationValidator observationValidator;

	@Mock
	private DatasetService datasetService;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateObservationUnitSuccess() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();

		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(true);

		this.observationValidator.validateObservationUnit(datasetId, observationUnitId);
	}

	@Test (expected = ResourceNotFoundException.class)
	public void testValidateObservationUnitReject() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();

		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(false);

		this.observationValidator.validateObservationUnit(datasetId, observationUnitId);
	}
	
	@Test
	public void testValidateObservationSuccess() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final int observationId = random.nextInt();
		final Integer studyId = random.nextInt();

		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(true);
		when(datasetService.getPhenotype(observationUnitId, observationId)).thenReturn(new Phenotype());


		this.observationValidator.validateObservation(studyId, datasetId, observationUnitId, observationId, null);
	}

	@Test (expected = ResourceNotFoundException.class)
	public void testValidateObservationWithInvalidObservationUnit() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final int observationId = random.nextInt();
		final Integer studyId = random.nextInt();

		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(false);

		this.observationValidator.validateObservation(studyId, datasetId, observationUnitId, observationId, "");
	}
	
	@Test (expected = ResourceNotFoundException.class)
	public void testValidateObservationWithInvalidObservation() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final int observationId = random.nextInt();
		final Integer studyId = random.nextInt();

		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(true);
		when(datasetService.getPhenotype(observationUnitId, observationId)).thenReturn(null);

		this.observationValidator.validateObservation(studyId, datasetId, observationUnitId, observationId, "");
	}

}
