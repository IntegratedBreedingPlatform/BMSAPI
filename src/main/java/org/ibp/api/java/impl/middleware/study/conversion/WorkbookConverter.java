
package org.ibp.api.java.impl.middleware.study.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.ibp.api.domain.study.EnvironmentLevelMeasurement;
import org.ibp.api.domain.study.EnvironmentLevelObservation;
import org.ibp.api.domain.study.EnvironmentLevelVariable;
import org.ibp.api.domain.study.MeasurementImportDTO;
import org.ibp.api.domain.study.ObservationImportDTO;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.impl.middleware.study.StudyBaseFactors;
import org.ibp.api.java.impl.middleware.study.StudyConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts from a front-end rest-domain {@link StudyImportDTO} to a back-end middleware-domain Workbook
 *
 * @author j-alberto
 *
 */
@Component
public class WorkbookConverter implements Converter<StudyImportDTO, Workbook> {

	@Autowired
	private MeasurementVariableConverter measurementVariableConverter;

	private Workbook workbook;
	private Map<Integer, MeasurementVariable> traitVariateMap;

	@Override
	public Workbook convert(final StudyImportDTO source) {

		this.traitVariateMap = new HashMap<>();
		this.workbook = new Workbook();

		this.buildStudyDetails(source);
		this.buildConditions(source);
		this.buildConstants(source);
		this.buildFactors(source);
		this.buildVariates(source);
		this.buildPlotObservations(source);
		this.buildEnvironmentObservations(source);

		return this.workbook;
	}

	private void buildStudyDetails(final StudyImportDTO source) {

		final StudyDetails studyDetails = new StudyDetails();
		final StudyType stype = StudyType.getStudyType(source.getStudyType());
		studyDetails.setStudyType(stype);
		studyDetails.setStudyName(source.getName());
		studyDetails.setObjective(source.getObjective());
		studyDetails.setTitle(source.getTitle());
		studyDetails.setStartDate(source.getStartDate());
		studyDetails.setEndDate(source.getEndDate());
		studyDetails.setSiteName(source.getSiteName());
		studyDetails.setParentFolderId(source.getFolderId());

		this.workbook.setStudyDetails(studyDetails);
	}

	/**
	 * Basic information for Nurseries and Trials
	 *
	 * @param source
	 */
	private void buildConditions(final StudyImportDTO source) {

		final List<MeasurementVariable> conditions = new ArrayList<>();
		conditions.add(StudyConditions.STUDY_NAME.asMeasurementVariable(source.getName()));
		conditions.add(StudyConditions.STUDY_TITLE.asMeasurementVariable(source.getTitle()));
		conditions.add(StudyConditions.START_DATE.asMeasurementVariable(source.getStartDate()));
		conditions.add(StudyConditions.END_DATE.asMeasurementVariable(source.getEndDate()));
		conditions.add(StudyConditions.OBJECTIVE.asMeasurementVariable(source.getObjective()));
		conditions.add(StudyConditions.STUDY_INSTITUTE.asMeasurementVariable(source.getStudyInstitute()));
		this.workbook.setConditions(conditions);
	}

	/**
	 * Constan values across a study, apply for the whole experiment.
	 *
	 * @param source
	 */
	private void buildConstants(final StudyImportDTO source) {

		final List<MeasurementVariable> constants = new ArrayList<>();
		constants.add(StudyBaseFactors.TRIAL_INSTANCE.asFactor());
		this.workbook.setConstants(constants);
	}

	private void buildFactors(final StudyImportDTO source) {

		final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
		factors.add(StudyBaseFactors.ENTRY_NUMBER.asFactor());
		factors.add(StudyBaseFactors.DESIGNATION.asFactor());
		factors.add(StudyBaseFactors.CROSS.asFactor());
		factors.add(StudyBaseFactors.GID.asFactor());
		factors.add(StudyBaseFactors.PLOT_NUMBER.asFactor());
		factors.add(StudyBaseFactors.REPLICATION_NO.asFactor());
		final MeasurementVariable numReps = StudyBaseFactors.NREP.asFactor();
		numReps.setValue(String.valueOf(source.getEnvironmentDetails().getNumberOfReplications()));
		factors.add(numReps);
		this.workbook.setFactors(factors);
	}

	private void buildVariates(final StudyImportDTO source) {
		final List<MeasurementVariable> variates = new ArrayList<>();

		for (final Trait trait : source.getTraits()) {
			final MeasurementVariable variate = this.measurementVariableConverter.convert(trait);
			variates.add(variate);
			this.traitVariateMap.put(trait.getTraitId(), variate);
		}

		this.workbook.setVariates(variates);
	}

	/**
	 * These are the environment level observations (such as Soil PH): <code>Workbook.trialObservations</code>.
	 */
	private void buildEnvironmentObservations(final StudyImportDTO source) {
		final List<MeasurementRow> environmentObservations = new ArrayList<MeasurementRow>();

		final Map<Integer, MeasurementVariable> environmentVariablesMap = new HashMap<>();
		for (final EnvironmentLevelVariable envVar : source.getEnvironmentDetails().getEnvironmentLevelVariables()) {
			final MeasurementVariable measurementVariable = new MeasurementVariable();
			measurementVariable.setTermId(envVar.getVariableId());
			measurementVariable.setName(envVar.getVariableName());
			measurementVariable.setDescription(envVar.getVariableName());
			measurementVariable.setValue(null);
			measurementVariable.setLabel(envVar.getVariableName());
			measurementVariable.setRole(PhenotypicType.VARIATE);
			measurementVariable.setFactor(false);
			environmentVariablesMap.put(envVar.getVariableId(), measurementVariable);
		}

		for (final EnvironmentLevelObservation envObs : source.getEnvironmentDetails().getEnvironmentLevelObservations()) {
			final MeasurementRow row = new MeasurementRow();
			final List<MeasurementData> dataList = new ArrayList<MeasurementData>();

			final MeasurementData instanceData =
					new MeasurementData(StudyBaseFactors.TRIAL_INSTANCE.name(), String.valueOf(envObs.getEnvironmentNumber()));
			instanceData.setMeasurementVariable(StudyBaseFactors.TRIAL_INSTANCE.asFactor());
			dataList.add(instanceData);

			for (final EnvironmentLevelMeasurement measurement : envObs.getMeasurements()) {
				final MeasurementData envVariateData = new MeasurementData();
				envVariateData.setMeasurementVariable(environmentVariablesMap.get(measurement.getVariableId()));
				envVariateData.setLabel(environmentVariablesMap.get(measurement.getVariableId()).getLabel());
				envVariateData.setValue(measurement.getVariableValue());
				dataList.add(envVariateData);
			}

			row.setDataList(dataList);
		}
		this.workbook.setTrialObservations(environmentObservations);
	}

	/**
	 * These are the plot level observations (such as Plant Height): <code>Workbook.observations</code>
	 */
	private void buildPlotObservations(final StudyImportDTO source) {
		final List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		for (final ObservationImportDTO observationUnit : source.getObservations()) {

			final MeasurementRow row = new MeasurementRow();
			final List<MeasurementData> dataList = new ArrayList<MeasurementData>();

			final StudyGermplasm studyGermplasm = source.findStudyGermplasm(observationUnit.getGid());

			final MeasurementData instanceData =
					new MeasurementData(StudyBaseFactors.TRIAL_INSTANCE.name(), String.valueOf(observationUnit.getEnvironmentNumber()));
			instanceData.setMeasurementVariable(StudyBaseFactors.TRIAL_INSTANCE.asFactor());
			dataList.add(instanceData);

			final MeasurementData replicationData =
					new MeasurementData(StudyBaseFactors.REPLICATION_NO.name(), String.valueOf(observationUnit.getReplicationNumber()));
			replicationData.setMeasurementVariable(StudyBaseFactors.REPLICATION_NO.asFactor());
			dataList.add(replicationData);

			final MeasurementData entryData =
					new MeasurementData(StudyBaseFactors.ENTRY_NUMBER.name(), String.valueOf(studyGermplasm.getEntryNumber()));
			entryData.setMeasurementVariable(StudyBaseFactors.ENTRY_NUMBER.asFactor());
			dataList.add(entryData);

			final MeasurementData designationData =
					new MeasurementData(StudyBaseFactors.DESIGNATION.name(), studyGermplasm.getGermplasmListEntrySummary().getDesignation());
			designationData.setMeasurementVariable(StudyBaseFactors.DESIGNATION.asFactor());
			dataList.add(designationData);

			final MeasurementData crossData =
					new MeasurementData(StudyBaseFactors.CROSS.name(), studyGermplasm.getGermplasmListEntrySummary().getCross());
			crossData.setMeasurementVariable(StudyBaseFactors.CROSS.asFactor());
			dataList.add(crossData);

			final MeasurementData gidData =
					new MeasurementData(StudyBaseFactors.GID.name(), studyGermplasm.getGermplasmListEntrySummary().getGid().toString());
			gidData.setMeasurementVariable(StudyBaseFactors.GID.asFactor());
			dataList.add(gidData);

			final MeasurementData plotData =
					new MeasurementData(StudyBaseFactors.PLOT_NUMBER.name(), String.valueOf(observationUnit.getPlotNumber()));
			plotData.setMeasurementVariable(StudyBaseFactors.PLOT_NUMBER.asFactor());
			dataList.add(plotData);

			for (final MeasurementImportDTO measurement : observationUnit.getMeasurements()) {
				final MeasurementData variateData = new MeasurementData();
				variateData.setMeasurementVariable(this.traitVariateMap.get(measurement.getTraitId()));
				variateData.setLabel(this.traitVariateMap.get(measurement.getTraitId()).getLabel());
				variateData.setValue(measurement.getTraitValue());
				dataList.add(variateData);
			}

			row.setDataList(dataList);
			observations.add(row);

			this.workbook.setObservations(observations);
		}
	}

	void setMeasurementVariableConverter(final MeasurementVariableConverter measurementVariableConverter) {
		this.measurementVariableConverter = measurementVariableConverter;
	}
}
