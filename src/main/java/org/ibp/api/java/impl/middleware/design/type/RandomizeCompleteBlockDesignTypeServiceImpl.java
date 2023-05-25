package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.generator.AbstractExperimentalDesignGenerator;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignProcessor;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
import org.ibp.api.java.impl.middleware.design.generator.RandomizeCompleteBlockDesignGenerator;
import org.ibp.api.java.impl.middleware.design.util.ExperimentalDesignUtil;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RandomizeCompleteBlockDesignTypeServiceImpl implements ExperimentalDesignTypeService {

	protected static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.REP_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.OBS_UNIT_ID.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId());

	@Resource
	private RandomizeCompleteBlockDesignGenerator experimentDesignGenerator;

	@Resource
	private ExperimentalDesignProcessor experimentalDesignProcessor;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private MeasurementVariableTransformer measurementVariableTransformer;

	@Resource
	private MeasurementVariableGenerator measurementVariableGenerator;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID, final List<StudyEntryDto> studyEntryDtoList) {

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		// Resolve treatment factors parameters
		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final Map<String, List<String>> treatmentFactorValues =
			this.getTreatmentFactorValues(experimentalDesignInput.getTreatmentFactorsData());
		treatmentFactorValues.put(entryNumberName, Collections.singletonList(Integer.toString(studyEntryDtoList.size())));

		final List<String> treatmentFactors = this.getTreatmentFactors(treatmentFactorValues);
		final List<String> treatmentLevels = this.getLevels(treatmentFactorValues);
		treatmentFactors.add(entryNumberName);
		treatmentLevels.add(Integer.toString(studyEntryDtoList.size()));

		experimentalDesignInput.setNumberOfBlocks(experimentalDesignInput.getReplicationsCount());

		// Generate experiment design parameter input to design runner
		final Map<BreedingViewVariableParameter, String> map = this.getBreedingViewVariablesMap(standardVariablesMap);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput, map, null, null, this.getListItemsMap(treatmentFactors, treatmentLevels, entryNumberName));

		// Generate observation unit rows
		final List<MeasurementVariable> measurementVariables = this.getMeasurementVariables(studyId, experimentalDesignInput, programUUID);
		return this.experimentalDesignProcessor
			.generateObservationUnitRows(experimentalDesignInput.getTrialInstancesForDesignGeneration(), measurementVariables,
				studyEntryDtoList, mainDesign, entryNumberName,
				treatmentFactorValues, new HashMap<>());
	}

	private Map<BreedingViewDesignParameter, List<ListItem>> getListItemsMap(final List<String> treatmentFactors,
		final List<String> treatmentLevels, final String entryVariableName) {

		final Map<BreedingViewDesignParameter, List<ListItem>> listItemsMap = new HashMap<>();
		listItemsMap.put(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER, this.getInitialTreatNumList(treatmentFactors,
			AbstractExperimentalDesignGenerator.STARTING_ENTRY_NUMBER, entryVariableName));
		listItemsMap.put(BreedingViewDesignParameter.TREATMENTFACTORS, ExperimentalDesignUtil.convertToListItemList(treatmentFactors));
		listItemsMap.put(BreedingViewDesignParameter.LEVELS, ExperimentalDesignUtil.convertToListItemList(treatmentLevels));

		return listItemsMap;
	}

	private Map<BreedingViewVariableParameter, String> getBreedingViewVariablesMap(
		final Map<Integer, StandardVariable> standardVariablesMap) {
		final String replicateNumberName = standardVariablesMap.get(TermId.REP_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();
		final Map<BreedingViewVariableParameter, String> map = new HashMap<>();
		map.put(BreedingViewVariableParameter.BLOCK, replicateNumberName);
		map.put(BreedingViewVariableParameter.PLOT, plotNumberName);
		return map;
	}

	@Override
	public Boolean requiresLicenseCheck() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId();
	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID) {

		final List<MeasurementVariable> measurementVariables = this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, programUUID, DESIGN_FACTOR_VARIABLES, EXPERIMENT_DESIGN_VARIABLES,
				experimentalDesignInput);

		final Map treatmentFactorsData = experimentalDesignInput.getTreatmentFactorsData();
		if (treatmentFactorsData != null) {
			final Map<Integer, Integer> treatmentFactorLevelToLabelIdMap = new HashMap<>();
			final Map<Integer, String> treatmentFactorLevelAliasMap = new HashMap<>();

			final Iterator keySetIter = treatmentFactorsData.keySet().iterator();
			while (keySetIter.hasNext()) {
				final String key = (String) keySetIter.next();
				final Map treatmentData = (Map) treatmentFactorsData.get(key);
				treatmentFactorLevelToLabelIdMap.put(Integer.valueOf(key), (Integer) treatmentData.get("variableId"));
				if (treatmentData.containsKey("alias") && StringUtils.isNotEmpty((String) treatmentData.get("alias"))) {
					treatmentFactorLevelAliasMap.put(Integer.valueOf(key), (String) treatmentData.get("alias"));
				}
			}

			final List<Integer> treatmentFactorIds = new ArrayList<>(treatmentFactorLevelToLabelIdMap.values());
			treatmentFactorIds.addAll(treatmentFactorLevelToLabelIdMap.keySet());
			final List<StandardVariable> standardVariables = this.ontologyDataManager.getStandardVariables(treatmentFactorIds, programUUID);
			final Map<Integer, StandardVariable> standardVariableMap =
				standardVariables.stream().collect(Collectors.toMap(StandardVariable::getId,
					Function.identity()));

			final Set<Integer> treatmentFactorLevelIds = treatmentFactorLevelToLabelIdMap.keySet();
			for (final Integer treatmentFactorLevelId : treatmentFactorLevelIds) {
				final MeasurementVariable treatmentFactorLevelVariable = this.measurementVariableTransformer
					.transform(standardVariableMap.get(treatmentFactorLevelId), true, VariableType.TREATMENT_FACTOR);
				treatmentFactorLevelVariable.setTreatmentLabel(treatmentFactorLevelVariable.getName());
				treatmentFactorLevelVariable.setValue(treatmentFactorLevelVariable.getName());
				if (treatmentFactorLevelAliasMap.containsKey(treatmentFactorLevelId)) {
					treatmentFactorLevelVariable.setAlias(treatmentFactorLevelAliasMap.get(treatmentFactorLevelId));
				}
				measurementVariables.add(treatmentFactorLevelVariable);

				final Integer treatmentFactorLabelId = treatmentFactorLevelToLabelIdMap.get(treatmentFactorLevelId);
				final MeasurementVariable treatmentFactorLabelVariable = this.measurementVariableTransformer
					.transform(standardVariableMap.get(treatmentFactorLabelId), true, VariableType.TREATMENT_FACTOR);
				treatmentFactorLabelVariable.setTreatmentLabel(treatmentFactorLevelVariable.getName());
				treatmentFactorLabelVariable.setValue(treatmentFactorLevelVariable.getName());
				measurementVariables.add(treatmentFactorLabelVariable);
			}
		}

		return measurementVariables;
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
			treatmentFactors.add(ExperimentalDesignUtil.cleanBVDesignKey(key));
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

	List<ListItem> getInitialTreatNumList(final List<String> treatmentFactors, final Integer initialTreatNum, final String entryNoVarName) {

		final List<ListItem> listItemList = new ArrayList<>();
		for (final String treatmentFactor : treatmentFactors) {
			if (treatmentFactor.equals(entryNoVarName)) {
				listItemList.add(new ListItem(String.valueOf(initialTreatNum)));
			} else {
				listItemList.add(new ListItem("1"));
			}
		}
		return listItemList;

	}

	void setMeasurementVariableTransformer(
		final MeasurementVariableTransformer measurementVariableTransformer) {
		this.measurementVariableTransformer = measurementVariableTransformer;
	}
}
