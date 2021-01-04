package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.SerializationUtils;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.MeasurementVariableGenerator;
import org.ibp.api.java.impl.middleware.design.util.ExperimentalDesignUtil;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class EntryListOrderDesignTypeServiceImpl implements ExperimentalDesignTypeService {

	protected static final List<Integer> DESIGN_FACTOR_VARIABLES = Arrays.asList(TermId.PLOT_NO.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId());

	protected static final List<Integer> EXPERIMENT_DESIGN_VARIABLES_WITH_CHECK_PLAN =
		Arrays.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.CHECK_START.getId(), TermId.CHECK_INTERVAL.getId(),
			TermId.CHECK_PLAN.getId());

	@Resource
	private MeasurementVariableGenerator measurementVariableGenerator;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID, final List<StudyEntryDto> studyEntryDtoList) {

		final List<StudyEntryDto> checkList = new LinkedList<>();

		final List<StudyEntryDto> testEntryList = new LinkedList<>();

		this.loadChecksAndTestEntries(studyEntryDtoList, checkList, testEntryList);

		final Integer startingPosition = experimentalDesignInput.getCheckStartingPosition();

		final Integer spacing = experimentalDesignInput.getCheckSpacing();

		final Integer insertionManner = experimentalDesignInput.getCheckInsertionManner();

		final List<StudyEntryDto> mergedGermplasmList =
			this.mergeTestAndCheckEntries(testEntryList, checkList, startingPosition, spacing, insertionManner);

		final List<MeasurementVariable> measurementVariables = this.getMeasurementVariables(studyId, experimentalDesignInput, programUUID);
		final List<ObservationUnitRow> observationUnitRows = new ArrayList<>();
		for (final Integer instanceNumber : experimentalDesignInput.getTrialInstancesForDesignGeneration()) {
			int plotNumber = experimentalDesignInput.getStartingPlotNo();

			for (final StudyEntryDto germplasm : mergedGermplasmList) {
				final ObservationUnitRow observationUnitRow =
					this.createObservationUnitRow(instanceNumber, germplasm, plotNumber++, measurementVariables);
				observationUnitRows.add(observationUnitRow);
			}
		}
		return observationUnitRows;
	}

	@Override
	public Boolean requiresLicenseCheck() {
		return Boolean.FALSE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.ENTRY_LIST_ORDER.getId();
	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentalDesignInput experimentalDesignInput,
		final String programUUID) {
		return this.measurementVariableGenerator
			.generateFromExperimentalDesignInput(studyId, programUUID, DESIGN_FACTOR_VARIABLES,
				experimentalDesignInput.getCheckSpacing() != null ? EXPERIMENT_DESIGN_VARIABLES_WITH_CHECK_PLAN :
					EXPERIMENT_DESIGN_VARIABLES, experimentalDesignInput);
	}

	ObservationUnitRow createObservationUnitRow(final int instanceNumber, final StudyEntryDto studyEntry,
		final int plotNumber, final List<MeasurementVariable> measurementVariables) {
		final ObservationUnitRow row = new ObservationUnitRow();
		row.setTrialInstance(instanceNumber);

		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		ObservationUnitData observationUnitData;

		observationUnitData = new ObservationUnitData(TermId.TRIAL_INSTANCE_FACTOR.getId(), String.valueOf(instanceNumber));
		observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
		for (final MeasurementVariable var : measurementVariables) {
			final Integer termId = var.getTermId();
			if (termId == TermId.PLOT_NO.getId()) {
				observationUnitData = new ObservationUnitData(termId, Integer.toString(plotNumber));
			} else {
				observationUnitData = ExperimentalDesignUtil.getObservationUnitData(row, termId, studyEntry);
			}

			observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
		}

		row.setVariables(observationUnitDataMap);
		row.setEnvironmentVariables(new HashMap<>());
		return row;
	}

	private void loadChecksAndTestEntries(final List<StudyEntryDto> studyEntryDtoList, final List<StudyEntryDto> checkList,
		final List<StudyEntryDto> testEntryList) {

		for (final StudyEntryDto studyEntryDto : studyEntryDtoList) {
			final Optional<String> entryType = studyEntryDto.getStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId());
			if (entryType.isPresent()) {
				if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() == Integer.valueOf(entryType.get())) {
					testEntryList.add(studyEntryDto);
				} else {
					checkList.add(studyEntryDto);
				}
			}
		}
	}

	private boolean isThereSomethingToMerge(final List<StudyEntryDto> entriesList, final List<StudyEntryDto> checkList,
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

	private List<StudyEntryDto> generateChecksToInsert(final List<StudyEntryDto> checkList, final Integer checkIndex,
		final Integer insertionManner) {
		final List<StudyEntryDto> newList = new ArrayList<>();
		if (insertionManner.equals(InsertionMannerItem.INSERT_ALL_CHECKS.getId())) {
			for (final StudyEntryDto checkGermplasm : checkList) {
				newList.add(SerializationUtils.clone(checkGermplasm));
			}
		} else {
			final int checkListIndex = checkIndex % checkList.size();
			final StudyEntryDto checkGermplasm = checkList.get(checkListIndex);
			newList.add(SerializationUtils.clone(checkGermplasm));
		}
		return newList;
	}

	private List<StudyEntryDto> mergeTestAndCheckEntries(final List<StudyEntryDto> testEntryList,
		final List<StudyEntryDto> checkList, final Integer startingIndex, final Integer spacing, final Integer insertionManner) {

		if (!this.isThereSomethingToMerge(testEntryList, checkList, startingIndex, spacing)) {
			return testEntryList;
		}

		final List<StudyEntryDto> newList = new ArrayList<>();

		int primaryEntry = 1;
		boolean isStarted = Boolean.FALSE;
		boolean shouldInsert = Boolean.FALSE;
		int checkIndex = 0;
		int intervalEntry = 0;
		for (final StudyEntryDto primaryGermplasm : testEntryList) {
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
				final List<StudyEntryDto> checks = this.generateChecksToInsert(checkList, checkIndex, insertionManner);
				checkIndex++;
				newList.addAll(checks);
			}
			final StudyEntryDto primaryNewGermplasm = SerializationUtils.clone(primaryGermplasm);
			primaryNewGermplasm.getProperties().put(TermId.ENTRY_TYPE.getId(), new StudyEntryPropertyData(null, TermId.ENTRY_TYPE.getId(),
					String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId())));

			newList.add(primaryNewGermplasm);

			primaryEntry++;
		}

		return newList;
	}

}
