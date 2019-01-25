package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class SubObservationDatasetLabelPrinting implements LabelPrintingStrategy {

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private DatasetService middlewareDatasetService;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyValidator studyValidator;

	private static Field STUDY_NAME_FIELD;
	private static Field YEAR_FIELD;
	private static Field SEASON_FIELD;
	private static List<Field> DEFAULT_STUDY_DETAILS_FIELDS;

	private static String PLOT = "PLOT";
	private static String OBS_UNIT_ID = "OBS_UNIT_ID";

	@PostConstruct
	void initStaticFields() {
		final String studyNamePropValue = messageSource.getMessage("label.printing.field.study.name", null, LocaleContextHolder.getLocale());
		final String yearPropValue = messageSource.getMessage("label.printing.field.year", null, LocaleContextHolder.getLocale());
		final String seasonNamePropValue = messageSource.getMessage("label.printing.field.season", null, LocaleContextHolder.getLocale());

		STUDY_NAME_FIELD = new Field(studyNamePropValue, studyNamePropValue);
		YEAR_FIELD = new Field(yearPropValue, yearPropValue);
		SEASON_FIELD = new Field(seasonNamePropValue, seasonNamePropValue);

		DEFAULT_STUDY_DETAILS_FIELDS = Arrays.asList(STUDY_NAME_FIELD, YEAR_FIELD, SEASON_FIELD);
	}

	@Override
	public void validateInputData(final LabelsNeededSummaryInput labelsNeededSummaryInput) {
		studyValidator.validate(labelsNeededSummaryInput.getStudyId(), false);
		datasetValidator.validateDataset(labelsNeededSummaryInput.getStudyId(), labelsNeededSummaryInput.getDatasetId(), true);
	}

	@Override
	public LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsNeededSummaryInput labelsNeededSummaryInput) {
		final LabelsNeededSummary labelsNeededSummary = new LabelsNeededSummary();
		final Map<String, Long> observationsByInstance =
				middlewareDatasetService.countObservationsGroupedByInstance(labelsNeededSummaryInput.getDatasetId());
		long totalNumberOfLabelsNeeded = 0;
		for (final String key : observationsByInstance.keySet()) {
			final LabelsNeededSummary.Row row = new LabelsNeededSummary.Row();
			row.setInstanceNumber(key);
			row.setLabelsNeeded(observationsByInstance.get(key));
			row.setSubObservationNumber(observationsByInstance.get(key));
			labelsNeededSummary.addRow(row);
			totalNumberOfLabelsNeeded += observationsByInstance.get(key);
		}
		labelsNeededSummary.setTotalNumberOfLabelsNeeded(totalNumberOfLabelsNeeded);
		return labelsNeededSummary;
	}

	@Override
	public LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary) {
		final String labelsNeededText = messageSource.getMessage("label.printing.labels.needed", null, LocaleContextHolder.getLocale());
		final String environmentText = messageSource.getMessage("label.printing.environment", null, LocaleContextHolder.getLocale());
		final String numberOfSubObsNeededText =
				messageSource.getMessage("label.printing.number.of.subobservations.needed", null, LocaleContextHolder.getLocale());
		final LabelsNeededSummaryResponse response = new LabelsNeededSummaryResponse();
		final List<String> headers = new LinkedList<>();
		headers.add(environmentText);
		headers.add(numberOfSubObsNeededText);
		headers.add(labelsNeededText);
		response.setHeaders(headers);
		final List<Map<String, String>> values = new LinkedList<>();
		for (LabelsNeededSummary.Row row : labelsNeededSummary.getRows()) {
			final Map<String, String> valuesMap = new LinkedHashMap<>();
			valuesMap.put(environmentText, row.getInstanceNumber());
			valuesMap.put(numberOfSubObsNeededText, String.valueOf(row.getSubObservationNumber()));
			valuesMap.put(labelsNeededText, String.valueOf(row.getLabelsNeeded()));
			values.add(valuesMap);
		}
		response.setValues(values);
		response.setTotalNumberOfLabelsNeeded(labelsNeededSummary.getTotalNumberOfLabelsNeeded());
		return response;
	}

	@Override
	public Map<String, String> getOriginResourceMetadata(final LabelsNeededSummaryInput labelsNeededSummaryInput) {
		final StudyDetails study = studyDataManager.getStudyDetails(labelsNeededSummaryInput.getStudyId());
		final DatasetDTO datasetDTO = middlewareDatasetService.getDataset(labelsNeededSummaryInput.getDatasetId());

		final Map<String, String> resultsMap = new LinkedHashMap<>();
		resultsMap.put(messageSource.getMessage("label.printing.name", null, LocaleContextHolder.getLocale()), study.getStudyName());
		resultsMap.put(messageSource.getMessage("label.printing.title", null, LocaleContextHolder.getLocale()), study.getDescription());
		resultsMap.put(messageSource.getMessage("label.printing.objective", null, LocaleContextHolder.getLocale()),
				(study.getObjective() == null) ? StringUtils.EMPTY : study.getObjective());

		resultsMap.put(messageSource.getMessage("label.printing.selected.dataset", null, LocaleContextHolder.getLocale()),
				datasetDTO.getName());
		resultsMap.put(messageSource.getMessage("label.printing.number.of.environments.in.dataset", null, LocaleContextHolder.getLocale()),
				String.valueOf(datasetDTO.getInstances().size()));

		return resultsMap;
	}

	@Override
	public List<LabelType> getAvailableLabelFields(final LabelsNeededSummaryInput labelsNeededSummaryInput) {
		final List<LabelType> labelTypes = new LinkedList<>();

		final String studyDetailsPropValue = messageSource.getMessage("label.printing.study.details", null, LocaleContextHolder.getLocale());
		final String datasetDetailsPropValue = messageSource.getMessage("label.printing.dataset.details", null, LocaleContextHolder.getLocale());

		final DatasetDTO dataSetDTO = middlewareDatasetService.getDataset(labelsNeededSummaryInput.getDatasetId());
		final int environmentDatasetId =
				this.studyDataManager.getDataSetsByType(labelsNeededSummaryInput.getStudyId(), DataSetType.SUMMARY_DATA).get(0).getId();
		final int plotDatasetId = dataSetDTO.getParentDatasetId();

		final List<MeasurementVariable> studyDetailsVariables = this.middlewareDatasetService
				.getMeasurementVariables(labelsNeededSummaryInput.getStudyId(), Arrays.asList(VariableType.STUDY_DETAIL.getId()));
		final List<MeasurementVariable> environmentVariables = this.middlewareDatasetService.getMeasurementVariables(environmentDatasetId,
				Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.STUDY_CONDITION.getId(), VariableType.TRAIT.getId()));
		final List<MeasurementVariable> treatmentFactors =
				this.middlewareDatasetService.getMeasurementVariables(plotDatasetId, Arrays.asList(VariableType.TREATMENT_FACTOR.getId()));
		final List<MeasurementVariable> plotVariables = this.middlewareDatasetService.getMeasurementVariables(plotDatasetId,
				Arrays.asList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId()));
		final List<MeasurementVariable> datasetVariables = this.middlewareDatasetService
				.getMeasurementVariables(labelsNeededSummaryInput.getDatasetId(), Arrays.asList(VariableType.OBSERVATION_UNIT.getId()));

		final LabelType studyDetailsLabelType = new LabelType(studyDetailsPropValue, studyDetailsPropValue);
		final List<Field> studyDetailsFields = new LinkedList<>();
		//Requirement to add Study Name as an available label when in fact it is not a variable.
		studyDetailsFields.addAll(DEFAULT_STUDY_DETAILS_FIELDS);
		studyDetailsFields.addAll(this.transform(studyDetailsVariables));
		studyDetailsFields.addAll(this.transform(environmentVariables));
		studyDetailsFields.addAll(this.transform(treatmentFactors));
		studyDetailsLabelType.setFields(studyDetailsFields);

		final LabelType datasetDetailsLabelType = new LabelType(datasetDetailsPropValue, datasetDetailsPropValue);
		final List<Field> datasetDetailsFields = new LinkedList<>();
		datasetDetailsFields.addAll(transform(plotVariables));
		// Requirement to add SubObs dataset type plus OBS_UNIT_ID when it is not a variable associated to the subObs dataset
		final Field subObsUnitIdfield = new Field(DataSetType.findById(dataSetDTO.getDatasetTypeId()).getReadableName().concat(" ").concat(OBS_UNIT_ID),
				DataSetType.findById(dataSetDTO.getDatasetTypeId()).getReadableName().concat(" ").concat(OBS_UNIT_ID));
		datasetDetailsFields.add(subObsUnitIdfield);
		datasetDetailsFields.addAll(transform(datasetVariables));
		datasetDetailsLabelType.setFields(datasetDetailsFields);

		labelTypes.add(studyDetailsLabelType);
		labelTypes.add(datasetDetailsLabelType);

		return labelTypes;
	}

	private List<Field> transform (final List<MeasurementVariable> measurementVariables) {
		final List<Field> fields = new LinkedList<>();
		for (final MeasurementVariable measurementVariable: measurementVariables) {
			final Field field = new Field(measurementVariable);
			//Requirement to show PLOT OBS_UNIT_ID label when variable = OBS_UNIT_ID in Plot Dataset
			//Which is in fact the only dataset that cointains this variable.
			if (field.getId().equals(String.valueOf(TermId.OBS_UNIT_ID.getId()))){
				field.setName(PLOT.concat(" ").concat(field.getName()));
			}
			fields.add(field);
		}
		return fields;
	}
}
