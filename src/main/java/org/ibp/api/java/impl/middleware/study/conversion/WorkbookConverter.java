
package org.ibp.api.java.impl.middleware.study.conversion;

import static org.ibp.api.java.impl.middleware.study.StudyConditions.END_DATE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.OBJECTIVE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.START_DATE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.STUDY_NAME;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.STUDY_TITLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.impl.middleware.study.StudyBaseFactors;
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
		this.buildObservations(source);

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
		conditions.add(STUDY_NAME.asMeasurementVariable(source.getName()));
		conditions.add(STUDY_TITLE.asMeasurementVariable(source.getTitle()));
		conditions.add(START_DATE.asMeasurementVariable(source.getStartDate()));
		conditions.add(END_DATE.asMeasurementVariable(source.getEndDate()));
		conditions.add(OBJECTIVE.asMeasurementVariable(source.getObjective()));

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

	private void buildObservations(final StudyImportDTO source) {
		final List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		for (final StudyGermplasm studyGermplasm : source.getGermplasm()) {

			final MeasurementRow row = new MeasurementRow();
			final List<MeasurementData> dataList = new ArrayList<MeasurementData>();
			final GermplasmListEntrySummary germplasm = studyGermplasm.getGermplasmListEntrySummary();

			final MeasurementData entryData = new MeasurementData(StudyBaseFactors.ENTRY_NUMBER.name(), germplasm.getEntryCode());
			entryData.setMeasurementVariable(StudyBaseFactors.ENTRY_NUMBER.asFactor());
			dataList.add(entryData);

			final MeasurementData designationData = new MeasurementData(StudyBaseFactors.DESIGNATION.name(), germplasm.getDesignation());
			designationData.setMeasurementVariable(StudyBaseFactors.DESIGNATION.asFactor());
			dataList.add(designationData);

			final MeasurementData crossData = new MeasurementData(StudyBaseFactors.CROSS.name(), germplasm.getCross());
			crossData.setMeasurementVariable(StudyBaseFactors.CROSS.asFactor());
			dataList.add(crossData);

			final MeasurementData gidData = new MeasurementData(StudyBaseFactors.GID.name(), germplasm.getGid().toString());
			gidData.setMeasurementVariable(StudyBaseFactors.GID.asFactor());
			dataList.add(gidData);

			final MeasurementData plotData = new MeasurementData(StudyBaseFactors.PLOT_NUMBER.name(), germplasm.getEntryCode());
			plotData.setMeasurementVariable(StudyBaseFactors.PLOT_NUMBER.asFactor());
			dataList.add(plotData);

			for (final Trait trait : source.getTraits()) {
				final MeasurementData variateData = new MeasurementData();
				variateData.setMeasurementVariable(this.traitVariateMap.get(trait.getTraitId()));
				variateData.setLabel(this.traitVariateMap.get(trait.getTraitId()).getLabel());
				variateData.setValue(source.findTraitValue(germplasm.getGid(), trait.getTraitId()));
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
