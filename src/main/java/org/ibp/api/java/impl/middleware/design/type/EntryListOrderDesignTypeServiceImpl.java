package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignValidationOutput;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class EntryListOrderDesignTypeServiceImpl implements ExperimentDesignTypeService {

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Override
	public void generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput) {

		// TODO: Get Germplasm list from DB
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();

		final List<ImportedGermplasm> checkList = new LinkedList<>();

		final List<ImportedGermplasm> testEntryList = new LinkedList<>();

		this.loadChecksAndTestEntries(germplasmList, checkList, testEntryList);

		final Integer startingPosition =
			(StringUtils.isEmpty(experimentDesignInput.getCheckStartingPosition())) ? null :
				Integer.parseInt(experimentDesignInput.getCheckStartingPosition());

		final Integer spacing = (StringUtils.isEmpty(experimentDesignInput.getCheckSpacing())) ? null :
			Integer.parseInt(experimentDesignInput.getCheckSpacing());

		final Integer insertionManner =
			(StringUtils.isEmpty(experimentDesignInput.getCheckInsertionManner())) ? null :
				Integer.parseInt(experimentDesignInput.getCheckInsertionManner());

		final List<ImportedGermplasm> mergedGermplasmList =
			this.mergeTestAndCheckEntries(testEntryList, checkList, startingPosition, spacing, insertionManner);

		final int environments = Integer.valueOf(experimentDesignInput.getNoOfEnvironments());

		for (int instanceNumber = 1; instanceNumber <= environments; instanceNumber++) {

			Integer plotNumber = Integer.parseInt(experimentDesignInput.getStartingPlotNo());
			// TODO:
			//  IBP-3124 Directly save the plot experiments based on the germplasm entries. Create a service/method at Middleware level.
			// 	  Germplasm factors (GID, ENTRY_NO, etc), Design factors (PLOT_NO, REP_NO, etc) should be saved at their respective tables in the DB.
			//    Treatment factors and checks should also be applied if applicable.
		}

	}

	@Override
	public ExperimentDesignValidationOutput validate(final ExperimentDesignInput experimentDesignInput,
		final List<ImportedGermplasm> germplasmList) {
		final Locale locale = LocaleContextHolder.getLocale();
		try {
			if (experimentDesignInput != null && germplasmList != null) {
				if (experimentDesignInput.getStartingPlotNo() != null && !NumberUtils.isNumber(experimentDesignInput.getStartingPlotNo())) {
					return new ExperimentDesignValidationOutput(Boolean.FALSE,
						this.messageSource.getMessage("plot.number.should.be.in.range", null, locale));
				} else {
					final List<ImportedGermplasm> checkList = new LinkedList<>();

					final List<ImportedGermplasm> testEntryList = new LinkedList<>();

					this.loadChecksAndTestEntries(germplasmList, checkList, testEntryList);

					if (testEntryList.isEmpty()) {
						return new ExperimentDesignValidationOutput(Boolean.FALSE,
							this.messageSource.getMessage("germplasm.list.all.entries.can.not.be.checks", null, locale));
					}

					if (experimentDesignInput.getTreatmentFactorsData().size() > 0) {
						return new ExperimentDesignValidationOutput(Boolean.FALSE,
							this.messageSource
								.getMessage("experiment.design.treatment.factors.error", null, LocaleContextHolder.getLocale()));
					}

					if (!checkList.isEmpty()) {
						if (experimentDesignInput.getCheckStartingPosition() == null || !NumberUtils
							.isNumber(experimentDesignInput.getCheckStartingPosition())) {
							return new ExperimentDesignValidationOutput(Boolean.FALSE,
								this.messageSource.getMessage("germplasm.list.start.index.whole.number.error", null, locale));
						}
						if (experimentDesignInput.getCheckSpacing() == null || !NumberUtils
							.isNumber(experimentDesignInput.getCheckSpacing())) {
							return new ExperimentDesignValidationOutput(Boolean.FALSE, this.messageSource
								.getMessage("germplasm.list.number.of.rows.between.insertion.should.be.a.whole.number", null, locale));
						}
						if (experimentDesignInput.getCheckInsertionManner() == null || !NumberUtils
							.isNumber(experimentDesignInput.getCheckInsertionManner())) {
							return new ExperimentDesignValidationOutput(Boolean.FALSE,
								this.messageSource.getMessage("check.manner.of.insertion.invalid", null, locale));
						}

						final Integer checkStartingPosition =
							(StringUtils.isEmpty(experimentDesignInput.getCheckStartingPosition())) ? null :
								Integer.parseInt(experimentDesignInput.getCheckStartingPosition());

						final Integer checkSpacing = (StringUtils.isEmpty(experimentDesignInput.getCheckSpacing())) ? null :
							Integer.parseInt(experimentDesignInput.getCheckSpacing());

						if (checkStartingPosition < 1) {
							return new ExperimentDesignValidationOutput(Boolean.FALSE, this.messageSource
								.getMessage("germplasm.list.starting.index.should.be.greater.than.zero", null, locale));
						}
						if (checkStartingPosition > testEntryList.size()) {
							return new ExperimentDesignValidationOutput(Boolean.FALSE,
								this.messageSource.getMessage("germplasm.list.start.index.less.than.germplasm.error", null, locale));
						}
						if (checkSpacing < 1) {
							return new ExperimentDesignValidationOutput(Boolean.FALSE, this.messageSource
								.getMessage("germplasm.list.number.of.rows.between.insertion.should.be.greater.than.zero", null,
									locale));
						}
						if (checkSpacing > testEntryList.size()) {
							return new ExperimentDesignValidationOutput(Boolean.FALSE,
								this.messageSource.getMessage("germplasm.list.spacing.less.than.germplasm.error", null, locale));
						}
						if (germplasmList.size() - checkList.size() == 0) {
							return new ExperimentDesignValidationOutput(Boolean.FALSE,
								this.messageSource.getMessage("germplasm.list.all.entries.can.not.be.checks", null, locale));
						}
					}
				}
			}
		} catch (final Exception e) {
			return new ExperimentDesignValidationOutput(Boolean.FALSE,
				this.messageSource.getMessage("experiment.design.invalid.generic.error", null, locale));
		}
		return new ExperimentDesignValidationOutput(Boolean.TRUE, StringUtils.EMPTY);
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.FALSE;
	}

	private void loadChecksAndTestEntries(final List<ImportedGermplasm> importedGermplasmList, final List<ImportedGermplasm> checkList,
		final List<ImportedGermplasm> testEntryList) {

		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			if (importedGermplasm.getEntryTypeCategoricalID().equals(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())) {
				testEntryList.add(importedGermplasm);
			} else {
				checkList.add(importedGermplasm);
			}
		}
	}

	private boolean isThereSomethingToMerge(final List<ImportedGermplasm> entriesList, final List<ImportedGermplasm> checkList,
		final Integer startEntry, final Integer interval) {
		Boolean isThereSomethingToMerge = Boolean.TRUE;
		if (checkList == null || checkList.isEmpty()) {
			isThereSomethingToMerge = Boolean.FALSE;
		} else if (entriesList == null || entriesList.isEmpty()) {
			isThereSomethingToMerge = Boolean.FALSE;
		} else if (startEntry < 1 || startEntry > entriesList.size() || interval < 1) {
			isThereSomethingToMerge = Boolean.FALSE;
		}
		return isThereSomethingToMerge;
	}

	private List<ImportedGermplasm> generateChecksToInsert(final List<ImportedGermplasm> checkList, final Integer checkIndex,
		final Integer insertionManner) {
		final List<ImportedGermplasm> newList = new ArrayList<>();
		if (insertionManner.equals(InsertionMannerItem.INSERT_ALL_CHECKS.getId())) {
			for (final ImportedGermplasm checkGerm : checkList) {
				newList.add(checkGerm.copy());
			}
		} else {
			final Integer checkListIndex = checkIndex % checkList.size();
			final ImportedGermplasm checkGerm = checkList.get(checkListIndex);
			newList.add(checkGerm.copy());
		}
		return newList;
	}

	private List<ImportedGermplasm> mergeTestAndCheckEntries(final List<ImportedGermplasm> testEntryList,
		final List<ImportedGermplasm> checkList, final Integer startingIndex, final Integer spacing, final Integer insertionManner) {

		if (!this.isThereSomethingToMerge(testEntryList, checkList, startingIndex, spacing)) {
			return testEntryList;
		}

		final List<ImportedGermplasm> newList = new ArrayList<>();

		int primaryEntry = 1;
		boolean isStarted = Boolean.FALSE;
		boolean shouldInsert = Boolean.FALSE;
		int checkIndex = 0;
		int intervalEntry = 0;
		for (final ImportedGermplasm primaryGermplasm : testEntryList) {
			if (primaryEntry == startingIndex || intervalEntry == spacing) {
				isStarted = Boolean.TRUE;
				shouldInsert = Boolean.TRUE;
				intervalEntry = 0;
			}

			if (isStarted) {
				intervalEntry++;
			}

			if (shouldInsert) {
				shouldInsert = Boolean.FALSE;
				final List<ImportedGermplasm> checks = this.generateChecksToInsert(checkList, checkIndex, insertionManner);
				checkIndex++;
				newList.addAll(checks);
			}
			final ImportedGermplasm primaryNewGermplasm = primaryGermplasm.copy();
			primaryNewGermplasm.setEntryTypeValue(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue());
			primaryNewGermplasm.setEntryTypeCategoricalID(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

			newList.add(primaryNewGermplasm);

			primaryEntry++;
		}

		return newList;
	}

}
