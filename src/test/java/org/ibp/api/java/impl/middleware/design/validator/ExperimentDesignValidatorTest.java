package org.ibp.api.java.impl.middleware.design.validator;

import com.google.common.base.Optional;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;

public class ExperimentDesignValidatorTest {

	private static final Integer STUDY_ID = new Random().nextInt();

	@Mock
	private org.generationcp.middleware.service.api.study.generation.ExperimentDesignService experimentDesignMiddlewareService;

	@InjectMocks
	private ExperimentDesignValidator experimentDesignValidator;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testExperimentDesignShouldExist_DesignDoesNotExist() {
		Mockito.doReturn(Optional.absent()).when(this.experimentDesignMiddlewareService).getStudyExperimentDesignTypeTermId(STUDY_ID);
		try {
			this.experimentDesignValidator.validateExperimentDesignExistence(STUDY_ID, true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.has.no.experiment.design"));
		}
	}

	@Test
	public void testExperimentDesignShouldExist_DesignExists() {
		Mockito.doReturn(Optional.of(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId())).when(this.experimentDesignMiddlewareService)
			.getStudyExperimentDesignTypeTermId(STUDY_ID);
		try {
			this.experimentDesignValidator.validateExperimentDesignExistence(STUDY_ID, true);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expected no validation exception but was thrown.");
		}
	}

	@Test
	public void testExperimentDesignShouldNotExist_DesignExists() {
		Mockito.doReturn(Optional.of(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId())).when(this.experimentDesignMiddlewareService)
			.getStudyExperimentDesignTypeTermId(STUDY_ID);
		try {
			this.experimentDesignValidator.validateExperimentDesignExistence(STUDY_ID, false);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.already.has.experiment.design"));
		}
	}

	@Test
	public void testExperimentDesignShouldNotExist_DesignDoesNotExist() {
		Mockito.doReturn(Optional.absent()).when(this.experimentDesignMiddlewareService).getStudyExperimentDesignTypeTermId(STUDY_ID);
		try {
			this.experimentDesignValidator.validateExperimentDesignExistence(STUDY_ID, false);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Expected no validation exception but was thrown.");
		}
	}

}
