package org.ibp.api.rest.labelprinting;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.dataset.InstanceDetailsDTO;
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
public class ObservationDatasetLabelPrinting extends LabelPrintingStrategy {

	private static final String UNDERSCORE = "_";

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
	private List<Field> defaultTransactioNDetailsFields;

	private static final String OBS_UNIT_ID = "OBS_UNIT_ID";
	private static final String LOCATION_ID = "LOCATION_ID";
	private static final String GID = "GID";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String ENTRY_NO = "ENTRY_NO";

	public static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);

	@PostConstruct
	void initStaticFields() {
		final String studyNamePropValue = this.getMessage("label.printing.field.study.name");
		final String yearPropValue = this.getMessage("label.printing.field.year");
		final String parentagePropValue = this.getMessage("label.printing.field.parentage");
		final String seasonPropValue = this.getMessage("label.printing.field.season");

		this.studyNameField = new Field(LabelPrintingStaticField.STUDY_NAME.getFieldId(), studyNamePropValue, FieldType.STATIC);
		this.yearField = new Field(LabelPrintingStaticField.YEAR.getFieldId(), yearPropValue, FieldType.STATIC);
		this.parentageField = new Field(LabelPrintingStaticField.PARENTAGE.getFieldId(), parentagePropValue, FieldType.STATIC);
		this.seasonField = new Field(TermId.SEASON_VAR.getId(), seasonPropValue, FieldType.VARIABLE);

		this.defaultStudyDetailsFields = Arrays.asList(this.studyNameField, this.yearField);

		this.defaultTransactioNDetailsFields = ObservationLabelPrintingHelper.buildTransactionDetailsFields(this.messageSource);

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
		final List<InstanceDetailsDTO> InstanceDetailsDTOs =
			this.middlewareDatasetService.getInstanceDetails(labelsInfoInput.getDatasetId(), labelsInfoInput.getStudyId());
		long totalNumberOfLabelsNeeded = 0;
		for (final InstanceDetailsDTO instanceDetailsDTO : InstanceDetailsDTOs) {
			final LabelsNeededSummary.Row row =
				new LabelsNeededSummary.Row(instanceDetailsDTO.getEnvironment().toString(), instanceDetailsDTO.getnOfObservations(),
					instanceDetailsDTO.getnOfObservations(),
					instanceDetailsDTO.getnOfReps(),
					instanceDetailsDTO.getnOfEntries());
			labelsNeededSummary.addRow(row);
			totalNumberOfLabelsNeeded += row.getLabelsNeeded();
		}
		labelsNeededSummary.setTotalNumberOfLabelsNeeded(totalNumberOfLabelsNeeded);
		return labelsNeededSummary;
	}

	@Override
	public LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary) {
		final String labelsNeededText = this.getMessage("label.printing.labels.needed");
		final String environmentText = this.getMessage("label.printing.environment");
		final String numberOfEntriesText = this.getMessage("label.printing.number.of.entries.needed");
		final String numberOfRepsText = this.getMessage("label.printing.number.of.reps.needed");
		final List<String> headers = new LinkedList<>();
		headers.add(environmentText);
		headers.add(numberOfEntriesText);
		headers.add(numberOfRepsText);
		headers.add(labelsNeededText);
		final List<Map<String, String>> values = new LinkedList<>();
		for (final LabelsNeededSummary.Row row : labelsNeededSummary.getRows()) {
			final Map<String, String> valuesMap = new LinkedHashMap<>();
			valuesMap.put(environmentText, row.getInstanceNumber());
			valuesMap.put(numberOfEntriesText, String.valueOf(row.getEntries()));
			valuesMap.put(numberOfRepsText, String.valueOf(row.getReps()));
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
		resultsMap.put(
			this.getMessage("label.printing.objective"), //
			(study.getObjective() == null) ? StringUtils.EMPTY : study.getObjective());
		resultsMap.put(this.getMessage("label.printing.selected.dataset"), datasetDTO.getName());
		resultsMap.put(
			this.getMessage("label.printing.number.of.environments.in.dataset"), //
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

		final int plotDatasetId = dataSetDTO.getDatasetId();

		final List<MeasurementVariable> studyDetailsVariables = this.middlewareDatasetService
			.getObservationSetVariables(labelsInfoInput.getStudyId(), Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final List<MeasurementVariable> environmentVariables = this.middlewareDatasetService.getObservationSetVariables(
			environmentDatasetId,
			Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
				VariableType.ENVIRONMENT_CONDITION.getId()));

		final List<MeasurementVariable> treatmentFactors =
			this.middlewareDatasetService.getObservationSetVariables(plotDatasetId, Arrays.asList(VariableType.TREATMENT_FACTOR.getId()));

		final List<MeasurementVariable> plotVariables = this.middlewareDatasetService.getObservationSetVariables(
			plotDatasetId,
			Arrays.asList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId(), VariableType.ENTRY_DETAIL.getId()));

		final List<MeasurementVariable> datasetVariables = this.middlewareDatasetService
			.getObservationSetVariables(
				labelsInfoInput.getDatasetId(), Arrays.asList(VariableType.OBSERVATION_UNIT.getId(), VariableType.SELECTION_METHOD
					.getId(), VariableType.TRAIT.getId()));

		final List<GermplasmNameTypeDTO> germplasmNameTypeDTOs = this.middlewareDatasetService.getDatasetNameTypes(plotDatasetId);

		final LabelType studyDetailsLabelType = new LabelType(studyDetailsPropValue, studyDetailsPropValue);
		final LabelType lotDetailsLabelType = new LabelType(lotDetailsPropValue, lotDetailsPropValue);
		final LabelType transactionDetailsLabelType = new LabelType(transactionDetailsPropValue, transactionDetailsPropValue);
		final LabelType namesType = new LabelType(namesPropValue, namesPropValue);

		final List<Field> nameFields = new LinkedList<>();
		nameFields.addAll(ObservationLabelPrintingHelper.transformNameTypesToFields(germplasmNameTypeDTOs));
		namesType.setFields(nameFields);

		lotDetailsLabelType.setFields(this.defaultLotDetailsFields);
		transactionDetailsLabelType.setFields(this.defaultTransactioNDetailsFields);

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

		final Map<String, Field> termIdFieldMap = Maps.uniqueIndex(labelsGeneratorInput.getAllAvailablefields(), field -> LabelPrintingStrategy.transformToCombinedKey(field));

		final Set<String> combinedKeys = new HashSet<>();
		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				combinedKeys.add(TermId.OBS_UNIT_ID.getId() + UNDERSCORE + FieldType.VARIABLE.getName());
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

		// Data to be exported
		final List<Map<String, String>> results = new LinkedList<>();

		for (final ObservationUnitRow observationUnitRow : observationUnitRows) {
			final Map<String, String> row = new HashMap<>();
			for (final String combinedKey : combinedKeys) {
				final Field field = termIdFieldMap.get(combinedKey);
				final String[] composedKey = combinedKey.split(UNDERSCORE);
				final String fieldTypeName = composedKey[0];
				final Integer fieldId = Integer.valueOf(composedKey[1]);

				if (FieldType.VARIABLE.getName().equals(fieldTypeName)) {
					// Special cases: LOCATION_NAME, PLOT OBS_UNIT_ID, CROP_SEASON_CODE
					if (TermId.getById(fieldId).equals(TermId.LOCATION_ID)) {
						row.put(combinedKey, observationUnitRow.getVariables().get(LOCATION_ID).getValue());
						continue;
					}
					if (TermId.getById(fieldId).equals(TermId.OBS_UNIT_ID)) {
						row.put(combinedKey, observationUnitRow.getVariables().get(OBS_UNIT_ID).getValue());
						continue;
					}
					if (TermId.getById(fieldId).equals(TermId.SEASON_VAR)) {
						final ObservationUnitData observationUnitData =
							observationUnitRow.getEnvironmentVariables().get("Crop_season_Code");
						row.put(
							combinedKey,
							ObservationLabelPrintingHelper.getSeason(observationUnitData != null ? observationUnitData.getValue() : null));
						continue;
					}

					final Optional<ObservationUnitData> observationVariables =
						ObservationLabelPrintingHelper.getObservationUnitData(observationUnitRow.getVariables(), field);
					if (observationVariables.isPresent()) {
						row.put(combinedKey, observationVariables.get().getValue());
						continue;
					}

					final Optional<ObservationUnitData> environmentVariables =
						ObservationLabelPrintingHelper.getObservationUnitData(observationUnitRow.getEnvironmentVariables(), field);
					if (environmentVariables.isPresent()) {
						row.put(combinedKey, environmentVariables.get().getValue());
						continue;
					}

				} else if (FieldType.STATIC.getName().equals(fieldTypeName)) {
					// If it is not a number it is a hardcoded field
					// Year, Study Name, Parentage
					if (LabelPrintingStaticField.YEAR.getFieldId().equals(fieldId)) {
						row.put(combinedKey, (StringUtils.isNotEmpty(study.getStartDate())) ? study.getStartDate().substring(0, 4) : StringUtils.EMPTY);
						continue;
					}
					if (LabelPrintingStaticField.STUDY_NAME.getFieldId().equals(fieldId)) {
						row.put(combinedKey, study.getStudyName());
						continue;
					}
					if (LabelPrintingStaticField.PARENTAGE.getFieldId().equals(fieldId)) {
						final String gid = observationUnitRow.getVariables().get(GID).getValue();
						row.put(combinedKey, this.getPedigree(gid, gidPedigreeMap));
						continue;
					}

					final StudyTransactionsDto studyTransactionsDto = observationUnitDtoTransactionDtoMap.get(observationUnitRow.getObsUnitId());

					//If it does not exist studyTransactionsDto, it isn't necessary to continue
					// filling in the Lots and Transaction data.
					if (studyTransactionsDto == null) {
						continue;
					}

					if (LabelPrintingStaticField.LOT_UID.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getLot().getLotUUID());
						continue;
					}
					if (LabelPrintingStaticField.LOT_ID.getFieldId().equals(fieldId)) {
						row.put(combinedKey, Objects.toString(studyTransactionsDto.getLot().getLotId(), ""));
						continue;
					}

					if (LabelPrintingStaticField.STOCK_ID.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getLot().getStockId());
						continue;
					}
					if (LabelPrintingStaticField.STORAGE_LOCATION_ABBR.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getLot().getLocationAbbr());
						continue;

					}
					if (LabelPrintingStaticField.STORAGE_LOCATION.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getLot().getLocationName());
						continue;

					}
					if (LabelPrintingStaticField.UNITS.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getLot().getUnitName());
						continue;
					}
					if (LabelPrintingStaticField.AVAILABLE_BALANCE.getFieldId().equals(fieldId)) {
						row.put(combinedKey, Objects.toString(studyTransactionsDto.getLot().getAvailableBalance(), ""));
						continue;

					}
					if (LabelPrintingStaticField.LOT_NOTES.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getLot().getNotes());
						continue;
					}

					if (LabelPrintingStaticField.TRN_ID.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getTransactionId().toString());
						continue;

					}
					if (LabelPrintingStaticField.STATUS.getFieldId().equals(fieldId)) {
						row.put(combinedKey, Objects.toString(studyTransactionsDto.getTransactionStatus(), ""));
						continue;

					}
					if (LabelPrintingStaticField.TYPE.getFieldId().equals(fieldId)) {
						row.put(combinedKey, Objects.toString(studyTransactionsDto.getTransactionType(), ""));
						continue;

					}
					if (LabelPrintingStaticField.CREATED.getFieldId().equals(fieldId)) {
						row.put(combinedKey, Objects.toString(studyTransactionsDto.getCreatedDate(), ""));
						continue;

					}
					if (LabelPrintingStaticField.TRN_NOTES.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getNotes());
						continue;

					}
					if (LabelPrintingStaticField.USERNAME.getFieldId().equals(fieldId)) {
						row.put(combinedKey, studyTransactionsDto.getCreatedByUsername());
						continue;

					}

				} else if (FieldType.NAME.getName().equals(fieldTypeName)) {
					final Optional<ObservationUnitData> observationVariables =
						ObservationLabelPrintingHelper.getObservationUnitData(observationUnitRow.getVariables(), field);
					if (observationVariables.isPresent()) {
						row.put(combinedKey, observationVariables.get().getValue());
						continue;
					}
				}
			}
			results.add(row);
		}

		return new LabelsData(FieldType.VARIABLE.getName() + UNDERSCORE + TermId.OBS_UNIT_ID.getId(), results);
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
