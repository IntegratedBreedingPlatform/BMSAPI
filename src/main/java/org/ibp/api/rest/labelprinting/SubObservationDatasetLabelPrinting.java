package org.ibp.api.rest.labelprinting;

import com.google.common.collect.Maps;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Transactional
public class SubObservationDatasetLabelPrinting extends LabelPrintingStrategy {

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

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;


	private static Field STUDY_NAME_FIELD;
	private static Field YEAR_FIELD;
	private static Field PARENTAGE_FIELD;
	private static List<Field> DEFAULT_STUDY_DETAILS_FIELDS;

	private static String PLOT = "PLOT";
	private static String OBS_UNIT_ID = "OBS_UNIT_ID";
	private static String PARENT_OBS_UNIT_ID = "PARENT_OBS_UNIT_ID";
	private static String LOCATION_ID = "LOCATION_ID";
	private static String GID = "GID";
	private static String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static String PLOT_NO = "PLOT_NO";
	private static String ENTRY_NO = "ENTRY_NO";

	private static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV);

	@PostConstruct
	void initStaticFields() {
		final String studyNamePropValue = this.getMessage("label.printing.field.study.name");
		final String yearPropValue = this.getMessage("label.printing.field.year");
		final String parentagePropValue = this.getMessage("label.printing.field.parentage");

		STUDY_NAME_FIELD = new Field(studyNamePropValue, studyNamePropValue);
		YEAR_FIELD = new Field(yearPropValue, yearPropValue);
		PARENTAGE_FIELD = new Field(parentagePropValue, parentagePropValue);

		DEFAULT_STUDY_DETAILS_FIELDS = Arrays.asList(STUDY_NAME_FIELD, YEAR_FIELD);
	}

	@Override
	public void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput) {
		studyValidator.validate(labelsInfoInput.getStudyId(), false);
		datasetValidator.validateDataset(labelsInfoInput.getStudyId(), labelsInfoInput.getDatasetId(), true);
	}

	@Override
	public void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput) {
		this.validateLabelsInfoInputData(labelsGeneratorInput);
		final Set<String> availableKeys = new HashSet<>();
		getAvailableLabelTypes(labelsGeneratorInput)
				.forEach(labelType -> labelType.getFields().forEach(field -> availableKeys.add(field.getId())));
		final Set<String> requestedFields = new HashSet<>();
		int totalRequestedFields = 0;
		for (final List<String> list: labelsGeneratorInput.getFields()) {
			for (final String key: list) {
				requestedFields.add(key);
				totalRequestedFields++;
			}
		}
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		if (requestedFields.isEmpty()) {
			//Error, at least one requested field is needed
			errors.reject("label.fields.selection.empty", StringUtils.EMPTY);
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (!availableKeys.containsAll(requestedFields)) {
			//Error, some of the requested fields are not available to use
			errors.reject("label.fields.invalid", StringUtils.EMPTY);
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (totalRequestedFields != requestedFields.size()) {
			// Error, duplicated requested field
			errors.reject("label.fields.duplicated", StringUtils.EMPTY);
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		if (labelsGeneratorInput.isBarcodeRequired() && !labelsGeneratorInput.isAutomaticBarcode()) {
			//Validate that at least one is selected
			if (labelsGeneratorInput.getBarcodeFields().isEmpty()) {
				errors.reject("barcode.fields.empty", StringUtils.EMPTY
				);
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
			//Validate that selected are availableFields
			if (!availableKeys.containsAll(labelsGeneratorInput.getBarcodeFields())) {
				//Error, some of the requested fields are not available to use
				errors.reject("barcode.fields.invalid", StringUtils.EMPTY);
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
		// Validation for the file name
		if (!FileUtils.isFilenameValid(labelsGeneratorInput.getFileName())) {
			errors.reject("common.error.invalid.filename.windows", StringUtils.EMPTY);
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	@Override
	public LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsInfoInput labelsInfoInput) {
		final LabelsNeededSummary labelsNeededSummary = new LabelsNeededSummary();
		final Map<String, Long> observationsByInstance =
			middlewareDatasetService.countObservationsGroupedByInstance(labelsInfoInput.getDatasetId());
		long totalNumberOfLabelsNeeded = 0;
		for (final String key : observationsByInstance.keySet()) {
			final Long observationsPerInstance = observationsByInstance.get(key);
			final LabelsNeededSummary.Row row = new LabelsNeededSummary.Row(key, observationsPerInstance, observationsPerInstance);
			labelsNeededSummary.addRow(row);
			totalNumberOfLabelsNeeded += observationsPerInstance;
		}
		labelsNeededSummary.setTotalNumberOfLabelsNeeded(totalNumberOfLabelsNeeded);
		return labelsNeededSummary;
	}

	@Override
	public LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary) {
		final String labelsNeededText = this.getMessage("label.printing.labels.needed");
		final String environmentText = this.getMessage("label.printing.environment");
		final String numberOfSubObsNeededText = this.getMessage("label.printing.number.of.subobservations.needed");
		final List<String> headers = new LinkedList<>();
		headers.add(environmentText);
		headers.add(numberOfSubObsNeededText);
		headers.add(labelsNeededText);
		final List<Map<String, String>> values = new LinkedList<>();
		for (LabelsNeededSummary.Row row : labelsNeededSummary.getRows()) {
			final Map<String, String> valuesMap = new LinkedHashMap<>();
			valuesMap.put(environmentText, row.getInstanceNumber());
			valuesMap.put(numberOfSubObsNeededText, String.valueOf(row.getSubObservationNumber()));
			valuesMap.put(labelsNeededText, String.valueOf(row.getLabelsNeeded()));
			values.add(valuesMap);
		}
		return new LabelsNeededSummaryResponse(headers, values, labelsNeededSummary.getTotalNumberOfLabelsNeeded());
	}

	@Override
	public OriginResourceMetadata getOriginResourceMetadata(final LabelsInfoInput labelsInfoInput) {
		final StudyDetails study = studyDataManager.getStudyDetails(labelsInfoInput.getStudyId());
		final DatasetDTO datasetDTO = middlewareDatasetService.getDataset(labelsInfoInput.getDatasetId());

		final String defaultFileName = getDefaultFileName(datasetDTO);

		final Map<String, String> resultsMap = new LinkedHashMap<>();
		resultsMap.put(getMessage("label.printing.name"), study.getStudyName());
		resultsMap.put(getMessage("label.printing.title"), study.getDescription());
		resultsMap.put(getMessage("label.printing.objective"), (study.getObjective() == null) ? StringUtils.EMPTY : study.getObjective());
		resultsMap.put(getMessage("label.printing.selected.dataset"), datasetDTO.getName());
		resultsMap.put(getMessage("label.printing.number.of.environments.in.dataset"),
			String.valueOf(datasetDTO.getInstances().size()));

		return new OriginResourceMetadata(defaultFileName, resultsMap);
	}

	@Override
	public List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput) {
		final List<LabelType> labelTypes = new LinkedList<>();

		final String studyDetailsPropValue = getMessage("label.printing.study.details");
		final String datasetDetailsPropValue = getMessage("label.printing.dataset.details");

		final DatasetDTO dataSetDTO = middlewareDatasetService.getDataset(labelsInfoInput.getDatasetId());
		final int environmentDatasetId =
				this.studyDataManager.getDataSetsByType(labelsInfoInput.getStudyId(), DataSetType.SUMMARY_DATA).get(0).getId();
		final int plotDatasetId = dataSetDTO.getParentDatasetId();

		final List<MeasurementVariable> studyDetailsVariables = this.middlewareDatasetService
				.getMeasurementVariables(labelsInfoInput.getStudyId(), Arrays.asList(VariableType.STUDY_DETAIL.getId()));
		final List<MeasurementVariable> environmentVariables = this.middlewareDatasetService.getMeasurementVariables(environmentDatasetId,
				Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
						VariableType.STUDY_CONDITION.getId(), VariableType.TRAIT.getId()));
		final List<MeasurementVariable> treatmentFactors =
				this.middlewareDatasetService.getMeasurementVariables(plotDatasetId, Arrays.asList(VariableType.TREATMENT_FACTOR.getId()));
		final List<MeasurementVariable> plotVariables = this.middlewareDatasetService.getMeasurementVariables(plotDatasetId,
				Arrays.asList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId()));
		final List<MeasurementVariable> datasetVariables = this.middlewareDatasetService
				.getMeasurementVariables(labelsInfoInput.getDatasetId(), Arrays.asList(VariableType.OBSERVATION_UNIT.getId()));

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
		datasetDetailsFields.add(PARENTAGE_FIELD);
		datasetDetailsLabelType.setFields(datasetDetailsFields);

		labelTypes.add(studyDetailsLabelType);
		labelTypes.add(datasetDetailsLabelType);

		return labelTypes;
	}

	@Override
	public LabelsData getLabelsData(final LabelsGeneratorInput labelsGeneratorInput) {
		final StudyDetails study = studyDataManager.getStudyDetails(labelsGeneratorInput.getStudyId());

		final DatasetDTO dataSetDTO = middlewareDatasetService.getDataset(labelsGeneratorInput.getDatasetId());
		final String subObsDatasetUnitIdFieldKey =
			DataSetType.findById(dataSetDTO.getDatasetTypeId()).getReadableName().concat(" ").concat(OBS_UNIT_ID);

		final Map<String, Field> termIdFieldMap = Maps.uniqueIndex(labelsGeneratorInput.getAllAvailablefields(), Field::getId);

		final Set<String> allRequiredKeys = new HashSet<>();
		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				allRequiredKeys.add(subObsDatasetUnitIdFieldKey);
			} else {
				allRequiredKeys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}
		labelsGeneratorInput.getFields().forEach(f -> allRequiredKeys.addAll(f));

		final Map<String, String> gidPedigreeMap = new HashMap<>();

		final List<ObservationUnitRow> observationUnitRows =
			this.middlewareDatasetService.getAllObservationUnitRows(labelsGeneratorInput.getStudyId(), labelsGeneratorInput.getDatasetId());

		Collections.sort(observationUnitRows,
				Comparator.comparing((ObservationUnitRow o) -> Integer.valueOf(o.getVariables().get(TRIAL_INSTANCE).getValue()))
						.thenComparing(o -> Integer.valueOf(o.getVariables().get(PLOT_NO).getValue()))
						.thenComparing(o -> Integer.valueOf(o.getVariables().get(ENTRY_NO).getValue())));

		final List<Map<String, String>> results = new LinkedList<>();
		for (final ObservationUnitRow observationUnitRow : observationUnitRows) {
			final Map<String, String> row = new HashMap<>();
			for (final String requiredField : allRequiredKeys) {
				final Field field = termIdFieldMap.get(requiredField);
				if (NumberUtils.isNumber(requiredField)) {
					// Special cases: LOCATION_NAME, PLOT OBS_UNIT_ID, CROP_SEASON_CODE
					final Integer termId = Integer.parseInt(requiredField);
					if (TermId.getById(termId).equals(TermId.LOCATION_ID)) {
						row.put(requiredField, observationUnitRow.getVariables().get(LOCATION_ID).getValue());
						continue;
					}
					if (TermId.getById(termId).equals(TermId.OBS_UNIT_ID)) {
						row.put(requiredField, observationUnitRow.getVariables().get(PARENT_OBS_UNIT_ID).getValue());
						continue;
					}
					if (TermId.getById(termId).equals(TermId.SEASON_VAR)) {
						row.put(requiredField, getSeason(observationUnitRow.getVariables().get(field.getName()).getValue()));
						continue;
					}
					row.put(requiredField, observationUnitRow.getVariables().get(field.getName()).getValue());

				} else {
					// If it is not a number it is a hardcoded field
					// Year, Study Name, Parentage, subObsDatasetUnitIdFieldKey
					if (requiredField.equals(YEAR_FIELD.getId())) {
						row.put(requiredField, (StringUtils.isNotEmpty(study.getStartDate())) ? study.getStartDate().substring(0,4): StringUtils.EMPTY);
						continue;
					}
					if (requiredField.equals(STUDY_NAME_FIELD.getId())) {
						row.put(requiredField, study.getStudyName());
						continue;
					}
					if (requiredField.equals(PARENTAGE_FIELD.getId())) {
						final String gid = observationUnitRow.getVariables().get(GID).getValue();
						row.put(requiredField, this.getPedigree(gid, gidPedigreeMap));
						continue;
					}
					if (requiredField.equals(subObsDatasetUnitIdFieldKey)) {
						row.put(subObsDatasetUnitIdFieldKey, observationUnitRow.getVariables().get(OBS_UNIT_ID).getValue());
						continue;
					}
				}
			}
			results.add(row);
		}

		return new LabelsData(subObsDatasetUnitIdFieldKey, results);
	}

	@Override
	public List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
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

	private String getDefaultFileName(final DatasetDTO datasetDTO) {
		final String fileName = "Labels-for-".concat(datasetDTO.getName()).concat("-").concat(String.valueOf(datasetDTO.getInstances().size()))
			.concat("-").concat(DateUtil.getCurrentDateAsStringValue());
		return FileUtils.cleanFileName(fileName);
	}

	private String getMessage(final String code) {
		return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	private String getPedigree(final String gid, final Map<String, String> gidPedigreeMap) {
		String pedigree;
		if (gidPedigreeMap.containsKey(gid)) {
			pedigree = gidPedigreeMap.get(gid);
		} else {
			pedigree = pedigreeService.getCrossExpansion(Integer.valueOf(gid), crossExpansionProperties);
			gidPedigreeMap.put(gid, pedigree);
		}
		return pedigree;
	}

	private String getSeason(final String seasonStr) {
		String value;
		if (seasonStr != null && Integer.parseInt(seasonStr.trim()) == TermId.SEASON_DRY.getId()) {
			value = Season.DRY.getLabel().toUpperCase();
		} else if (seasonStr != null && Integer.parseInt(seasonStr.trim()) == TermId.SEASON_WET.getId()) {
			value = Season.WET.getLabel().toUpperCase();
		} else {
			value = Season.GENERAL.getLabel().toUpperCase();
		}
		return value;
	}
}
