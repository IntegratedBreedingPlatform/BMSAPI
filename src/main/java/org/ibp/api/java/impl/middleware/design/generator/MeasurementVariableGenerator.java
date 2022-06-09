package org.ibp.api.java.impl.middleware.design.generator;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class MeasurementVariableGenerator {

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private MeasurementVariableTransformer measurementVariableTransformer;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private DatasetService datasetService;

	public List<MeasurementVariable> generateFromExperimentalDesignInput(final int studyId, final String programUUID,
		final List<Integer> designFactors, final List<Integer> experimentDesignFactors,
		final ExperimentalDesignInput experimentalDesignInput) {

		// Add the germplasm and environment detail variables from study.
		// This adds the specified trial design factor, experiment design factor and treatment factor variables.
		final int plotDatasetId =
			this.studyDataManager.getDataSetsByType(studyId, DatasetTypeEnum.PLOT_DATA.getId()).get(0).getId();
		final Set<MeasurementVariable> measurementVariables =
			new HashSet<>(this.datasetService.getDatasetMeasurementVariablesByVariableType(plotDatasetId,
				Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.ENTRY_DETAIL.getId())));

		measurementVariables.addAll(
			this.convertToMeasurementVariables(experimentDesignFactors, VariableType.ENVIRONMENT_DETAIL, experimentalDesignInput,
				programUUID));
		measurementVariables.addAll(
			this.convertToMeasurementVariables(designFactors, VariableType.EXPERIMENTAL_DESIGN, experimentalDesignInput, programUUID));

		return new ArrayList<>(measurementVariables);
	}

	List<MeasurementVariable> convertToMeasurementVariables(final List<Integer> variableIds, final VariableType variableType,
		final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID) {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final List<StandardVariable> treatmentFactorStandardVariables =
			this.ontologyDataManager.getStandardVariables(variableIds, programUUID);

		for (final StandardVariable standardVariable : treatmentFactorStandardVariables) {
			measurementVariables.add(this.convertToMeasurementVariable(standardVariable, variableType,
				this.getMeasurementValueFromDesignInput(experimentalDesignInput, standardVariable.getId())));
		}
		return measurementVariables;
	}

	MeasurementVariable convertToMeasurementVariable(final StandardVariable standardVariable, final VariableType variableType,
		final String value) {
		final MeasurementVariable measurementVariable =
			this.measurementVariableTransformer.transform(standardVariable, true, variableType);
		measurementVariable.setValue(value);
		return measurementVariable;
	}

	String getMeasurementValueFromDesignInput(final ExperimentalDesignInput experimentalDesignInput, final int termId) {

		if (termId == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
			if (experimentalDesignInput.getDesignType() != null) {
				return String.valueOf(ExperimentDesignType
					.getTermIdByDesignTypeId(experimentalDesignInput.getDesignType(), experimentalDesignInput.getUseLatenized()));
			}
		} else if (termId == TermId.NUMBER_OF_REPLICATES.getId()) {
			return String.valueOf(experimentalDesignInput.getReplicationsCount());
		} else if (termId == TermId.PERCENTAGE_OF_REPLICATION.getId()) {
			return String.valueOf(experimentalDesignInput.getReplicationPercentage());
		} else if (termId == TermId.BLOCK_SIZE.getId()) {
			return String.valueOf(experimentalDesignInput.getBlockSize());
		} else if (termId == TermId.REPLICATIONS_MAP.getId()) {
			if (experimentalDesignInput.getReplicationsArrangement() != null) {
				switch (experimentalDesignInput.getReplicationsArrangement()) {
					case 1:
						return String.valueOf(TermId.REPS_IN_SINGLE_COL.getId());
					case 2:
						return String.valueOf(TermId.REPS_IN_SINGLE_ROW.getId());
					case 3:
						return String.valueOf(TermId.REPS_IN_ADJACENT_COLS.getId());
					default:
				}
			}
		} else if (termId == TermId.NO_OF_REPS_IN_COLS.getId()) {
			return experimentalDesignInput.getReplatinGroups();
		} else if (termId == TermId.NO_OF_CBLKS_LATINIZE.getId()) {
			return String.valueOf(experimentalDesignInput.getNblatin());
		} else if (termId == TermId.NO_OF_ROWS_IN_REPS.getId()) {
			return String.valueOf(experimentalDesignInput.getRowsPerReplications());
		} else if (termId == TermId.NO_OF_COLS_IN_REPS.getId()) {
			return String.valueOf(experimentalDesignInput.getColsPerReplications());
		} else if (termId == TermId.NO_OF_CCOLS_LATINIZE.getId()) {
			return String.valueOf(experimentalDesignInput.getNclatin());
		} else if (termId == TermId.NO_OF_CROWS_LATINIZE.getId()) {
			return String.valueOf(experimentalDesignInput.getNrlatin());
		} else if (termId == TermId.EXPT_DESIGN_SOURCE.getId()) {
			return experimentalDesignInput.getFileName();
		} else if (termId == TermId.NBLKS.getId()) {
			return String.valueOf(experimentalDesignInput.getNumberOfBlocks());
		} else if (termId == TermId.CHECK_START.getId()) {
			return String.valueOf(experimentalDesignInput.getCheckStartingPosition());
		} else if (termId == TermId.CHECK_INTERVAL.getId()) {
			return String.valueOf(experimentalDesignInput.getCheckSpacing());
		} else if (termId == TermId.CHECK_PLAN.getId()) {
			return String.valueOf(experimentalDesignInput.getCheckInsertionManner());
		}
		return "";
	}
}
