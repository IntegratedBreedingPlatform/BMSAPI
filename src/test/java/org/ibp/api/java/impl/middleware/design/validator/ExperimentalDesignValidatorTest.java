package org.ibp.api.java.impl.middleware.design.validator;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.design.ExperimentalDesignService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;

public class ExperimentalDesignValidatorTest {

	private static final Integer STUDY_ID = new Random().nextInt();

	@Mock
	private ExperimentalDesignService experimentalDesignService;

	@InjectMocks
	private ExperimentalDesignValidator experimentalDesignValidator;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(Arrays.asList(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK, ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK,
			ExperimentDesignType.ENTRY_LIST_ORDER)).when(this.experimentalDesignService).getExperimentalDesignTypes();
	}

	@Test
	public void testExperimentDesignShouldExist_DesignDoesNotExist() {
		Mockito.doReturn(Optional.empty()).when(this.experimentalDesignService).getStudyExperimentalDesignTypeTermId(STUDY_ID);
		try {
			this.experimentalDesignValidator.validateExperimentalDesignExistence(STUDY_ID, true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.has.no.experiment.design"));
		}
	}

	@Test
	public void testExperimentDesignShouldExist_DesignExists() {
		Mockito.doReturn(Optional.of(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId())).when(this.experimentalDesignService)
			.getStudyExperimentalDesignTypeTermId(STUDY_ID);
		this.experimentalDesignValidator.validateExperimentalDesignExistence(STUDY_ID, true);
	}

	@Test
	public void testExperimentDesignShouldNotExist_DesignExists() {
		Mockito.doReturn(Optional.of(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId())).when(this.experimentalDesignService)
			.getStudyExperimentalDesignTypeTermId(STUDY_ID);
		try {
			this.experimentalDesignValidator.validateExperimentalDesignExistence(STUDY_ID, false);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("study.already.has.experiment.design"));
		}
	}

	@Test
	public void testExperimentDesignShouldNotExist_DesignDoesNotExist() {
		Mockito.doReturn(Optional.empty()).when(this.experimentalDesignService).getStudyExperimentalDesignTypeTermId(STUDY_ID);
		this.experimentalDesignValidator.validateExperimentalDesignExistence(STUDY_ID, false);
	}

	@Test
	public void testValidateStudyExperimentalDesign_InvalidDesignTypeId() {
		try {
			this.experimentalDesignValidator.validateStudyExperimentalDesign(STUDY_ID, 100);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("invalid.experimental.design.type"));
		}
	}

	@Test
	public void testValidateStudyExperimentalDesign_DifferentDesignTypeFromExistingInStudy() {
		Mockito.doReturn(Optional.of(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId())).when(this.experimentalDesignService)
			.getStudyExperimentalDesignTypeTermId(STUDY_ID);
		try {
			this.experimentalDesignValidator
				.validateStudyExperimentalDesign(STUDY_ID, ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getId());
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("design.type.is.different.from.existing.design"));
		}
	}

	@Test
	public void testValidateStudyExperimentalDesign_ValidDesignTypeAndMatchesExistingStudyDesign() {
		Mockito.doReturn(Optional.of(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId())).when(this.experimentalDesignService)
			.getStudyExperimentalDesignTypeTermId(STUDY_ID);
		this.experimentalDesignValidator
			.validateStudyExperimentalDesign(STUDY_ID, ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId());
	}

	@Test
	public void testValidateStudyExperimentalDesign_ValidDesignTypeAndNoExistingStudyDesign() {
		Mockito.doReturn(Optional.empty()).when(this.experimentalDesignService)
			.getStudyExperimentalDesignTypeTermId(STUDY_ID);
		this.experimentalDesignValidator
			.validateStudyExperimentalDesign(STUDY_ID, ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId());
	}

}
