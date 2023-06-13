package org.ibp.api.brapi.v2.attribute;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.brapi.v2.attribute.AttributeDTO;
import org.generationcp.middleware.domain.search_request.brapi.v2.AttributeSearchRequestDTO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.v2.AttributeServiceBrapi;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.BrapiSearchDto;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.ArrayList;
import java.util.List;

@Api(value = "BrAPI V2 Attribute Services")
@Controller(value = "AttributeResourceBrapiV2")
public class AttributeResourceBrapi {

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private AttributeServiceBrapi attributeServiceBrapi;

	@ApiOperation(value = "Submit a search request for Germplasm `Attributes`", notes = "Submit a search request for Germplasm `Attributes`")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM')")
	@RequestMapping(value = "/{crop}/brapi/v2/search/attributes", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<BrapiSearchDto>> postSearchGermplasmAttributes(
		@PathVariable final String crop,
		@RequestBody final AttributeSearchRequestDTO attributeSearchRequestDTO) {
		final BrapiSearchDto searchDto =
			new BrapiSearchDto(this.searchRequestService.saveSearchRequest(attributeSearchRequestDTO, AttributeSearchRequestDTO.class)
				.toString());
		final SingleEntityResponse<BrapiSearchDto> singleAttributeSearchRequestDTO = new SingleEntityResponse<>(searchDto);

		return new ResponseEntity<>(singleAttributeSearchRequestDTO, HttpStatus.OK);
	}

	@ApiOperation(value = "Get the results of a Germplasm `Attributes` search request", notes = "Get the results of a Germplasm `Attributes` search request")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'GERMPLASM', 'MANAGE_GERMPLASM')")
	@RequestMapping(value = "/{crop}/brapi/v2/search/attribute/{searchResultsDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<AttributeDTO>> getAttributeSearchResults(
		@PathVariable final String crop,
		@PathVariable final String searchResultsDbId,
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final AttributeSearchRequestDTO requestDTO;
		try {
			requestDTO =
				(AttributeSearchRequestDTO) this.searchRequestService
					.getSearchRequest(Integer.valueOf(searchResultsDbId), AttributeSearchRequestDTO.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<AttributeDTO>(new Result<>(new ArrayList<>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest = new PageRequest(finalPageNumber, finalPageSize);

		final PagedResult<AttributeDTO> resultPage =
			new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<AttributeDTO>() {

				@Override
				public long getCount() {
					return AttributeResourceBrapi.this.attributeServiceBrapi.countGermplasmAttributes(requestDTO);
				}

				@Override
				public List<AttributeDTO> getResults(final PagedResult<AttributeDTO> pagedResult) {
					return AttributeResourceBrapi.this.attributeServiceBrapi
						.getGermplasmAttributes(crop, requestDTO, pageRequest);
				}
			});

		final Result<AttributeDTO> result = new Result<AttributeDTO>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<AttributeDTO> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

}
