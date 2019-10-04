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
public class RandomizeCompleteBlockDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId());

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

		if (experimentDesignInput == null || germplasmList == null) {
			return output;
		}

		if (!NumberUtils.isNumber(experimentDesignInput.getReplicationsCount())) {
			output = new ExperimentDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.replication.count.should.be.a.number", null, locale));
			return output;
		}

		if (experimentDesignInput.getStartingPlotNo() != null && !NumberUtils.isNumber(experimentDesignInput.getStartingPlotNo())) {
			output =
				new ExperimentDesignValidationOutput(false, this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
			return output;
		}
		if (experimentDesignInput.getStartingEntryNo() != null && !NumberUtils.isNumber(experimentDesignInput.getStartingEntryNo())) {
			output =
				new ExperimentDesignValidationOutput(false, this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
			return output;
		}

		final int replicationCount = Integer.valueOf(experimentDesignInput.getReplicationsCount());

		if (replicationCount <= 0 || replicationCount >= 13) {
			output = new ExperimentDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.replication.count.rcbd.error", null, locale));
			return output;
		}

		final Integer entryNumber = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);
		final Integer plotNumber = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
		final Integer germplasmCount = germplasmList.size();
		final Integer maxEntry = germplasmCount + entryNumber - 1;
		final Integer maxPlot = (germplasmCount * replicationCount) + plotNumber - 1;

		if (Objects.equals(entryNumber, 0)) {
			output =
				new ExperimentDesignValidationOutput(false, this.messageSource.getMessage("entry.number.should.be.in.range", null, locale));
		} else if (Objects.equals(plotNumber, 0)) {
			output =
				new ExperimentDesignValidationOutput(false, this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
		} else if (entryNumber != null && maxEntry > ExperimentDesignTypeService.MAX_ENTRY_NO) {
			output = new ExperimentDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.entry.number.should.not.exceed", new Object[] {maxEntry}, locale));
		} else if (entryNumber != null && plotNumber != null && maxPlot > ExperimentDesignTypeService.MAX_PLOT_NO) {
			output = new ExperimentDesignValidationOutput(false,
				this.messageSource.getMessage("experiment.design.plot.number.should.not.exceed", new Object[] {maxPlot}, locale));
		}

		return output;
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}
}
