package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.util.ExperimentalDesignUtil;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentalDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ResolvableRowColumnDesignTypeServiceImpl implements ExperimentDesignTypeService {

	protected static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.REP_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.ROW.getId(), TermId.COL.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES_LATINIZED = Arrays
		.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.NO_OF_ROWS_IN_REPS.getId(),
			TermId.NO_OF_COLS_IN_REPS.getId(), TermId.NO_OF_CROWS_LATINIZE.getId(), TermId.NO_OF_CCOLS_LATINIZE.getId(),
			TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_REPS_IN_COLS.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES = Arrays
		.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.NO_OF_ROWS_IN_REPS.getId(),
			TermId.NO_OF_COLS_IN_REPS.getId());

	@Resource
	private ExperimentalDesignTypeValidator experimentalDesignTypeValidator;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID, final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.experimentalDesignTypeValidator.validateResolvableRowColumnDesign(experimentalDesignInput, studyGermplasmDtoList);

		final int nTreatments = studyGermplasmDtoList.size();
		final Integer rows = experimentalDesignInput.getRowsPerReplications();
		final Integer cols = experimentalDesignInput.getColsPerReplications();
		final Integer replicates = experimentalDesignInput.getReplicationsCount();

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final String replicateNumberName = standardVariablesMap.get(TermId.REP_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();
		final String rowName = standardVariablesMap.get(TermId.ROW.getId()).getName();
		final String colName = standardVariablesMap.get(TermId.COL.getId()).getName();

		ExperimentalDesignUtil.setReplatinGroups(experimentalDesignInput);

		final Integer plotNo = experimentalDesignInput.getStartingPlotNo() == null? 1 : experimentalDesignInput.getStartingPlotNo();

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableRowColDesign(nTreatments, replicates, rows, cols, entryNumberName,
				replicateNumberName, rowName, colName, plotNumberName, plotNo,
				experimentalDesignInput.getNrlatin(), experimentalDesignInput.getNclatin(), experimentalDesignInput.getReplatinGroups(), "",
				experimentalDesignInput.getUseLatenized());

		final List<MeasurementVariable> measurementVariables = this.getMeasurementVariables(studyId, experimentalDesignInput, programUUID);
		return this.experimentDesignGenerator
			.generateExperimentDesignMeasurements(experimentalDesignInput.getTrialInstancesForDesignGeneration(), measurementVariables, studyGermplasmDtoList, mainDesign,
				entryNumberName, null,
				new HashMap<>());
	}

	@Override
	public Boolean requiresLicenseCheck() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.ROW_COL.getId();
	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID) {
		return this.experimentDesignGenerator
			.constructMeasurementVariables(studyId, programUUID, DESIGN_FACTOR_VARIABLES,
				(experimentalDesignInput.getUseLatenized() != null && experimentalDesignInput.getUseLatenized()) ?
					EXPERIMENT_DESIGN_VARIABLES_LATINIZED : EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput);
	}

}
