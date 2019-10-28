package org.ibp.api.java.impl.middleware.design.validator;

import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

@Component
public class ExperimentDesignTypeValidator {

	private static final int MINIMUM_REPLICATION_PERCENTAGE = 0;
	private static final int MAXIMUM_REPLICATION_PERCENTAGE = 100;

	private static final String PLOT_NUMBER_SHOULD_BE_IN_RANGE = "plot.number.should.be.in.range";
	private static final String EXPERIMENT_DESIGN_REPLICATION_COUNT_RCBD_ERROR = "experiment.design.replication.count.rcbd.error";
	private static final String EXPERIMENT_DESIGN_PLOT_NUMBER_SHOULD_NOT_EXCEED = "experiment.design.plot.number.should.not.exceed";
	private static final String EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR = "experiment.design.treatment.factors.error";
	private static final String EXPERIMENT_DESIGN_REPLICATION_COUNT_RESOLVABLE_ERROR =
		"experiment.design.replication.count.resolvable.error";
	private static final String EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_GREATER_THAN_1 =
		"experiment.design.block.size.should.be.a.greater.than.1";
	private static final String EXPERIMENT_DESIGN_BLOCK_LEVEL_SHOULD_BE_GREATER_THAN_ONE =
		"experiment.design.block.level.should.be.greater.than.one";
	private static final String EXPERIMENT_DESIGN_BLOCK_SIZE_NOT_A_FACTOR_OF_TREATMENT_SIZE =
		"experiment.design.block.size.not.a.factor.of.treatment.size";
	private static final String EXPERIMENT_DESIGN_NBLATIN_SHOULD_NOT_BE_GREATER_THAN_BLOCK_LEVEL =
		"experiment.design.nblatin.should.not.be.greater.than.block.level";
	private static final String EXPERIMENT_DESIGN_REPLATING_GROUPS_NOT_EQUAL_TO_REPLICATES =
		"experiment.design.replating.groups.not.equal.to.replicates";
	private static final String EXPERIMENT_DESIGN_RESOLVABLE_INCORRECT_ROW_AND_COL_PRODUCT_TO_GERMPLASM_SIZE =
		"experiment.design.resolvable.incorrect.row.and.col.product.to.germplasm.size";
	private static final String EXPERIMENT_DESIGN_NRLATIN_SHOULD_BE_LESS_THAN_ROWS_PER_REPLICATION =
		"experiment.design.nrlatin.should.be.less.than.rows.per.replication";
	private static final String EXPERIMENT_DESIGN_NCLATIN_SHOULD_BE_LESS_THAN_COLS_PER_REPLICATION =
		"experiment.design.nclatin.should.be.less.than.cols.per.replication";
	private static final String GERMPLASM_LIST_CHECK_REQUIRED_AUGMENTED_DESIGN = "germplasm.list.check.required.augmented.design";
	private static final String EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED =
		"experiment.design.replication.percentage.should.be.between.zero.and.hundred";
	private static final String GERMPLASM_LIST_ALL_ENTRIES_CAN_NOT_BE_CHECKS = "germplasm.list.all.entries.can.not.be.checks";
	private static final String GERMPLASM_LIST_START_INDEX_WHOLE_NUMBER_ERROR = "germplasm.list.start.index.whole.number.error";
	private static final String GERMPLASM_LIST_NUMBER_OF_ROWS_BETWEEN_INSERTION_SHOULD_BE_A_WHOLE_NUMBER =
		"germplasm.list.number.of.rows.between.insertion.should.be.a.whole.number";
	private static final String CHECK_MANNER_OF_INSERTION_INVALID = "check.manner.of.insertion.invalid";
	private static final String GERMPLASM_LIST_STARTING_INDEX_SHOULD_BE_GREATER_THAN_ZERO =
		"germplasm.list.starting.index.should.be.greater.than.zero";
	private static final String GERMPLASM_LIST_START_INDEX_LESS_THAN_GERMPLASM_ERROR =
		"germplasm.list.start.index.less.than.germplasm.error";
	private static final String GERMPLASM_LIST_NUMBER_OF_ROWS_BETWEEN_INSERTION_SHOULD_BE_GREATER_THAN_ZERO =
		"germplasm.list.number.of.rows.between.insertion.should.be.greater.than.zero";
	private static final String GERMPLASM_LIST_SPACING_LESS_THAN_GERMPLASM_ERROR = "germplasm.list.spacing.less.than.germplasm.error";
	private static final String EXPERIMENT_DESIGN_GENERATE_NO_GERMPLASM = "experiment.design.generate.no.germplasm";

	private BindingResult errors;

	/**
	 * Validates the parameters and germplasm entries required for generating randomized block design.
	 *
	 * @param experimentalDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validateRandomizedCompleteBlockDesign(final ExperimentalDesignInput experimentalDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.validateStudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentalDesignInput != null && studyGermplasmDtoList != null) {
			this.validateReplicationCountLimit(experimentalDesignInput, EXPERIMENT_DESIGN_REPLICATION_COUNT_RCBD_ERROR);
			this.validatePlotNumberRange(experimentalDesignInput);
			this.validatePlotNumberShouldNotExceedLimit(experimentalDesignInput, studyGermplasmDtoList.size());
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	/**
	 * Validates the parameters and germplasm entries required for generating resolvable incomplete block design.
	 *
	 * @param experimentalDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validateResolvableIncompleteBlockDesign(final ExperimentalDesignInput experimentalDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.validateStudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentalDesignInput != null && studyGermplasmDtoList != null) {

			this.validateReplicationCountLimit(experimentalDesignInput, EXPERIMENT_DESIGN_REPLICATION_COUNT_RESOLVABLE_ERROR);
			this.validatePlotNumberRange(experimentalDesignInput);
			this.validatePlotNumberShouldNotExceedLimit(experimentalDesignInput, studyGermplasmDtoList.size());
			this.validateNoTreatmentFactors(experimentalDesignInput);

			final Integer blockSize = experimentalDesignInput.getBlockSize() == null? -1 : experimentalDesignInput.getBlockSize();
			final Integer replicationCount = experimentalDesignInput.getReplicationsCount();
			final Integer treatmentSize = studyGermplasmDtoList.size();
			final int blockLevel = treatmentSize / blockSize;

			if (blockSize <= 1) {
				this.errors.reject(EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_GREATER_THAN_1);
			} else if (blockLevel == 1) {
				this.errors.reject(EXPERIMENT_DESIGN_BLOCK_LEVEL_SHOULD_BE_GREATER_THAN_ONE);
			} else if (treatmentSize % blockSize != 0) {
				this.errors.reject(EXPERIMENT_DESIGN_BLOCK_SIZE_NOT_A_FACTOR_OF_TREATMENT_SIZE);
			} else if (experimentalDesignInput.getUseLatenized() != null && experimentalDesignInput.getUseLatenized()) {
				// we add validation for latinize
				final Integer nbLatin =
					experimentalDesignInput.getNblatin() != null ? experimentalDesignInput.getNblatin() : 0;
				/*
				 * The value set for "nblatin" xml parameter cannot be value higher than or equal the block level value. To get the
				 * block levels, we just need to divide the "ntreatments" value by the "blocksize" value. This means the BVDesign
				 * tool works to any value you specify in the "nblatin" parameter as long as it does not exceed the computed block
				 * levels value. As mentioned in the requirements, an "nblatin" parameter with value 0 means there is no
				 * latinization that will take place. The sum of the values set for "replatingroups" should always be equal to the
				 * "nreplicates" value specified by the plant breeder.
				 */

				// nbLatin should be less than the block level
				if (nbLatin >= blockLevel) {
					this.errors.reject(EXPERIMENT_DESIGN_NBLATIN_SHOULD_NOT_BE_GREATER_THAN_BLOCK_LEVEL);
				} else if (experimentalDesignInput.getReplicationsArrangement() != null
					&& experimentalDesignInput.getReplicationsArrangement() == 3) {
					// meaning adjacent
					final StringTokenizer tokenizer = new StringTokenizer(experimentalDesignInput.getReplatinGroups(), ",");
					int totalReplatingGroup = 0;

					while (tokenizer.hasMoreTokens()) {
						totalReplatingGroup += Integer.parseInt(tokenizer.nextToken());
					}
					if (totalReplatingGroup != replicationCount) {
						this.errors.reject(EXPERIMENT_DESIGN_REPLATING_GROUPS_NOT_EQUAL_TO_REPLICATES);
					}
				}
			}
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	/**
	 * Validates the parameters and germplasm entries required for generating resolvable row column design.
	 *
	 * @param experimentalDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validateResolvableRowColumnDesign(final ExperimentalDesignInput experimentalDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.validateStudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentalDesignInput != null && studyGermplasmDtoList != null) {

			this.validatePlotNumberRange(experimentalDesignInput);
			this.validateReplicationCountLimit(experimentalDesignInput, EXPERIMENT_DESIGN_REPLICATION_COUNT_RESOLVABLE_ERROR);
			this.validatePlotNumberShouldNotExceedLimit(experimentalDesignInput, studyGermplasmDtoList.size());
			this.validateNoTreatmentFactors(experimentalDesignInput);
			final int size = studyGermplasmDtoList.size();

			final Integer rowsPerReplication = experimentalDesignInput.getRowsPerReplications() == null ? 0 : experimentalDesignInput.getRowsPerReplications();
			final Integer colsPerReplication = experimentalDesignInput.getColsPerReplications() == null ? 0 : experimentalDesignInput.getColsPerReplications();
			final Integer replicationCount = experimentalDesignInput.getReplicationsCount();

			if (size != (rowsPerReplication * colsPerReplication)) {
				this.errors.reject(EXPERIMENT_DESIGN_RESOLVABLE_INCORRECT_ROW_AND_COL_PRODUCT_TO_GERMPLASM_SIZE);
			} else if (experimentalDesignInput.getUseLatenized() != null && experimentalDesignInput.getUseLatenized()) {
				// we add validation for latinize
				final Integer nrLatin = experimentalDesignInput.getNrlatin();
				final Integer ncLatin = experimentalDesignInput.getNclatin();
				/*
				 * "nrows" and "ncolumns" are indeed the factors of the "ntreatments" value. Equation: nrows x ncolumns =
				 * ntreatments. "nrlatin" parameter value should be a positive integer less than the "nrows" value set "nclatin"
				 * parameter value should be a positive integer less than the "ncolumns" value set The sum of the values set for
				 * "replatingroups" should always be equal to the "nreplicates" value specified by the plant breeder. nrlatin
				 * somehow cannot exceed the nreplicates value specified. A technical error is thrown with this unclear message:
				 * "Error from CycDesigN: output parameters 13, 0, 0, 0." This might be a possible bug.
				 */
				// nrlatin and nclatin validation
				if (nrLatin >= rowsPerReplication) {
					this.errors.reject(EXPERIMENT_DESIGN_NRLATIN_SHOULD_BE_LESS_THAN_ROWS_PER_REPLICATION);
				} else if (ncLatin >= colsPerReplication) {
					this.errors.reject(EXPERIMENT_DESIGN_NCLATIN_SHOULD_BE_LESS_THAN_COLS_PER_REPLICATION);
				} else if (experimentalDesignInput.getReplicationsArrangement() != null
					&& experimentalDesignInput.getReplicationsArrangement().intValue() == 3) {
					// meaning adjacent
					final StringTokenizer tokenizer = new StringTokenizer(experimentalDesignInput.getReplatinGroups(), ",");
					int totalReplatingGroup = 0;

					while (tokenizer.hasMoreTokens()) {
						totalReplatingGroup += Integer.parseInt(tokenizer.nextToken());
					}
					if (totalReplatingGroup != replicationCount) {
						this.errors.reject(EXPERIMENT_DESIGN_REPLATING_GROUPS_NOT_EQUAL_TO_REPLICATES);
					}
				}
			}
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	/**
	 * Validates the parameters and germplasm entries required for generating P-rep design.
	 *
	 * @param experimentalDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validatePrepDesign(final ExperimentalDesignInput experimentalDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.validateStudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentalDesignInput != null && studyGermplasmDtoList != null) {

			if (experimentalDesignInput.getReplicationPercentage() == null
				|| experimentalDesignInput.getReplicationPercentage() < MINIMUM_REPLICATION_PERCENTAGE
				|| experimentalDesignInput.getReplicationPercentage() > MAXIMUM_REPLICATION_PERCENTAGE) {
				this.errors.reject(EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED);
			}

			this.validatePlotNumberRange(experimentalDesignInput);
			this.validateNoTreatmentFactors(experimentalDesignInput);
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	/**
	 * Validates the parameters and germplasm entries required for generating augmented design.
	 *
	 * @param experimentalDesignInput
	 * @param studyGermplasmDtoList
	 * @);
	 */
	public void validateAugmentedDesign(final ExperimentalDesignInput experimentalDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.validateStudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentalDesignInput != null && studyGermplasmDtoList != null) {

			final int treatmentSize = studyGermplasmDtoList.size();

			this.validateIfCheckEntriesExistInstudyGermplasmDtoList(studyGermplasmDtoList);
			this.validateStartingPlotNo(experimentalDesignInput, treatmentSize);
			this.validateNoTreatmentFactors(experimentalDesignInput);

		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	/**
	 * Validates the parameters and germplasm entries required for generating entry list order design.
	 *
	 * @param experimentalDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validateEntryListOrderDesign(final ExperimentalDesignInput experimentalDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (experimentalDesignInput != null && studyGermplasmDtoList != null) {

			final List<StudyGermplasmDto> checkList = new LinkedList<>();

			final List<StudyGermplasmDto> testEntryList = new LinkedList<>();

			this.loadChecksAndTestEntries(studyGermplasmDtoList, checkList, testEntryList);

			if (testEntryList.isEmpty()) {
				this.errors.reject(GERMPLASM_LIST_ALL_ENTRIES_CAN_NOT_BE_CHECKS);
			}

			this.validateNoTreatmentFactors(experimentalDesignInput);

			if (!checkList.isEmpty()) {
				final Integer checkStartingPosition = experimentalDesignInput.getCheckStartingPosition();
				final Integer checkSpacing = experimentalDesignInput.getCheckSpacing();

				// Validate check starting position
				if (checkStartingPosition == null) {
					this.errors.reject(GERMPLASM_LIST_START_INDEX_WHOLE_NUMBER_ERROR);
				} else if (checkStartingPosition < 1) {
					this.errors.reject(GERMPLASM_LIST_STARTING_INDEX_SHOULD_BE_GREATER_THAN_ZERO);
				} else if (checkStartingPosition > testEntryList.size()) {
					this.errors.reject(GERMPLASM_LIST_START_INDEX_LESS_THAN_GERMPLASM_ERROR);
				}

				// Validate check Spacing
				if (checkSpacing == null ) {
					this.errors.reject(GERMPLASM_LIST_NUMBER_OF_ROWS_BETWEEN_INSERTION_SHOULD_BE_A_WHOLE_NUMBER);
				} else if (checkSpacing < 1) {
					this.errors.reject(GERMPLASM_LIST_NUMBER_OF_ROWS_BETWEEN_INSERTION_SHOULD_BE_GREATER_THAN_ZERO);
				} else if (checkSpacing > testEntryList.size()) {
					this.errors.reject(GERMPLASM_LIST_SPACING_LESS_THAN_GERMPLASM_ERROR);
				}

				if (experimentalDesignInput.getCheckInsertionManner() == null) {
					this.errors.reject(CHECK_MANNER_OF_INSERTION_INVALID);
				}

				if (studyGermplasmDtoList.size() - checkList.size() == 0) {
					this.errors.reject(GERMPLASM_LIST_ALL_ENTRIES_CAN_NOT_BE_CHECKS);
				}
			}
		}
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	void validateNoTreatmentFactors(final ExperimentalDesignInput experimentalDesignInput) {
		if (!CollectionUtils.isEmpty(experimentalDesignInput.getTreatmentFactorsData())) {
			this.errors.reject(EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR);
		}
	}

	void validateIfCheckEntriesExistInstudyGermplasmDtoList(final List<StudyGermplasmDto> studyGermplasmDtoList) {

		final Integer checkEntryType = Integer.valueOf(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
			if (checkEntryType.equals(studyGermplasmDto.getCheckType())) {
				return;
			}
		}

		this.errors.reject(GERMPLASM_LIST_CHECK_REQUIRED_AUGMENTED_DESIGN);

	}

	void validateStartingPlotNo(final ExperimentalDesignInput experimentalDesignInput, final int treatmentSize) {

		final Integer startingPlotNo = experimentalDesignInput.getStartingPlotNo();

		if (startingPlotNo != null) {
			final int plotNumber = startingPlotNo;
			if (plotNumber != 0 && ((treatmentSize + plotNumber - 1) <= ExperimentDesignTypeService.MAX_PLOT_NO)) {
				return;
			}
		}

		this.errors.reject(PLOT_NUMBER_SHOULD_BE_IN_RANGE);

	}


	private void loadChecksAndTestEntries(final List<StudyGermplasmDto> studyGermplasmDtoList, final List<StudyGermplasmDto> checkList,
		final List<StudyGermplasmDto> testEntryList) {

		final Integer testEntryType = Integer.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());
		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
			if (testEntryType.equals(studyGermplasmDto.getCheckType())) {
				testEntryList.add(studyGermplasmDto);
			} else {
				checkList.add(studyGermplasmDto);
			}
		}
	}


	void validatePlotNumberRange(final ExperimentalDesignInput experimentalDesignInput) {
		final Integer plotNumber = experimentalDesignInput.getStartingPlotNo();
		if (plotNumber == null || Objects.equals(plotNumber, 0)) {
			this.errors.reject(PLOT_NUMBER_SHOULD_BE_IN_RANGE);
		}
	}

	void validatePlotNumberShouldNotExceedLimit(final ExperimentalDesignInput experimentalDesignInput,
		final int germplasmCount) {
		final Integer plotNumber = experimentalDesignInput.getStartingPlotNo() == null? 1 : experimentalDesignInput.getStartingPlotNo();
		final Integer replicationsCount = experimentalDesignInput.getReplicationsCount() == null? 1 : experimentalDesignInput.getReplicationsCount();
		final Integer maxPlot = (germplasmCount * replicationsCount) + plotNumber - 1;
		if (plotNumber != null && maxPlot > ExperimentDesignTypeService.MAX_PLOT_NO) {
			this.errors.reject(EXPERIMENT_DESIGN_PLOT_NUMBER_SHOULD_NOT_EXCEED, new Object[] {maxPlot}, "");
		}
	}

	void validateReplicationCountLimit(final ExperimentalDesignInput experimentalDesignInput, final String errorCode) {
		final Integer replicationCount = experimentalDesignInput.getReplicationsCount();

		if (replicationCount == null || replicationCount <= 0 || replicationCount >= 13) {
			this.errors.reject(errorCode);
		}
	}

	private void validateStudyGermplasmDtoList(final List<StudyGermplasmDto> studyGermplasmDtoList) {
		if (studyGermplasmDtoList == null || studyGermplasmDtoList.isEmpty()) {
			this.errors.reject(EXPERIMENT_DESIGN_GENERATE_NO_GERMPLASM);
		}
	}

	void setErrors(final BindingResult errors) {
		this.errors = errors;
	}
}
