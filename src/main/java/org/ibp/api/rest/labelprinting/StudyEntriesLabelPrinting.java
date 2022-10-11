package org.ibp.api.rest.labelprinting;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.brapi.v1.attribute.AttributeDTO;
import org.generationcp.middleware.api.germplasm.GermplasmAttributeService;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.ibp.api.java.study.StudyEntryService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.ibp.api.rest.labelprinting.domain.SortableFieldDto;
import org.ibp.api.rest.preset.domain.LabelPrintingPresetDTO;
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

	private static final int ATTRIBUTE_DISPLAY_MAX_LENGTH = 200;

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
		final String tempFileName = study.getStudyName().concat("_").concat(StudyEntriesLabelPrinting.ORIG_FINAL_NAME);
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
			if (VariableType.GERMPLASM_DESCRIPTOR.equals(variable.getVariableType())) {
				germplasmFields.add(field);
			} else if (VariableType.ENTRY_DETAIL.equals(variable.getVariableType())) {
				entryDetailsFields.add(field);
			} else if (VariableType.GERMPLASM_PASSPORT.equals(variable.getVariableType()) ||
					   VariableType.GERMPLASM_ATTRIBUTE.equals(variable.getVariableType())) {
				attributeFields.add(field);
			} else if (variable.getTermId() > 0 && null == variable.getVariableType()) {
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
		final List<Map<Integer, String>> results = new LinkedList<>();

		//Get Germplasm names, attributes, entry details data
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();

		final Set<Integer> keys = this.getSelectedFieldIds(labelsGeneratorInput);

		final List<Integer> gids = studyEntryDtos.stream().map(StudyEntryDto::getGid).collect(Collectors.toList());
		this.getAttributeValuesMap(attributeValues, gids);
		this.getNameValuesMap(nameValues, gids);

		final boolean isPdf = FileType.PDF.equals(labelsGeneratorInput.getFileType());

		studyEntryDtos.forEach((studyEntry) -> {
			final Map<Integer, String> row = new HashMap<>();
			keys.forEach((key) -> {
				if (TermId.CROSS.getId() == key) {
					row.put(key,
						truncateValueIfPdf(isPdf, studyEntry.getCross(), StudyEntriesLabelPrinting.NAME_DISPLAY_MAX_LENGTH));
				}else if (studyEntry.getProperties().containsKey(key)) {
					final StudyEntryPropertyData data = studyEntry.getProperties().get(key);
					row.put(key,
						truncateValueIfPdf(isPdf, data.getValue(), StudyEntriesLabelPrinting.NAME_DISPLAY_MAX_LENGTH));
				} else {
					this.getAttributeOrNameDataRowValue(isPdf, studyEntry, attributeValues, nameValues, key, row);
				}

			});
			results.add(row);
		});

		return new LabelsData(TermId.GID.getId(), results);
	}

	private String truncateValueIfPdf(final boolean isPdf, final String value, final int maxLength) {
		return isPdf && StringUtils.length(value) > maxLength ?
			value.substring(0, maxLength) + "..." : value;
	}

	private void getAttributeOrNameDataRowValue(final boolean isPdf, final StudyEntryDto studyEntry, final Map<Integer, Map<Integer, String>> attributeValues,
		final Map<Integer, Map<Integer, String>> nameValues, final int id, final Map<Integer, String> row) {

		final Map<Integer, String> attributeMap = attributeValues.get(studyEntry.getGid());
		final Map<Integer, String> nameMap = nameValues.get(studyEntry.getGid());

		if (attributeMap != null && attributeMap.containsKey(id)) {
			row.put(id, truncateValueIfPdf(isPdf, attributeMap.get(id), StudyEntriesLabelPrinting.ATTRIBUTE_DISPLAY_MAX_LENGTH));
		} else if (nameMap != null && nameMap.containsKey(id)) {
			row.put(id, truncateValueIfPdf(isPdf, nameMap.get(id), StudyEntriesLabelPrinting.NAME_DISPLAY_MAX_LENGTH));
		}
	}

	Set<Integer> getSelectedFieldIds(final LabelsGeneratorInput labelsGeneratorInput) {
		final Set<Integer> keys = labelsGeneratorInput.getFields().stream().flatMap(Collection::stream).collect(Collectors.toSet());

		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				keys.add(TermId.GID.getId());
			} else {
				keys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}
		return keys;
	}

	void getNameValuesMap(final Map<Integer, Map<Integer, String>> nameValues, final List<Integer> gids) {
		final List<GermplasmNameDto> germplasmNames = this.germplasmNameService.getGermplasmNamesByGids(gids);
		germplasmNames.forEach(name -> {
			nameValues.putIfAbsent(name.getGid(), new HashMap<>());
			nameValues.get(name.getGid()).put(name.getNameTypeId(), name.getName());
		});
	}

	void getAttributeValuesMap(final Map<Integer, Map<Integer, String>> attributeValues, final List<Integer> gids) {
		final Map<Integer, List<AttributeDTO>> attributesByGIDsMap = this.germplasmAttributeService.getAttributesByGIDsMap(gids);
		for (final Map.Entry<Integer, List<AttributeDTO>> gidAttributes : attributesByGIDsMap.entrySet()) {
			final Map<Integer, String> attributesMap = new HashMap<>();
			gidAttributes.getValue().forEach(attributeDTO -> attributesMap.put(attributeDTO.getAttributeDbId(), attributeDTO.getValue()));
			attributeValues.put(gidAttributes.getKey(), attributesMap);
		}
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
