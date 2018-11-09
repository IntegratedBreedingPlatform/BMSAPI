package org.ibp.api.java.impl.middleware.dataset.validator;

import static org.mockito.Mockito.when;

import java.util.Random;

import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
		
		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(true);
		when(datasetService.isValidObservation(observationUnitId, observationId)).thenReturn(true);

		this.observationValidator.validateObservation(datasetId, observationUnitId, observationId);
	}

	@Test (expected = ResourceNotFoundException.class)
	public void testValidateObservationWithInvalidObservationUnit() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final int observationId = random.nextInt();
		
		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(false);

		this.observationValidator.validateObservation(datasetId, observationUnitId, observationId);
	}
	
	@Test (expected = ResourceNotFoundException.class)
	public void testValidateObservationWithInvalidObservation() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();
		final int observationId = random.nextInt();

		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(true);
		when(datasetService.isValidObservation(observationUnitId, observationId)).thenReturn(false);

		this.observationValidator.validateObservation(datasetId, observationUnitId, observationId);
	}

}
