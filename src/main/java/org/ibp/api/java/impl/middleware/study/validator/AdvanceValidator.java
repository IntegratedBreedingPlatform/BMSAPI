package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.collections.CollectionUtils;
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
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.ruleengine.naming.expression.SelectionTraitExpression;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.study.advance.resolver.level.SelectionTraitDataResolver;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BreedingMethodValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.study.StudyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Component
public class AdvanceValidator {

	public static final String BREEDING_METHOD_VARIABLE_PROPERTY = "Breeding method";
	public static final String SELECTED_LINE_VARIABLE_PROPERTY = "Selections";

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private DatasetService datasetService;

	@Resource
	private InstanceValidator instanceValidator;

	@Resource
	private BreedingMethodValidator breedingMethodValidator;

	@Resource
	private DatasetValidator datasetValidator;

	@Resource
	private StudyService studyService;

	public void validateAdvanceStudy(final Integer studyId, final AdvanceStudyRequest request) {
		checkNotNull(request, "request.null");

		final Integer plotDatasetId = this.commonValidations(studyId, request.getInstanceIds());

		final List<MeasurementVariable> plotDatasetVariables = this.datasetService.getObservationSetVariables(plotDatasetId);
		final BreedingMethodDTO selectedBreedingMethodDTO =
			this.validateAdvanceStudyBreedingMethodSelection(request.getBreedingMethodSelectionRequest(), plotDatasetVariables);
		this.validateLineSelection(request, selectedBreedingMethodDTO, plotDatasetVariables);
		this.validateBulkingSelection(request, selectedBreedingMethodDTO, plotDatasetVariables);
		this.validateSelectionTrait(studyId, request, selectedBreedingMethodDTO);
		this.validateReplicationNumberSelection(request.getSelectedReplications(), plotDatasetVariables);
	}

	public void validateAdvanceSamples(final Integer studyId, final AdvanceSamplesRequest request) {
		checkNotNull(request, "request.null");

		final Integer plotDatasetId = this.commonValidations(studyId, request.getInstanceIds());

		final List<MeasurementVariable> plotDatasetVariables = this.datasetService.getObservationSetVariables(plotDatasetId);

		final Boolean hasSamples = this.studyService.isSampled(studyId);
		if (!hasSamples) {
			throw new ApiRequestValidationException("advance.samples.required", new Object[] {});
		}

		this.validateAdvanceSamplesBreedingMethodSelection(request.getBreedingMethodId());
		this.validateReplicationNumberSelection(request.getSelectedReplications(), plotDatasetVariables);
	}

	private Integer commonValidations(final Integer studyId, final List<Integer> instanceIds) {
		this.studyValidator.validate(studyId, true);
		final DataSet dataset = this.studyValidator.validateStudyHasPlotDataset(studyId);
		final DatasetDTO plotDataset = this.datasetService.getDataset(dataset.getId());
		final boolean studyHasExperimentDesign =
			plotDataset.getInstances().stream().anyMatch(i -> i.isHasExperimentalDesign() == Boolean.TRUE);

		if (!studyHasExperimentDesign) {
			throw new ApiRequestValidationException("study.not.has.experimental.design.created", new Object[] {});
		}

		if (CollectionUtils.isEmpty(instanceIds)) {
			throw new ApiRequestValidationException("study.instances.required", new Object[] {});
		}

		this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(instanceIds));

		return dataset.getId();
	}

	BreedingMethodDTO validateAdvanceStudyBreedingMethodSelection(
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest,
		final List<MeasurementVariable> plotDatasetVariables) {
		checkNotNull(breedingMethodSelectionRequest, "request.null");

		if ((breedingMethodSelectionRequest.getBreedingMethodId() == null && breedingMethodSelectionRequest.getMethodVariateId() == null)
			|| (breedingMethodSelectionRequest.getBreedingMethodId() != null
			&& breedingMethodSelectionRequest.getMethodVariateId() != null)) {
			throw new ApiRequestValidationException("advance.breeding-method.selection.required", new Object[] {});
		}

		final BreedingMethodDTO breedingMethodDTO;
		if (breedingMethodSelectionRequest.getBreedingMethodId() != null) {
			breedingMethodDTO =
				this.breedingMethodValidator.validateMethod(breedingMethodSelectionRequest.getBreedingMethodId());

			if (MethodType.isGenerative(breedingMethodDTO.getType())) {
				throw new ApiRequestValidationException("advance.breeding-method.selection.generative.invalid", new Object[] {});
			}
		} else {
			breedingMethodDTO = null;
		}

		if (breedingMethodSelectionRequest.getMethodVariateId() != null) {
			final MeasurementVariable variable =
				this.validatePlotdataSetHasVariable(plotDatasetVariables, breedingMethodSelectionRequest.getMethodVariateId(),
					"advance.breeding-method.selection.variate.not-present");
			if (variable.getVariableType() != VariableType.SELECTION_METHOD) {
				throw new ApiRequestValidationException("advance.breeding-method.selection.variate.type.invalid",
					new Object[] {VariableType.SELECTION_METHOD.getName()});
			}
			if (!BREEDING_METHOD_VARIABLE_PROPERTY.equals(variable.getProperty())) {
				throw new ApiRequestValidationException("advance.breeding-method.selection.variate.property.invalid",
					new Object[] {BREEDING_METHOD_VARIABLE_PROPERTY});
			}
		}

		return breedingMethodDTO;
	}

	void validateAdvanceSamplesBreedingMethodSelection(final Integer breedingMethodId) {
		if (breedingMethodId == null) {
			throw new ApiRequestValidationException("advance.breeding-method.selection.required", new Object[] {});
		}

		final BreedingMethodDTO breedingMethodDTO = this.breedingMethodValidator.validateMethod(breedingMethodId);
		if (MethodType.isGenerative(breedingMethodDTO.getType())) {
			throw new ApiRequestValidationException("advance.breeding-method.selection.generative.invalid", new Object[] {});
		}

		if (breedingMethodDTO.getIsBulkingMethod()) {
			throw new ApiRequestValidationException("advance.samples.breeding-method.selection.bulking.invalid", new Object[] {});
		}
	}

	void validateLineSelection(final AdvanceStudyRequest request, final BreedingMethodDTO selectedBreedingMethod,
		final List<MeasurementVariable> plotDatasetVariables) {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			request.getBreedingMethodSelectionRequest();
		if ((breedingMethodSelectionRequest.getBreedingMethodId() != null && (Boolean.FALSE
			.equals(selectedBreedingMethod.getIsBulkingMethod()))) || (breedingMethodSelectionRequest.getMethodVariateId() != null)) {
			final AdvanceStudyRequest.LineSelectionRequest lineSelectionRequest = request.getLineSelectionRequest();
			checkNotNull(lineSelectionRequest, "request.null");

			if ((lineSelectionRequest.getLinesSelected() == null && lineSelectionRequest.getLineVariateId() == null) ||
				(lineSelectionRequest.getLinesSelected() != null && lineSelectionRequest.getLineVariateId() != null)) {
				throw new ApiRequestValidationException("advance.lines.selection.required", new Object[] {});
			}

			if (lineSelectionRequest.getLineVariateId() != null) {
				final MeasurementVariable variable =
					this.validatePlotdataSetHasVariable(plotDatasetVariables, lineSelectionRequest.getLineVariateId(),
						"advance.lines.selection.variate.not-present");
				if (variable.getVariableType() != VariableType.SELECTION_METHOD) {
					throw new ApiRequestValidationException("advance.lines.selection.variate.type.invalid",
						new Object[] {VariableType.SELECTION_METHOD});
				}
				if (!SELECTED_LINE_VARIABLE_PROPERTY.equals(variable.getProperty())) {
					throw new ApiRequestValidationException("advance.lines.selection.variate.property.invalid",
						new Object[] {SELECTED_LINE_VARIABLE_PROPERTY});
				}
			}
		}
	}

	void validateBulkingSelection(final AdvanceStudyRequest request, final BreedingMethodDTO selectedBreedingMethod,
		final List<MeasurementVariable> plotDatasetVariables) {
		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			request.getBreedingMethodSelectionRequest();
		if ((breedingMethodSelectionRequest.getBreedingMethodId() != null && selectedBreedingMethod.getIsBulkingMethod()) || (
			breedingMethodSelectionRequest.getMethodVariateId() != null)) {
			final AdvanceStudyRequest.BulkingRequest bulkingRequest = request.getBulkingRequest();
			checkNotNull(bulkingRequest, "request.null");

			if ((bulkingRequest.getAllPlotsSelected() == null && bulkingRequest.getPlotVariateId() == null) ||
				(bulkingRequest.getAllPlotsSelected() != null && bulkingRequest.getPlotVariateId() != null)) {
				throw new ApiRequestValidationException("advance.bulking.selection.required", new Object[] {});
			}

			if (bulkingRequest.getPlotVariateId() != null) {
				final MeasurementVariable variable =
					this.validatePlotdataSetHasVariable(plotDatasetVariables, bulkingRequest.getPlotVariateId(),
						"advance.bulking.selection.variate.not-present");
				if (variable.getVariableType() != VariableType.SELECTION_METHOD) {
					throw new ApiRequestValidationException("advance.bulking.selection.variate.type.invalid",
						new Object[] {VariableType.SELECTION_METHOD});
				}
				if (!SELECTED_LINE_VARIABLE_PROPERTY.equals(variable.getProperty())) {
					throw new ApiRequestValidationException("advance.bulking.selection.variate.property.invalid",
						new Object[] {SELECTED_LINE_VARIABLE_PROPERTY});
				}
			}
		}

	}

	void validateSelectionTrait(final Integer studyId, final AdvanceStudyRequest request,
		final BreedingMethodDTO selectedBreedingMethod) {

		final AdvanceStudyRequest.BreedingMethodSelectionRequest breedingMethodSelectionRequest =
			request.getBreedingMethodSelectionRequest();
		if (breedingMethodSelectionRequest.getMethodVariateId() != null || (breedingMethodSelectionRequest.getBreedingMethodId() != null
			&& (SelectionTraitExpression.KEY.equals(selectedBreedingMethod.getPrefix()) || SelectionTraitExpression.KEY
			.equals(selectedBreedingMethod.getSuffix())))) {

			// Check if there is at least a selection trait variables at any level
			final Map<Integer, List<MeasurementVariable>> selectionTraitVariablesByDatasetIds =
				this.getSelectionTraitVariablesByDatasetIds(studyId);
			final boolean selectionTraitVariablesPresent =
				selectionTraitVariablesByDatasetIds.values().stream().anyMatch(CollectionUtils::isNotEmpty);

			if (selectionTraitVariablesPresent) {
				final AbstractAdvanceRequest.SelectionTraitRequest selectionTraitRequest = request.getSelectionTraitRequest();
				checkNotNull(selectionTraitRequest, "request.null");
				checkArgument(selectionTraitRequest.getDatasetId() != null, "field.is.required",
					new String[] {"selectionTraitRequest.datasetId"});
				checkArgument(selectionTraitRequest.getVariableId() != null, "field.is.required",
					new String[] {"selectionTraitRequest.variableId"});

				if (!studyId.equals(selectionTraitRequest.getDatasetId())) {
					this.datasetValidator.validateDatasetBelongsToStudy(studyId, selectionTraitRequest.getDatasetId());
				}

				final Optional<MeasurementVariable> selectionTraitVariable =
					selectionTraitVariablesByDatasetIds.get(selectionTraitRequest.getDatasetId()).stream()
						.filter(measurementVariable -> measurementVariable.getTermId() == selectionTraitRequest.getVariableId())
						.findFirst();
				if (!selectionTraitVariable.isPresent()) {
					throw new ApiRequestValidationException("advance.selection-trait.not-present",
						new Object[] {String.valueOf(selectionTraitRequest.getVariableId())});
				}
			}
		}
	}

	void validateReplicationNumberSelection(final List<String> selectedReplications,
		final List<MeasurementVariable> plotDatasetVariables) {
		final Optional<MeasurementVariable> replicationNumberVariable = plotDatasetVariables.stream()
			.filter(measurementVariable -> measurementVariable.getTermId() == TermId.REP_NO.getId())
			.findFirst();
		if (replicationNumberVariable.isPresent() && CollectionUtils.isEmpty(selectedReplications)) {
			throw new ApiRequestValidationException("advance.replication-number.selection.required", new Object[] {});
		}
	}

	private MeasurementVariable validatePlotdataSetHasVariable(final List<MeasurementVariable> plotDatasetVariables,
		final Integer variableId,
		final String errorCode) {
		final Optional<MeasurementVariable> methodVariate = plotDatasetVariables.stream()
			.filter(measurementVariable -> measurementVariable.getTermId() == variableId)
			.findFirst();
		if (!methodVariate.isPresent()) {
			throw new ApiRequestValidationException(errorCode, new Object[] {String.valueOf(variableId)});
		}
		return methodVariate.get();
	}

	private Map<Integer, List<MeasurementVariable>> getSelectionTraitVariablesByDatasetIds(final Integer studyId) {
		final List<MeasurementVariable> studyVariables = this.datasetService
			.getDatasetMeasurementVariablesByVariableType(studyId, Arrays.asList(VariableType.STUDY_DETAIL.getId()));
		final List<MeasurementVariable> studySelectionTraitVariables = this.filterSelectionTraitVariables(studyVariables);

		final Set<Integer> datasetTypeIds = new HashSet<>();
		datasetTypeIds.add(DatasetTypeEnum.PLOT_DATA.getId());
		datasetTypeIds.add(DatasetTypeEnum.SUMMARY_DATA.getId());
		final Map<Integer, List<MeasurementVariable>> envAndPlotDatasetSelectionTraitVariables =
			this.datasetService.getDatasetsWithVariables(studyId, datasetTypeIds).stream()
				.collect(Collectors
					.toMap(DatasetDTO::getDatasetId, datasetDTO -> this.filterSelectionTraitVariables(datasetDTO.getVariables())));

		final Map<Integer, List<MeasurementVariable>> selectionTraitVariablesByDatasetIds = new HashMap<>();
		selectionTraitVariablesByDatasetIds.put(studyId, studySelectionTraitVariables);
		selectionTraitVariablesByDatasetIds.putAll(envAndPlotDatasetSelectionTraitVariables);
		return selectionTraitVariablesByDatasetIds;
	}

	private List<MeasurementVariable> filterSelectionTraitVariables(final List<MeasurementVariable> variables) {
		return variables.stream()
			.filter(measurementVariable -> SelectionTraitDataResolver.SELECTION_TRAIT_PROPERTY.equals(measurementVariable.getProperty()))
			.collect(
				Collectors.toList());
	}

}
