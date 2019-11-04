package org.ibp.api.java.impl.middleware.design.generator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.BVDesignTrialInstance;
import org.ibp.api.domain.design.ExperimentDesignParameter;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.runner.DesignRunner;
import org.ibp.api.java.impl.middleware.design.util.ExperimentalDesignUtil;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExperimentalDesignProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(ExperimentalDesignProcessor.class);
	private static final List<Integer> EXP_DESIGN_VARIABLE_IDS =
		Arrays.asList(TermId.PLOT_NO.getId(), TermId.REP_NO.getId(), TermId.BLOCK_NO.getId(), TermId.ROW.getId(), TermId.COL.getId());


	@Resource
	private DesignRunner designRunner;

	public List<ObservationUnitRow> generateObservationUnitRows(
		final Set<Integer> trialInstanceForDesignGeneration, final List<MeasurementVariable> generateDesignVariables,
		final List<StudyGermplasmDto> studyGermplasmDtoList, final MainDesign mainDesign, final String entryNumberIdentifier,
		final Map<String, List<String>> treatmentFactorValues, final Map<Integer, Integer> designExpectedEntriesMap) {

		final Integer numberOfTrials = trialInstanceForDesignGeneration.size();
		// Specify number of study instances for BVDesign generation
		mainDesign.getDesign().getParameters()
			.add(new ExperimentDesignParameter(ExperimentDesignGenerator.NUMBER_TRIALS_PARAM, String.valueOf(numberOfTrials), null));
		BVDesignOutput bvOutput;
		try {
			bvOutput = this.designRunner.runBVDesign(mainDesign);
		} catch (final Exception e) {
			ExperimentalDesignProcessor.LOG.error(e.getMessage(), e);
			throw new BVDesignException("experiment.design.bv.exe.error.generate.generic.error");
		}

		if (bvOutput == null || !bvOutput.isSuccess()) {
			throw new BVDesignException("experiment.design.generate.generic.error");
		}

		//Converting studyGermplasmDto List to map
		final Map<Integer, StudyGermplasmDto> studyGermplasmDtoMap =
			studyGermplasmDtoList.stream().collect(Collectors.toMap(StudyGermplasmDto::getEntryNumber,
				Function.identity()));
		final List<ObservationUnitRow> rows = new ArrayList<>();

		final List<Integer> trialInstancesList = new ArrayList<>(trialInstanceForDesignGeneration);
		Collections.sort(trialInstancesList);
		final ListIterator<Integer> trialInstanceIterator = trialInstancesList.listIterator();
		for (final BVDesignTrialInstance instance : bvOutput.getTrialInstances()) {
			final Integer trialInstanceNumber = trialInstanceIterator.next();
			for (final Map<String, String> row : instance.getRows()) {
				final String entryNoValue = row.get(entryNumberIdentifier);
				final Integer entryNumber = StringUtil.parseInt(entryNoValue, null);
				if (entryNumber == null) {
					throw new BVDesignException("experiment.design.bv.exe.error.output.invalid.error");
				}
				final Optional<StudyGermplasmDto> studyGermplasmDto =
					this.findStudyGermplasmDtoByEntryNumberAndChecks(studyGermplasmDtoMap, entryNumber, designExpectedEntriesMap);

				if (!studyGermplasmDto.isPresent()) {
					throw new BVDesignException("experiment.design.bv.exe.error.output.invalid.error");
				}
				final ObservationUnitRow observationUnitRow =
					this.createObservationUnitRow(generateDesignVariables, studyGermplasmDto.get(), row,
						treatmentFactorValues, trialInstanceNumber);
				rows.add(observationUnitRow);
			}
		}
		return rows;
	}



	ObservationUnitRow createObservationUnitRow(
		final List<MeasurementVariable> measurementVariables, final StudyGermplasmDto studyGermplasmDto,
		final Map<String, String> bvEntryMap, final Map<String, List<String>> treatmentFactorValues, final int trialNo) {

		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		observationUnitRow.setTrialInstance(trialNo);
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		final Map<String, ObservationUnitData> environmentObservationUnitDataMap = new HashMap<>();
		ObservationUnitData treatmentLevelData = null;
		ObservationUnitData observationUnitData;

		observationUnitData = new ObservationUnitData(TermId.TRIAL_INSTANCE_FACTOR.getId(), String.valueOf(trialNo));
		observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);

		for (final MeasurementVariable measurementVariable : measurementVariables) {

			final int termId = measurementVariable.getTermId();
			observationUnitData = null;

			if (measurementVariable.getVariableType() == VariableType.ENVIRONMENT_DETAIL
				|| measurementVariable.getVariableType() == VariableType.STUDY_CONDITION) {
				observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), String.valueOf(measurementVariable.getValue()));
				environmentObservationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
			} else {
				if (termId == TermId.ENTRY_NO.getId()) {
					final Integer entryNumber = studyGermplasmDto.getEntryNumber();
					observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), String.valueOf(
						entryNumber));
					observationUnitRow.setEntryNumber(entryNumber);
				} else if (termId == TermId.SOURCE.getId() || termId == TermId.GERMPLASM_SOURCE.getId()) {
					observationUnitData =
						new ObservationUnitData(measurementVariable.getTermId(),
							studyGermplasmDto.getSeedSource() != null ? studyGermplasmDto.getSeedSource() : "");
				} else if (termId == TermId.GROUPGID.getId()) {
					observationUnitData = new ObservationUnitData(measurementVariable.getTermId(),
						studyGermplasmDto.getGroupId() != null ? studyGermplasmDto.getGroupId().toString() : "");
				} else if (termId == TermId.STOCKID.getId()) {
					observationUnitData =
						new ObservationUnitData(measurementVariable.getTermId(),
							studyGermplasmDto.getStockIds() != null ? studyGermplasmDto.getStockIds() : "");
				} else if (termId == TermId.CROSS.getId()) {
					observationUnitData =
						new ObservationUnitData(measurementVariable.getTermId(), studyGermplasmDto.getCross());
				} else if (termId == TermId.DESIG.getId()) {
					observationUnitData =
						new ObservationUnitData(measurementVariable.getTermId(), studyGermplasmDto.getDesignation());
				} else if (termId == TermId.GID.getId()) {
					observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), String.valueOf(studyGermplasmDto.getGermplasmId()));
				} else if (termId == TermId.ENTRY_CODE.getId()) {
					observationUnitData =
						new ObservationUnitData(measurementVariable.getTermId(), studyGermplasmDto.getEntryCode());
				} else if (EXP_DESIGN_VARIABLE_IDS.contains(termId)) {
					observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), bvEntryMap.get(measurementVariable.getName()));
				} else if (termId == TermId.CHECK.getId()) {
					observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), Integer.toString(studyGermplasmDto.getCheckType()));
				} else if (termId == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					observationUnitData =
						new ObservationUnitData(measurementVariable.getTermId(), Integer.toString(trialNo));
				} else if (!StringUtils.isEmpty(measurementVariable.getTreatmentLabel())) {
					if (treatmentLevelData == null) {
						observationUnitData = new ObservationUnitData(measurementVariable.getTermId(),
							bvEntryMap.get(ExperimentalDesignUtil.cleanBVDesignKey(Integer.toString(measurementVariable.getTermId()))));
						treatmentLevelData = observationUnitData;
					} else {
						final String level = treatmentLevelData.getValue();
						if (NumberUtils.isNumber(level)) {
							final int index = Integer.valueOf(level) - 1;
							if (treatmentFactorValues != null && treatmentFactorValues
								.containsKey(String.valueOf(treatmentLevelData.getVariableId()))) {
								final Object tempObj =
									treatmentFactorValues.get(String.valueOf(treatmentLevelData.getVariableId()))
										.get(index);
								String value = "";
								if (tempObj != null) {
									if (tempObj instanceof String) {
										value = (String) tempObj;
									} else {
										value = Integer.toString((Integer) tempObj);
									}
								}
								if (measurementVariable.getDataTypeId() != null
									&& measurementVariable.getDataTypeId().intValue() == TermId.DATE_VARIABLE.getId()) {
									value = DateUtil.convertToDBDateFormat(measurementVariable.getDataTypeId(), value);
									observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), value);
								} else if (measurementVariable.getPossibleValues() != null && !measurementVariable.getPossibleValues()
									.isEmpty() && NumberUtils
									.isNumber(value)) {
									observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), value);
								} else {
									observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), value);
								}
							}
						}
						treatmentLevelData = null;
					}

				} else {
					// meaning non factor
					observationUnitData = new ObservationUnitData(measurementVariable.getTermId(), "");
				}

				observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
			}

		}
		observationUnitRow.setVariables(observationUnitDataMap);
		observationUnitRow.setEnvironmentVariables(environmentObservationUnitDataMap);
		return observationUnitRow;
	}


	Optional<StudyGermplasmDto> findStudyGermplasmDtoByEntryNumberAndChecks(
		final Map<Integer, StudyGermplasmDto> studyGermplasmDtoMap,
		final Integer entryNumber, final Map<Integer, Integer> designExpectedEntriesMap) {

		final Integer resolvedEntryNumber = this.resolveMappedEntryNumber(entryNumber, designExpectedEntriesMap);

		if (studyGermplasmDtoMap.containsKey(resolvedEntryNumber)) {
			return Optional.of(studyGermplasmDtoMap.get(resolvedEntryNumber));
		}

		return Optional.empty();

	}

	Integer resolveMappedEntryNumber(final Integer entryNumber, final Map<Integer, Integer> designExpectedEntriesMap) {

		if (designExpectedEntriesMap.containsKey(entryNumber)) {
			return designExpectedEntriesMap.get(entryNumber);
		}

		return entryNumber;

	}

}
