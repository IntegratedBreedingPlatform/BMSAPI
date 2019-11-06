package org.ibp.api.java.impl.middleware.design.generator;

import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.design.ExperimentDesign;
import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

@Component
public class ExperimentDesignGenerator {

	public static final String NCLATIN_PARAM = "nclatin";
	public static final String NRLATIN_PARAM = "nrlatin";
	public static final String REPLATINGROUPS_PARAM = "replatingroups";
	public static final String COLUMNFACTOR_PARAM = "columnfactor";
	public static final String ROWFACTOR_PARAM = "rowfactor";
	public static final String NCOLUMNS_PARAM = "ncolumns";
	public static final String NROWS_PARAM = "nrows";
	public static final String NBLATIN_PARAM = "nblatin";
	public static final String REPLICATEFACTOR_PARAM = "replicatefactor";
	public static final String TREATMENTFACTOR_PARAM = "treatmentfactor";
	public static final String INITIAL_TREATMENT_NUMBER_PARAM = "initialtreatnum";
	public static final String NREPLICATES_PARAM = "nreplicates";
	public static final String NTREATMENTS_PARAM = "ntreatments";
	public static final String BLOCKSIZE_PARAM = "blocksize";
	public static final String TIMELIMIT_PARAM = "timelimit";
	public static final String LEVELS_PARAM = "levels";
	public static final String TREATMENTFACTORS_PARAM = "treatmentfactors";
	public static final String PLOTFACTOR_PARAM = "plotfactor";
	public static final String INITIAL_PLOT_NUMBER_PARAM = "initialplotnum";
	public static final String BLOCKFACTOR_PARAM = "blockfactor";
	public static final String NBLOCKS_PARAM = "nblocks";
	public static final String OUTPUTFILE_PARAM = "outputfile";
	public static final String SEED_PARAM = "seed";
	public static final String NCONTROLS_PARAM = "ncontrols";
	public static final String NUMBER_TRIALS_PARAM = "numbertrials";
	public static final String NREPEATS_PARAM = "nrepeats";



	private static final int STARTING_ENTRY_NUMBER = 1;

	private final Random random = new Random();

	public MainDesign createRandomizedCompleteBlockDesign(final ExperimentalDesignInput experimentalDesignInput,
		final String blockFactor, final String plotFactor, final String entryNoVarName, final List<String> treatmentFactors,
		final List<String> levels) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, String.valueOf(experimentalDesignInput.getNumberOfBlocks()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(experimentalDesignInput.getStartingPlotNo()), null));

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM, null,
			this.getInitialTreatNumList(treatmentFactors, STARTING_ENTRY_NUMBER, entryNoVarName)));

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM, null,
			this.convertToListItemList(treatmentFactors)));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.LEVELS_PARAM, null, this.convertToListItemList(levels)));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		final ExperimentDesign design = new ExperimentDesign(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getBvDesignName(), paramList);

		return new MainDesign(design);
	}

	public MainDesign createResolvableIncompleteBlockDesign(final ExperimentalDesignInput experimentalDesignInput,
		final Integer nTreatments,
		final String treatmentFactor, final String replicateFactor, final String blockFactor, final String plotFactor) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.BLOCKSIZE_PARAM, String.valueOf(experimentalDesignInput.getBlockSize()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(nTreatments), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NREPLICATES_PARAM, String.valueOf(experimentalDesignInput.getReplicationsCount()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmentNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(experimentalDesignInput.getStartingPlotNo()), null));

		this.addLatinizeParametersForResolvableIncompleteBlockDesign(experimentalDesignInput.getUseLatenized(), paramList,
			String.valueOf(experimentalDesignInput.getNblatin()), experimentalDesignInput.getReplatinGroups());

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		final ExperimentDesign design = new ExperimentDesign(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getBvDesignName(), paramList);

		return new MainDesign(design);
	}

	public MainDesign createResolvableRowColDesign(final ExperimentalDesignInput experimentalDesignInput,
		final Integer nTreatments, final String treatmentFactor, final String replicateFactor, final String rowFactor,
		final String columnFactor, final String plotFactor) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(nTreatments), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NREPLICATES_PARAM, String.valueOf(experimentalDesignInput.getReplicationsCount()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NROWS_PARAM, String.valueOf(experimentalDesignInput.getRowsPerReplications()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NCOLUMNS_PARAM, String.valueOf(experimentalDesignInput.getColsPerReplications()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmentNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.ROWFACTOR_PARAM, rowFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.COLUMNFACTOR_PARAM, columnFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(experimentalDesignInput.getStartingPlotNo()), null));

		this.addLatinizeParametersForResolvableRowAndColumnDesign(experimentalDesignInput.getUseLatenized(), paramList,
			experimentalDesignInput.getReplatinGroups(), experimentalDesignInput.getNrlatin(), experimentalDesignInput.getNclatin());

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		final ExperimentDesign design = new ExperimentDesign(ExperimentDesignType.ROW_COL.getBvDesignName(), paramList);

		return new MainDesign(design);
	}

	public MainDesign createAugmentedRandomizedBlockDesign(
		final ExperimentalDesignInput experimentalDesignInput, final Integer numberOfTreatments,
		final Integer numberOfControls, final String treatmentFactor,
		final String blockFactor,
		final String plotFactor) {

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(numberOfTreatments), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NCONTROLS_PARAM, String.valueOf(numberOfControls), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, String.valueOf(experimentalDesignInput.getNumberOfBlocks()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(experimentalDesignInput.getStartingPlotNo()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		this.addInitialTreatmentNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		final ExperimentDesign design = new ExperimentDesign(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getBvDesignName(), paramList);

		return new MainDesign(design);
	}

	public MainDesign createPRepDesign(
		final ExperimentalDesignInput experimentalDesignInput, final Integer nTreatments, final List<ListItem> nRepeatsListItem,
		final String treatmentFactor, final String blockFactor, final String plotFactor) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExperimentDesignParameter> paramList = new ArrayList<>();
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(nTreatments), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, String.valueOf(experimentalDesignInput.getNumberOfBlocks()), null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NREPEATS_PARAM, null, nRepeatsListItem));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmentNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(experimentalDesignInput.getStartingPlotNo()), null));

		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		final ExperimentDesign design = new ExperimentDesign(ExperimentDesignType.P_REP.getBvDesignName(), paramList);

		return new MainDesign(design);
	}

	public List<ListItem> createReplicationListItemForPRepDesign(
		final List<StudyGermplasmDto> studyGermplasmDtoList, final float replicationPercentage,
		final int replicationNumber) {

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
			randomTestEntryNumbers.add(testEntryNumbers.get(this.random.nextInt(testEntryNumbers.size())));
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

		return replicationListItem;
	}


	String getPlotNumberStringValueOrDefault(final Integer initialPlotNumber) {
		return (initialPlotNumber == null) ? "1" : String.valueOf(initialPlotNumber);
	}

	void addInitialTreatmentNumberIfAvailable(final Integer initialEntryNumber, final List<ExperimentDesignParameter> paramList) {

		if (initialEntryNumber != null) {
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM,
				String.valueOf(initialEntryNumber), null));
		}

	}

	void addLatinizeParametersForResolvableIncompleteBlockDesign(
		final boolean useLatinize, final List<ExperimentDesignParameter> paramList,
		final String nBlatin, final String replatingGroups) {

		if (useLatinize) {
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NBLATIN_PARAM, nBlatin, null));
			// we add the string tokenize replating groups
			// we tokenize the replating groups
			final StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			final List<ListItem> replatingList = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NBLATIN_PARAM, "0", null));
		}

	}

	void addLatinizeParametersForResolvableRowAndColumnDesign(
		final Boolean useLatinize, final List<ExperimentDesignParameter> paramList,
		final String replatingGroups, final Integer nrLatin, final Integer ncLatin) {

		if (useLatinize != null && useLatinize.booleanValue()) {
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NRLATIN_PARAM, String.valueOf(nrLatin), null));
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NCLATIN_PARAM, String.valueOf(ncLatin), null));
			// we tokenize the replating groups
			final StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			final List<ListItem> replatingList = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NRLATIN_PARAM, "0", null));
			paramList.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NCLATIN_PARAM, "0", null));
		}

	}

	List<ListItem> convertToListItemList(final List<String> listString) {

		final List<ListItem> listItemList = new ArrayList<>();
		for (final String value : listString) {
			listItemList.add(new ListItem(value));
		}
		return listItemList;

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

}
