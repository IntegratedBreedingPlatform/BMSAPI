package org.ibp.api.rest.labelprinting;

import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListMeasurementVariableDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchResponse;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListStaticColumns;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.GermplasmListDataDetail;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmListDataService;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.FieldType;
import org.ibp.api.rest.labelprinting.domain.LabelPrintingFieldUtils;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.ibp.api.rest.labelprinting.domain.SortableFieldDto;
import org.generationcp.middleware.domain.labelprinting.FilePresetConfigurationDTO;
import org.generationcp.middleware.domain.labelprinting.LabelPrintingPresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional
public class GermplasmListLabelPrinting extends GermplasmLabelPrinting {

	static final String LABELS_FOR = "Labels-for-";
	private static final String SORT_BY_ENTRY_NO = "VARIABLE_" + TermId.ENTRY_NO.getId();

	@Autowired
	private GermplasmListService germplasmListService;

	@Autowired
	private GermplasmListDataService germplasmListDataService;

	@Autowired
	private UserService userService;

	@Override
	@PostConstruct
	public void initStaticFields() {

		this.defaultGermplasmDetailsFields = this.buildGermplasmDetailsFields();
		this.germplasmFieldIds = this.defaultGermplasmDetailsFields.stream().map(Field::getId).collect(Collectors.toList());

		this.defaultPedigreeDetailsFields = this.buildPedigreeDetailsFields();
		this.pedigreeFieldIds = this.defaultPedigreeDetailsFields.stream().map(Field::getId).collect(Collectors.toList());

	}

	@Override
	public void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final long germplasmCount =
			this.germplasmListDataService.countSearchGermplasmListData(labelsInfoInput.getListId(), new GermplasmListDataSearchRequest());
		if (germplasmCount > this.maxTotalResults) {
			throw new ApiRequestValidationException(Arrays.asList(
				new ObjectError(
					"", new String[] {"exceed.germplasm.list.export.labels.threshold"}, new Object[] {this.maxTotalResults}, null))
			);
		}
	}

	@Override
	public OriginResourceMetadata getOriginResourceMetadata(final LabelsInfoInput labelsInfoInput, final String programUUID) {

		final GermplasmListDto germplasmListDto = this.germplasmListService.getGermplasmListById(labelsInfoInput.getListId());
		final WorkbenchUser user = this.userService.getUserById(germplasmListDto.getOwnerId());
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmListIds(Collections.singletonList(labelsInfoInput.getListId()));
		final long germplasmCount =
			this.germplasmListDataService.countSearchGermplasmListData(labelsInfoInput.getListId(), new GermplasmListDataSearchRequest());
		final String tempFileName = LABELS_FOR.concat(germplasmListDto.getListName());
		final String defaultFileName = FileNameGenerator.generateFileName(FileUtils.cleanFileName(tempFileName));

		final Map<String, String> resultsMap = new LinkedHashMap<>();
		resultsMap.put(this.getMessage("label.printing.list.name"), germplasmListDto.getListName());
		resultsMap.put(this.getMessage("label.printing.description"), germplasmListDto.getDescription());
		resultsMap.put(this.getMessage("label.printing.owner"), user.getPerson().getDisplayName());
		resultsMap.put(this.getMessage("label.printing.date"),
			Util.getSimpleDateFormat(Util.DATE_AS_NUMBER_FORMAT).format(germplasmListDto.getCreationDate()));
		resultsMap.put(this.getMessage("label.printing.noOfEntries"), String.valueOf(germplasmCount));
		return new OriginResourceMetadata(defaultFileName, resultsMap);
	}

	@Override
	public List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final List<LabelType> labelTypes = new LinkedList<>();
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmListIds(Collections.singletonList(labelsInfoInput.getListId()));
		final List<GermplasmSearchResponse> germplasmSearchResponses = this.germplasmSearchService
			.searchGermplasm(germplasmSearchRequest, null, programUUID);

		// Germplasm Details labels
		final String germplasmPropValue = this.getMessage("label.printing.germplasm.details");
		final LabelType germplasmType = new LabelType(germplasmPropValue, germplasmPropValue);
		germplasmType.setFields(new ArrayList<>(this.defaultGermplasmDetailsFields));
		germplasmType.getFields().addAll(new ArrayList<>(this.defaultPedigreeDetailsFields));
		labelTypes.add(germplasmType);

		this.populateNamesAndAttributesLabelType(programUUID, labelTypes, germplasmSearchResponses);

		// Entry Details labels
		final String entryDetailsPropValue = this.getMessage("label.printing.entry.details");
		final LabelType entryDetailsType = new LabelType(entryDetailsPropValue, entryDetailsPropValue);
		entryDetailsType.setFields(this.getEntryDetailsFields(programUUID, labelsInfoInput.getListId()));
		labelTypes.add(entryDetailsType);

		return labelTypes;
	}

	@Override
	public LabelsData getLabelsData(
		final LabelsGeneratorInput labelsGeneratorInput, final String programUUID) {
		// Get germplasm data
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmListIds(Collections.singletonList(labelsGeneratorInput.getListId()));
		this.setAddedColumnsToSearchRequest(labelsGeneratorInput, germplasmSearchRequest);
		final List<GermplasmSearchResponse> responseList =
			this.germplasmService.searchGermplasm(germplasmSearchRequest, null, programUUID);

		//Get Germplasm names, attributes, entry details data
		final List<Integer> germplasmFieldIds = new ArrayList<>();
		germplasmFieldIds.addAll(this.germplasmFieldIds);
		germplasmFieldIds.addAll(this.pedigreeFieldIds);
		final Set<String> combinedKeys = this.getSelectedFieldIds(labelsGeneratorInput);
		final boolean fieldsContainsNonGermplasmFields =
			combinedKeys.stream().anyMatch(fieldId -> !germplasmFieldIds.contains(fieldId));
		final Map<Integer, Map<Integer, String>> attributeValues = new HashMap<>();
		final Map<Integer, Map<Integer, String>> nameValues = new HashMap<>();
		final Map<Integer, Map<Integer, String>> entryDetailValues = new HashMap<>();
		if (fieldsContainsNonGermplasmFields) {
			final List<Integer> gids = responseList.stream().map(GermplasmSearchResponse::getGid).collect(Collectors.toList());
			this.getAttributeValuesMap(attributeValues, gids);
			this.getNameValuesMap(nameValues, gids);
			this.getEntryDetailValues(entryDetailValues, labelsGeneratorInput.getListId());
		}

		//Get Germplasm List Data
		final PageRequest listDataPageRequest =
			new PageRequest(0, this.maxTotalResults, new Sort(Sort.Direction.ASC, GermplasmListLabelPrinting.SORT_BY_ENTRY_NO));
		final List<GermplasmListDataSearchResponse> listDataSearchResponseList = this.germplasmListDataService
			.searchGermplasmListData(labelsGeneratorInput.getListId(), new GermplasmListDataSearchRequest(), listDataPageRequest);
		final Map<Integer, GermplasmSearchResponse> germplasmSearchResponseMap = responseList.stream()
			.collect(Collectors.toMap(GermplasmSearchResponse::getGid, Function.identity()));

		final boolean isPdf = FileType.PDF.equals(labelsGeneratorInput.getFileType());

		// Data to be exported
		final List<Map<String, String>> data = new ArrayList<>();
		for (final GermplasmListDataSearchResponse listData : listDataSearchResponseList) {
			final Integer gid = (Integer) listData.getData().get(GermplasmListStaticColumns.GID.getName());
			data.add(this.getDataRow(isPdf, combinedKeys, listData, germplasmSearchResponseMap.get(gid), attributeValues, nameValues, entryDetailValues));
		}

		return new LabelsData(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, LabelPrintingStaticField.GUID.getFieldId()), data);
	}

	void getEntryDetailValues(final Map<Integer, Map<Integer, String>> entryDetailValues, final Integer listId) {
		final List<GermplasmListDataDetail> germplasmListDataDetails = this.germplasmListDataService.getGermplasmListDataDetailList(listId);
		germplasmListDataDetails.forEach(listDataDetail -> {
			final Integer listDataId = listDataDetail.getListData().getListDataId();
			entryDetailValues.putIfAbsent(listDataId, new HashMap<>());
			entryDetailValues.get(listDataId).put(listDataDetail.getVariableId(), listDataDetail.getValue());
		});
	}

	private List<Field> getEntryDetailsFields(final String programUUID, final Integer listId) {
		final List<Variable> germplasmEntryDetailVariables = this.germplasmListService
			.getGermplasmListVariables(programUUID, listId, VariableType.ENTRY_DETAIL.getId());

		final List<Field> entryDetailFields = germplasmEntryDetailVariables.stream()
			.map(variable -> new Field(FieldType.VARIABLE, variable.getId(), variable.getName()))
			.collect(Collectors.toList());

		return entryDetailFields;
	}

	Map<String, String> getDataRow(final boolean isPdf,
		final Set<String> combinedKeys, final GermplasmListDataSearchResponse listData,
		final GermplasmSearchResponse germplasmSearchResponse, final Map<Integer, Map<Integer, String>> attributeValues,
		final Map<Integer, Map<Integer, String>> nameValues, final Map<Integer, Map<Integer, String>> entryDetailValues) {

		final Map<String, String> columns = new HashMap<>();
		for (final String combinedKey : combinedKeys) {
			final FieldType fieldType = FieldType.find(LabelPrintingFieldUtils.getFieldTypeNameFromCombinedKey(combinedKey));
			final Integer fieldId = LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey);
			if (FieldType.VARIABLE.equals(fieldType)) {
				this.getDataRowFromVariableFieldType(columns, isPdf, combinedKey, germplasmSearchResponse, attributeValues);
				this.getEntryDetailDataRowValue(columns, combinedKey, listData, entryDetailValues);

			} else if (FieldType.STATIC.equals(fieldType)) {
				this.getDataRowFromStaticFieldType(columns, isPdf, combinedKey, germplasmSearchResponse);
				/*
				 * Germplasm list data stores precalculated pedigree strings with a certain cross expansion level
				 * in grpname
				 */
				if (LabelPrintingStaticField.CROSS.getFieldId().equals(fieldId)) {
					columns.put(combinedKey, Objects.toString(listData.getData().get(GermplasmListStaticColumns.CROSS.name()), ""));
				}
			} else if (FieldType.NAME.equals(fieldType)) {
				this.getDataRowFromNameFieldType(columns, isPdf, combinedKey, germplasmSearchResponse, nameValues);
			}
		}
		return columns;
	}

	public void getEntryDetailDataRowValue(final Map<String, String> columns, final String combinedKey,
		final GermplasmListDataSearchResponse listData,
		final Map<Integer, Map<Integer, String>> entryDetailValues) {
		// Especial case for ENTRY NO.
		final TermId term = TermId.getById(LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey));
		if (TermId.ENTRY_NO.equals(term)) {
			columns.put(combinedKey, Objects.toString(listData.getData().get(GermplasmListLabelPrinting.SORT_BY_ENTRY_NO), ""));
		} else {
			// Not part of the fixed columns
			// Entry Details
			final Map<Integer, String> entryDetails = entryDetailValues.get(listData.getListDataId());
			if (entryDetails != null) {
				final String entryDetailValue = entryDetails.get(LabelPrintingFieldUtils.getFieldIdFromCombinedKey(combinedKey));
				if (entryDetailValue != null) {
					columns.put(combinedKey, entryDetailValue);
				}
			}
		}
	}

	private void getEntryDetailFieldDataRowValue(
		final GermplasmListDataSearchResponse listData, final Map<Integer, String> columns, final Integer key, final int id) {
		final TermId term = TermId.getById(id);
		switch (term) {
			case ENTRY_NO:
				columns.put(key, Objects.toString(listData.getData().get(TermId.ENTRY_NO.name()), ""));
				return;
			default:
				//do nothing
		}
	}

	@Override
	public List<SortableFieldDto> getSortableFields() {
		return Collections.emptyList();
	}

	void setMaxTotalResults(final int maxTotalResults) {
		this.maxTotalResults = maxTotalResults;
	}

	@Override
	public LabelPrintingPresetDTO getDefaultSetting(final LabelsInfoInput labelsInfoInput, final String programUUID) {
		final List<GermplasmListMeasurementVariableDTO> germplasmListMeasurementVariableDTOs =
			this.germplasmListDataService.getGermplasmListDataTableHeader(labelsInfoInput.getListId(), programUUID);
		final ArrayList<String> fields = new ArrayList<>();
		germplasmListMeasurementVariableDTOs.stream().forEach(dto -> {
			final TermId termId = TermId.getById(dto.getTermId());

			switch (termId) {
				case GID:
					fields.add(
						LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.GID.getId()));
					break;
				case GID_ACTIVE_LOTS_COUNT:
					fields.add(
						LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, LabelPrintingStaticField.LOTS.getFieldId()));
					break;
				case GID_AVAILABLE_BALANCE:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.AVAILABLE_INVENTORY.getId()));
					break;
				case GID_UNIT:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.UNITS_INVENTORY.getId()));
					break;
				case IMMEDIATE_SOURCE_GID:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC,
						LabelPrintingStaticField.IMMEDIATE_SOURCE_GID.getFieldId()));
					break;
				case IMMEDIATE_SOURCE_NAME:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC,
						LabelPrintingStaticField.IMMEDIATE_SOURCE_NAME.getFieldId()));
					break;
				case GROUP_SOURCE_GID:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC,
						LabelPrintingStaticField.GROUP_SOURCE_GID.getFieldId()));
					break;
				case GROUP_SOURCE_NAME:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC,
						LabelPrintingStaticField.GROUP_SOURCE_PREFERRED_NAME.getFieldId()));
					break;
				case GUID:
					fields.add(
						LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, LabelPrintingStaticField.GUID.getFieldId()));
					break;
				case GERMPLASM_REFERENCE:
					fields.add(
						LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, LabelPrintingStaticField.REFERENCE.getFieldId()));
					break;
				case CROSS:
					fields.add(
						LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC, LabelPrintingStaticField.CROSS.getFieldId()));
					break;
				case BREEDING_METHOD_NAME:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.BREEDING_METHOD.getId()));
					break;
				case BREEDING_METHOD_ABBREVIATION:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC,
						LabelPrintingStaticField.METHOD_CODE.getFieldId()));
					break;
				case BREEDING_METHOD_GROUP:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.STATIC,
						LabelPrintingStaticField.METHOD_GROUP.getFieldId()));
					break;
				case DESIG:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.PREFERRED_NAME.getId()));
					break;
				case FGID:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.CROSS_FEMALE_GID.getId()));
					break;
				case MGID:
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.CROSS_MALE_GID.getId()));
					break;
				case MALE_PARENT:
					fields.add(
						LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.CROSS_MALE_PREFERRED_NAME.getId()));
					break;
				case FEMALE_PARENT:
					fields.add(
						LabelPrintingFieldUtils.buildCombinedKey(FieldType.VARIABLE, TermId.CROSS_FEMALE_PREFERRED_NAME.getId()));
					break;
				default:
					final FieldType fieldType = dto.getColumnCategory().NAMES.equals(dto.getColumnCategory()) ?
						FieldType.NAME : FieldType.find(dto.getColumnCategory().name());
					fields.add(LabelPrintingFieldUtils.buildCombinedKey(fieldType, dto.getTermId()));
					break;
			}

		});

		final LabelPrintingPresetDTO labelPrintingPresetDTO = new LabelPrintingPresetDTO();
		final FilePresetConfigurationDTO filePresetConfigurationDTO = new FilePresetConfigurationDTO();
		filePresetConfigurationDTO.setOutputType(FileType.XLS.getExtension());
		labelPrintingPresetDTO.setFileConfiguration(filePresetConfigurationDTO);
		labelPrintingPresetDTO.setSelectedFields(Arrays.asList(fields));
		labelPrintingPresetDTO.setBarcodeSetting(new LabelPrintingPresetDTO.BarcodeSetting(false, false, null));
		labelPrintingPresetDTO.setIncludeHeadings(true);
		labelPrintingPresetDTO.setToolSection(ToolSection.GERMPLASM_LIST_LABEL_PRINTING_PRESET.name());
		labelPrintingPresetDTO.setType("LabelPrintingPreset");
		return labelPrintingPresetDTO;
	}
}
