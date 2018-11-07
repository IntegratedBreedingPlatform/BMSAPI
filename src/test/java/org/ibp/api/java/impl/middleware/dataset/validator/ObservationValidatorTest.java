package org.ibp.api.java.impl.middleware.dataset.validator;

import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Random;

import static org.mockito.Mockito.when;

public class ObservationValidatorTest extends ApiUnitTestBase {

	@Autowired
	private ObservationValidator observationValidator;

	@Autowired
	private DatasetService datasetService;

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public DatasetService datasetService() {
			return Mockito.mock(DatasetService.class);
		}
	}

	@Test
	public void testValidateObservationSuccess() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();

		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(true);

		this.observationValidator.validateObservation(datasetId, observationUnitId);
	}

	@Test (expected = ApiRequestValidationException.class)
	public void testValidateObservationReject() {

		final Random random = new Random();
		final int datasetId = random.nextInt();
		final int observationUnitId = random.nextInt();

		when(datasetService.isValidObservationUnit(datasetId, observationUnitId)).thenReturn(false);

		this.observationValidator.validateObservation(datasetId, observationUnitId);
	}

}
