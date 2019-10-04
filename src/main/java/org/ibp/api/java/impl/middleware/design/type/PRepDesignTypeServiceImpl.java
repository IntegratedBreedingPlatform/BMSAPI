package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignValidationOutput;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class PRepDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.BLOCK_SIZE.getId(),
			TermId.PERCENTAGE_OF_REPLICATION.getId());
	private static final String EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED =
		"experiment.design.replication.percentage.should.be.between.zero.and.hundred";
	private static final String EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_NUMBER = "experiment.design.block.size.should.be.a.number";
	private static final String EXPERIMENT_DESIGN_REPLICATION_COUNT_SHOULD_BE_A_NUMBER =
		"experiment.design.replication.count.should.be.a.number";
	private static final String PLOT_NUMBER_SHOULD_BE_IN_RANGE = "plot.number.should.be.in.range";
	private static final String ENTRY_NUMBER_SHOULD_BE_IN_RANGE = "entry.number.should.be.in.range";
	private static final String EXPERIMENT_DESIGN_INVALID_GENERIC_ERROR = "experiment.design.invalid.generic.error";
	private static final int MINIMUM_REPLICATION_PERCENTAGE = 0;
	private static final int MAXIMUM_REPLICATION_PERCENTAGE = 100;
	private static final String EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR = "experiment.design.treatment.factors.error";

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Override
	public void generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput) {
		// TODO:
		// 1. IBP-3123 Create BVDesign XML input file (e.g.)
		/**
		 * 	final MainDesign mainDesign = this.experimentDesignGenerator
		 * 				.createRandomizedCompleteBlockDesign(block, stdvarRep.getName(), stdvarPlot.getName(), plotNo, entryNo, stdvarTreatment.getName(), treatmentFactors,
		 * 					levels, "");
		 */
		// 2. IBP-3123 Run BV Design and get the design output
		// 3. IBP-3124 Parse the design output and determine the variables / values that will be saved for each plot experiment.
		// 4. IBP-3124 Directly save the plot experiments based on the design output. Create a service/method at Middleware level.
		// 	  Germplasm factors (GID, ENTRY_NO, etc), Design factors (PLOT_NO, REP_NO, etc) should be saved at their respective tables in the DB.
		//    Treatment factors and checks should also be applied if applicable.
		// 5. Save experimental design variables (check if this is study level or environment level).
	}

	@Override
	public ExperimentDesignValidationOutput validate(final ExperimentDesignInput experimentDesignInput,
		final List<ImportedGermplasm> germplasmList) {
		final Locale locale = LocaleContextHolder.getLocale();
		try {
			if (experimentDesignInput != null && germplasmList != null) {

				if (experimentDesignInput.getReplicationPercentage() == null
					|| experimentDesignInput.getReplicationPercentage() < MINIMUM_REPLICATION_PERCENTAGE
					|| experimentDesignInput.getReplicationPercentage() > MAXIMUM_REPLICATION_PERCENTAGE) {
					return new ExperimentDesignValidationOutput(
						false,
						this.messageSource
							.getMessage(EXPERIMENT_DESIGN_REPLICATION_PERCENTAGE_SHOULD_BE_BETWEEN_ZERO_AND_HUNDRED, null, locale));
				}
				if (!NumberUtils.isNumber(experimentDesignInput.getBlockSize())) {
					return new ExperimentDesignValidationOutput(
						false,
						this.messageSource.getMessage(EXPERIMENT_DESIGN_BLOCK_SIZE_SHOULD_BE_A_NUMBER, null, locale));
				}
				if (!NumberUtils.isNumber(experimentDesignInput.getReplicationsCount())) {
					return new ExperimentDesignValidationOutput(
						false,
						this.messageSource.getMessage(EXPERIMENT_DESIGN_REPLICATION_COUNT_SHOULD_BE_A_NUMBER, null, locale));
				}
				if (experimentDesignInput.getStartingPlotNo() != null && !NumberUtils
					.isNumber(experimentDesignInput.getStartingPlotNo())) {
					return new ExperimentDesignValidationOutput(
						false,
						this.messageSource.getMessage(PLOT_NUMBER_SHOULD_BE_IN_RANGE, null, locale));
				}
				if (experimentDesignInput.getStartingEntryNo() != null && !NumberUtils
					.isNumber(experimentDesignInput.getStartingEntryNo())) {
					return new ExperimentDesignValidationOutput(
						false,
						this.messageSource.getMessage(ENTRY_NUMBER_SHOULD_BE_IN_RANGE, null, locale));
				}
				if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
					return new ExperimentDesignValidationOutput(
						false,
						this.messageSource.getMessage(EXPERIMENT_DESIGN_TREATMENT_FACTORS_ERROR, null, locale));
				} else {
					final Integer entryNumber = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);
					final Integer plotNumber = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);

					if (Objects.equals(entryNumber, 0)) {
						return new ExperimentDesignValidationOutput(
							false,
							this.messageSource.getMessage(ENTRY_NUMBER_SHOULD_BE_IN_RANGE, null, locale));
					} else if (Objects.equals(plotNumber, 0)) {
						return new ExperimentDesignValidationOutput(
							false,
							this.messageSource.getMessage(PLOT_NUMBER_SHOULD_BE_IN_RANGE, null, locale));
					}
				}
			}
		} catch (final Exception e) {
			return new ExperimentDesignValidationOutput(
				false,
				this.messageSource.getMessage(EXPERIMENT_DESIGN_INVALID_GENERIC_ERROR, null, locale));
		}

		return new ExperimentDesignValidationOutput(true, "");
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}
}
