package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignProcessor;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
import org.ibp.api.java.impl.middleware.design.generator.ResolvableIncompleteBlockDesignGenerator;
import org.ibp.api.java.impl.middleware.design.util.ExperimentalDesignUtil;
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
public class ResolvableIncompleteBlockDesignTypeServiceImpl implements ExperimentalDesignTypeService {

	protected static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.REP_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.BLOCK_NO.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES_LATINIZED =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
			TermId.NO_OF_CBLKS_LATINIZE.getId(), TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_REPS_IN_COLS.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId());

	@Resource
	private ResolvableIncompleteBlockDesignGenerator experimentDesignGenerator;

	@Resource
	private MeasurementVariableGenerator measurementVariableGenerator;

	@Resource
	private ExperimentalDesignProcessor experimentalDesignProcessor;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID, final List<StudyEntryDto> studyEntryDtoList) {

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		// Generate experiment design parameters input to design runner
		final int nTreatments = studyEntryDtoList.size();
		ExperimentalDesignUtil.setReplatinGroups(experimentalDesignInput);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput, this.getBreedingViewVariablesMap(standardVariablesMap), nTreatments, null, null);

		// Generate observation unit rows
		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final List<MeasurementVariable> measurementVariables = this.getMeasurementVariables(studyId, experimentalDesignInput, programUUID);
		return this.experimentalDesignProcessor
			.generateObservationUnitRows(experimentalDesignInput.getTrialInstancesForDesignGeneration(), measurementVariables, studyEntryDtoList, mainDesign, entryNumberName,
				null,
				new HashMap<>());
	}

	private Map<BreedingViewVariableParameter, String> getBreedingViewVariablesMap(final Map<Integer, StandardVariable> standardVariablesMap) {
		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final String blockNumberName = standardVariablesMap.get(TermId.BLOCK_NO.getId()).getName();
		final String repNumberName = standardVariablesMap.get(TermId.REP_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();

		final Map<BreedingViewVariableParameter, String> map = new HashMap<>();
		map.put(BreedingViewVariableParameter.ENTRY, entryNumberName);
		map.put(BreedingViewVariableParameter.BLOCK, blockNumberName);
		map.put(BreedingViewVariableParameter.PLOT, plotNumberName);
		map.put(BreedingViewVariableParameter.REP, repNumberName);
		return map;
	}

	@Override
	public Boolean requiresLicenseCheck() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId();
	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID) {
		return this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, programUUID, DESIGN_FACTOR_VARIABLES,
				(experimentalDesignInput.getUseLatenized() != null && experimentalDesignInput.getUseLatenized()) ?
					EXPERIMENT_DESIGN_VARIABLES_LATINIZED : EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput);
	}
}
