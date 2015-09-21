
package org.ibp.api.java.impl.middleware.study.conversion;

import static org.ibp.api.java.impl.middleware.study.StudyConditions.END_DATE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.OBJECTIVE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.START_DATE;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.STUDY_NAME;
import static org.ibp.api.java.impl.middleware.study.StudyConditions.STUDY_TITLE;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.StudyWorkbook;
import org.ibp.api.domain.study.Trait;
import org.ibp.api.java.impl.middleware.study.StudyBaseFactors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts from a front-end rest-domain {@link StudyWorkbook} to a back-end middleware-domain Workbook
 *
 * @author j-alberto
 *
 */
@Component
public class WorkbookConverter implements Converter<StudyWorkbook, Workbook> {

	@Autowired
	MeasurementVariableConverter converter;

	private final Logger LOGGER = LoggerFactory.getLogger(WorkbookConverter.class);
	private Workbook workbook;
	private List<MeasurementVariable> variates;

	@Override
	public Workbook convert(final StudyWorkbook source) {

		this.variates = null;
		this.workbook = new Workbook();

		this.buildStudyDetails(source); // details, done
		this.buildConditions(source); // details2, done
		this.buildConstants(source);
		this.buildFactors(source);
		this.buildVariates(source);
		this.buildObservations(source);

		return this.workbook;
	}

	private void buildStudyDetails(final StudyWorkbook source) {

		final StudyDetails studyDetails = new StudyDetails();

		final StudyType stype = StudyType.getStudyType(source.getStudyType());
		studyDetails.setStudyType(stype);

		studyDetails.setStudyName(source.getName());
		studyDetails.setObjective(source.getObjective());
		studyDetails.setTitle(source.getTitle());
		studyDetails.setStartDate(source.getStartDate());
		studyDetails.setEndDate(source.getEndDate());
		studyDetails.setSiteName(source.getSiteName());

		this.LOGGER.info("setting default folder: 25133 (folder 'JRNurseriesFolder')");
		studyDetails.setParentFolderId(25133);

		this.workbook.setStudyDetails(studyDetails);

	}

	/**
	 * Basic information for Nurseries and Trials
	 *
	 * @param source
	 */
	private void buildConditions(final StudyWorkbook source) {
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
	private void buildConstants(final StudyWorkbook source) {
		final List<MeasurementVariable> constants = new ArrayList<>();
		constants.add(StudyBaseFactors.TRIAL_INSTANCE.asFactor());

		this.workbook.setConstants(constants);

	}

	private void buildFactors(final StudyWorkbook source) {
		final List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();

		factors.add(StudyBaseFactors.ENTRY_NUMBER.asFactor());
		factors.add(StudyBaseFactors.DESIGNATION.asFactor());
		factors.add(StudyBaseFactors.CROSS.asFactor());
		factors.add(StudyBaseFactors.GID.asFactor());
		factors.add(StudyBaseFactors.PLOT_NUMBER.asFactor());
		this.workbook.setFactors(factors);

	}

	private void buildVariates(final StudyWorkbook source) {
		this.variates = new ArrayList<>();

		for (final Trait trait : source.getTraits()) {
			this.variates.add(this.converter.convert(trait));
		}

		this.workbook.setVariates(this.variates);

	}

	private void buildObservations(final StudyWorkbook source) {
		final List<MeasurementRow> observations = new ArrayList<MeasurementRow>();
		List<MeasurementData> dataList;

		for (int numGermEntry = 0; numGermEntry < source.getGermplasms().size(); numGermEntry++) {
			final MeasurementRow row = new MeasurementRow();
			final GermplasmListEntrySummary germ = source.getGermplasms().get(numGermEntry).getGermplasmListEntrySummary();
			dataList = new ArrayList<MeasurementData>();

			final MeasurementData entryData = new MeasurementData(StudyBaseFactors.ENTRY_NUMBER.name(), germ.getEntryCode());
			entryData.setMeasurementVariable(StudyBaseFactors.ENTRY_NUMBER.asFactor());
			dataList.add(entryData);

			final MeasurementData designationData = new MeasurementData(StudyBaseFactors.DESIGNATION.name(), germ.getDesignation());
			designationData.setMeasurementVariable(StudyBaseFactors.DESIGNATION.asFactor());
			dataList.add(designationData);

			final MeasurementData crossData = new MeasurementData(StudyBaseFactors.CROSS.name(), germ.getCross());
			crossData.setMeasurementVariable(StudyBaseFactors.CROSS.asFactor());
			dataList.add(crossData);

			final MeasurementData gidData = new MeasurementData(StudyBaseFactors.GID.name(), germ.getGid().toString());
			gidData.setMeasurementVariable(StudyBaseFactors.GID.asFactor());
			dataList.add(gidData);

			final MeasurementData plotData = new MeasurementData(StudyBaseFactors.PLOT_NUMBER.name(), germ.getEntryCode());
			plotData.setMeasurementVariable(StudyBaseFactors.PLOT_NUMBER.asFactor());
			dataList.add(plotData);

			for (int numVariate = 0; numVariate < this.variates.size(); numVariate++) {
				final MeasurementData variateData = new MeasurementData(this.variates.get(numVariate).getLabel(), ""); // empty value for
				// now, get from
				// trait values
				variateData.setMeasurementVariable(this.variates.get(numVariate));

				final String traitValue = source.getTraitValues()[numGermEntry][numVariate];
				variateData.setValue(traitValue);

				dataList.add(variateData);
			}

			row.setDataList(dataList);
			observations.add(row);

			this.workbook.setObservations(observations);
		}
	}
}
