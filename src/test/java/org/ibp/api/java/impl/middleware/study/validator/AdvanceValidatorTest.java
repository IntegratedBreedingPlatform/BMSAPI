package org.ibp.api.java.impl.middleware.study.validator;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.ruleengine.naming.expression.SelectionTraitExpression;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BreedingMethodValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AdvanceValidatorTest {

	private static final Integer STUDY_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final Integer PLOT_DATASET_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final Integer INSTANCE_ID = new Random().nextInt(Integer.MAX_VALUE);

	// Breeding method selection
	private static final Integer BREEDING_METHOD_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final Integer METHOD_VARIATE_ID = new Random().nextInt(Integer.MAX_VALUE);

	// Line selection
	private static final Integer LINE_SELECTED_NUMBER = new Random().nextInt(Integer.MAX_VALUE);
	private static final Integer LINE_VARIATE_ID = new Random().nextInt(Integer.MAX_VALUE);

	// Trait selection
	private static final Integer SELECTION_TRAIT_DATASET_ID = new Random().nextInt(Integer.MAX_VALUE);
	private static final Integer SELECTION_TRAIT_VARIABLE_ID = new Random().nextInt(Integer.MAX_VALUE);

	// Bulking selection
	private static final Integer PLOT_VARIATE_ID = new Random().nextInt(Integer.MAX_VALUE);

	@InjectMocks
	private AdvanceValidator advanceValidator;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private DatasetService datasetService;

	@Mock
	private InstanceValidator instanceValidator;

	@Mock
	private BreedingMethodValidator breedingMethodValidator;

	@Mock
	private DatasetValidator datasetValidator;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void validate_OK_usingSameNotBulkingMethodAndLinesSelected() {

		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(new ArrayList<>());

		this.mockValidateMethod(MethodType.DERIVATIVE, false);

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(LINE_SELECTED_NUMBER, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(Arrays.asList(INSTANCE_ID), null, breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		this.advanceValidator.validateAdvanceStudy(STUDY_ID, request);

		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.studyValidator).validateStudyHasPlotDataset(STUDY_ID);
		Mockito.verify(this.datasetService).getDataset(PLOT_DATASET_ID);
		Mockito.verify(this.instanceValidator).validateStudyInstance(ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.anySet());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(PLOT_DATASET_ID);

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());

		Mockito.verify(request, Mockito.never()).getBulkingRequest();
		Mockito.verify(request, Mockito.never()).getSelectionTraitRequest();
	}

	@Test
	public void validate_OK_usingSameBulkingMethodAndLinesSelectedAndVariateForBulking() {

		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		final MeasurementVariable plotVariate = this.mockMeasurementVariable(PLOT_VARIATE_ID);
		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(Arrays.asList(plotVariate));

		this.mockValidateMethod(MethodType.DERIVATIVE, true);

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(LINE_SELECTED_NUMBER, null);
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(null, PLOT_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(Arrays.asList(INSTANCE_ID), null, breedingMethodSelectionRequest, lineSelectionRequest,
				bulkingRequest,
				null);
		this.advanceValidator.validateAdvanceStudy(STUDY_ID, request);

		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.studyValidator).validateStudyHasPlotDataset(STUDY_ID);
		Mockito.verify(this.datasetService).getDataset(PLOT_DATASET_ID);
		Mockito.verify(this.instanceValidator).validateStudyInstance(ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.anySet());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(PLOT_DATASET_ID);

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());

		Mockito.verify(request).getBulkingRequest();
		Mockito.verify(request, Mockito.never()).getSelectionTraitRequest();
	}

	@Test
	public void validate_OK_usingSameNotBulkingMethodAndVariateForLinesSelected() {

		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		final MeasurementVariable plotDatasetVariables = this.mockMeasurementVariable(LINE_VARIATE_ID);
		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(Arrays.asList(plotDatasetVariables));

		this.mockValidateMethod(MethodType.DERIVATIVE, false);

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(null, LINE_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(Arrays.asList(INSTANCE_ID), null, breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		this.advanceValidator.validateAdvanceStudy(STUDY_ID, request);

		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.studyValidator).validateStudyHasPlotDataset(STUDY_ID);
		Mockito.verify(this.datasetService).getDataset(PLOT_DATASET_ID);
		Mockito.verify(this.instanceValidator).validateStudyInstance(ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.anySet());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(PLOT_DATASET_ID);

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());

		Mockito.verify(request, Mockito.never()).getBulkingRequest();
		Mockito.verify(request, Mockito.never()).getSelectionTraitRequest();
	}

	@Test
	public void validate_OK_usingVariateForMethodAndLinesSelectedAndAllPlotAreSelected() {

		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		final MeasurementVariable plotDatasetVariables = this.mockMeasurementVariable(METHOD_VARIATE_ID);
		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(Arrays.asList(plotDatasetVariables));

		final MeasurementVariable selectionTraitDatasetVariables = this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID);
		Mockito.when(this.datasetService.getObservationSetVariables(SELECTION_TRAIT_DATASET_ID))
			.thenReturn(Arrays.asList(selectionTraitDatasetVariables));

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(LINE_SELECTED_NUMBER, null);
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(true, null);
		final AdvanceStudyRequest.SelectionTraitRequest selectionTraitRequest =
			this.mockSelectionTraitRequest(SELECTION_TRAIT_DATASET_ID, SELECTION_TRAIT_VARIABLE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(Arrays.asList(INSTANCE_ID), null, breedingMethodSelectionRequest, lineSelectionRequest,
				bulkingRequest,
				selectionTraitRequest);
		this.advanceValidator.validateAdvanceStudy(STUDY_ID, request);

		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.studyValidator).validateStudyHasPlotDataset(STUDY_ID);
		Mockito.verify(this.datasetService).getDataset(PLOT_DATASET_ID);
		Mockito.verify(this.instanceValidator).validateStudyInstance(ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.anySet());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(PLOT_DATASET_ID);

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(SELECTION_TRAIT_DATASET_ID);

		Mockito.verify(this.datasetValidator)
			.validateDatasetBelongsToStudy(STUDY_ID, SELECTION_TRAIT_DATASET_ID);

		Mockito.verify(request).getBulkingRequest();
		Mockito.verify(request).getSelectionTraitRequest();
	}

	@Test
	public void validate_OK_usingVariateForMethodAndLinesSelectedAndVariateForBulking() {

		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		final MeasurementVariable methodVariate = this.mockMeasurementVariable(METHOD_VARIATE_ID);
		final MeasurementVariable plotVariate = this.mockMeasurementVariable(PLOT_VARIATE_ID);
		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(Arrays.asList(methodVariate, plotVariate));

		final MeasurementVariable selectionTraitDatasetVariables = this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID);
		Mockito.when(this.datasetService.getObservationSetVariables(SELECTION_TRAIT_DATASET_ID))
			.thenReturn(Arrays.asList(selectionTraitDatasetVariables));

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(LINE_SELECTED_NUMBER, null);
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(null, PLOT_VARIATE_ID);
		final AdvanceStudyRequest.SelectionTraitRequest selectionTraitRequest =
			this.mockSelectionTraitRequest(SELECTION_TRAIT_DATASET_ID, SELECTION_TRAIT_VARIABLE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(Arrays.asList(INSTANCE_ID), null, breedingMethodSelectionRequest, lineSelectionRequest,
				bulkingRequest,
				selectionTraitRequest);
		this.advanceValidator.validateAdvanceStudy(STUDY_ID, request);

		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.studyValidator).validateStudyHasPlotDataset(STUDY_ID);
		Mockito.verify(this.datasetService).getDataset(PLOT_DATASET_ID);
		Mockito.verify(this.instanceValidator).validateStudyInstance(ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.anySet());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(PLOT_DATASET_ID);

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(SELECTION_TRAIT_DATASET_ID);

		Mockito.verify(this.datasetValidator)
			.validateDatasetBelongsToStudy(STUDY_ID, SELECTION_TRAIT_DATASET_ID);

		Mockito.verify(request).getBulkingRequest();
		Mockito.verify(request).getSelectionTraitRequest();
	}

	@Test
	public void validateBreedingMethodSelection_FAIL_requestRequired() {
		try {
			this.advanceValidator.validateBreedingMethodSelection(null, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateBreedingMethodSelection_FAIL_selectionRequired() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, null);

		try {
			this.advanceValidator.validateBreedingMethodSelection(breedingMethodSelectionRequest, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.required"));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateBreedingMethodSelection_FAIL_bothSelectionPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, METHOD_VARIATE_ID);

		try {
			this.advanceValidator.validateBreedingMethodSelection(breedingMethodSelectionRequest, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.required"));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateBreedingMethodSelection_FAIL_generativeMethod() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);

		this.mockValidateMethod(MethodType.GENERATIVE, false);

		try {
			this.advanceValidator.validateBreedingMethodSelection(breedingMethodSelectionRequest, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.generative.invalid"));
		}

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);
	}

	@Test
	public void validateBreedingMethodSelection_FAIL_methodVariateNotPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);

		try {
			this.advanceValidator.validateBreedingMethodSelection(breedingMethodSelectionRequest, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.variate.not-present"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(METHOD_VARIATE_ID.toString()));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateLineSelection_FAIL_requestRequiredUsingSameNotBulkingMethod() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null, null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}
	}

	@Test
	public void validateLineSelection_FAIL_requestRequiredUsingVariateForMethod() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null, null);

		try {
			this.advanceValidator.validateLineSelection(request, Mockito.mock(BreedingMethodDTO.class), new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}
	}

	@Test
	public void validateLineSelection_FAIL_selectionRequired() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(null, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.required"));
		}
	}

	@Test
	public void validateLineSelection_FAIL_bothSelectionPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest =
			this.mockLineSelectionRequest(LINE_SELECTED_NUMBER, LINE_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.required"));
		}
	}

	@Test
	public void validateLineSelection_FAIL_lineVariateNotPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(null, LINE_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.variate.not-present"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(LINE_VARIATE_ID.toString()));
		}
	}

	@Test
	public void validateBulkingSelection_FAIL_requestRequiredUsingSameBulkingMethod() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null, null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, true);

		try {
			this.advanceValidator.validateBulkingSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}
	}

	@Test
	public void validateBulkingSelection_FAIL_requestRequiredUsingVariateForMethod() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null, null);

		try {
			this.advanceValidator.validateBulkingSelection(request, Mockito.mock(BreedingMethodDTO.class), new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateBulkingSelection_FAIL_selectionRequired() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(null, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, bulkingRequest,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateBulkingSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.bulking.selection.required"));
		}
	}

	@Test
	public void validateBulkingSelection_FAIL_bothSelectionPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(null, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, bulkingRequest,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateBulkingSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.bulking.selection.required"));
		}
	}

	@Test
	public void validateBulkingSelection_FAIL_plotVariateNotPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(null, PLOT_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, bulkingRequest,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateBulkingSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.bulking.selection.variate.not-present"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(PLOT_VARIATE_ID.toString()));
		}
	}

	@Test
	public void validateSelectionTrait_FAIL_requestRequiredUsingVariateForMethod() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null, null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateSelectionTrait(STUDY_ID, request, breedingMethodDTO);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
		Mockito.verify(this.datasetService, Mockito.never()).getObservationSetVariables(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_requestRequiredUsingSameMethodWithSelTraitAsPrefix() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null, null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);
		Mockito.when(breedingMethodDTO.getPrefix()).thenReturn(SelectionTraitExpression.KEY);

		try {
			this.advanceValidator.validateSelectionTrait(STUDY_ID, request, breedingMethodDTO);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
		Mockito.verify(this.datasetService, Mockito.never()).getObservationSetVariables(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_requestRequiredUsingSameMethodWithSelTraitAsSuffix() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null, null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);
		Mockito.when(breedingMethodDTO.getSuffix()).thenReturn(SelectionTraitExpression.KEY);

		try {
			this.advanceValidator.validateSelectionTrait(STUDY_ID, request, breedingMethodDTO);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
		Mockito.verify(this.datasetService, Mockito.never()).getObservationSetVariables(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_datasetIdRequired() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.SelectionTraitRequest selectionTraitRequest = this.mockSelectionTraitRequest(null, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null,
				selectionTraitRequest);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateSelectionTrait(STUDY_ID, request, breedingMethodDTO);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("field.is.required"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is("selectionTraitRequest.datasetId"));
		}

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
		Mockito.verify(this.datasetService, Mockito.never()).getObservationSetVariables(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_variableIdRequired() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.SelectionTraitRequest selectionTraitRequest =
			this.mockSelectionTraitRequest(SELECTION_TRAIT_DATASET_ID, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null,
				selectionTraitRequest);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateSelectionTrait(STUDY_ID, request, breedingMethodDTO);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("field.is.required"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is("selectionTraitRequest.variableId"));
		}

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
		Mockito.verify(this.datasetService, Mockito.never()).getObservationSetVariables(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_variableNotPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.SelectionTraitRequest selectionTraitRequest =
			this.mockSelectionTraitRequest(SELECTION_TRAIT_DATASET_ID, SELECTION_TRAIT_VARIABLE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null,
				selectionTraitRequest);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateSelectionTrait(STUDY_ID, request, breedingMethodDTO);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.selection-trait.not-present"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(SELECTION_TRAIT_VARIABLE_ID.toString()));
		}

		Mockito.verify(this.datasetValidator).validateDatasetBelongsToStudy(STUDY_ID, SELECTION_TRAIT_DATASET_ID);
		Mockito.verify(this.datasetService).getObservationSetVariables(SELECTION_TRAIT_DATASET_ID);
	}

	@Test
	public void validateReplicationNumberSelection_FAIL_selectionRequired() {
		final MeasurementVariable variable = this.mockMeasurementVariable(TermId.REP_NO.getId());

		try {
			this.advanceValidator.validateReplicationNumberSelection(new ArrayList<>(), Arrays.asList(variable));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.replication-number.selection.required"));
		}
	}

	private void mockValidateStudyHasPlotDataset() {
		final DataSet dataSet = Mockito.mock(DataSet.class);
		Mockito.when(dataSet.getId()).thenReturn(PLOT_DATASET_ID);
		Mockito.when(this.studyValidator.validateStudyHasPlotDataset(STUDY_ID)).thenReturn(dataSet);
	}

	private void mockGetDataset(final boolean hasExperimentalDesign) {
		final StudyInstance studyInstance = Mockito.mock(StudyInstance.class);
		Mockito.when(studyInstance.isHasExperimentalDesign()).thenReturn(hasExperimentalDesign);

		final DatasetDTO datasetDTO = Mockito.mock(DatasetDTO.class);
		Mockito.when(datasetDTO.getInstances()).thenReturn(Arrays.asList(studyInstance));
		Mockito.when(this.datasetService.getDataset(PLOT_DATASET_ID)).thenReturn(datasetDTO);
	}

	private void mockValidateMethod(final MethodType type, final Boolean isBulking) {
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(type, isBulking);
		Mockito.when(this.breedingMethodValidator.validateMethod(BREEDING_METHOD_ID)).thenReturn(breedingMethodDTO);
	}

	private BreedingMethodDTO mockBreedingMethodDTO(final MethodType type, final Boolean isBulking) {
		final BreedingMethodDTO breedingMethodDTO = Mockito.mock(BreedingMethodDTO.class);
		Mockito.when(breedingMethodDTO.getType()).thenReturn(type.getCode());
		Mockito.when(breedingMethodDTO.getIsBulkingMethod()).thenReturn(isBulking);
		return breedingMethodDTO;
	}

	private AdvanceStudyRequest.BreedingMethodSelectionRequest mockBreedingMethodSelectionRequest(final Integer breedingMethodId,
		final Integer methodVariateId) {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest request =
			Mockito.mock(AdvanceStudyRequest.BreedingMethodSelectionRequest.class);
		Mockito.when(request.getBreedingMethodId()).thenReturn(breedingMethodId);
		Mockito.when(request.getMethodVariateId()).thenReturn(methodVariateId);
		return request;
	}

	private AdvanceStudyRequest.LineSelectionRequest mockLineSelectionRequest(final Integer linesSelected,
		final Integer lineVariateId) {
		final AdvanceStudyRequest.LineSelectionRequest request =
			Mockito.mock(AdvanceStudyRequest.LineSelectionRequest.class);
		Mockito.when(request.getLinesSelected()).thenReturn(linesSelected);
		Mockito.when(request.getLineVariateId()).thenReturn(lineVariateId);
		return request;
	}

	private AdvanceStudyRequest.BulkingRequest mockBulkingRequest(final Boolean allPlotsSelected,
		final Integer plotVariateId) {
		final AdvanceStudyRequest.BulkingRequest request =
			Mockito.mock(AdvanceStudyRequest.BulkingRequest.class);
		Mockito.when(request.getAllPlotsSelected()).thenReturn(allPlotsSelected);
		Mockito.when(request.getPlotVariateId()).thenReturn(plotVariateId);
		return request;
	}

	private AdvanceStudyRequest.SelectionTraitRequest mockSelectionTraitRequest(final Integer datasetId,
		final Integer variableId) {
		final AdvanceStudyRequest.SelectionTraitRequest request =
			Mockito.mock(AdvanceStudyRequest.SelectionTraitRequest.class);
		Mockito.when(request.getDatasetId()).thenReturn(datasetId);
		Mockito.when(request.getVariableId()).thenReturn(variableId);
		return request;
	}

	private AdvanceStudyRequest mockAdvanceStudyRequest(final List<Integer> instanceIds, final List<String> selectedReplications,
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest,
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest, final AdvanceStudyRequest.BulkingRequest bulkingRequest,
		final AdvanceStudyRequest.SelectionTraitRequest selectionTraitRequest) {
		final AdvanceStudyRequest request = Mockito.mock(AdvanceStudyRequest.class);
		Mockito.when(request.getInstanceIds()).thenReturn(instanceIds);
		Mockito.when(request.getSelectedReplications()).thenReturn(selectedReplications);
		Mockito.when(request.getBreedingMethodSelectionRequest()).thenReturn(breedingMethodSelectionRequest);
		Mockito.when(request.getLineSelectionRequest()).thenReturn(lineSelectionRequest);
		Mockito.when(request.getBulkingRequest()).thenReturn(bulkingRequest);
		Mockito.when(request.getSelectionTraitRequest()).thenReturn(selectionTraitRequest);
		return request;
	}

	private MeasurementVariable mockMeasurementVariable(final Integer variableId) {
		final MeasurementVariable measurementVariable = Mockito.mock(MeasurementVariable.class);
		Mockito.when(measurementVariable.getTermId()).thenReturn(variableId);
		return measurementVariable;
	}

}
