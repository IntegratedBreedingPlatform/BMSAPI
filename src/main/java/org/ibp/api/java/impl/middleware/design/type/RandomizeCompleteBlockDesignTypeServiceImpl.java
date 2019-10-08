package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.util.ExpDesignUtil;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RandomizeCompleteBlockDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final Logger LOG = LoggerFactory.getLogger(RandomizeCompleteBlockDesignTypeServiceImpl.class);

	private static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.REP_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId());

	private static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId());

	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput,
		final String programUUID, final List<ImportedGermplasm> germplasmList) {

		this.experimentDesignTypeValidator.validateRandomizedCompleteBlockDesign(experimentDesignInput, germplasmList);

		final String block = experimentDesignInput.getReplicationsCount();
		final int environments = Integer.valueOf(experimentDesignInput.getNoOfEnvironments());
		final int environmentsToAdd = Integer.valueOf(experimentDesignInput.getNoOfEnvironmentsToAdd());

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final String replicateNumberName = standardVariablesMap.get(TermId.REP_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();

		final Map<String, List<String>> treatmentFactorValues =
			this.getTreatmentFactorValues(experimentDesignInput.getTreatmentFactorsData());
		final List<String> treatmentFactors = this.getTreatmentFactors(treatmentFactorValues);
		final List<String> treatmentLevels = this.getLevels(treatmentFactorValues);

		treatmentFactorValues.put(entryNumberName, Arrays.asList(Integer.toString(germplasmList.size())));
		treatmentFactors.add(entryNumberName);
		treatmentLevels.add(Integer.toString(germplasmList.size()));

		final Integer plotNo = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
		final Integer entryNo = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createRandomizedCompleteBlockDesign(block, replicateNumberName, plotNumberName, plotNo, entryNo,
				entryNumberName, treatmentFactors,
				treatmentLevels, "");

		/**
		 * TODO: return ObservationUnitRows from  this.experimentDesignGenerator.generateExperimentDesignMeasurements
		 final List<ObservationUnitRow> observationUnitRows = this.experimentDesignGenerator
		 .generateExperimentDesignMeasurements(environments, environmentsToAdd, trialVariables, factors, nonTrialFactors,
		 variates, treatmentVariables, reqVarList, germplasmList, mainDesign, stdvarTreatment.getName(),
		 treatmentFactorValues, new HashMap<Integer, Integer>());
		 **/
		return new ArrayList<>();
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId();
	}

	@Override
	public Map<Integer, MeasurementVariable> getMeasurementVariablesMap(final int studyId, final String programUUID) {
		return this.experimentDesignGenerator.getMeasurementVariablesMap(studyId, programUUID, DESIGN_FACTOR_VARIABLES, new ArrayList<>());
	}

	Map<String, List<String>> getTreatmentFactorValues(final Map treatmentFactorsData) {

		final Map<String, List<String>> treatmentFactorValues = new HashMap<>();

		if (treatmentFactorsData != null) {
			final Iterator keySetIter = treatmentFactorsData.keySet().iterator();
			while (keySetIter.hasNext()) {
				final String key = (String) keySetIter.next();
				final Map treatmentData = (Map) treatmentFactorsData.get(key);
				treatmentFactorValues.put(key, (List) treatmentData.get("labels"));
			}
		}

		return treatmentFactorValues;
	}

	List<String> getTreatmentFactors(final Map<String, List<String>> treatmentFactorValues) {
		final List<String> treatmentFactors = new ArrayList<>();
		final Set<String> keySet = treatmentFactorValues.keySet();
		for (final String key : keySet) {
			treatmentFactors.add(ExpDesignUtil.cleanBVDesingKey(key));
		}
		return treatmentFactors;
	}

	List<String> getLevels(final Map<String, List<String>> treatmentFactorValues) {
		final List<String> levels = new ArrayList<>();
		final Set<String> keySet = treatmentFactorValues.keySet();
		for (final String key : keySet) {
			final int level = treatmentFactorValues.get(key).size();
			levels.add(Integer.toString(level));
		}
		return levels;
	}

}
