package org.ibp.api.rest.labelprinting;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.inventory.study.StudyTransactionsService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.FieldType;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.ibp.api.rest.labelprinting.domain.SortableFieldDto;
import org.generationcp.middleware.domain.labelprinting.LabelPrintingPresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
@Transactional
public class SubObservationDatasetLabelPrinting extends ObservationDatasetLabelPrinting {

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
	private DatasetTypeService datasetTypeService;

	@Autowired
	private StudyTransactionsService studyTransactionsService;

	private Field studyNameField;
	private Field yearField;
	private Field parentageField;
	private Field seasonField;
	private List<Field> defaultStudyDetailsFields;
	private List<Field> defaultLotDetailsFields;
	private List<Field> defaultTransactionDetailsFields;

	private static final String OBS_UNIT_ID = "OBS_UNIT_ID";
	private static final String PARENT_OBS_UNIT_ID = "PARENT_OBS_UNIT_ID";
	private static final String LOCATION_ID = "LOCATION_ID";
	private static final String GID = "GID";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String ENTRY_NO = "ENTRY_NO";

	private static final String UNDERSCORE = "_";

	protected static final List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);

	//Variable ids of PI_NAME_ID and COOPERATOR_ID
	static final List<Integer> PAIR_ID_VARIABLES = Arrays.asList(TermId.PI_ID.getId(), TermId.COOPERATOOR_ID.getId());

	@PostConstruct
	void initStaticFields() {
		final String studyNamePropValue = this.getMessage("label.printing.field.study.name");
		final String yearPropValue = this.getMessage("label.printing.field.year");
		final String parentagePropValue = this.getMessage("label.printing.field.parentage");
		final String seasonPropValue = this.getMessage("label.printing.field.season");

		this.studyNameField = new Field(LabelPrintingStaticField.STUDY_NAME.getFieldId(), studyNamePropValue, FieldType.STATIC);
		this.yearField = new Field(LabelPrintingStaticField.YEAR.getFieldId(), yearPropValue, FieldType.STATIC);
		this.parentageField = new Field(LabelPrintingStaticField.PARENTAGE.getFieldId(), parentagePropValue, FieldType.STATIC);
		this.seasonField = new Field(TermId.SEASON_VAR.getId(), seasonPropValue, FieldType.STATIC);
		this.defaultStudyDetailsFields = Arrays.asList(this.studyNameField, this.yearField);

		this.defaultTransactionDetailsFields = ObservationLabelPrintingHelper.buildTransactionDetailsFields(this.messageSource);

		this.defaultLotDetailsFields = ObservationLabelPrintingHelper.buildLotDetailsFields(this.messageSource);

	}

	@Override
	public void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		this.studyValidator.validate(labelsInfoInput.getStudyId(), false);
		this.datasetValidator.validateDataset(labelsInfoInput.getStudyId(), labelsInfoInput.getDatasetId());
		this.datasetValidator.validateObservationDatasetType(labelsInfoInput.getDatasetId());
	}

	@Override
	public void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		super.validateLabelsGeneratorInputData(labelsGeneratorInput, programUUID);
	}

	@Override
	public LabelPrintingPresetDTO getDefaultSetting(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		return null;
	}

	@Override
	public LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsInfoInput labelsInfoInput) {
		final LabelsNeededSummary labelsNeededSummary = new LabelsNeededSummary();
		final Map<String, Long> observationsByInstance =
			this.middlewareDatasetService.countObservationsGroupedByInstance(labelsInfoInput.getDatasetId());
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
		for (final LabelsNeededSummary.Row row : labelsNeededSummary.getRows()) {
			final Map<String, String> valuesMap = new LinkedHashMap<>();
			valuesMap.put(environmentText, row.getInstanceNumber());
			valuesMap.put(numberOfSubObsNeededText, String.valueOf(row.getSubObservationNumber()));
			valuesMap.put(labelsNeededText, String.valueOf(row.getLabelsNeeded()));
			values.add(valuesMap);
		}
		return new LabelsNeededSummaryResponse(headers, values, labelsNeededSummary.getTotalNumberOfLabelsNeeded());
	}

	@Override
	public OriginResourceMetadata getOriginResourceMetadata(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final StudyDetails study = this.studyDataManager.getStudyDetails(labelsInfoInput.getStudyId());
		final DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(labelsInfoInput.getDatasetId());
		final String tempFileName = "Labels-for-".concat(study.getStudyName()).concat("-").concat(datasetDTO.getName());
		final String defaultFileName = FileNameGenerator.generateFileName(FileUtils.cleanFileName(tempFileName));

		final Map<String, String> resultsMap = new LinkedHashMap<>();
		resultsMap.put(this.getMessage("label.printing.name"), study.getStudyName());
		resultsMap.put(this.getMessage("label.printing.title"), study.getDescription());
		resultsMap
			.put(this.getMessage("label.printing.objective"), (study.getObjective() == null) ? StringUtils.EMPTY : study.getObjective());
		resultsMap.put(this.getMessage("label.printing.selected.dataset"), datasetDTO.getName());
		resultsMap.put(
			this.getMessage("label.printing.number.of.environments.in.dataset"),
			String.valueOf(datasetDTO.getInstances().size()));

		return new OriginResourceMetadata(defaultFileName, resultsMap);
	}

	@Override
	public List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final List<LabelType> labelTypes = new LinkedList<>();

		final String studyDetailsPropValue = this.getMessage("label.printing.study.details");
		final String datasetDetailsPropValue = this.getMessage("label.printing.dataset.details");

		final String lotDetailsPropValue = this.getMessage("label.printing.study.lot.list.details");
		final String transactionDetailsPropValue = this.getMessage("label.printing.study.transaction.list.details");

		final String namesPropValue = this.getMessage("label.printing.names.details");

		final DatasetDTO dataSetDTO = this.middlewareDatasetService.getDataset(labelsInfoInput.getDatasetId());
		final int environmentDatasetId =
			this.studyDataManager.getDataSetsByType(labelsInfoInput.getStudyId(), DatasetTypeEnum.SUMMARY_DATA.getId()).get(0).getId();
		final int plotDatasetId = dataSetDTO.getParentDatasetId();

		final List<MeasurementVariable> studyDetailsVariables = this.middlewareDatasetService
			.getObservationSetVariables(labelsInfoInput.getStudyId(), Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final List<MeasurementVariable> environmentVariables =
			this.middlewareDatasetService.getObservationSetVariables(
				environmentDatasetId,
				Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
					VariableType.ENVIRONMENT_CONDITION.getId()));

		final List<MeasurementVariable> treatmentFactors =
			this.middlewareDatasetService.getObservationSetVariables(plotDatasetId, Arrays.asList(VariableType.TREATMENT_FACTOR.getId()));

		final List<MeasurementVariable> plotVariables = this.middlewareDatasetService.getObservationSetVariables(
			plotDatasetId,
			Arrays.asList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.ENTRY_DETAIL.getId()));

		final List<MeasurementVariable> datasetVariables = this.middlewareDatasetService
			.getObservationSetVariables(labelsInfoInput.getDatasetId(), Arrays.asList(VariableType.OBSERVATION_UNIT.getId()));

		final List<GermplasmNameTypeDTO> germplasmNameTypeDTOs = this.middlewareDatasetService.getDatasetNameTypes(plotDatasetId);

		final LabelType studyDetailsLabelType = new LabelType(studyDetailsPropValue, studyDetailsPropValue);
		final LabelType lotDetailsLabelType = new LabelType(lotDetailsPropValue, lotDetailsPropValue);
		final LabelType transactionDetailsLabelType = new LabelType(transactionDetailsPropValue, transactionDetailsPropValue);
		final LabelType namesType = new LabelType(namesPropValue, namesPropValue);

		final List<Field> nameFields = new LinkedList<>();
		nameFields.addAll(ObservationLabelPrintingHelper.transformNameTypesToFields(germplasmNameTypeDTOs));
		namesType.setFields(nameFields);


		lotDetailsLabelType.setFields(this.defaultLotDetailsFields);
		transactionDetailsLabelType.setFields(this.defaultTransactionDetailsFields);

		final List<Field> studyDetailsFields = new LinkedList<>();
		//Requirement to add Study Name as an available label when in fact it is not a variable.
		studyDetailsFields.addAll(this.defaultStudyDetailsFields);
		studyDetailsFields.addAll(ObservationLabelPrintingHelper.transform(studyDetailsVariables));
		studyDetailsFields.addAll(ObservationLabelPrintingHelper.transform(environmentVariables));
		studyDetailsFields.addAll(ObservationLabelPrintingHelper.transform(treatmentFactors));
		studyDetailsLabelType.setFields(studyDetailsFields);

		final LabelType datasetDetailsLabelType = new LabelType(datasetDetailsPropValue, datasetDetailsPropValue);
		final List<Field> datasetDetailsFields = new LinkedList<>();
		datasetDetailsFields.addAll(ObservationLabelPrintingHelper.transform(plotVariables));
		// Requirement to add SubObs dataset type plus OBS_UNIT_ID when it is not a variable associated to the subObs dataset
		final DatasetTypeDTO datasetType = this.datasetTypeService.getDatasetTypeById(dataSetDTO.getDatasetTypeId());
		final Field subObsUnitIdfield = new Field(
			LabelPrintingStaticField.SUB_OBSERVATION_DATASET_OBS_UNIT_ID.getFieldId(),
			datasetType.getName().concat(" ").concat(OBS_UNIT_ID), FieldType.STATIC);
		datasetDetailsFields.add(subObsUnitIdfield);
		datasetDetailsFields.addAll(ObservationLabelPrintingHelper.transform(datasetVariables));
		datasetDetailsFields.add(this.parentageField);

		if (!studyDetailsFields.contains(this.seasonField)) {
			studyDetailsFields.add(this.seasonField);
		}

		datasetDetailsLabelType.setFields(datasetDetailsFields);

		labelTypes.add(studyDetailsLabelType);
		labelTypes.add(datasetDetailsLabelType);
		labelTypes.add(lotDetailsLabelType);
		labelTypes.add(transactionDetailsLabelType);
		ObservationLabelPrintingHelper.removePairIdVariables(labelTypes);
		labelTypes.add(namesType);
		return labelTypes;
	}

	@Override
	public LabelsData getLabelsData(final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		final StudyDetails study = this.studyDataManager.getStudyDetails(labelsGeneratorInput.getStudyId());

		final StudyTransactionsRequest studyTransactionsRequest = new StudyTransactionsRequest();
		final TransactionsSearchDto transactionsSearch = new TransactionsSearchDto();

		transactionsSearch.setPlantingStudyIds(Arrays.asList(labelsGeneratorInput.getStudyId()));
		studyTransactionsRequest.setTransactionsSearch(transactionsSearch);

		final List<StudyTransactionsDto> studyTransactionsDtos =
			this.studyTransactionsService.searchStudyTransactions(labelsGeneratorInput.getStudyId(), studyTransactionsRequest, null);

		final Map<String, StudyTransactionsDto> observationUnitDtoTransactionDtoMap = new HashMap<>();
		studyTransactionsDtos.forEach(studyTransactionsDto -> studyTransactionsDto.getObservationUnits().forEach(
			observationUnitDto -> observationUnitDtoTransactionDtoMap.put(observationUnitDto.getObsUnitId(), studyTransactionsDto)));

		final Map<String, Field> combinedKeyFieldMap =
			Maps.uniqueIndex(labelsGeneratorInput.getAllAvailablefields(), field -> LabelPrintingStrategy.transformToCombinedKey(field));

		final Set<String> combinedKeys = new HashSet<>();
		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				combinedKeys.add(FieldType.STATIC.getName() + UNDERSCORE + LabelPrintingStaticField.SUB_OBSERVATION_DATASET_OBS_UNIT_ID.getFieldId());
			} else {
				combinedKeys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}
		labelsGeneratorInput.getFields().forEach(combinedKeys::addAll);

		final Map<String, String> gidPedigreeMap = new HashMap<>();

		final List<ObservationUnitRow> observationUnitRows =
			this.middlewareDatasetService.getAllObservationUnitRows(labelsGeneratorInput.getStudyId(), labelsGeneratorInput.getDatasetId());

		Collections.sort(
			observationUnitRows,
			Comparator.comparing((ObservationUnitRow o) -> Integer.valueOf(o.getVariables().get(TRIAL_INSTANCE).getValue()))
				.thenComparing(o -> Integer.valueOf(o.getVariables().get(PLOT_NO).getValue()))
				.thenComparing(o -> Integer.valueOf(o.getVariables().get(ENTRY_NO).getValue())));

		final List<Map<String, String>> results = new LinkedList<>();

		for (final ObservationUnitRow observationUnitRow : observationUnitRows) {
			final Map<String, String> row = new HashMap<>();
			for (final String combinedKey : combinedKeys) {
				final Field field = combinedKeyFieldMap.get(combinedKey);

				if (FieldType.VARIABLE.equals(field.getFieldType())) {
					this.getVariableDataRowValue(row, field, combinedKey, observationUnitRow);
				} else if (FieldType.STATIC.equals(field.getFieldType())) {
					if (LabelPrintingStaticField.SUB_OBSERVATION_DATASET_OBS_UNIT_ID.getFieldId().equals(field.getId())) {
						row.put(combinedKey, observationUnitRow.getVariables().get(OBS_UNIT_ID).getValue());
						continue;
					}
					this.getStaticDataRowValue(row, field, combinedKey, observationUnitRow, study, observationUnitDtoTransactionDtoMap,
						gidPedigreeMap);
				} else if (FieldType.NAME.equals(field.getFieldType())) {
					this.getNameDataRowValue(row, field, combinedKey, observationUnitRow);
				}
			}
			results.add(row);
		}

		return new LabelsData(FieldType.STATIC.getName() + UNDERSCORE + LabelPrintingStaticField.SUB_OBSERVATION_DATASET_OBS_UNIT_ID.getFieldId(), results);
	}

	@Override
	public List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
	}

	@Override
	List<SortableFieldDto> getSortableFields() {
		return Collections.emptyList();
	}

	String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
