package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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

	private static List<Field> staticStudyDetailsFields;

	private static List<Field> staticDatasetDetailsFields;

	private static Field studyNameField;

	@PostConstruct
	void initStaticFields() {
		studyNameField = new Field(messageSource.getMessage("label.printing.field.study.name", null, LocaleContextHolder.getLocale()), messageSource.getMessage("label.printing.field.study.name", null, LocaleContextHolder.getLocale()));
		staticStudyDetailsFields = Arrays.asList(studyNameField);
		staticDatasetDetailsFields = new ArrayList<>();
	}

	@Override
	public void validateInputData(final LabelsNeededSummaryInput labelsNeededSummaryInput) {
		studyValidator.validate(labelsNeededSummaryInput.getStudyId(), false);
		datasetValidator.validateDataset(labelsNeededSummaryInput.getStudyId(), labelsNeededSummaryInput.getDatasetId(), true);
	}

	@Override
	public LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsNeededSummaryInput labelsNeededSummaryInput) {
		final LabelsNeededSummary labelsNeededSummary = new LabelsNeededSummary();
		final List<Pair<String, Long>> observationsByInstance =
				middlewareDatasetService.countObservationsGroupedByInstance(labelsNeededSummaryInput.getDatasetId());
		long totalNumberOfLabelsNeeded = 0;
		for (Pair<String, Long> pair : observationsByInstance) {
			final LabelsNeededSummary.Row row = new LabelsNeededSummary.Row();
			row.setInstanceNumber(pair.getLeft());
			row.setLabelsNeeded(pair.getRight());
			row.setSubObservationNumber(pair.getRight());
			labelsNeededSummary.addRow(row);
			totalNumberOfLabelsNeeded += pair.getRight();
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

		final LabelType studyDetailsLabelType = new LabelType(messageSource.getMessage("label.printing.study.details", null, LocaleContextHolder.getLocale()), messageSource.getMessage("label.printing.study.details", null, LocaleContextHolder.getLocale()));
		final List<Field> studyDetailsFields = new LinkedList<>();
		studyDetailsFields.addAll(staticStudyDetailsFields);

		final DatasetDTO dataSetDTO = middlewareDatasetService.getDataset(labelsNeededSummaryInput.getDatasetId());

		final List<MeasurementVariable> studyDetailsVariables =
			this.middlewareDatasetService.getMeasurementVariables(labelsNeededSummaryInput.getStudyId(), Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		studyDetailsFields.addAll(this.transform(studyDetailsVariables));

		final int environmentDatasetId = this.studyDataManager.getDataSetsByType(labelsNeededSummaryInput.getStudyId(), DataSetType.SUMMARY_DATA).get(0).getId();
		final int plotDatasetId = dataSetDTO.getParentDatasetId();

		final List<MeasurementVariable> environmentVariables =
			this.middlewareDatasetService
				.getMeasurementVariables(
					environmentDatasetId, Arrays
						.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
							VariableType.STUDY_CONDITION.getId(), VariableType.TRAIT.getId()));

		studyDetailsFields.addAll(this.transform(environmentVariables));

		final List<MeasurementVariable> treatmentFactors=
			this.middlewareDatasetService.getMeasurementVariables(plotDatasetId, Arrays
				.asList(VariableType.TREATMENT_FACTOR.getId()
					));

		studyDetailsFields.addAll(this.transform(treatmentFactors));

		studyDetailsLabelType.setFields(studyDetailsFields);

		final List<MeasurementVariable> plotVariables =
			this.middlewareDatasetService.getMeasurementVariables(plotDatasetId, Arrays
				.asList(VariableType.EXPERIMENTAL_DESIGN.getId(),
					VariableType.GERMPLASM_DESCRIPTOR.getId()));

		final List<Field> datasetDetailsFields = new LinkedList<>();
		datasetDetailsFields.addAll(transform(plotVariables));

		final List<MeasurementVariable> datasetVariables = this.middlewareDatasetService
			.getMeasurementVariables(labelsNeededSummaryInput.getDatasetId(), Arrays
				.asList(VariableType.OBSERVATION_UNIT.getId()));
		datasetDetailsFields.addAll(transform(datasetVariables));

		final LabelType datasetDetailsLabelType = new LabelType(messageSource.getMessage("label.printing.dataset.details", null, LocaleContextHolder.getLocale()), messageSource.getMessage("label.printing.dataset.details", null, LocaleContextHolder.getLocale()));
		datasetDetailsLabelType.setFields(datasetDetailsFields);
		labelTypes.add(studyDetailsLabelType);
		labelTypes.add(datasetDetailsLabelType);

		return labelTypes;
	}

	// it can be moved to a common place
	private List<Field> transform (final List<MeasurementVariable> measurementVariables) {
		final List<Field> fields = new LinkedList<>();
		for (final MeasurementVariable measurementVariable: measurementVariables) {
			fields.add(new Field(measurementVariable));
		}
		return fields;
	}
}
