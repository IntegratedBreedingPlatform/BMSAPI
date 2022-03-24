package org.ibp.api.brapi.v2.list;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.list.GermplasmListImportRequestDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmListSearchRequestDTO;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.GermplasmListDTO;
import org.ibp.api.brapi.GermplasmListServiceBrapi;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v2.BrapiResponseMessageGenerator;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.list.validator.GermplasmListImportRequestValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI v2 List Services")
@Controller(value = "ListResourceBrapi")
public class ListResourceBrapi {

	@Autowired
	private GermplasmListServiceBrapi germplasmListServiceBrapi;

	@Autowired
	private BrapiResponseMessageGenerator<GermplasmListDTO> responseMessageGenerator;

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@ApiOperation(value = "Get filtered set of generic lists", notes = "Get filtered set of generic lists")
	@RequestMapping(value = "/{crop}/brapi/v2/lists", method = RequestMethod.GET)
	@JsonView(BrapiView.BrapiV2.class)
	@ResponseBody
	public ResponseEntity<EntityListResponse<GermplasmListDTO>> getLists(@PathVariable final String crop,
		@ApiParam(value = "The type of objects contained by this generic list(only 'germplasm' listType is supported)")
		@RequestParam(value = "listType", required = false) final String listType,
		@ApiParam(value = "The human readable name of this generic list")
		@RequestParam(value = "listName", required = false) final String listName,
		@ApiParam(value = "The unique ID of this generic list")
		@RequestParam(value = "listDbId", required = false) final String listDbId,
		@ApiParam(value = "The source tag of this generic list")
		@RequestParam(value = "listSource", required = false) final String listSource,
		@ApiParam(value = "An external reference ID. Could be a simple string or a URI. (use with externalReferenceSource parameter)")
		@RequestParam(value = "externalReferenceID", required = false) final String externalReferenceID,
		@ApiParam(value = "An identifier for the source system or database of an external reference (use with externalReferenceID parameter)")
		@RequestParam(value = "externalReferenceSource", required = false) final String externalReferenceSource,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page", required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {

		if(StringUtils.isNotEmpty(listType) && !GermplasmListImportRequestValidator.ALLOWED_LIST_TYPE.equalsIgnoreCase(listType)) {
			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message",
				this.messageSource.getMessage("list.list.type.invalid", null, LocaleContextHolder.getLocale())));
			final Metadata metadata = new Metadata(null, status);
			final EntityListResponse<GermplasmListDTO> entityListResponse = new EntityListResponse<>(metadata, new Result<>());

			return new ResponseEntity<>(entityListResponse, HttpStatus.BAD_REQUEST);
		}

		final GermplasmListSearchRequestDTO	requestDTO = new GermplasmListSearchRequestDTO(listType, listName,
			Collections.singletonList(listDbId), listSource, externalReferenceID, externalReferenceSource);

		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest = new PageRequest(finalPageNumber, finalPageSize);

		final PagedResult<GermplasmListDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<GermplasmListDTO>() {

				@Override
				public long getCount() {
					return ListResourceBrapi.this.germplasmListServiceBrapi.countGermplasmListDTOs(requestDTO);
				}

				@Override
				public List<GermplasmListDTO> getResults(final PagedResult<GermplasmListDTO> pagedResult) {
					return ListResourceBrapi.this.germplasmListServiceBrapi.searchGermplasmListDTOs(requestDTO, pageRequest);
				}
			});

		final Result<GermplasmListDTO> result = new Result<GermplasmListDTO>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<GermplasmListDTO> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Create New List Objects", notes = "Create new list objects in the database")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'LISTS', 'MANAGE_GERMPLASM_LISTS')")
	@RequestMapping(value = "/{crop}/brapi/v2/lists", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<GermplasmListDTO>> createLists(@PathVariable final String crop,
		@RequestBody final List<GermplasmListImportRequestDTO> germplasmListImportRequestDTOS) {
		BaseValidator.checkNotNull(germplasmListImportRequestDTOS, "list.import.request.null");
		final GermplasmListImportResponse
			germplasmListImportResponse = this.germplasmListServiceBrapi.createGermplasmLists(crop, germplasmListImportRequestDTOS);
		final Result<GermplasmListDTO> results = new Result<GermplasmListDTO>().withData(germplasmListImportResponse.getEntityList());

		final Metadata metadata = new Metadata().withStatus(this.responseMessageGenerator.getMessagesList(germplasmListImportResponse));
		final EntityListResponse<GermplasmListDTO> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

}
