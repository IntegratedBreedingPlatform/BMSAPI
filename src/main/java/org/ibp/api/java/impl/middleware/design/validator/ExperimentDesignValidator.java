package org.ibp.api.java.impl.middleware.design.validator;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.ibp.api.exception.DesignValidationException;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Resource;
import java.util.List;

public class ExperimentDesignValidator {

	@Resource
	private MessageSource messageSource;

	/**
	 * Validates the parameters and germplasm entries required for generating augmented design.
	 *
	 * @param experimentDesignInput
	 * @param importedGermplasmList
	 * @throws DesignValidationException
	 */
	public void validateAugmentedDesign(final ExperimentDesignInput experimentDesignInput,
		final List<ImportedGermplasm> importedGermplasmList)
		throws DesignValidationException {

		if (experimentDesignInput != null && importedGermplasmList != null) {

			final int treatmentSize = importedGermplasmList.size();

			this.validateIfCheckEntriesExistInGermplasmList(importedGermplasmList);
			this.validateStartingPlotNo(experimentDesignInput, treatmentSize);
			this.validateStartingEntryNo(experimentDesignInput, treatmentSize);
			this.validateNumberOfBlocks(experimentDesignInput);
			this.validateTreatmentFactors(experimentDesignInput);

		}

	}

	private void validateTreatmentFactors(final ExperimentDesignInput experimentDesignInput) throws DesignValidationException {
		if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
			throw new DesignValidationException(
				this.messageSource.getMessage("experiment.design.treatment.factors.error", null, LocaleContextHolder.getLocale()));
		}
	}

	void validateIfCheckEntriesExistInGermplasmList(final List<ImportedGermplasm> importedGermplasmList) throws DesignValidationException {

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId())) {
				return;
			}
		}

		throw new DesignValidationException(
			this.messageSource.getMessage("germplasm.list.check.required.augmented.design", null, LocaleContextHolder.getLocale()));

	}

	void validateStartingPlotNo(final ExperimentDesignInput experimentDesignInput, final int treatmentSize)
		throws DesignValidationException {

		final String startingPlotNo = experimentDesignInput.getStartingPlotNo();

		if (startingPlotNo != null && NumberUtils.isNumber(startingPlotNo)) {
			final Integer plotNumber = Integer.valueOf(startingPlotNo);
			if (plotNumber != 0 && ((treatmentSize + plotNumber - 1) <= ExperimentDesignTypeService.MAX_PLOT_NO)) {
				return;
			}
		}

		throw new DesignValidationException(
			this.messageSource.getMessage("plot.number.should.be.in.range", null, LocaleContextHolder.getLocale()));

	}

	void validateStartingEntryNo(final ExperimentDesignInput experimentDesignInput, final int treatmentSize)
		throws DesignValidationException {

		final String startingEntryNo = experimentDesignInput.getStartingEntryNo();

		if (startingEntryNo != null && NumberUtils.isNumber(startingEntryNo)) {
			final Integer entryNumber = Integer.valueOf(startingEntryNo);
			if (entryNumber != 0 && ((treatmentSize + entryNumber - 1) <= ExperimentDesignTypeService.MAX_ENTRY_NO)) {
				return;
			}
		}

		throw new DesignValidationException(
			this.messageSource.getMessage("entry.number.should.be.in.range", null, LocaleContextHolder.getLocale()));

	}

	void validateNumberOfBlocks(final ExperimentDesignInput experimentDesignInput) throws DesignValidationException {

		if (!NumberUtils.isNumber(experimentDesignInput.getNumberOfBlocks())) {
			throw new DesignValidationException(
				this.messageSource.getMessage("number.of.blocks.should.be.numeric", null, LocaleContextHolder.getLocale()));
		}

	}

}
