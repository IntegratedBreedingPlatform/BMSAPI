package org.ibp.api.java.impl.middleware.list.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.list.GermplasmListImportRequestDTO;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchRequest;
import org.generationcp.middleware.api.germplasmlist.search.GermplasmListSearchResponse;
import org.generationcp.middleware.domain.sqlfilter.SqlTextFilter;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.ibp.api.java.germplasm.GermplasmListService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class GermplasmListImportRequestValidator {
	public static final String ALLOWED_LIST_TYPE = "germplasm";
	private static final Integer LIST_NAME_MAX_LENGTH = 50;
	private static final Integer LIST_DESCRIPTION_MAX_LENGTH = 255;
	private static final int MAX_REFERENCE_ID_LENGTH = 2000;
	private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;
	protected BindingResult errors;

	@Autowired
	private UserService userService;

	@Autowired
	private GermplasmListService germplasmListService;

	public BindingResult pruneListsInvalidForImport(final List<GermplasmListImportRequestDTO> importRequestDTOS) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		BaseValidator.checkNotEmpty(importRequestDTOS, "list.import.request.null");

		final Map<GermplasmListImportRequestDTO, Integer> importRequestByIndexMap = IntStream.range(0, importRequestDTOS.size())
			.boxed().collect(Collectors.toMap(importRequestDTOS::get, i -> i));

		final List<Integer> userIds = importRequestDTOS.stream().filter(gl -> StringUtils.isNotEmpty(gl.getListOwnerPersonDbId()))
			.map(gl -> Integer.parseInt(gl.getListOwnerPersonDbId())).collect(Collectors.toList());

		final Map<String, WorkbenchUser> usersMap = CollectionUtils.isEmpty(userIds) ? new HashMap<>()
			: this.userService.getUsersByIds(userIds).stream()
			.collect(Collectors.toMap(user -> user.getUserid().toString(), Function.identity()));
		final List<String> germplasmListNames = new ArrayList<>();

		importRequestDTOS.removeIf(l -> {
			final Integer index = importRequestByIndexMap.get(l) + 1;

			if (this.isGermplasmListBasicInfoInvalid(germplasmListNames, l, index) ||
				this.isAnyExternalReferenceInvalid(l, index) ||
				this.isListOwnerPersonDbIdInvalid(usersMap, l, index) ||
				this.isListTypeInvalid(l, index)) {
				return true;
			}

			// Add current list name on the listNames after it passed all validations
			germplasmListNames.add(l.getListName());

			return false;
		});

		return this.errors;
	}

	boolean isListTypeInvalid(final GermplasmListImportRequestDTO importRequestDTO, final Integer index) {
		if(StringUtils.isNotEmpty(importRequestDTO.getListType()) && !ALLOWED_LIST_TYPE.equalsIgnoreCase(importRequestDTO.getListType())) {
			this.errors.reject("list.import.invalid.list.type", new String[] {index.toString()}, "");
			return true;
		}
		return  false;
	}

	boolean isListOwnerPersonDbIdInvalid(final Map<String, WorkbenchUser> userMap, final GermplasmListImportRequestDTO importRequestDTO,
		final Integer index) {
		if(!StringUtils.isEmpty(importRequestDTO.getListOwnerPersonDbId()) && !userMap.containsKey(importRequestDTO.getListOwnerPersonDbId())) {
			this.errors.reject("list.import.owner.invalid", new String[] {index.toString()}, "");
			return true;
		}
		return false;
	}

	boolean isGermplasmListBasicInfoInvalid(final List<String> germplasmListNames,
		final GermplasmListImportRequestDTO importRequestDTO, final Integer index) {

		if (StringUtils.isEmpty(importRequestDTO.getListName())) {
			this.errors.reject("list.import.name.null", new String[] {index.toString()}, "");
			return true;
		}

		if (importRequestDTO.getListName().length() > LIST_NAME_MAX_LENGTH) {
			this.errors.reject("list.import.name.exceed.length", new String[] {index.toString()}, "");
			return true;
		}

		if (germplasmListNames.contains(importRequestDTO.getListName())) {
			this.errors.reject("list.import.name.duplicate.import", new String[] {index.toString()}, "");
			return true;
		}

		final GermplasmListSearchRequest request = new GermplasmListSearchRequest();
		final SqlTextFilter filter = new SqlTextFilter();
		filter.setType(SqlTextFilter.Type.EXACTMATCH);
		filter.setValue(importRequestDTO.getListName());
		request.setListNameFilter(filter);
		final List<GermplasmListSearchResponse> response = this.germplasmListService.searchGermplasmList(request, null, null);
		if (!CollectionUtils.isEmpty(response)) {
			this.errors.reject("list.import.name.duplicate.not.unique", new String[] {index.toString()}, "");
			return true;
		}

		if (StringUtils.isNotEmpty(importRequestDTO.getListDescription())
			&& importRequestDTO.getListDescription().length() > LIST_DESCRIPTION_MAX_LENGTH) {
			this.errors.reject("list.import.description.exceed.length", new String[] {index.toString()}, "");
			return true;
		}

		if (importRequestDTO.getDateCreated() != null) {
			final Date dateCreated = Util.tryParseDate(importRequestDTO.getDateCreated(), Util.FRONTEND_DATE_FORMAT);
			if (dateCreated == null) {
				this.errors.reject("list.import.date.created.invalid.format", new String[] {index.toString()}, "");
				return true;
			}
		}

		return false;
	}

	private boolean isAnyExternalReferenceInvalid(final GermplasmListImportRequestDTO importRequestDTO, final Integer index) {
		if (importRequestDTO.getExternalReferences() != null) {
			return importRequestDTO.getExternalReferences().stream().anyMatch(r -> {
				if (r == null || StringUtils.isEmpty(r.getReferenceID()) || StringUtils.isEmpty(r.getReferenceSource())) {
					this.errors.reject("list.import.reference.null", new String[] {index.toString()}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceID()) && r.getReferenceID().length() > MAX_REFERENCE_ID_LENGTH) {
					this.errors.reject("list.import.reference.id.exceeded.length", new String[] {index.toString()}, "");
					return true;
				}
				if (StringUtils.isNotEmpty(r.getReferenceSource()) && r.getReferenceSource().length() > MAX_REFERENCE_SOURCE_LENGTH) {
					this.errors.reject("list.import.reference.source.exceeded.length", new String[] {index.toString()},"");
					return true;
				}
				return false;
			});
		}
		return false;
	}
}
