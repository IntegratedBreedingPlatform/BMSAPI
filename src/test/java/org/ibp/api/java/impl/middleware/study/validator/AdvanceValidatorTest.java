package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.study.AbstractAdvanceRequest;
import org.generationcp.middleware.api.study.AdvanceSamplesRequest;
import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.ruleengine.naming.expression.SelectionTraitExpression;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.generationcp.middleware.service.impl.study.advance.resolver.level.SelectionTraitDataResolver;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BreedingMethodValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.study.StudyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

	@Mock
	private StudyService studyService;

	@Mock
	private StudyDataManager studyDataManager;

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

		final MeasurementVariable plotVariate =
			this.mockMeasurementVariable(PLOT_VARIATE_ID, AdvanceValidator.SELECTED_LINE_VARIABLE_PROPERTY, VariableType.SELECTION_METHOD);
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

		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(LINE_VARIATE_ID, AdvanceValidator.SELECTED_LINE_VARIABLE_PROPERTY, VariableType.SELECTION_METHOD);
		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(Arrays.asList(plotDatasetVariables));

		Mockito.when(this.studyDataManager.countPlotsWithRecordedVariatesInDataset(PLOT_DATASET_ID, Arrays.asList(LINE_VARIATE_ID)))
			.thenReturn(1);

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

		Mockito.verify(this.studyDataManager).countPlotsWithRecordedVariatesInDataset(PLOT_DATASET_ID, Arrays.asList(LINE_VARIATE_ID));

		Mockito.verify(request, Mockito.never()).getBulkingRequest();
		Mockito.verify(request, Mockito.never()).getSelectionTraitRequest();
	}

	@Test
	public void validate_OK_usingVariateForMethodAndLinesSelectedAndAllPlotAreSelected() {

		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(METHOD_VARIATE_ID, AdvanceValidator.BREEDING_METHOD_VARIABLE_PROPERTY,
				VariableType.SELECTION_METHOD);
		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(Arrays.asList(plotDatasetVariables));

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		final MeasurementVariable selectionTraitDatasetVariables =
			this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID, SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.TRAIT);
		final DatasetDTO datasetDTO = Mockito.mock(DatasetDTO.class);
		Mockito.when(datasetDTO.getDatasetId()).thenReturn(SELECTION_TRAIT_DATASET_ID);
		Mockito.when(datasetDTO.getVariables()).thenReturn(Arrays.asList(selectionTraitDatasetVariables));
		Mockito.when(
			this.datasetService.getDatasetsWithVariables(STUDY_ID, datasetTypeIds)).thenReturn(Arrays.asList(datasetDTO));

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

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator)
			.validateDatasetBelongsToStudy(STUDY_ID, SELECTION_TRAIT_DATASET_ID);

		Mockito.verify(request).getBulkingRequest();
		Mockito.verify(request).getSelectionTraitRequest();
	}

	@Test
	public void validate_OK_usingVariateForMethodAndLinesSelectedAndVariateForBulking() {

		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		final MeasurementVariable methodVariate =
			this.mockMeasurementVariable(METHOD_VARIATE_ID, AdvanceValidator.BREEDING_METHOD_VARIABLE_PROPERTY,
				VariableType.SELECTION_METHOD);
		final MeasurementVariable plotVariate =
			this.mockMeasurementVariable(PLOT_VARIATE_ID, AdvanceValidator.SELECTED_LINE_VARIABLE_PROPERTY, VariableType.SELECTION_METHOD);
		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(Arrays.asList(methodVariate, plotVariate));

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		final MeasurementVariable selectionTraitDatasetVariables =
			this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID, SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.TRAIT);
		final DatasetDTO datasetDTO = Mockito.mock(DatasetDTO.class);
		Mockito.when(datasetDTO.getDatasetId()).thenReturn(SELECTION_TRAIT_DATASET_ID);
		Mockito.when(datasetDTO.getVariables()).thenReturn(Arrays.asList(selectionTraitDatasetVariables));
		Mockito.when(
			this.datasetService.getDatasetsWithVariables(STUDY_ID, datasetTypeIds)).thenReturn(Arrays.asList(datasetDTO));

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

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator)
			.validateDatasetBelongsToStudy(STUDY_ID, SELECTION_TRAIT_DATASET_ID);

		Mockito.verify(request).getBulkingRequest();
		Mockito.verify(request).getSelectionTraitRequest();
	}

	@Test
	public void validateAdvanceStudyBreedingMethodSelection_FAIL_requestRequired() {
		try {
			this.advanceValidator.validateAdvanceStudyBreedingMethodSelection(null, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("request.null"));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateAdvanceStudyBreedingMethodSelection_FAIL_selectionRequired() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, null);

		try {
			this.advanceValidator.validateAdvanceStudyBreedingMethodSelection(breedingMethodSelectionRequest, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.required"));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateAdvanceStudyBreedingMethodSelection_FAIL_bothSelectionPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, METHOD_VARIATE_ID);

		try {
			this.advanceValidator.validateAdvanceStudyBreedingMethodSelection(breedingMethodSelectionRequest, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.required"));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateAdvanceStudyBreedingMethodSelection_FAIL_generativeMethod() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(BREEDING_METHOD_ID, null);

		this.mockValidateMethod(MethodType.GENERATIVE, false);

		try {
			this.advanceValidator.validateAdvanceStudyBreedingMethodSelection(breedingMethodSelectionRequest, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.generative.invalid"));
		}

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);
	}

	@Test
	public void validateAdvanceStudyBreedingMethodSelection_FAIL_methodVariateNotPresent() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);

		try {
			this.advanceValidator.validateAdvanceStudyBreedingMethodSelection(breedingMethodSelectionRequest, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.variate.not-present"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(METHOD_VARIATE_ID.toString()));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateAdvanceStudyBreedingMethodSelection_FAIL_invalidMethodVariableType() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);

		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(METHOD_VARIATE_ID, AdvanceValidator.BREEDING_METHOD_VARIABLE_PROPERTY, VariableType.ENTRY_DETAIL);

		try {
			this.advanceValidator
				.validateAdvanceStudyBreedingMethodSelection(breedingMethodSelectionRequest, Arrays.asList(plotDatasetVariables));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.variate.type.invalid"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(VariableType.SELECTION_METHOD.getName()));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(ArgumentMatchers.anyInt());
	}

	@Test
	public void validateAdvanceStudyBreedingMethodSelection_FAIL_invalidMethodProperty() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);

		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(METHOD_VARIATE_ID, RandomStringUtils.randomAlphabetic(10), VariableType.SELECTION_METHOD);

		try {
			this.advanceValidator
				.validateAdvanceStudyBreedingMethodSelection(breedingMethodSelectionRequest, Arrays.asList(plotDatasetVariables));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.variate.property.invalid"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(AdvanceValidator.BREEDING_METHOD_VARIABLE_PROPERTY));
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
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, PLOT_DATASET_ID, new ArrayList<>());
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
			this.advanceValidator.validateLineSelection(request, Mockito.mock(BreedingMethodDTO.class), PLOT_DATASET_ID, new ArrayList<>());
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
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, PLOT_DATASET_ID, new ArrayList<>());
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
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, PLOT_DATASET_ID, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.required"));
		}
	}

	@Test
	public void validateLineSelection_FAIL_invalidLineNumber() {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest =
			this.mockLineSelectionRequest(0, null);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, PLOT_DATASET_ID, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.number.invalid"));
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
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, PLOT_DATASET_ID, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.variate.not-present"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(LINE_VARIATE_ID.toString()));
		}
	}

	@Test
	public void validateLineSelection_FAIL_invalidVariableType() {

		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(LINE_VARIATE_ID, AdvanceValidator.SELECTED_LINE_VARIABLE_PROPERTY, VariableType.ENTRY_DETAIL);

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(null, LINE_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, PLOT_DATASET_ID, Arrays.asList(plotDatasetVariables));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.variate.type.invalid"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(VariableType.SELECTION_METHOD));
		}
	}

	@Test
	public void validateLineSelection_FAIL_invalidVariableProperty() {
		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(LINE_VARIATE_ID, AdvanceValidator.SELECTED_LINE_VARIABLE_PROPERTY, VariableType.SELECTION_METHOD);

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(null, LINE_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		Mockito.when(this.studyDataManager.countPlotsWithRecordedVariatesInDataset(PLOT_DATASET_ID, Arrays.asList(LINE_VARIATE_ID)))
			.thenReturn(0);

		try {
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, PLOT_DATASET_ID, Arrays.asList(plotDatasetVariables));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.variate.empty.observations"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(LINE_VARIATE_ID.toString()));
		}

		Mockito.verify(this.studyDataManager).countPlotsWithRecordedVariatesInDataset(PLOT_DATASET_ID, Arrays.asList(LINE_VARIATE_ID));
	}

	@Test
	public void validateLineSelection_FAIL_noPlotsSetWithLineVariable() {
		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(LINE_VARIATE_ID, RandomStringUtils.randomAlphabetic(10), VariableType.SELECTION_METHOD);

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = this.mockLineSelectionRequest(null, LINE_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, lineSelectionRequest, null,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateLineSelection(request, breedingMethodDTO, PLOT_DATASET_ID, Arrays.asList(plotDatasetVariables));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.lines.selection.variate.property.invalid"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(AdvanceValidator.SELECTED_LINE_VARIABLE_PROPERTY));
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
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(true, PLOT_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, bulkingRequest,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateBulkingSelection(request, breedingMethodDTO, new ArrayList<>());
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.bulking.selection.both-selected"));
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
	public void validateBulkingSelection_FAIL_invalidVariableType() {

		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(PLOT_VARIATE_ID, AdvanceValidator.SELECTED_LINE_VARIABLE_PROPERTY, VariableType.ENTRY_DETAIL);

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(null, PLOT_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, bulkingRequest,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateBulkingSelection(request, breedingMethodDTO, Arrays.asList(plotDatasetVariables));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.bulking.selection.variate.type.invalid"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(VariableType.SELECTION_METHOD));
		}
	}

	@Test
	public void validateBulkingSelection_FAIL_invalidVariableProperty() {
		final MeasurementVariable plotDatasetVariables =
			this.mockMeasurementVariable(PLOT_VARIATE_ID, RandomStringUtils.randomAlphabetic(10), VariableType.SELECTION_METHOD);

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.BulkingRequest bulkingRequest = this.mockBulkingRequest(null, PLOT_VARIATE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, bulkingRequest,
				null);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		try {
			this.advanceValidator.validateBulkingSelection(request, breedingMethodDTO, Arrays.asList(plotDatasetVariables));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.bulking.selection.variate.property.invalid"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is(AdvanceValidator.SELECTED_LINE_VARIABLE_PROPERTY));
		}
	}

	@Test
	public void validateSelectionTrait_OK_datasetIdAsGivenSelectionTraitDatasetId() {

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		final MeasurementVariable selectionTraitDatasetVariables =
			this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID, SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.TRAIT);
		final DatasetDTO datasetDTO = Mockito.mock(DatasetDTO.class);
		Mockito.when(datasetDTO.getDatasetId()).thenReturn(SELECTION_TRAIT_DATASET_ID);
		Mockito.when(datasetDTO.getVariables()).thenReturn(Arrays.asList(selectionTraitDatasetVariables));
		Mockito.when(
			this.datasetService.getDatasetsWithVariables(STUDY_ID, datasetTypeIds)).thenReturn(Arrays.asList(datasetDTO));

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AbstractAdvanceRequest.SelectionTraitRequest selectionTraitRequest =
			this.mockSelectionTraitRequest(SELECTION_TRAIT_DATASET_ID, SELECTION_TRAIT_VARIABLE_ID);
		final AdvanceStudyRequest request =
			this.mockAdvanceStudyRequest(new ArrayList<>(), new ArrayList<>(), breedingMethodSelectionRequest, null, null,
				selectionTraitRequest);
		final BreedingMethodDTO breedingMethodDTO = this.mockBreedingMethodDTO(MethodType.DERIVATIVE, false);

		this.advanceValidator.validateSelectionTrait(STUDY_ID, request, breedingMethodDTO);

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator)
			.validateDatasetBelongsToStudy(STUDY_ID, SELECTION_TRAIT_DATASET_ID);
	}

	@Test
	public void validateSelectionTrait_FAIL_requestRequiredUsingVariateForMethod() {
		final MeasurementVariable selectionTraitVariable =
			this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID, SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.SELECTION_METHOD);
		Mockito.when(
			this.datasetService.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId())))
			.thenReturn(Arrays.asList(selectionTraitVariable));

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

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_requestRequiredUsingSameMethodWithSelTraitAsPrefix() {

		final MeasurementVariable selectionTraitVariable =
			this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID, SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.SELECTION_METHOD);
		Mockito.when(
			this.datasetService.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId())))
			.thenReturn(Arrays.asList(selectionTraitVariable));

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

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_requestRequiredUsingSameMethodWithSelTraitAsSuffix() {

		final MeasurementVariable selectionTraitVariable =
			this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID, SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.SELECTION_METHOD);
		Mockito.when(
			this.datasetService.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId())))
			.thenReturn(Arrays.asList(selectionTraitVariable));

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

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_datasetIdRequired() {
		final MeasurementVariable selectionTraitVariable =
			this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID, SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.SELECTION_METHOD);
		Mockito.when(
			this.datasetService.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId())))
			.thenReturn(Arrays.asList(selectionTraitVariable));

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

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_variableIdRequired() {
		final MeasurementVariable selectionTraitVariable =
			this.mockMeasurementVariable(SELECTION_TRAIT_VARIABLE_ID, SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.SELECTION_METHOD);
		Mockito.when(
			this.datasetService.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId())))
			.thenReturn(Arrays.asList(selectionTraitVariable));

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

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
	}

	@Test
	public void validateSelectionTrait_FAIL_variableNotPresent() {

		final MeasurementVariable selectionTraitVariable =
			this.mockMeasurementVariable(new Random().nextInt(), SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY,
				VariableType.SELECTION_METHOD);
		Mockito.when(
			this.datasetService.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId())))
			.thenReturn(Arrays.asList(selectionTraitVariable));

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			this.mockBreedingMethodSelectionRequest(null, METHOD_VARIATE_ID);
		final AdvanceStudyRequest.SelectionTraitRequest selectionTraitRequest =
			this.mockSelectionTraitRequest(STUDY_ID, SELECTION_TRAIT_VARIABLE_ID);
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

		Mockito.verify(this.datasetService)
			.getDatasetMeasurementVariablesByVariableType(STUDY_ID, Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, datasetTypeIds);

		Mockito.verify(this.datasetValidator, Mockito.never())
			.validateDatasetBelongsToStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
	}

	@Test
	public void validateReplicationNumberSelection_OK() {
		final MeasurementVariable replicationVariable = this.mockMeasurementVariable(TermId.REP_NO.getId());
		final MeasurementVariable replicationNumberVariable = this.mockMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), "5");

		this.mockGetEnvironmentDatasetWithVariables(Arrays.asList(replicationNumberVariable));

		this.advanceValidator.validateReplicationNumberSelection(STUDY_ID, Arrays.asList(5, 3, 1, 4),
			Arrays.asList(replicationVariable));

		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
	}

	@Test
	public void validateReplicationNumberSelection_FAIL_selectionRequired() {
		final MeasurementVariable variable = this.mockMeasurementVariable(TermId.REP_NO.getId());

		try {
			this.advanceValidator.validateReplicationNumberSelection(STUDY_ID, new ArrayList<>(), Arrays.asList(variable));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.replication-number.selection.required"));
		}

		Mockito.verify(this.datasetService, Mockito.never()).getDatasetsWithVariables(ArgumentMatchers.anyInt(), ArgumentMatchers.anySet());
	}

	@Test
	public void validateReplicationNumberSelection_FAIL_invalidReplicationNumbers() {
		final MeasurementVariable replicationVariable = this.mockMeasurementVariable(TermId.REP_NO.getId());
		final MeasurementVariable replicationNumberVariable = this.mockMeasurementVariable(TermId.NUMBER_OF_REPLICATES.getId(), "5");

		this.mockGetEnvironmentDatasetWithVariables(Arrays.asList(replicationNumberVariable));

		try {
			this.advanceValidator
				.validateReplicationNumberSelection(STUDY_ID, Arrays.asList(-1, 0, 4, 5, 1, 6, -1), Arrays.asList(replicationVariable));
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.replication-number.invalid"));
			assertThat(exception.getErrors().get(0).getArguments()[0], is("-1, 0, 6"));
		}

		Mockito.verify(this.datasetService).getDatasetsWithVariables(STUDY_ID, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId()));
	}

	@Test
	public void validateAdvanceSamples_OK() {
		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(new ArrayList<>());

		Mockito.when(this.studyService.isSampled(STUDY_ID)).thenReturn(true);

		this.mockValidateMethod(MethodType.DERIVATIVE, false);

		final AdvanceSamplesRequest request =
			this.mockAdvanceSampledPlantsRequest(Arrays.asList(INSTANCE_ID), BREEDING_METHOD_ID);
		this.advanceValidator.validateAdvanceSamples(STUDY_ID, request);

		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.studyValidator).validateStudyHasPlotDataset(STUDY_ID);
		Mockito.verify(this.datasetService).getDataset(PLOT_DATASET_ID);
		Mockito.verify(this.instanceValidator).validateStudyInstance(ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.anySet());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(PLOT_DATASET_ID);

		Mockito.verify(this.studyService).isSampled(STUDY_ID);

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);
	}

	@Test
	public void validateAdvanceSamples_FAIL_studyHasNoSamples() {
		this.mockValidateStudyHasPlotDataset();
		this.mockGetDataset(true);

		Mockito.when(this.datasetService.getObservationSetVariables(PLOT_DATASET_ID)).thenReturn(new ArrayList<>());

		Mockito.when(this.studyService.isSampled(STUDY_ID)).thenReturn(false);

		this.mockValidateMethod(MethodType.DERIVATIVE, false);

		final AdvanceSamplesRequest request =
			this.mockAdvanceSampledPlantsRequest(Arrays.asList(INSTANCE_ID), BREEDING_METHOD_ID);

		try {
			this.advanceValidator.validateAdvanceSamples(STUDY_ID, request);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.samples.required"));
		}

		Mockito.verify(this.studyValidator).validate(STUDY_ID, true);
		Mockito.verify(this.studyValidator).validateStudyHasPlotDataset(STUDY_ID);
		Mockito.verify(this.datasetService).getDataset(PLOT_DATASET_ID);
		Mockito.verify(this.instanceValidator).validateStudyInstance(ArgumentMatchers.eq(STUDY_ID), ArgumentMatchers.anySet());
		Mockito.verify(this.datasetService, Mockito.times(1)).getObservationSetVariables(PLOT_DATASET_ID);

		Mockito.verify(this.studyService).isSampled(STUDY_ID);

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(BREEDING_METHOD_ID);
	}

	@Test
	public void validateAdvanceSamplesBreedingMethodSelection_OK() {
		this.mockValidateMethod(MethodType.DERIVATIVE, false);

		this.advanceValidator.validateAdvanceSamplesBreedingMethodSelection(BREEDING_METHOD_ID);

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);
	}

	@Test
	public void validateAdvanceSamplesBreedingMethodSelection_FAIL_breedingMethodRequired() {
		try {
			this.advanceValidator.validateAdvanceSamplesBreedingMethodSelection(null);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.required"));
		}

		Mockito.verify(this.breedingMethodValidator, Mockito.never()).validateMethod(BREEDING_METHOD_ID);
	}

	@Test
	public void validateAdvanceSamplesBreedingMethodSelection_FAIL_generativeBreedingMethod() {
		this.mockValidateMethod(MethodType.GENERATIVE, false);

		try {
			this.advanceValidator.validateAdvanceSamplesBreedingMethodSelection(BREEDING_METHOD_ID);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.breeding-method.selection.generative.invalid"));
		}

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);
	}

	@Test
	public void validateAdvanceSamplesBreedingMethodSelection_FAIL_bulkingBreedingMethod() {
		this.mockValidateMethod(MethodType.DERIVATIVE, true);

		try {
			this.advanceValidator.validateAdvanceSamplesBreedingMethodSelection(BREEDING_METHOD_ID);
			fail("should have failed");
		} catch (final ApiRequestValidationException exception) {
			assertThat(exception, instanceOf(ApiRequestValidationException.class));
			assertThat(exception.getErrors().get(0).getCode(), is("advance.samples.breeding-method.selection.bulking.invalid"));
		}

		Mockito.verify(this.breedingMethodValidator).validateMethod(BREEDING_METHOD_ID);
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

	private AdvanceStudyRequest mockAdvanceStudyRequest(final List<Integer> instanceIds, final List<Integer> selectedReplications,
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
		return this.mockMeasurementVariable(variableId, RandomStringUtils.randomAlphabetic(10), VariableType.ENTRY_DETAIL,
			RandomStringUtils.randomAlphabetic(3));
	}

	private MeasurementVariable mockMeasurementVariable(final Integer variableId, final String value) {
		return this.mockMeasurementVariable(variableId, RandomStringUtils.randomAlphabetic(10), VariableType.ENTRY_DETAIL, value);
	}

	private MeasurementVariable mockMeasurementVariable(final Integer variableId, final String property, final VariableType variableType) {
		return this.mockMeasurementVariable(variableId, property, variableType, RandomStringUtils.randomAlphabetic(3));
	}

	private MeasurementVariable mockMeasurementVariable(final Integer variableId, final String property, final VariableType variableType,
		final String value) {
		final MeasurementVariable measurementVariable = Mockito.mock(MeasurementVariable.class);
		Mockito.when(measurementVariable.getTermId()).thenReturn(variableId);
		Mockito.when(measurementVariable.getProperty()).thenReturn(property);
		Mockito.when(measurementVariable.getVariableType()).thenReturn(variableType);
		Mockito.when(measurementVariable.getValue()).thenReturn(value);
		return measurementVariable;
	}

	private AdvanceSamplesRequest mockAdvanceSampledPlantsRequest(final List<Integer> instanceIds, final Integer breedingMethodId) {
		final AdvanceSamplesRequest request = Mockito.mock(AdvanceSamplesRequest.class);
		Mockito.when(request.getInstanceIds()).thenReturn(instanceIds);
		Mockito.when(request.getSelectedReplications()).thenReturn(new ArrayList<>());
		Mockito.when(request.getBreedingMethodId()).thenReturn(breedingMethodId);
		return request;
	}

	private void mockGetEnvironmentDatasetWithVariables(final List<MeasurementVariable> variables) {
		final DatasetDTO datasetDTO = Mockito.mock(DatasetDTO.class);
		Mockito.when(datasetDTO.getVariables()).thenReturn(variables);
		Mockito.when(this.datasetService.getDatasetsWithVariables(STUDY_ID, Collections.singleton(DatasetTypeEnum.SUMMARY_DATA.getId())))
			.thenReturn(Arrays.asList(datasetDTO));
	}

}
