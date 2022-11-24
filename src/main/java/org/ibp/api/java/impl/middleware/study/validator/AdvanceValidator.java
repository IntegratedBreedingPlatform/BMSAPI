package org.ibp.api.java.impl.middleware.study.validator;

import org.apache.commons.collections.CollectionUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.study.AdvanceSampledPlantsRequest;
import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.ruleengine.naming.expression.SelectionTraitExpression;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BreedingMethodValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Component
public class AdvanceValidator {

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

	public void validateAdvanceStudy(final Integer studyId, final AdvanceStudyRequest request) {
		checkNotNull(request, "request.null");

		this.studyValidator.validate(studyId, true);
		final DataSet dataset = this.studyValidator.validateStudyHasPlotDataset(studyId);
		final DatasetDTO plotDataset = this.datasetService.getDataset(dataset.getId());
		final boolean studyHasExperimentDesign =
			plotDataset.getInstances().stream().anyMatch(i -> i.isHasExperimentalDesign() == Boolean.TRUE);

		if (!studyHasExperimentDesign) {
			throw new ApiRequestValidationException("study.not.has.experimental.design.created", new Object[] {});
		}

		if (CollectionUtils.isEmpty(request.getInstanceIds())) {
			throw new ApiRequestValidationException("study.instances.required", new Object[] {});
		}

		this.instanceValidator.validateStudyInstance(studyId, new HashSet<>(request.getInstanceIds()));

		final List<MeasurementVariable> plotDatasetVariables = this.datasetService.getObservationSetVariables(dataset.getId());
		final BreedingMethodDTO selectedBreedingMethodDTO =
			this.validateBreedingMethodSelection(request.getBreedingMethodSelectionRequest(), plotDatasetVariables);
		this.validateLineSelection(request, selectedBreedingMethodDTO, plotDatasetVariables);
		this.validateBulkingSelection(request, selectedBreedingMethodDTO, plotDatasetVariables);
		this.validateSelectionTrait(studyId, request, selectedBreedingMethodDTO);
		this.validateReplicationNumberSelection(request.getSelectedReplications(), plotDatasetVariables);
	}

	public void validateAdvanceSamples(final Integer studyId, final AdvanceSampledPlantsRequest request) {
		// TODO: validate samples are created
		// TODO: validate instances
		// TODO: validate non-bulking DER and MAN methods are allowed
		// TODO: validate replications
		// TODO: validate harvest date
	}

	BreedingMethodDTO validateBreedingMethodSelection(
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
			this.validatePlotdataSetHasVariable(plotDatasetVariables, breedingMethodSelectionRequest.getMethodVariateId(),
				"advance.breeding-method.selection.variate.not-present");
		}

		return breedingMethodDTO;
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
				this.validatePlotdataSetHasVariable(plotDatasetVariables, lineSelectionRequest.getLineVariateId(),
					"advance.lines.selection.variate.not-present");
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
				this.validatePlotdataSetHasVariable(plotDatasetVariables, bulkingRequest.getPlotVariateId(),
					"advance.bulking.selection.variate.not-present");
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
			final AdvanceStudyRequest.SelectionTraitRequest selectionTraitRequest = request.getSelectionTraitRequest();
			checkNotNull(selectionTraitRequest, "request.null");
			checkArgument(selectionTraitRequest.getDatasetId() != null, "field.is.required",
				new String[] {"selectionTraitRequest.datasetId"});
			checkArgument(selectionTraitRequest.getVariableId() != null, "field.is.required",
				new String[] {"selectionTraitRequest.variableId"});

			this.datasetValidator.validateDatasetBelongsToStudy(studyId, selectionTraitRequest.getDatasetId());

			final List<MeasurementVariable> datasetVariables =
				this.datasetService.getObservationSetVariables(selectionTraitRequest.getDatasetId());
			final Optional<MeasurementVariable> selectionTraitVariable = datasetVariables.stream()
				.filter(measurementVariable -> measurementVariable.getTermId() == selectionTraitRequest.getVariableId())
				.findFirst();
			if (!selectionTraitVariable.isPresent()) {
				throw new ApiRequestValidationException("advance.selection-trait.not-present",
					new Object[] {String.valueOf(selectionTraitRequest.getVariableId())});
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

	private void validatePlotdataSetHasVariable(final List<MeasurementVariable> plotDatasetVariables, final Integer variableId,
		final String errorCode) {
		final Optional<MeasurementVariable> methodVariate = plotDatasetVariables.stream()
			.filter(measurementVariable -> measurementVariable.getTermId() == variableId)
			.findFirst();
		if (!methodVariate.isPresent()) {
			throw new ApiRequestValidationException(errorCode, new Object[] {String.valueOf(variableId)});
		}
	}

}
