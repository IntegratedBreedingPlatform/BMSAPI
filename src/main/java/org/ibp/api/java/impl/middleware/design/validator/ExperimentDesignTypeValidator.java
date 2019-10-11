package org.ibp.api.java.impl.middleware.design.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.BVDesignLicenseInfo;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.BVLicenseParseException;
import org.ibp.api.java.design.DesignLicenseUtil;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

@Component
public class ExperimentDesignTypeValidator {

	private static final int MINIMUM_REPLICATION_PERCENTAGE = 0;
	private static final int MAXIMUM_REPLICATION_PERCENTAGE = 100;

	public static final String PLOT_NUMBER_SHOULD_BE_IN_RANGE = "plot.number.should.be.in.range";
	public static final String ENTRY_NUMBER_SHOULD_BE_IN_RANGE = "entry.number.should.be.in.range";
	public static final String EXPERIMENT_DESIGN_REPLICATION_COUNT_SHOULD_BE_A_NUMBER =
		"experiment.design.replication.count.should.be.a.number";
	public static final String EXPERIMENT_DESIGN_REPLICATION_COUNT_RCBD_ERROR = "experiment.design.replication.count.rcbd.error";
	public static final String EXPERIMENT_DESIGN_ENTRY_NUMBER_SHOULD_NOT_EXCEED = "experiment.design.entry.number.should.not.exceed";
	public static final String EXPERIMENT_DESIGN_PLOT_NUMBER_SHOULD_NOT_EXCEED = "experiment.design.plot.number.should.not.exceed";
	public static final String EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_NUMBER = "experiment.design.block.size.should.be.a.number";
	public static final String EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR = "experiment.design.treatment.factors.error";
	public static final String EXPERIMENT_DESIGN_REPLICATION_COUNT_RESOLVABLE_ERROR =
		"experiment.design.replication.count.resolvable.error";
	public static final String EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_GREATER_THAN_1 =
		"experiment.design.block.size.should.be.a.greater.than.1";
	public static final String EXPERIMENT_DESIGN_BLOCK_LEVEL_SHOULD_BE_GREATER_THAN_ONE =
		"experiment.design.block.level.should.be.greater.than.one";
	public static final String EXPERIMENT_DESIGN_BLOCK_SIZE_NOT_A_FACTOR_OF_TREATMENT_SIZE =
		"experiment.design.block.size.not.a.factor.of.treatment.size";
	public static final String EXPERIMENT_DESIGN_NBLATIN_SHOULD_NOT_BE_GREATER_THAN_BLOCK_LEVEL =
		"experiment.design.nblatin.should.not.be.greater.than.block.level";
	public static final String EXPERIMENT_DESIGN_REPLATING_GROUPS_NOT_EQUAL_TO_REPLICATES =
		"experiment.design.replating.groups.not.equal.to.replicates";
	public static final String EXPERIMENT_DESIGN_ROWS_PER_REPLICATION_SHOULD_BE_A_NUMBER =
		"experiment.design.rows.per.replication.should.be.a.number";
	public static final String EXPERIMENT_DESIGN_COLS_PER_REPLICATION_SHOULD_BE_A_NUMBER =
		"experiment.design.cols.per.replication.should.be.a.number";
	public static final String EXPERIMENT_DESIGN_RESOLVABLE_INCORRECT_ROW_AND_COL_PRODUCT_TO_GERMPLASM_SIZE =
		"experiment.design.resolvable.incorrect.row.and.col.product.to.germplasm.size";
	public static final String EXPERIMENT_DESIGN_NRLATIN_SHOULD_BE_LESS_THAN_ROWS_PER_REPLICATION =
		"experiment.design.nrlatin.should.be.less.than.rows.per.replication";
	public static final String EXPERIMENT_DESIGN_NCLATIN_SHOULD_BE_LESS_THAN_COLS_PER_REPLICATION =
		"experiment.design.nclatin.should.be.less.than.cols.per.replication";
	public static final String GERMPLASM_LIST_CHECK_REQUIRED_AUGMENTED_DESIGN = "germplasm.list.check.required.augmented.design";
	public static final String NUMBER_OF_BLOCKS_SHOULD_BE_NUMERIC = "number.of.blocks.should.be.numeric";
	private static final String EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED =
		"experiment.design.replication.percentage.should.be.between.zero.and.hundred";
	public static final String GERMPLASM_LIST_ALL_ENTRIES_CAN_NOT_BE_CHECKS = "germplasm.list.all.entries.can.not.be.checks";
	public static final String GERMPLASM_LIST_START_INDEX_WHOLE_NUMBER_ERROR = "germplasm.list.start.index.whole.number.error";
	public static final String GERMPLASM_LIST_NUMBER_OF_ROWS_BETWEEN_INSERTION_SHOULD_BE_A_WHOLE_NUMBER =
		"germplasm.list.number.of.rows.between.insertion.should.be.a.whole.number";
	public static final String CHECK_MANNER_OF_INSERTION_INVALID = "check.manner.of.insertion.invalid";
	public static final String GERMPLASM_LIST_STARTING_INDEX_SHOULD_BE_GREATER_THAN_ZERO =
		"germplasm.list.starting.index.should.be.greater.than.zero";
	public static final String GERMPLASM_LIST_START_INDEX_LESS_THAN_GERMPLASM_ERROR =
		"germplasm.list.start.index.less.than.germplasm.error";
	public static final String GERMPLASM_LIST_NUMBER_OF_ROWS_BETWEEN_INSERTION_SHOULD_BE_GREATER_THAN_ZERO =
		"germplasm.list.number.of.rows.between.insertion.should.be.greater.than.zero";
	public static final String GERMPLASM_LIST_SPACING_LESS_THAN_GERMPLASM_ERROR = "germplasm.list.spacing.less.than.germplasm.error";
	public static final String EXPERIMENT_DESIGN_GENERATE_NO_GERMPLASM = "experiment.design.generate.no.germplasm";
	public static final String EXPERIMENT_DESIGN_LICENSE_EXPIRED = "experiment.design.license.expired";

	private BindingResult errors;

	@Resource
	private DesignLicenseUtil bvDesignLicenseUtil;

	/**
	 * Validates the parameters and germplasm entries required for generating randomized block design.
	 *
	 * @param experimentDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validateRandomizedCompleteBlockDesign(final ExperimentDesignInput experimentDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.checkBVDesignLicense();
		this.validatestudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentDesignInput != null && studyGermplasmDtoList != null) {
			this.validateReplicationCount(experimentDesignInput);
			this.validateReplicationCountLimitForRCBD(experimentDesignInput);
			this.validatePlotNumberRange(experimentDesignInput);
			this.validatePlotNumberAndEntryNumberShouldNotExceedLimit(experimentDesignInput, studyGermplasmDtoList.size());
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	/**
	 * Validates the parameters and germplasm entries required for generating resolvable incomplete block design.
	 *
	 * @param experimentDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validateResolvableIncompleteBlockDesign(final ExperimentDesignInput experimentDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.checkBVDesignLicense();
		this.validatestudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentDesignInput != null && studyGermplasmDtoList != null) {

			this.validateReplicationCount(experimentDesignInput);
			this.validateReplicationCountLimitResolvable(experimentDesignInput);
			this.validatePlotNumberRange(experimentDesignInput);
			this.validatePlotNumberAndEntryNumberShouldNotExceedLimit(experimentDesignInput, studyGermplasmDtoList.size());
			this.validateBlockSize(experimentDesignInput);

			if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
				this.errors.reject(EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR);
			} else {

				final int blockSize = Integer.parseInt(experimentDesignInput.getBlockSize());
				final int replicationCount = Integer.parseInt(experimentDesignInput.getReplicationsCount());
				final int treatmentSize = studyGermplasmDtoList.size();
				final int blockLevel = treatmentSize / blockSize;

				if (blockSize <= 1) {
					this.errors.reject(EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_GREATER_THAN_1);
				} else if (blockLevel == 1) {
					this.errors.reject(EXPERIMENT_DESIGN_BLOCK_LEVEL_SHOULD_BE_GREATER_THAN_ONE);
				} else if (treatmentSize % blockSize != 0) {
					this.errors.reject(EXPERIMENT_DESIGN_BLOCK_SIZE_NOT_A_FACTOR_OF_TREATMENT_SIZE);
				} else if (experimentDesignInput.getUseLatenized() != null && experimentDesignInput.getUseLatenized().booleanValue()) {
					// we add validation for latinize
					final int nbLatin =
						experimentDesignInput.getNblatin() != null ? Integer.parseInt(experimentDesignInput.getNblatin()) : 0;
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
					} else if (experimentDesignInput.getReplicationsArrangement() != null
						&& experimentDesignInput.getReplicationsArrangement().intValue() == 3) {
						// meaning adjacent
						final StringTokenizer tokenizer = new StringTokenizer(experimentDesignInput.getReplatinGroups(), ",");
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
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	/**
	 * Validates the parameters and germplasm entries required for generating resolvable row column design.
	 *
	 * @param experimentDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validateResolvableRowColumnDesign(final ExperimentDesignInput experimentDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.checkBVDesignLicense();
		this.validatestudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentDesignInput != null && studyGermplasmDtoList != null) {

			this.validatePlotNumberRange(experimentDesignInput);
			this.validateReplicationCount(experimentDesignInput);
			this.validateReplicationCountLimitResolvable(experimentDesignInput);
			this.validatePlotNumberAndEntryNumberShouldNotExceedLimit(experimentDesignInput, studyGermplasmDtoList.size());

			final int size = studyGermplasmDtoList.size();
			if (!NumberUtils.isNumber(experimentDesignInput.getRowsPerReplications())) {
				this.errors.reject(EXPERIMENT_DESIGN_ROWS_PER_REPLICATION_SHOULD_BE_A_NUMBER);
			} else if (!NumberUtils.isNumber(experimentDesignInput.getColsPerReplications())) {
				this.errors.reject(EXPERIMENT_DESIGN_COLS_PER_REPLICATION_SHOULD_BE_A_NUMBER);
			} else if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
				this.errors.reject(EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR);
			} else {

				final int rowsPerReplication = Integer.parseInt(experimentDesignInput.getRowsPerReplications());
				final int colsPerReplication = Integer.parseInt(experimentDesignInput.getColsPerReplications());
				final int replicationCount = Integer.parseInt(experimentDesignInput.getReplicationsCount());

				if (size != rowsPerReplication * colsPerReplication) {
					this.errors.reject(EXPERIMENT_DESIGN_RESOLVABLE_INCORRECT_ROW_AND_COL_PRODUCT_TO_GERMPLASM_SIZE);
				} else if (experimentDesignInput.getUseLatenized() != null && experimentDesignInput.getUseLatenized().booleanValue()) {
					// we add validation for latinize
					final int nrLatin = Integer.parseInt(experimentDesignInput.getNrlatin());
					final int ncLatin = Integer.parseInt(experimentDesignInput.getNclatin());
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
					} else if (experimentDesignInput.getReplicationsArrangement() != null
						&& experimentDesignInput.getReplicationsArrangement().intValue() == 3) {
						// meaning adjacent
						final StringTokenizer tokenizer = new StringTokenizer(experimentDesignInput.getReplatinGroups(), ",");
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
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	/**
	 * Validates the parameters and germplasm entries required for generating P-rep design.
	 *
	 * @param experimentDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validatePrepDesign(final ExperimentDesignInput experimentDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.checkBVDesignLicense();
		this.validatestudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentDesignInput != null && studyGermplasmDtoList != null) {

			if (experimentDesignInput.getReplicationPercentage() == null
				|| experimentDesignInput.getReplicationPercentage() < MINIMUM_REPLICATION_PERCENTAGE
				|| experimentDesignInput.getReplicationPercentage() > MAXIMUM_REPLICATION_PERCENTAGE) {
				this.errors.reject(EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED);
			}

			this.validateBlockSize(experimentDesignInput);
			this.validateReplicationCount(experimentDesignInput);
			this.validatePlotNumberRange(experimentDesignInput);

			if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
				this.errors.reject(EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR);
			}
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	/**
	 * Validates the parameters and germplasm entries required for generating augmented design.
	 *
	 * @param experimentDesignInput
	 * @param gemplasmList
	 * @);
	 */
	public void validateAugmentedDesign(final ExperimentDesignInput experimentDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		this.checkBVDesignLicense();
		this.validatestudyGermplasmDtoList(studyGermplasmDtoList);

		if (experimentDesignInput != null && studyGermplasmDtoList != null) {

			final int treatmentSize = studyGermplasmDtoList.size();

			this.validateIfCheckEntriesExistInstudyGermplasmDtoList(studyGermplasmDtoList);
			this.validateStartingPlotNo(experimentDesignInput, treatmentSize);
			this.validateStartingEntryNo(experimentDesignInput, treatmentSize);
			this.validateNumberOfBlocks(experimentDesignInput);
			this.validateTreatmentFactors(experimentDesignInput);

		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	/**
	 * Validates the parameters and germplasm entries required for generating entry list order design.
	 *
	 * @param experimentDesignInput
	 * @param studyGermplasmDtoList
	 */
	public void validateEntryListOrderDesign(final ExperimentDesignInput experimentDesignInput,
		final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (experimentDesignInput != null && studyGermplasmDtoList != null) {
			if (experimentDesignInput.getStartingPlotNo() != null && !NumberUtils.isNumber(experimentDesignInput.getStartingPlotNo())) {
				this.errors.reject(PLOT_NUMBER_SHOULD_BE_IN_RANGE);
			} else {
				final List<StudyGermplasmDto> checkList = new LinkedList<>();

				final List<StudyGermplasmDto> testEntryList = new LinkedList<>();

				this.loadChecksAndTestEntries(studyGermplasmDtoList, checkList, testEntryList);

				if (testEntryList.isEmpty()) {
					this.errors.reject(GERMPLASM_LIST_ALL_ENTRIES_CAN_NOT_BE_CHECKS);
				}

				if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
					this.errors.reject(EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR);
				}

				if (!checkList.isEmpty()) {
					if (experimentDesignInput.getCheckStartingPosition() == null || !NumberUtils
						.isNumber(experimentDesignInput.getCheckStartingPosition())) {
						this.errors.reject(GERMPLASM_LIST_START_INDEX_WHOLE_NUMBER_ERROR);
					}
					if (experimentDesignInput.getCheckSpacing() == null || !NumberUtils
						.isNumber(experimentDesignInput.getCheckSpacing())) {
						this.errors.reject(GERMPLASM_LIST_NUMBER_OF_ROWS_BETWEEN_INSERTION_SHOULD_BE_A_WHOLE_NUMBER);
					}
					if (experimentDesignInput.getCheckInsertionManner() == null || !NumberUtils
						.isNumber(experimentDesignInput.getCheckInsertionManner())) {
						this.errors.reject(CHECK_MANNER_OF_INSERTION_INVALID);
					}

					final Integer checkStartingPosition =
						(StringUtils.isEmpty(experimentDesignInput.getCheckStartingPosition())) ? null :
							Integer.parseInt(experimentDesignInput.getCheckStartingPosition());

					final Integer checkSpacing = (StringUtils.isEmpty(experimentDesignInput.getCheckSpacing())) ? null :
						Integer.parseInt(experimentDesignInput.getCheckSpacing());

					if (checkStartingPosition < 1) {
						this.errors.reject(GERMPLASM_LIST_STARTING_INDEX_SHOULD_BE_GREATER_THAN_ZERO);
					}
					if (checkStartingPosition > testEntryList.size()) {
						this.errors.reject(GERMPLASM_LIST_START_INDEX_LESS_THAN_GERMPLASM_ERROR);
					}
					if (checkSpacing < 1) {
						this.errors.reject(GERMPLASM_LIST_NUMBER_OF_ROWS_BETWEEN_INSERTION_SHOULD_BE_GREATER_THAN_ZERO);
					}
					if (checkSpacing > testEntryList.size()) {
						this.errors.reject(GERMPLASM_LIST_SPACING_LESS_THAN_GERMPLASM_ERROR);
					}
					if (studyGermplasmDtoList.size() - checkList.size() == 0) {
						this.errors.reject(GERMPLASM_LIST_ALL_ENTRIES_CAN_NOT_BE_CHECKS);
					}
				}
			}
		}
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void checkBVDesignLicense() {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		try {
			final BVDesignLicenseInfo bvDesignLicenseInfo = this.bvDesignLicenseUtil.retrieveLicenseInfo();
			if (this.bvDesignLicenseUtil.isExpired(bvDesignLicenseInfo)) {
				this.errors.reject(EXPERIMENT_DESIGN_LICENSE_EXPIRED);
			}
		} catch (final BVLicenseParseException e) {
			this.errors.reject(e.getMessage(), e.getMessage());
		}

	}

	private void validateTreatmentFactors(final ExperimentDesignInput experimentDesignInput) {
		if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
			this.errors.reject(EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR);
		}
	}

	void validateIfCheckEntriesExistInstudyGermplasmDtoList(final List<StudyGermplasmDto> studyGermplasmDtoList) {

		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
			if (studyGermplasmDto.getCheckType().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				return;
			}
		}

		this.errors.reject(GERMPLASM_LIST_CHECK_REQUIRED_AUGMENTED_DESIGN);

	}

	void validateStartingPlotNo(final ExperimentDesignInput experimentDesignInput, final int treatmentSize) {

		final String startingPlotNo = experimentDesignInput.getStartingPlotNo();

		if (startingPlotNo != null && NumberUtils.isNumber(startingPlotNo)) {
			final int plotNumber = Integer.parseInt(startingPlotNo);
			if (plotNumber != 0 && ((treatmentSize + plotNumber - 1) <= ExperimentDesignTypeService.MAX_PLOT_NO)) {
				return;
			}
		}

		this.errors.reject(PLOT_NUMBER_SHOULD_BE_IN_RANGE);

	}

	void validateStartingEntryNo(final ExperimentDesignInput experimentDesignInput, final int treatmentSize) {

		final String startingEntryNo = experimentDesignInput.getStartingEntryNo();

		if (startingEntryNo != null && NumberUtils.isNumber(startingEntryNo)) {
			final int entryNumber = Integer.parseInt(startingEntryNo);
			if (entryNumber != 0 && ((treatmentSize + entryNumber - 1) <= ExperimentDesignTypeService.MAX_ENTRY_NO)) {
				return;
			}
		}

		this.errors.reject(ENTRY_NUMBER_SHOULD_BE_IN_RANGE);

	}

	void validateNumberOfBlocks(final ExperimentDesignInput experimentDesignInput) {

		if (!NumberUtils.isNumber(experimentDesignInput.getNumberOfBlocks())) {
			this.errors.reject(NUMBER_OF_BLOCKS_SHOULD_BE_NUMERIC);
		}

	}

	void loadChecksAndTestEntries(final List<StudyGermplasmDto> studyGermplasmDtoList, final List<StudyGermplasmDto> checkList,
		final List<StudyGermplasmDto> testEntryList) {

		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
			if (studyGermplasmDto.getCheckType()
				.equals(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())) {
				testEntryList.add(studyGermplasmDto);
			} else {
				checkList.add(studyGermplasmDto);
			}
		}
	}

	void validateReplicationCount(final ExperimentDesignInput experimentDesignInput) {
		if (!NumberUtils.isNumber(experimentDesignInput.getReplicationsCount())) {
			this.errors.reject(EXPERIMENT_DESIGN_REPLICATION_COUNT_SHOULD_BE_A_NUMBER);
		}
	}

	void validatePlotNumberRange(final ExperimentDesignInput experimentDesignInput) {
		final Integer plotNumber = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
		if (plotNumber == null || Objects.equals(plotNumber, 0)) {
			this.errors.reject(PLOT_NUMBER_SHOULD_BE_IN_RANGE);
		}
	}

	void validatePlotNumberAndEntryNumberShouldNotExceedLimit(final ExperimentDesignInput experimentDesignInput,
		final int germplasmCount) {

		final Integer entryNumber = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), 1);
		final Integer plotNumber = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), 1);
		final Integer maxEntry = germplasmCount + entryNumber - 1;
		final Integer maxPlot = (germplasmCount * Integer.parseInt(experimentDesignInput.getReplicationsCount())) + plotNumber - 1;

		if (entryNumber != null && maxEntry > ExperimentDesignTypeService.MAX_ENTRY_NO) {
			this.errors.reject(EXPERIMENT_DESIGN_ENTRY_NUMBER_SHOULD_NOT_EXCEED, new Object[] {maxEntry}, "");
		}
		if (entryNumber != null && plotNumber != null && maxPlot > ExperimentDesignTypeService.MAX_PLOT_NO) {
			this.errors.reject(EXPERIMENT_DESIGN_PLOT_NUMBER_SHOULD_NOT_EXCEED, new Object[] {maxPlot}, "");
		}

	}

	void validateReplicationCountLimitForRCBD(final ExperimentDesignInput experimentDesignInput) {
		final int replicationCount = Integer.parseInt(experimentDesignInput.getReplicationsCount());

		if (replicationCount <= 0 || replicationCount >= 13) {
			this.errors.reject(EXPERIMENT_DESIGN_REPLICATION_COUNT_RCBD_ERROR);
		}
	}

	void validateReplicationCountLimitResolvable(final ExperimentDesignInput experimentDesignInput) {
		final int replicationCount = Integer.parseInt(experimentDesignInput.getReplicationsCount());

		if (replicationCount <= 0 || replicationCount >= 13) {
			this.errors.reject(EXPERIMENT_DESIGN_REPLICATION_COUNT_RESOLVABLE_ERROR);
		}
	}

	void validateBlockSize(final ExperimentDesignInput experimentDesignInput) {
		if (!NumberUtils.isNumber(experimentDesignInput.getBlockSize())) {
			this.errors.reject(EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_NUMBER);
		}
	}

	void validatestudyGermplasmDtoList(final List<StudyGermplasmDto> studyGermplasmDtoList) {
		if (studyGermplasmDtoList == null || studyGermplasmDtoList.isEmpty()) {
			this.errors.reject(EXPERIMENT_DESIGN_GENERATE_NO_GERMPLASM);
		}
	}

}
