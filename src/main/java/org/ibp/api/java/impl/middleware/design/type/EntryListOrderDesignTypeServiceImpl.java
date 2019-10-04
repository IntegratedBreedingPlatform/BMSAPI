package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.BindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EntryListOrderDesignTypeServiceImpl implements ExperimentDesignTypeService {

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	private BindingResult errors;

	@Override
	public void generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput, final String programUUID) {

		// TODO: Get Germplasm list from DB
		final List<ImportedGermplasm> germplasmList = new ArrayList<>();
		this.experimentDesignTypeValidator.validateEntryListOrderDesign(experimentDesignInput, germplasmList);

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
