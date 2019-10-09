package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class EntryListOrderDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> DESIGN_FACTOR_VARIABLES = Arrays.asList(TermId.PLOT_NO.getId());

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput,
		final String programUUID, final List<StudyGermplasmDto> studyGermplasmDtoList) {

		this.experimentDesignTypeValidator.validateEntryListOrderDesign(experimentDesignInput, studyGermplasmDtoList);

		final List<StudyGermplasmDto> checkList = new LinkedList<>();

		final List<StudyGermplasmDto> testEntryList = new LinkedList<>();

		this.loadChecksAndTestEntries(studyGermplasmDtoList, checkList, testEntryList);

		final Integer startingPosition =
			(StringUtils.isEmpty(experimentDesignInput.getCheckStartingPosition())) ? null :
				Integer.parseInt(experimentDesignInput.getCheckStartingPosition());

		final Integer spacing = (StringUtils.isEmpty(experimentDesignInput.getCheckSpacing())) ? null :
			Integer.parseInt(experimentDesignInput.getCheckSpacing());

		final Integer insertionManner =
			(StringUtils.isEmpty(experimentDesignInput.getCheckInsertionManner())) ? null :
				Integer.parseInt(experimentDesignInput.getCheckInsertionManner());

		final List<StudyGermplasmDto> mergedGermplasmList =
			this.mergeTestAndCheckEntries(testEntryList, checkList, startingPosition, spacing, insertionManner);

		final int environments = Integer.parseInt(experimentDesignInput.getNoOfEnvironments());

		for (int instanceNumber = 1; instanceNumber <= environments; instanceNumber++) {

			int plotNumber = Integer.parseInt(experimentDesignInput.getStartingPlotNo());

			for (final StudyGermplasmDto germplasm : mergedGermplasmList) {
				/**
				 * TODO: Create ObservationUnitRow per germplasm
				 final MeasurementRow measurementRow =
				 this.createMeasurementRow(instanceNumber, germplasm, germplasm.getEntryId(), plotNumber++, trialVariables, variates,
				 nonTrialFactors, factors);
				 measurementRows.add(measurementRow);
				 **/
			}
		}
		return new ArrayList<>();
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

	private void loadChecksAndTestEntries(final List<StudyGermplasmDto> studyGermplasmDtoList, final List<StudyGermplasmDto> checkList,
		final List<StudyGermplasmDto> testEntryList) {

		for (final StudyGermplasmDto studyGermplasmDto : studyGermplasmDtoList) {
			if (studyGermplasmDto.getCheckType().equals(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())) {
				testEntryList.add(studyGermplasmDto);
			} else {
				checkList.add(studyGermplasmDto);
			}
		}
	}

	private boolean isThereSomethingToMerge(final List<StudyGermplasmDto> entriesList, final List<StudyGermplasmDto> checkList,
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

	private List<StudyGermplasmDto> generateChecksToInsert(final List<StudyGermplasmDto> checkList, final Integer checkIndex,
		final Integer insertionManner) {
		final List<StudyGermplasmDto> newList = new ArrayList<>();
		if (insertionManner.equals(InsertionMannerItem.INSERT_ALL_CHECKS.getId())) {
			for (final StudyGermplasmDto checkGermplasm : checkList) {
				newList.add(SerializationUtils.clone(checkGermplasm));
			}
		} else {
			final int checkListIndex = checkIndex % checkList.size();
			final StudyGermplasmDto checkGermplasm = checkList.get(checkListIndex);
			newList.add(SerializationUtils.clone(checkGermplasm));
		}
		return newList;
	}

	private List<StudyGermplasmDto> mergeTestAndCheckEntries(final List<StudyGermplasmDto> testEntryList,
		final List<StudyGermplasmDto> checkList, final Integer startingIndex, final Integer spacing, final Integer insertionManner) {

		if (!this.isThereSomethingToMerge(testEntryList, checkList, startingIndex, spacing)) {
			return testEntryList;
		}

		final List<StudyGermplasmDto> newList = new ArrayList<>();

		int primaryEntry = 1;
		boolean isStarted = Boolean.FALSE;
		boolean shouldInsert = Boolean.FALSE;
		int checkIndex = 0;
		int intervalEntry = 0;
		for (final StudyGermplasmDto primaryGermplasm : testEntryList) {
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
				final List<StudyGermplasmDto> checks = this.generateChecksToInsert(checkList, checkIndex, insertionManner);
				checkIndex++;
				newList.addAll(checks);
			}
			final StudyGermplasmDto primaryNewGermplasm = SerializationUtils.clone(primaryGermplasm);
			primaryNewGermplasm.setCheckType(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

			newList.add(primaryNewGermplasm);

			primaryEntry++;
		}

		return newList;
	}

}
