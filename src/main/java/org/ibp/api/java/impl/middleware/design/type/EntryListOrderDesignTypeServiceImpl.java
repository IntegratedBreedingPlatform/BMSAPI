package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.util.ExpDesignUtil;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class EntryListOrderDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> DESIGN_FACTOR_VARIABLES = Arrays.asList(TermId.PLOT_NO.getId());

	@Resource
	private ResourceBundleMessageSource messageSource;

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput,
		final String programUUID, final List<ImportedGermplasm> germplasmList) {

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

		final List<MeasurementVariable> measurementVariables = new ArrayList<>(this.getMeasurementVariablesMap(studyId, programUUID).values());
		final List<ObservationUnitRow> observationUnitRows = new ArrayList<>();
		for (int instanceNumber = 1; instanceNumber <= environments; instanceNumber++) {

			int plotNumber = Integer.parseInt(experimentDesignInput.getStartingPlotNo());

			for (final ImportedGermplasm germplasm : mergedGermplasmList) {
				final ObservationUnitRow observationUnitRow =
					this.createObservationUnitRow(instanceNumber, germplasm, plotNumber++, measurementVariables);
				observationUnitRows.add(observationUnitRow);
			}
		}
		return observationUnitRows;
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.FALSE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.ENTRY_LIST_ORDER.getId();
	}

	@Override
	public Map<Integer, MeasurementVariable> getMeasurementVariablesMap(final int studyId, final String programUUID) {
		return this.experimentDesignGenerator.getMeasurementVariablesMap(studyId, programUUID, DESIGN_FACTOR_VARIABLES, new ArrayList<>());
	}

	ObservationUnitRow createObservationUnitRow(final int instanceNumber, final ImportedGermplasm germplasm,
		final int plotNumber, final List<MeasurementVariable> measurementVariables) {
		final ObservationUnitRow row = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		ObservationUnitData observationUnitData;

		observationUnitData = ExpDesignUtil.createObservationUnitData(TermId.TRIAL_INSTANCE_FACTOR.getId(), String.valueOf(instanceNumber));
		observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
		for (final MeasurementVariable var : measurementVariables) {
			final Integer termId = var.getTermId();
			if (termId == TermId.ENTRY_NO.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, String.valueOf(germplasm.getEntryId()));
			} else if (termId == TermId.SOURCE.getId() || termId == TermId.GERMPLASM_SOURCE.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, germplasm.getSource() != null ? germplasm.getSource() : StringUtils.EMPTY);
			} else if (termId == TermId.GROUPGID.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, germplasm.getGroupId() != null ? germplasm.getGroupId().toString() : StringUtils.EMPTY);
			} else if (termId == TermId.STOCKID.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, germplasm.getStockIDs() != null ? germplasm.getStockIDs() : StringUtils.EMPTY);
			} else if (termId == TermId.CROSS.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, germplasm.getCross());
			} else if (termId == TermId.DESIG.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, germplasm.getDesig());
			} else if (termId == TermId.GID.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, germplasm.getGid());
			} else if (termId == TermId.ENTRY_CODE.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, germplasm.getEntryCode());
			} else if (termId == TermId.PLOT_NO.getId()) {
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, Integer.toString(plotNumber));
			} else if (termId == TermId.ENTRY_TYPE.getId()) {
				// if germplasm has defined check value, use that
				if (germplasm.getEntryTypeCategoricalID() != null) {
					observationUnitData = ExpDesignUtil.createObservationUnitData(termId, germplasm.getEntryTypeName());
				} else {
					observationUnitData = ExpDesignUtil.createObservationUnitData(termId, SystemDefinedEntryType.TEST_ENTRY.getEntryTypeValue());
				}
			} else {
				// meaning non factor
				observationUnitData = ExpDesignUtil.createObservationUnitData(termId, StringUtils.EMPTY);
			}
			observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
		}
		return row;
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
