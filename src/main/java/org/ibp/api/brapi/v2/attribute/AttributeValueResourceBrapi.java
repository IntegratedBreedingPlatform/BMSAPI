package org.ibp.api.brapi.v2.attribute;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeValueDto;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeValueSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.AttributeValueServiceBrapi;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.BrapiSearchDto;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Api(value = "BrAPI v2 Attribute Value Services")
@Controller(value = "AttributeValueResourceBrapiV2")
public class AttributeValueResourceBrapi {

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private AttributeValueServiceBrapi attributeValueServiceBrapi;

	@ApiOperation(value = "Search attribute values", notes = "Submit a search request for attribute values")
	@RequestMapping(value = "/{crop}/brapi/v2/search/attributevalues", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<BrapiSearchDto>> postSearchAttributeValues(
		@PathVariable final String crop,
		@RequestBody final AttributeValueSearchRequestDto attributeValueSearchRequest) {
		final BrapiSearchDto searchDto =
			new BrapiSearchDto(
				this.searchRequestService.saveSearchRequest(attributeValueSearchRequest, AttributeValueSearchRequestDto.class)
					.toString());
		final SingleEntityResponse<BrapiSearchDto> attributeSearchResponse = new SingleEntityResponse<>(searchDto);

		return new ResponseEntity<>(attributeSearchResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get search attributes results", notes = "Get the results of attributes search request")
	@RequestMapping(value = "/{crop}/brapi/v2/search/attributevalues/{searchResultsDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<AttributeValueDto>> getAttributeValuesSearchResults(
		@PathVariable final String crop,
		@PathVariable final String searchResultsDbId,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final AttributeValueSearchRequestDto searchRequestDto;
		try {
			searchRequestDto =
				(AttributeValueSearchRequestDto) this.searchRequestService
					.getSearchRequest(Integer.valueOf(searchResultsDbId), AttributeValueSearchRequestDto.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<AttributeValueDto>(new Result<>(new ArrayList<>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(this.getAttributeValueDtoEntityListResponse(searchRequestDto,
			currentPage, pageSize, programUUID), HttpStatus.OK);

	}

	private EntityListResponse<AttributeValueDto> getAttributeValueDtoEntityListResponse(
		final AttributeValueSearchRequestDto attributeValueSearchDTO,
		final Integer currentPage, final Integer pageSize, final String programUUID) {
		final PagedResult<AttributeValueDto> resultPage =
			this.getAttributeValuesDtoPagedResult(attributeValueSearchDTO, currentPage,
				pageSize, programUUID);

		final Result<AttributeValueDto> results = new Result<AttributeValueDto>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		return new EntityListResponse<>(metadata, results);
	}

	private PagedResult<AttributeValueDto> getAttributeValuesDtoPagedResult(
		final AttributeValueSearchRequestDto attributeValueSearchDTO,
		final Integer currentPage, final Integer pageSize, final String programUUID) {
		final Integer finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		return new PaginatedSearch()
			.executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<AttributeValueDto>() {

					@Override
					public long getCount() {
						return AttributeValueResourceBrapi.this.attributeValueServiceBrapi.countAttributeValues(attributeValueSearchDTO,
							programUUID);
					}

					@Override
					public List<AttributeValueDto> getResults(final PagedResult<AttributeValueDto> pagedResult) {
						return AttributeValueResourceBrapi.this.attributeValueServiceBrapi
							.getAttributeValues(attributeValueSearchDTO, new PageRequest(finalPageNumber, finalPageSize), programUUID);
					}
				});
	}

}
