package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.ibp.api.java.study.StudyEntryService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.FieldType;
import org.ibp.api.rest.labelprinting.domain.LabelPrintingFieldUtils;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class StudyEntriesLabelPrinting extends LabelPrintingStrategy {

	private static final int NAME_DISPLAY_MAX_LENGTH = 200;

	private static final List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF, FileType.XLS);

	private static final String ORIG_FINAL_NAME = "entries";

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private StudyEntryService studyEntryService;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private GermplasmNameService germplasmNameService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Override
	void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput, final String programUUID) {
	}

	@Override
	LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsInfoInput labelsInfoInput) {
		return null;
	}

	@Override
	LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary) {
		return null;
	}

	@Override
	OriginResourceMetadata getOriginResourceMetadata(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final StudyDetails study = this.studyDataManager.getStudyDetails(labelsInfoInput.getStudyId());
		final String tempFileName = study.getStudyName().concat(LabelPrintingFieldUtils.UNDERSCORE).concat(StudyEntriesLabelPrinting.ORIG_FINAL_NAME);
		final String fileName = FileNameGenerator.generateFileName(tempFileName);
		return new OriginResourceMetadata(FileUtils.cleanFileName(fileName), new HashMap<>());
	}

	@Override
	List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final List<LabelType> labelTypes = new LinkedList<>();

		// Germplasm Details labels
		final String germplasmDetailsPropValue = this.getMessage("label.printing.germplasm.details");
		final String entryDetailsPropValue = this.getMessage("label.printing.entry.details");
		final String attributesPropValue = this.getMessage("label.printing.attributes.details");
		final String namesPropValue = this.getMessage("label.printing.names.details");

		final LabelType germplasmLabelType = new LabelType(germplasmDetailsPropValue, germplasmDetailsPropValue);
		final LabelType entryDetailsLabelType = new LabelType(entryDetailsPropValue, entryDetailsPropValue);
		final LabelType attributesType = new LabelType(attributesPropValue, attributesPropValue);
		final LabelType namesType = new LabelType(namesPropValue, namesPropValue);

		final List<Field> germplasmFields = new LinkedList<>();
		final List<Field> entryDetailsFields = new LinkedList<>();
		final List<Field> attributeFields = new LinkedList<>();
		final List<Field> nameFields = new LinkedList<>();

		germplasmLabelType.setFields(germplasmFields);
		entryDetailsLabelType.setFields(entryDetailsFields);
		attributesType.setFields(attributeFields);
		namesType.setFields(nameFields);

		labelTypes.add(germplasmLabelType);
		labelTypes.add(entryDetailsLabelType);
		labelTypes.add(attributesType);
		labelTypes.add(namesType);

		final List<MeasurementVariable> variables = this.studyEntryService.getEntryTableHeader(labelsInfoInput.getStudyId());

		variables.forEach((variable) -> {
			final Field field = new Field(variable);
			field.setFieldType(FieldType.VARIABLE);
			if (VariableType.GERMPLASM_DESCRIPTOR.equals(variable.getVariableType())) {
				germplasmFields.add(field);
			} else if (VariableType.ENTRY_DETAIL.equals(variable.getVariableType())) {
				entryDetailsFields.add(field);
			} else if (VariableType.GERMPLASM_PASSPORT.equals(variable.getVariableType()) ||
				VariableType.GERMPLASM_ATTRIBUTE.equals(variable.getVariableType())) {
				attributeFields.add(field);
			} else if (variable.getTermId() > 0 && null == variable.getVariableType()) {
				field.setFieldType(FieldType.NAME);
				nameFields.add(field);
			}
		});

		return labelTypes;
	}

	@Override
	LabelsData getLabelsData(final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		final StudyEntrySearchDto.Filter filter = null;
		Pageable pageable = null;
		List<StudyEntryDto> studyEntryDtos = this.studyEntryService.getStudyEntries(labelsGeneratorInput.getStudyId(), filter, pageable);

		// Data to be exported
		final List<Map<String, String>> results = new LinkedList<>();

		final Set<String> combinedKeys = this.getSelectedFieldIds(labelsGeneratorInput);

		final boolean isPdf = FileType.PDF.equals(labelsGeneratorInput.getFileType());

		studyEntryDtos.forEach((studyEntry) -> {
			final Map<String, String> row = new HashMap<>();
			combinedKeys.forEach((combinedKey) -> {
				final Integer key = LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey);
				if (TermId.CROSS.getId() == key) {
					row.put(combinedKey,
						truncateValueIfPdf(isPdf, studyEntry.getCross(), StudyEntriesLabelPrinting.NAME_DISPLAY_MAX_LENGTH));
				}else if (studyEntry.getProperties().containsKey(key)) {
					final StudyEntryPropertyData data = studyEntry.getProperties().get(key);
					row.put(combinedKey,
						truncateValueIfPdf(isPdf, data.getValue(), StudyEntriesLabelPrinting.NAME_DISPLAY_MAX_LENGTH));
				}

			});
			results.add(row);
		});
		return new LabelsData(LabelPrintingFieldUtils.transformToCombinedKey(FieldType.VARIABLE, TermId.GID.getId()), results);
	}

	private String truncateValueIfPdf(final boolean isPdf, final String value, final int maxLength) {
		return isPdf && StringUtils.length(value) > maxLength ?
			value.substring(0, maxLength) + "..." : value;
	}

	Set<String> getSelectedFieldIds(final LabelsGeneratorInput labelsGeneratorInput) {
		final Set<String> keys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toSet());

		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				keys.add(LabelPrintingFieldUtils.transformToCombinedKey(FieldType.VARIABLE, TermId.GID.getId()));
			} else {
				keys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}
		return keys;
	}

	@Override
	List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
	}

	@Override
	List<SortableFieldDto> getSortableFields() {
		return null;
	}

	@Override
	LabelPrintingPresetDTO getDefaultSetting(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		return null;
	}

	public String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

}
