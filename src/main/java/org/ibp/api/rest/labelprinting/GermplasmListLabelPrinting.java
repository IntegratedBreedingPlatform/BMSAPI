package org.ibp.api.rest.labelprinting;

import com.google.common.collect.ImmutableList;
import org.generationcp.commons.util.FileNameGenerator;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchRequest;
import org.generationcp.middleware.api.germplasm.search.GermplasmSearchResponse;
import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
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
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.ibp.api.rest.labelprinting.domain.SortableFieldDto;
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
	private List<Field> defaultEntryDetailsFields;
	private List<Integer> defaultEntryDetailsFieldIds;

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

		this.defaultEntryDetailsFields = this.buildEntryDetailsFields();
		this.defaultEntryDetailsFieldIds = this.defaultEntryDetailsFields.stream().map(Field::getId).collect(Collectors.toList());
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
		germplasmFieldIds.addAll(this.defaultEntryDetailsFieldIds);
		final Set<Integer> keys = this.getSelectedFieldIds(labelsGeneratorInput);
		final boolean fieldsContainsNonGermplasmFields =
			keys.stream().anyMatch(fieldId -> !germplasmFieldIds.contains(fieldId));
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
			new PageRequest(0, this.maxTotalResults, new Sort(Sort.Direction.ASC, GermplasmListStaticColumns.ENTRY_NO.getName()));
		final List<GermplasmListDataSearchResponse> listDataSearchResponseList = this.germplasmListDataService
			.searchGermplasmListData(labelsGeneratorInput.getListId(), new GermplasmListDataSearchRequest(), listDataPageRequest);
		final Map<Integer, GermplasmSearchResponse> germplasmSearchResponseMap = responseList.stream()
			.collect(Collectors.toMap(GermplasmSearchResponse::getGid, Function.identity()));

		// Data to be exported
		final List<Map<Integer, String>> data = new ArrayList<>();
		for (final GermplasmListDataSearchResponse listData : listDataSearchResponseList) {
			final Integer gid = (Integer) listData.getData().get(GermplasmListStaticColumns.GID.getName());
			data.add(this.getDataRow(keys, listData, germplasmSearchResponseMap.get(gid), attributeValues, nameValues, entryDetailValues));
		}

		return new LabelsData(LabelPrintingStaticField.GUID.getFieldId(), data);
	}

	void getEntryDetailValues(final Map<Integer, Map<Integer, String>> entryDetailValues, final Integer listId) {
		final List<GermplasmListDataDetail> germplasmListDataDetails = this.germplasmListDataService.getGermplasmListDataDetailList(listId);
		germplasmListDataDetails.forEach(listDataDetail -> {
			final Integer listDataId = listDataDetail.getListData().getListDataId();
			entryDetailValues.putIfAbsent(listDataId, new HashMap<>());
			entryDetailValues.get(listDataId).put(listDataDetail.getVariableId(), listDataDetail.getValue());
		});
	}

	private List<Field> buildEntryDetailsFields() {
		final String entryNoPropValue = this.getMessage("label.printing.field.germplasm.list.entry.no");
		final String entryCodePropValue = this.getMessage("label.printing.field.germplasm.list.entry.code");
		return ImmutableList.<Field>builder()
			.add(new Field(TermId.ENTRY_NO.getId(), entryNoPropValue))
			.add(new Field(TermId.ENTRY_CODE.getId(), entryCodePropValue))
			.build();
	}

	private List<Field> getEntryDetailsFields(final String programUUID, final Integer listId) {
		final List<Field> entryDetailFields = new ArrayList<>(this.defaultEntryDetailsFields);
		final List<Variable> germplasmEntryDetailVariables = this.germplasmListService
			.getGermplasmListVariables(programUUID, listId, VariableType.ENTRY_DETAIL.getId());

		entryDetailFields.addAll(germplasmEntryDetailVariables.stream()
			.map(variable -> new Field(toKey(variable.getId()), variable.getName()))
			.collect(Collectors.toList()));

		return entryDetailFields;
	}

	Map<Integer, String> getDataRow(
		final Set<Integer> keys, final GermplasmListDataSearchResponse listData,
		final GermplasmSearchResponse germplasmSearchResponse, final Map<Integer, Map<Integer, String>> attributeValues,
		final Map<Integer, Map<Integer, String>> nameValues, final Map<Integer, Map<Integer, String>> entryDetailValues) {

		final Map<Integer, String> columns = new HashMap<>();
		for (final Integer key : keys) {
			final int id = toId(key);
			if (this.germplasmFieldIds.contains(id)) {
				this.getGermplasmFieldDataRowValue(germplasmSearchResponse, columns, key, id);
			} else if (this.pedigreeFieldIds.contains(id)) {
				this.getPedigreeFieldDataRowValue(germplasmSearchResponse, columns, key, id);
			} else if (this.defaultEntryDetailsFieldIds.contains(id)) {
				this.getEntryDetailFieldDataRowValue(listData, columns, key, id);
			} else {
				this.getAttributeOrNameDataRowValue(germplasmSearchResponse, attributeValues, nameValues, columns, key, id);
				this.getEntryDetailDataRowValue(listData, entryDetailValues, columns, key, id);
			}
		}
		return columns;
	}

	public void getEntryDetailDataRowValue(
		final GermplasmListDataSearchResponse listData,
		final Map<Integer, Map<Integer, String>> entryDetailValues, final Map<Integer, String> columns, final Integer key, final int id) {
		// Not part of the fixed columns
		// Entry Details
		final Map<Integer, String> entryDetails = entryDetailValues.get(listData.getListDataId());
		if (entryDetails != null) {
			final String entryDetailValue = entryDetails.get(id);
			if (entryDetailValue != null) {
				columns.put(key, entryDetailValue);
			}
		}
	}

	private void getEntryDetailFieldDataRowValue(
		final GermplasmListDataSearchResponse listData, final Map<Integer, String> columns, final Integer key, final int id) {
		final TermId term = TermId.getById(id);
		switch (term) {
			case ENTRY_NO:
				columns.put(key, Objects.toString(listData.getData().get(GermplasmListStaticColumns.ENTRY_NO.name()), ""));
				return;
			case ENTRY_CODE:
				columns.put(key, Objects.toString(listData.getData().get(GermplasmListStaticColumns.ENTRY_CODE.name()), ""));
				return;
			default:
				//do nothing
		}
	}

	@Override
	public List<SortableFieldDto> getSortableFields() {
		return Collections.emptyList();
	}

	public List<Field> getDefaultEntryDetailsFields() {
		return this.defaultEntryDetailsFields;
	}

	void setMaxTotalResults(final int maxTotalResults) {
		this.maxTotalResults = maxTotalResults;
	}
}
