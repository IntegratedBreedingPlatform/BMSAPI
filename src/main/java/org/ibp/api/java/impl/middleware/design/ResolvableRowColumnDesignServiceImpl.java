package org.ibp.api.java.impl.middleware.design;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.java.design.ExperimentDesignService;
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
import java.util.StringTokenizer;

@Component
public class ResolvableRowColumnDesignServiceImpl implements ExperimentDesignService {

	private final List<Integer> EXPERIMENT_DESIGN_VARIABLES_LATINIZED = Arrays
		.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.NO_OF_ROWS_IN_REPS.getId(),
			TermId.NO_OF_COLS_IN_REPS.getId(), TermId.NO_OF_CROWS_LATINIZE.getId(), TermId.NO_OF_CCOLS_LATINIZE.getId(),
			TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_REPS_IN_COLS.getId());

	private final List<Integer> EXPERIMENT_DESIGN_VARIABLES = Arrays
		.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.NO_OF_ROWS_IN_REPS.getId(),
			TermId.NO_OF_COLS_IN_REPS.getId());

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
		ExperimentDesignValidationOutput output = new ExperimentDesignValidationOutput(true, "");
		try {
			if (experimentDesignInput != null && germplasmList != null) {
				final int size = germplasmList.size();
				if (!NumberUtils.isNumber(experimentDesignInput.getRowsPerReplications())) {
					output = new ExperimentDesignValidationOutput(false,
						this.messageSource.getMessage("experiment.design.rows.per.replication.should.be.a.number", null, locale));
					return output;
				} else if (!NumberUtils.isNumber(experimentDesignInput.getColsPerReplications())) {
					output = new ExperimentDesignValidationOutput(false,
						this.messageSource.getMessage("experiment.design.cols.per.replication.should.be.a.number", null, locale));
					return output;
				} else if (!NumberUtils.isNumber(experimentDesignInput.getReplicationsCount())) {
					output = new ExperimentDesignValidationOutput(false,
						this.messageSource.getMessage("experiment.design.replication.count.should.be.a.number", null, locale));
					return output;
				} else if (experimentDesignInput.getStartingPlotNo() != null && !NumberUtils
					.isNumber(experimentDesignInput.getStartingPlotNo())) {
					output = new ExperimentDesignValidationOutput(false,
						this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
					return output;
				} else if (experimentDesignInput.getStartingEntryNo() != null && !NumberUtils
					.isNumber(experimentDesignInput.getStartingEntryNo())) {
					output = new ExperimentDesignValidationOutput(false,
						this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
					return output;
				} else if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
					output = new ExperimentDesignValidationOutput(Boolean.FALSE,
						this.messageSource.getMessage("experiment.design.treatment.factors.error", null, LocaleContextHolder.getLocale()));
				} else {

					final int rowsPerReplication = Integer.valueOf(experimentDesignInput.getRowsPerReplications());
					final int colsPerReplication = Integer.valueOf(experimentDesignInput.getColsPerReplications());
					final int replicationCount = Integer.valueOf(experimentDesignInput.getReplicationsCount());
					final Integer entryNumber = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);
					final Integer plotNumber = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
					final Integer germplasmCount = germplasmList.size();
					final Integer maxEntry = germplasmCount + entryNumber - 1;
					final Integer maxPlot = (germplasmCount * replicationCount) + plotNumber - 1;

					if (Objects.equals(entryNumber, 0)) {
						output = new ExperimentDesignValidationOutput(false,
							this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
					} else if (Objects.equals(plotNumber, 0)) {
						output = new ExperimentDesignValidationOutput(false,
							this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
					} else if (replicationCount <= 1 || replicationCount >= 13) {
						output = new ExperimentDesignValidationOutput(false,
							this.messageSource.getMessage("experiment.design.replication.count.resolvable.error", null, locale));
					} else if (entryNumber != null && maxEntry > ExperimentDesignService.MAX_ENTRY_NO) {
						output = new ExperimentDesignValidationOutput(false, this.messageSource
							.getMessage("experiment.design.entry.number.should.not.exceed", new Object[] {maxEntry}, locale));
					} else if (entryNumber != null && plotNumber != null && maxPlot > ExperimentDesignService.MAX_PLOT_NO) {
						output = new ExperimentDesignValidationOutput(false, this.messageSource
							.getMessage("experiment.design.plot.number.should.not.exceed", new Object[] {maxPlot}, locale));
					} else if (size != rowsPerReplication * colsPerReplication) {
						output = new ExperimentDesignValidationOutput(false, this.messageSource
							.getMessage("experiment.design.resolvable.incorrect.row.and.col.product.to.germplasm.size", null, locale));
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
							output = new ExperimentDesignValidationOutput(false, this.messageSource
								.getMessage("experiment.design.nrlatin.should.be.less.than.rows.per.replication", null, locale));
						} else if (ncLatin >= colsPerReplication) {
							output = new ExperimentDesignValidationOutput(false, this.messageSource
								.getMessage("experiment.design.nclatin.should.be.less.than.cols.per.replication", null, locale));
						} else if (experimentDesignInput.getReplicationsArrangement() != null
							&& experimentDesignInput.getReplicationsArrangement().intValue() == 3) {
							// meaning adjacent
							final StringTokenizer tokenizer = new StringTokenizer(experimentDesignInput.getReplatinGroups(), ",");
							int totalReplatingGroup = 0;

							while (tokenizer.hasMoreTokens()) {
								totalReplatingGroup += Integer.parseInt(tokenizer.nextToken());
							}
							if (totalReplatingGroup != replicationCount) {
								output = new ExperimentDesignValidationOutput(false, this.messageSource
									.getMessage("experiment.design.replating.groups.not.equal.to.replicates", null, locale));
							}
						}
					}
				}
			}
		} catch (final Exception e) {
			output = new ExperimentDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}

		return output;
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}
}
