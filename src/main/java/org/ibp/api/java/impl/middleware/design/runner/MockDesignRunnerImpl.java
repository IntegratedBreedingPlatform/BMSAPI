package org.ibp.api.java.impl.middleware.design.runner;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.ExperimentDesign;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.runner.DesignRunner;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnProperty(
	value = "design.runner",
	havingValue = "org.ibp.api.java.impl.middleware.design.runner.MockDesignRunnerImpl")
public class MockDesignRunnerImpl implements DesignRunner {

	public static final String ENTRY_NO = "ENTRY_NO";
	public static final String TRIAL = "TRIAL";
	public static final String PLOT_NO = "PLOT_NO";
	public static final String BLOCK_NO = "BLOCK_NO";
	public static final String REP_NO = "REP_NO";
	public static final String ROW = "ROW";
	public static final String COL = "COL";

	@Override
	public BVDesignOutput runBVDesign(final MainDesign design) {

		final ExperimentDesign experimentDesign = design.getDesign();

		final int numberOfTreatments = this.getNumberOfTreatments(design);
		final int replications = this.getNumberOfReplications(design);
		final String numberTrialsParam = experimentDesign.getParameterValue(BreedingViewDesignParameter.NUMBER_TRIALS.getParameterName());
		final int numberTrials = StringUtils.isNumeric(numberTrialsParam) ? Integer.valueOf(numberTrialsParam) : 1;

		final String initEntryNoParam =
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName());
		int startingEntryNo = StringUtils.isNumeric(initEntryNoParam) ? Integer.valueOf(initEntryNoParam) : 1;

		final String blockSizeParam = experimentDesign.getParameterValue(BreedingViewDesignParameter.NBLOCKS.getParameterName());
		final int blockSize = StringUtils.isNumeric(blockSizeParam) ? Integer.valueOf(blockSizeParam) : 1;

		final List<Integer> entryNumbers = new ArrayList<>();
		for (int i = 1; i <= numberOfTreatments; i++) {
			entryNumbers.add(startingEntryNo++);
		}
		final String initPlotNoParam =
			experimentDesign.getParameterValue(BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName());

		final List<List<String>> tfValuesListForCSV = this.getTreatmentFactorValuesCombinations(experimentDesign);
		final List<String[]> csvLines = new ArrayList<>();

		if (ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getBvDesignName().equals(experimentDesign.getName())) {
			csvLines
				.addAll(this.createDesignDataForRandomizedBlockDesign(design, numberTrials, initPlotNoParam, replications, entryNumbers,
					tfValuesListForCSV));
		} else if (ExperimentDesignType.ROW_COL.getBvDesignName().equals(experimentDesign.getName())) {
			csvLines.addAll(
				this.createDesignDataForResolvableRowColumnDesign(design, numberTrials, initPlotNoParam, replications, entryNumbers));
		} else if (ExperimentDesignType.P_REP.getBvDesignName().equals(experimentDesign.getName())) {
			csvLines.addAll(this.createDesignDataForPRepDesign(design, numberTrials, blockSize, initPlotNoParam, entryNumbers));
		} else {
			// Default data
			csvLines.addAll(this.createDesignData(numberTrials, initPlotNoParam, replications, entryNumbers));
		}

		final BVDesignOutput output = new BVDesignOutput(0);
		output.setResults(csvLines);
		return output;
	}

	protected List<String[]> createDesignData(
		final int numberTrials, final String initPlotNoParam, final int replications,
		final List<Integer> entryNumbers) {
		final List<String[]> csvLines = new ArrayList<>();
		// Add header
		csvLines.add(new String[] {TRIAL, PLOT_NO, REP_NO, ENTRY_NO});

		for (int instance = 1; instance <= numberTrials; instance++) {
			int startingPlotNo = StringUtils.isNumeric(initPlotNoParam) ? Integer.valueOf(initPlotNoParam) : 1;
			for (int rep = 1; rep <= replications; rep++) {
				// Randomize entry number arrangements per replication
				Collections.shuffle(entryNumbers);
				for (int j = 0; j < entryNumbers.size(); j++) {
					final List<String> csvLine = new ArrayList<>();
					csvLine.add(String.valueOf(instance));
					csvLine.add(String.valueOf(startingPlotNo++));
					csvLine.add(String.valueOf(rep));
					csvLine.add(entryNumbers.get(j).toString());
					final String[] csvLineAsArray = new String[csvLine.size()];
					csvLine.toArray(csvLineAsArray);
					csvLines.add(csvLineAsArray);
				}
			}
		}

		return csvLines;
	}

	protected List<String[]> createDesignDataForRandomizedBlockDesign(
		final MainDesign design, final int numberTrials, final String initPlotNoParam, final int replications,
		final List<Integer> entryNumbers, final List<List<String>> tfValuesList) {
		final List<String[]> csvLines = new ArrayList<>();
		final ExperimentDesign experimentDesign = design.getDesign();
		// Add header
		final List<String> constants = Arrays.asList(TRIAL, PLOT_NO, REP_NO);
		final List<String> headers = new ArrayList<>(constants);
		final List<ListItem> treatmentFactorsList =
			experimentDesign.getParameterList(BreedingViewDesignParameter.TREATMENTFACTORS.getParameterName());
		for (final ListItem item : treatmentFactorsList) {
			headers.add(item.getValue());
		}
		final String[] headersAsArray = new String[headers.size()];
		headers.toArray(headersAsArray);
		csvLines.add(headersAsArray);

		for (int instance = 1; instance <= numberTrials; instance++) {
			int startingPlotNo = StringUtils.isNumeric(initPlotNoParam) ? Integer.valueOf(initPlotNoParam) : 1;
			for (int rep = 1; rep <= replications; rep++) {
				// Randomize entry number arrangements per replication
				Collections.shuffle(entryNumbers);
				for (int j = 0; j < entryNumbers.size(); j++) {
					final List<String> csvLine = new ArrayList<>();
					csvLine.add(String.valueOf(instance));

					if (treatmentFactorsList.size() > 1) {
						//Randomize the treatment factor values combinations
						Collections.shuffle(tfValuesList);
						for (final List<String> tfValues : tfValuesList) {
							final List<String> tfCsvLine = new ArrayList<>(csvLine);
							tfCsvLine.add(String.valueOf(startingPlotNo++));
							tfCsvLine.add(String.valueOf(rep));
							tfCsvLine.addAll(tfValues);
							tfCsvLine.add(entryNumbers.get(j).toString());
							final String[] csvLineAsArray = new String[tfCsvLine.size()];
							tfCsvLine.toArray(csvLineAsArray);
							csvLines.add(csvLineAsArray);
						}
					} else {
						csvLine.add(String.valueOf(startingPlotNo++));
						csvLine.add(String.valueOf(rep));
						csvLine.add(entryNumbers.get(j).toString());
						final String[] csvLineAsArray = new String[csvLine.size()];
						csvLine.toArray(csvLineAsArray);
						csvLines.add(csvLineAsArray);
					}
				}
			}
		}

		return csvLines;
	}

	protected List<String[]> createDesignDataForResolvableRowColumnDesign(
		final MainDesign design, final int numberTrials, final String initPlotNoParam, final int replications,
		final List<Integer> entryNumbers) {
		final List<String[]> csvLines = new ArrayList<>();
		final ExperimentDesign experimentDesign = design.getDesign();

		final List<Pair<Integer, Integer>> rowColTuples = new ArrayList<>();
		final int rows = Integer.parseInt(experimentDesign.getParameterValue(BreedingViewDesignParameter.NROWS.getParameterName()));
		final int cols = Integer.parseInt(experimentDesign.getParameterValue(BreedingViewDesignParameter.NCOLUMNS.getParameterName()));

		for (int r = 1; r <= rows; r++) {
			for (int c = 1; c <= cols; c++) {
				rowColTuples.add(new ImmutablePair<>(r, c));
			}
		}

		// Create header
		csvLines.add(new String[] {TRIAL, PLOT_NO, REP_NO, ENTRY_NO, ROW, COL});

		for (int instance = 1; instance <= numberTrials; instance++) {
			int startingPlotNo = StringUtils.isNumeric(initPlotNoParam) ? Integer.valueOf(initPlotNoParam) : 1;
			for (int rep = 1; rep <= replications; rep++) {
				int rowColTuplesCounter = 0;
				// Randomize entry number arrangements per replication
				Collections.shuffle(entryNumbers);
				for (int j = 0; j < entryNumbers.size(); j++) {
					final List<String> csvLine = new ArrayList<>();
					csvLine.add(String.valueOf(instance));
					csvLine.add(String.valueOf(startingPlotNo++));
					csvLine.add(String.valueOf(rep));
					csvLine.add(entryNumbers.get(j).toString());
					final Pair<Integer, Integer> rowColTuple = rowColTuples.get(rowColTuplesCounter);
					csvLine.add(String.valueOf(rowColTuple.getLeft()));
					csvLine.add(String.valueOf(rowColTuple.getRight()));
					rowColTuplesCounter++;
					final String[] csvLineAsArray = new String[csvLine.size()];
					csvLine.toArray(csvLineAsArray);
					csvLines.add(csvLineAsArray);
				}
			}
		}
		return csvLines;
	}

	protected List<String[]> createDesignDataForPRepDesign(
		final MainDesign design,
		final int numberTrials, final int blockSize, final String initPlotNoParam,
		final List<Integer> entryNumbers) {
		final List<String[]> csvLines = new ArrayList<>();

		// Add header
		csvLines.add(new String[] {TRIAL, PLOT_NO, BLOCK_NO, ENTRY_NO});

		final List<ListItem> nRepeats = design.getDesign().getParameterList(BreedingViewDesignParameter.NREPEATS.getParameterName());

		for (int instance = 1; instance <= numberTrials; instance++) {
			int startingPlotNo = StringUtils.isNumeric(initPlotNoParam) ? Integer.valueOf(initPlotNoParam) : 1;

			final List<Integer> replicatedEntryNumbers = new ArrayList<>();
			int entryIndex = 0;
			for (final ListItem listItem : nRepeats) {
				final int replicates = Integer.parseInt(listItem.getValue());
				for (int i = 0; i < replicates; i++) {
					replicatedEntryNumbers.add(entryNumbers.get(entryIndex));
				}
				entryIndex++;
			}

			final int noOfEntriesPerBlock = replicatedEntryNumbers.size() / blockSize;
			int blockCounter = 1;
			int block = 1;

			Collections.shuffle(replicatedEntryNumbers);

			for (int j = 0; j < replicatedEntryNumbers.size(); j++) {
				final List<String> csvLine = new ArrayList<>();
				csvLine.add(String.valueOf(instance));
				csvLine.add(String.valueOf(startingPlotNo++));
				csvLine.add(String.valueOf(block));
				csvLine.add(replicatedEntryNumbers.get(j).toString());
				final String[] csvLineAsArray = new String[csvLine.size()];
				csvLine.toArray(csvLineAsArray);
				csvLines.add(csvLineAsArray);

				if (blockCounter == noOfEntriesPerBlock) {
					block++;
					blockCounter = 1;
				} else {
					blockCounter++;
				}
			}

		}

		return csvLines;
	}

	public List<List<String>> getTreatmentFactorValuesCombinations(final ExperimentDesign experimentDesign) {
		final List<ListItem> levelList = experimentDesign.getParameterList(BreedingViewDesignParameter.LEVELS.getParameterName());
		final List<List<String>> tfValuesListForCSV = new ArrayList<>();
		if (ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getBvDesignName().equals(experimentDesign.getName()) && levelList.size() != 1) {
			//Create the lists of treatment factor values
			final List<List<String>> tfValuesList = new ArrayList<>();
			for (int tfIndex = 0; tfIndex < levelList.size() - 1; tfIndex++) {
				final List<String> tfValues = new ArrayList<>();
				for (int levelIndex = 1; levelIndex <= Integer.valueOf(levelList.get(tfIndex).getValue()); levelIndex++) {
					tfValues.add(String.valueOf(levelIndex));
				}
				tfValuesList.add(tfValues);
			}

			if (CollectionUtils.isEmpty(tfValuesList)) {
				return tfValuesListForCSV;
			} else {
				this.getCombinations(tfValuesList, tfValuesListForCSV, 0, new ArrayList<String>());
				return tfValuesListForCSV;
			}
		}
		return tfValuesListForCSV;
	}

	private void getCombinations(
		final List<List<String>> tfValuesList, final List<List<String>> tfValuesListForCsv, final int tfValuesSize,
		final List<String> tfValues) {
		// if number of elements in tfValues, final reached, add and return
		if (tfValuesSize == tfValuesList.size()) {
			tfValuesListForCsv.add(tfValues);
			return;
		}

		// iterate from current list and copy current element N times, one for each element
		final List<String> currentCollection = tfValuesList.get(tfValuesSize);
		for (final String element : currentCollection) {
			final List<String> copy = new ArrayList<>(tfValues);
			copy.add(element);
			this.getCombinations(tfValuesList, tfValuesListForCsv, tfValuesSize + 1, copy);
		}
	}

	private int getNumberOfTreatments(final MainDesign design) {
		final ExperimentDesign experimentDesign = design.getDesign();
		if (ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getBvDesignName().equals(experimentDesign.getName())) {
			final List<ListItem> levelList = experimentDesign.getParameterList(BreedingViewDesignParameter.LEVELS.getParameterName());
			return Integer.parseInt(levelList.get(levelList.size() - 1).getValue());
		} else {
			return Integer.parseInt(experimentDesign.getParameterValue(BreedingViewDesignParameter.NTREATMENTS.getParameterName()));
		}
	}

	private int getNumberOfReplications(final MainDesign design) {
		final ExperimentDesign experimentDesign = design.getDesign();
		if (ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getBvDesignName().equals(experimentDesign.getName())) {
			return Integer.parseInt(experimentDesign.getParameterValue(BreedingViewDesignParameter.NBLOCKS.getParameterName()));
		} else if (ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getBvDesignName().equals(experimentDesign.getName())) {
			return Integer.parseInt(experimentDesign.getParameterValue(BreedingViewDesignParameter.NBLOCKS.getParameterName()));
		} else if (ExperimentDesignType.P_REP.getBvDesignName().equals(experimentDesign.getName())) {
			// If P-rep design, the replicates number is specified in nrepeats list items.
			return 0;
		} else {
			return Integer.parseInt(experimentDesign.getParameterValue(BreedingViewDesignParameter.NREPLICATES.getParameterName()));
		}
	}

}
