package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignProcessor;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
import org.ibp.api.java.impl.middleware.design.generator.PRepDesignGenerator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PRepDesignTypeServiceImpl implements ExperimentalDesignTypeService {

	protected static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.BLOCK_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
			TermId.PERCENTAGE_OF_REPLICATION.getId());

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private PRepDesignGenerator experimentDesignGenerator;

	@Resource
	private ExperimentalDesignProcessor experimentalDesignProcessor;

	@Resource
	private MeasurementVariableGenerator measurementVariableGenerator;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID, final List<StudyGermplasmDto> studyGermplasmDtoList) {

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		// Generate experiment design parameters input to design runner
		final Map<BreedingViewDesignParameter, List<ListItem>> listItems =
			this.createReplicationListItems(studyGermplasmDtoList, experimentalDesignInput);
		final MainDesign mainDesign = this.experimentDesignGenerator
			.generate(experimentalDesignInput, this.getBreedingViewVariablesMap(standardVariablesMap), studyGermplasmDtoList.size(), null,
				listItems);

		// Generate observation unit rows
		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final List<MeasurementVariable> measurementVariables = this.getMeasurementVariables(studyId, experimentalDesignInput, programUUID);
		return this.experimentalDesignProcessor
			.generateObservationUnitRows(experimentalDesignInput.getTrialInstancesForDesignGeneration(), measurementVariables, studyGermplasmDtoList, mainDesign,
				entryNumberName, null,
				new HashMap<>());
	}

	private Map<BreedingViewVariableParameter, String> getBreedingViewVariablesMap(final Map<Integer, StandardVariable> standardVariablesMap) {
		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final String blockNumberName = standardVariablesMap.get(TermId.BLOCK_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();

		final Map<BreedingViewVariableParameter, String> map = new HashMap<>();
		map.put(BreedingViewVariableParameter.ENTRY, entryNumberName);
		map.put(BreedingViewVariableParameter.BLOCK, blockNumberName);
		map.put(BreedingViewVariableParameter.PLOT, plotNumberName);
		return map;
	}

	@Override
	public Boolean requiresLicenseCheck() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.P_REP.getId();
	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID) {
		return this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, programUUID, DESIGN_FACTOR_VARIABLES, EXPERIMENT_DESIGN_VARIABLES,
				experimentalDesignInput);
	}

	Map<BreedingViewDesignParameter, List<ListItem>> createReplicationListItems(
		final List<StudyGermplasmDto> studyGermplasmDtoList, final ExperimentalDesignInput experimentalDesignInput) {

		final float replicationPercentage = experimentalDesignInput.getReplicationPercentage();
		final int replicationNumber = experimentalDesignInput.getReplicationsCount();
		// Count how many test entries we have in the studyGermplasmDto list.
		int testEntryCount = 0;

		// Determine which of the studyGermplasmDto entries are test entries
		final List<Integer> testEntryNumbers = new ArrayList<>();

		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
			if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() == studyGermplasmDto.getCheckType()) {
				testEntryCount++;
				testEntryNumbers.add(studyGermplasmDto.getEntryNumber());
			}
		}

		// Compute how may test entries we can replicate based on replicationPercentage (% of test entries to replicate)
		final float noOfTestEntriesToReplicate = Math.round((float) testEntryCount * (replicationPercentage / 100));
		// Pick any random test entries to replicate
		final Set<Integer> randomTestEntryNumbers = new HashSet<>();
		while (randomTestEntryNumbers.size() < noOfTestEntriesToReplicate) {
			randomTestEntryNumbers.add(testEntryNumbers.get(new Random().nextInt(testEntryNumbers.size())));
		}

		final List<ListItem> replicationListItem = new LinkedList<>();
		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
			if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() != studyGermplasmDto.getCheckType()) {
				// All Check Entries in the list should be replicated
				replicationListItem.add(new ListItem(String.valueOf(replicationNumber)));
			} else if (randomTestEntryNumbers.contains(studyGermplasmDto.getEntryNumber())) {
				// Randomized Test Entries should be replicated
				replicationListItem.add(new ListItem(String.valueOf(replicationNumber)));
			} else {
				// Default replication number is 1
				replicationListItem.add(new ListItem(String.valueOf(1)));
			}
		}

		return Collections.singletonMap(BreedingViewDesignParameter.NREPEATS, replicationListItem);
	}
}
