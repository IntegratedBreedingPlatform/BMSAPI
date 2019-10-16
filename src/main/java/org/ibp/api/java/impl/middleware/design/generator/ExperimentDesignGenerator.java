package org.ibp.api.java.impl.middleware.design.generator;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.BVDesignTrialInstance;
import org.ibp.api.domain.design.ExpDesign;
import org.ibp.api.domain.design.ExpDesignParameter;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.runner.DesignRunner;
import org.ibp.api.java.impl.middleware.design.transformer.StandardVariableTransformer;
import org.ibp.api.java.impl.middleware.design.util.ExpDesignUtil;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.stream.Collectors;

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

	public static final String RANDOMIZED_COMPLETE_BLOCK_DESIGN = "RandomizedBlock";
	public static final String RESOLVABLE_INCOMPLETE_BLOCK_DESIGN = "ResolvableIncompleteBlock";
	public static final String RESOLVABLE_ROW_COL_DESIGN = "ResolvableRowColumn";
	public static final String AUGMENTED_RANDOMIZED_BLOCK_DESIGN = "Augmented";
	public static final String P_REP_DESIGN = "Prep";

	public static final int STARTING_ENTRY_NUMBER = 1;

	private static final Logger LOG = LoggerFactory.getLogger(ExperimentDesignGenerator.class);
	private static final List<Integer> EXP_DESIGN_VARIABLE_IDS =
		Arrays.asList(TermId.PLOT_NO.getId(), TermId.REP_NO.getId(), TermId.BLOCK_NO.getId(), TermId.ROW.getId(), TermId.COL.getId());

	@Resource
	private DesignRunner designRunner;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private DatasetService datasetService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private StandardVariableTransformer standardVariableTransformer;

	private final Random random = new Random();

	public MainDesign createRandomizedCompleteBlockDesign(
		final Integer nBlock, final String blockFactor, final String plotFactor,
		final Integer initialPlotNumber, final String entryNoVarName, final List<String> treatmentFactors,
		final List<String> levels, final String outputfile) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, String.valueOf(nBlock), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(initialPlotNumber), null));

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM, null,
			this.getInitialTreatNumList(treatmentFactors, STARTING_ENTRY_NUMBER, entryNoVarName)));

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM, null,
			this.convertToListItemList(treatmentFactors)));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.LEVELS_PARAM, null, this.convertToListItemList(levels)));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createResolvableIncompleteBlockDesign(
		final Integer blockSize, final Integer nTreatments, final Integer nReplicates,
		final String treatmentFactor, final String replicateFactor, final String blockFactor, final String plotFactor,
		final Integer initialPlotNumber, final Integer nBlatin, final String replatingGroups,
		final String outputfile, final boolean useLatinize) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKSIZE_PARAM, String.valueOf(blockSize), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(nTreatments), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NREPLICATES_PARAM, String.valueOf(nReplicates), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmenNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(initialPlotNumber), null));

		this.addLatinizeParametersForResolvableIncompleteBlockDesign(useLatinize, paramList, String.valueOf(nBlatin), replatingGroups);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createResolvableRowColDesign(
		final Integer nTreatments, final Integer nReplicates, final Integer nRows,
		final Integer nColumns, final String treatmentFactor, final String replicateFactor, final String rowFactor,
		final String columnFactor, final String plotFactor, final Integer initialPlotNumber,
		final Integer nrLatin, final Integer ncLatin, final String replatingGroups, final String outputfile, final Boolean useLatinize) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final String plotNumberStrValue = (initialPlotNumber == null) ? "1" : String.valueOf(initialPlotNumber);

		final List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(nTreatments), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NREPLICATES_PARAM, String.valueOf(nReplicates), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NROWS_PARAM, String.valueOf(nRows), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NCOLUMNS_PARAM, String.valueOf(nColumns), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmenNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.ROWFACTOR_PARAM, rowFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.COLUMNFACTOR_PARAM, columnFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM, plotNumberStrValue, null));

		this.addLatinizeParametersForResolvableRowAndColumnDesign(useLatinize, paramList, replatingGroups, nrLatin, ncLatin);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RESOLVABLE_ROW_COL_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createAugmentedRandomizedBlockDesign(
		final Integer numberOfBlocks, final Integer numberOfTreatments,
		final Integer numberOfControls, final Integer startingPlotNumber, final String treatmentFactor,
		final String blockFactor,
		final String plotFactor) {

		final List<ExpDesignParameter> paramList = new ArrayList<>();

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(numberOfTreatments), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NCONTROLS_PARAM, String.valueOf(numberOfControls), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, String.valueOf(numberOfBlocks), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(startingPlotNumber), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		this.addInitialTreatmenNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createPRepDesign(
		final Integer numberOfBlocks, final Integer nTreatments, final List<ListItem> nRepeatsListItem,
		final String treatmentFactor, final String blockFactor, final String plotFactor,
		final Integer initialPlotNumber) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(nTreatments), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, String.valueOf(numberOfBlocks), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NREPEATS_PARAM, null, nRepeatsListItem));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmenNumberIfAvailable(STARTING_ENTRY_NUMBER, paramList);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(initialPlotNumber), null));

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.P_REP_DESIGN, paramList);

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

	public List<ObservationUnitRow> generateExperimentDesignMeasurements(
		final int numberOfTrials, final List<MeasurementVariable> generateDesignVariables,
		final List<StudyGermplasmDto> studyGermplasmDtoList, final MainDesign mainDesign, final String entryNumberIdentifier,
		final Map<String, List<String>> treatmentFactorValues, final Map<Integer, Integer> designExpectedEntriesMap) {

		// Specify number of study instances for BVDesign generation
		mainDesign.getDesign().getParameters()
			.add(this.createExpDesignParameter(NUMBER_TRIALS_PARAM, String.valueOf(numberOfTrials), null));
		BVDesignOutput bvOutput = null;
		try {
			bvOutput = this.designRunner.runBVDesign(mainDesign);
		} catch (final Exception e) {
			ExperimentDesignGenerator.LOG.error(e.getMessage(), e);
			throw new BVDesignException("experiment.design.bv.exe.error.generate.generic.error");
		}

		if (bvOutput == null || !bvOutput.isSuccess()) {
			throw new BVDesignException("experiment.design.generate.generic.error");
		}

		//Converting studyGermplasmDto List to map
		final Map<Integer, StudyGermplasmDto> studyGermplasmDtoMap =
			studyGermplasmDtoList.stream().collect(Collectors.toMap(StudyGermplasmDto::getEntryNumber,
				Function.identity()));
		final List<ObservationUnitRow> rows = new ArrayList<>();
		int trialInstanceNumber = 1;
		for (final BVDesignTrialInstance instance : bvOutput.getTrialInstances()) {
			for (final Map<String, String> row : instance.getRows()) {
				final String entryNoValue = row.get(entryNumberIdentifier);
				final Integer entryNumber = StringUtil.parseInt(entryNoValue, null);
				if (entryNumber == null) {
					throw new BVDesignException("experiment.design.bv.exe.error.output.invalid.error");
				}
				final Optional<StudyGermplasmDto> studyGermplasmDto =
					this.findStudyGermplasmDtoByEntryNumberAndChecks(studyGermplasmDtoMap, entryNumber, designExpectedEntriesMap);

				if (!studyGermplasmDto.isPresent()) {
					throw new BVDesignException("experiment.design.bv.exe.error.output.invalid.error");
				}
				final ObservationUnitRow observationUnitRow =
					this.createObservationUnitRow(generateDesignVariables, studyGermplasmDto.get(), row,
						treatmentFactorValues, trialInstanceNumber);
				rows.add(observationUnitRow);
			}
			trialInstanceNumber++;
		}
		return rows;
	}

	public List<MeasurementVariable> constructMeasurementVariables(final int studyId, final String programUUID,
		final List<Integer> designFactors, final List<Integer> experimentDesignFactors,
		final ExperimentDesignInput experimentDesignInput) {

		// Add the germplasm and environment detail variables from study.
		// This adds the specified trial design factor, experiment design factor and treatment factor variables.

		final int plotDatasetId =
			this.studyDataManager.getDataSetsByType(studyId, DatasetTypeEnum.PLOT_DATA.getId()).get(0).getId();
		final Set<MeasurementVariable> measurementVariables =
			new HashSet<>(this.datasetService.getDatasetMeasurementVariablesByVariableType(plotDatasetId,
				Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId())));

		measurementVariables.addAll(
			this.convertToMeasurementVariables(experimentDesignFactors, VariableType.ENVIRONMENT_DETAIL, experimentDesignInput,
				programUUID));
		measurementVariables.addAll(
			this.convertToMeasurementVariables(designFactors, VariableType.EXPERIMENTAL_DESIGN, experimentDesignInput, programUUID));

		return new ArrayList<>(measurementVariables);
	}

	List<MeasurementVariable> convertToMeasurementVariables(final List<Integer> variableIds, final VariableType variableType,
		final ExperimentDesignInput experimentDesignInput,
		final String programUUID) {
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		final List<StandardVariable> treatmentFactorStandardVariables =
			this.ontologyDataManager.getStandardVariables(variableIds, programUUID);

		for (final StandardVariable standardVariable : treatmentFactorStandardVariables) {
			measurementVariables.add(this.convertToMeasurementVariable(standardVariable, variableType,
				this.getMeasurementValueFromDesignInput(experimentDesignInput, standardVariable.getId())));
		}
		return measurementVariables;
	}

	MeasurementVariable convertToMeasurementVariable(final StandardVariable standardVariable, final VariableType variableType,
		final String value) {
		final MeasurementVariable measurementVariable =
			this.standardVariableTransformer.convert(standardVariable, variableType);
		measurementVariable.setValue(value);
		return measurementVariable;
	}

	ObservationUnitRow createObservationUnitRow(
		final List<MeasurementVariable> measurementVariables, final StudyGermplasmDto studyGermplasmDto,
		final Map<String, String> bvEntryMap, final Map<String, List<String>> treatmentFactorValues, final int trialNo) {

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setTrialInstance(trialNo);
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		final Map<String, ObservationUnitData> environmentObservationUnitDataMap = new HashMap<>();
		ObservationUnitData treatmentLevelData = null;
		ObservationUnitData observationUnitData;

		observationUnitData = ExpDesignUtil.createObservationUnitData(TermId.TRIAL_INSTANCE_FACTOR.getId(), String.valueOf(trialNo));
		observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);

		for (final MeasurementVariable measurementVariable : measurementVariables) {

			final int termId = measurementVariable.getTermId();
			observationUnitData = null;

			if (measurementVariable.getVariableType() == VariableType.ENVIRONMENT_DETAIL
				|| measurementVariable.getVariableType() == VariableType.STUDY_CONDITION) {
				observationUnitData = ExpDesignUtil
					.createObservationUnitData(measurementVariable.getTermId(), String.valueOf(measurementVariable.getValue()));

				environmentObservationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
			} else {
				if (termId == TermId.ENTRY_NO.getId()) {
					final Integer entryNumber = studyGermplasmDto.getEntryNumber();
					observationUnitData = ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), String.valueOf(
						entryNumber));
					observationUnitRow.setEntryNumber(entryNumber);
				} else if (termId == TermId.SOURCE.getId() || termId == TermId.GERMPLASM_SOURCE.getId()) {
					observationUnitData =
						ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(),
							studyGermplasmDto.getSeedSource() != null ? studyGermplasmDto.getSeedSource() : "");
				} else if (termId == TermId.GROUPGID.getId()) {
					observationUnitData = ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(),
						studyGermplasmDto.getGroupId() != null ? studyGermplasmDto.getGroupId().toString() : "");
				} else if (termId == TermId.STOCKID.getId()) {
					observationUnitData =
						ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(),
							studyGermplasmDto.getStockIds() != null ? studyGermplasmDto.getStockIds() : "");
				} else if (termId == TermId.CROSS.getId()) {
					observationUnitData =
						ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), studyGermplasmDto.getCross());
				} else if (termId == TermId.DESIG.getId()) {
					observationUnitData =
						ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), studyGermplasmDto.getDesignation());
				} else if (termId == TermId.GID.getId()) {
					observationUnitData =
						ExpDesignUtil
							.createObservationUnitData(measurementVariable.getTermId(), String.valueOf(studyGermplasmDto.getGermplasmId()));
				} else if (termId == TermId.ENTRY_CODE.getId()) {
					observationUnitData =
						ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), studyGermplasmDto.getEntryCode());
				} else if (EXP_DESIGN_VARIABLE_IDS.contains(termId)) {
					observationUnitData = ExpDesignUtil
						.createObservationUnitData(measurementVariable.getTermId(), bvEntryMap.get(measurementVariable.getName()));
				} else if (termId == TermId.CHECK.getId()) {
					observationUnitData =
						ExpDesignUtil
							.createObservationUnitData(measurementVariable.getTermId(), Integer.toString(studyGermplasmDto.getCheckType()));
				} else if (termId == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					observationUnitData =
						ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), Integer.toString(trialNo));
				} else if (!StringUtils.isEmpty(measurementVariable.getTreatmentLabel())) {
					if (treatmentLevelData == null) {
						observationUnitData = ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(),
							bvEntryMap.get(ExpDesignUtil.cleanBVDesingKey(Integer.toString(measurementVariable.getTermId()))));
						treatmentLevelData = observationUnitData;
					} else {
						final String level = treatmentLevelData.getValue();
						if (NumberUtils.isNumber(level)) {
							final int index = Integer.valueOf(level) - 1;
							if (treatmentFactorValues != null && treatmentFactorValues
								.containsKey(String.valueOf(treatmentLevelData.getVariableId()))) {
								final Object tempObj =
									treatmentFactorValues.get(String.valueOf(treatmentLevelData.getVariableId()))
										.get(index);
								String value = "";
								if (tempObj != null) {
									if (tempObj instanceof String) {
										value = (String) tempObj;
									} else {
										value = Integer.toString((Integer) tempObj);
									}
								}
								if (measurementVariable.getDataTypeId() != null
									&& measurementVariable.getDataTypeId().intValue() == TermId.DATE_VARIABLE.getId()) {
									value = DateUtil.convertToDBDateFormat(measurementVariable.getDataTypeId(), value);
									observationUnitData = ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), value);
								} else if (measurementVariable.getPossibleValues() != null && !measurementVariable.getPossibleValues()
									.isEmpty() && NumberUtils
									.isNumber(value)) {
									observationUnitData = ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), value);
								} else {
									observationUnitData = ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), value);
								}
							}
						}
						treatmentLevelData = null;
					}

				} else {
					// meaning non factor
					observationUnitData = ExpDesignUtil.createObservationUnitData(measurementVariable.getTermId(), "");
				}

				observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
			}

		}
		observationUnitRow.setVariables(observationUnitDataMap);
		observationUnitRow.setEnvironmentVariables(environmentObservationUnitDataMap);
		return observationUnitRow;
	}

	ExpDesignParameter createExpDesignParameter(final String name, final String value, final List<ListItem> items) {

		final ExpDesignParameter designParam = new ExpDesignParameter(name, value);
		if (items != null && !items.isEmpty()) {
			designParam.setListItem(items);
		}
		return designParam;
	}

	Optional<StudyGermplasmDto> findStudyGermplasmDtoByEntryNumberAndChecks(
		final Map<Integer, StudyGermplasmDto> studyGermplasmDtoMap,
		final Integer entryNumber, final Map<Integer, Integer> designExpectedEntriesMap) {

		final Integer resolvedEntryNumber = this.resolveMappedEntryNumber(entryNumber, designExpectedEntriesMap);

		if (studyGermplasmDtoMap.containsKey(resolvedEntryNumber)) {
			return Optional.of(studyGermplasmDtoMap.get(resolvedEntryNumber));
		}

		return Optional.absent();

	}

	Integer resolveMappedEntryNumber(final Integer entryNumber, final Map<Integer, Integer> designExpectedEntriesMap) {

		if (designExpectedEntriesMap.containsKey(entryNumber)) {
			return designExpectedEntriesMap.get(entryNumber);
		}

		return entryNumber;

	}

	String getPlotNumberStringValueOrDefault(final Integer initialPlotNumber) {
		return (initialPlotNumber == null) ? "1" : String.valueOf(initialPlotNumber);
	}

	void addInitialTreatmenNumberIfAvailable(final Integer initialEntryNumber, final List<ExpDesignParameter> paramList) {

		if (initialEntryNumber != null) {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM,
				String.valueOf(initialEntryNumber), null));
		}

	}

	void addLatinizeParametersForResolvableIncompleteBlockDesign(
		final boolean useLatinize, final List<ExpDesignParameter> paramList,
		final String nBlatin, final String replatingGroups) {

		if (useLatinize) {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLATIN_PARAM, nBlatin, null));
			// we add the string tokenize replating groups
			// we tokenize the replating groups
			final StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			final List<ListItem> replatingList = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLATIN_PARAM, "0", null));
		}

	}

	void addLatinizeParametersForResolvableRowAndColumnDesign(
		final Boolean useLatinize, final List<ExpDesignParameter> paramList,
		final String replatingGroups, final Integer nrLatin, final Integer ncLatin) {

		if (useLatinize != null && useLatinize.booleanValue()) {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NRLATIN_PARAM, String.valueOf(nrLatin), null));
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NCLATIN_PARAM, String.valueOf(ncLatin), null));
			// we tokenize the replating groups
			final StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			final List<ListItem> replatingList = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NRLATIN_PARAM, "0", null));
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NCLATIN_PARAM, "0", null));
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

	String getMeasurementValueFromDesignInput(final ExperimentDesignInput experimentDesignInput, final int termId) {

		if (termId == TermId.EXPERIMENT_DESIGN_FACTOR.getId()) {
			if (experimentDesignInput.getDesignType() != null) {
				return String.valueOf(ExperimentDesignType
					.getTermIdByDesignTypeId(experimentDesignInput.getDesignType(), experimentDesignInput.getUseLatenized()));
			}
		} else if (termId == TermId.NUMBER_OF_REPLICATES.getId()) {
			return String.valueOf(experimentDesignInput.getReplicationsCount());
		} else if (termId == TermId.PERCENTAGE_OF_REPLICATION.getId()) {
			return String.valueOf(experimentDesignInput.getReplicationPercentage());
		} else if (termId == TermId.BLOCK_SIZE.getId()) {
			return String.valueOf(experimentDesignInput.getBlockSize());
		} else if (termId == TermId.REPLICATIONS_MAP.getId()) {
			if (experimentDesignInput.getReplicationsArrangement() != null) {
				switch (experimentDesignInput.getReplicationsArrangement()) {
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
			return experimentDesignInput.getReplatinGroups();
		} else if (termId == TermId.NO_OF_CBLKS_LATINIZE.getId()) {
			return String.valueOf(experimentDesignInput.getNblatin());
		} else if (termId == TermId.NO_OF_ROWS_IN_REPS.getId()) {
			return String.valueOf(experimentDesignInput.getRowsPerReplications());
		} else if (termId == TermId.NO_OF_COLS_IN_REPS.getId()) {
			return String.valueOf(experimentDesignInput.getColsPerReplications());
		} else if (termId == TermId.NO_OF_CCOLS_LATINIZE.getId()) {
			return String.valueOf(experimentDesignInput.getNclatin());
		} else if (termId == TermId.NO_OF_CROWS_LATINIZE.getId()) {
			return String.valueOf(experimentDesignInput.getNrlatin());
		} else if (termId == TermId.EXPT_DESIGN_SOURCE.getId()) {
			return experimentDesignInput.getFileName();
		} else if (termId == TermId.NBLKS.getId()) {
			return String.valueOf(experimentDesignInput.getNumberOfBlocks());
		} else if (termId == TermId.CHECK_START.getId()) {
			return String.valueOf(experimentDesignInput.getCheckStartingPosition());
		} else if (termId == TermId.CHECK_INTERVAL.getId()) {
			return String.valueOf(experimentDesignInput.getCheckSpacing());
		} else if (termId == TermId.CHECK_PLAN.getId()) {
			return String.valueOf(experimentDesignInput.getCheckInsertionManner());
		}
		return "";
	}

}
