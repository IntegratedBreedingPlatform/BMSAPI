package org.ibp.api.brapi.v2.germplasm;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeSearchRequest;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.PedigreeServiceBrapi;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.v2.BrapiResponseMessageGenerator;
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
import java.util.Map;

@Api(value = "BrAPI v2.1 Pedigree Services")
@Controller
public class PedigreeResourceBrapi {

	@Autowired
	private PedigreeServiceBrapi pedigreeServiceBrapi;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private BrapiResponseMessageGenerator<PedigreeNodeDTO> responseMessageGenerator;

	@ApiOperation(value = "Send a list of pedigree nodes to update existing information on a server", notes = "Send a list of pedigree nodes to update existing information on a server")
	@RequestMapping(value = "/{crop}/brapi/v2/pedigree", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<EntityListResponse<PedigreeNodeDTO>> updatePedigreeNodes(@PathVariable final String crop,
		@RequestBody final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap) {
		final PedigreeNodesUpdateResponse pedigreeNodesUpdateResponse = this.pedigreeServiceBrapi.updatePedigreeNodes(pedigreeNodeDTOMap);
		final Result<PedigreeNodeDTO> results = new Result<PedigreeNodeDTO>().withData(pedigreeNodesUpdateResponse.getEntityList());
		final Pagination pagination = new Pagination().withPageNumber(BrapiPagedResult.DEFAULT_PAGE_NUMBER).withPageSize(1)
			.withTotalCount(Long.valueOf(pedigreeNodesUpdateResponse.getUpdatedSize())).withTotalPages(1);
		final Metadata metadata = new Metadata().withPagination(pagination)
			.withStatus(this.responseMessageGenerator.getMessagesList(pedigreeNodesUpdateResponse));
		final EntityListResponse<PedigreeNodeDTO> entityListResponse = new EntityListResponse<>(metadata, results);
		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Submit a search request for Pedigree", notes = "Submit a search request for Pedigree")
	@RequestMapping(value = "/{crop}/brapi/v2/search/pedigree", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<BrapiSearchDto>> postSearchPedigree(
		@PathVariable final String crop,
		@RequestBody final PedigreeNodeSearchRequest pedigreeNodeSearchRequest) {
		final BrapiSearchDto searchDto =
			new BrapiSearchDto(this.searchRequestService.saveSearchRequest(pedigreeNodeSearchRequest, PedigreeNodeSearchRequest.class)
				.toString());
		final SingleEntityResponse<BrapiSearchDto> singleGermplasmSearchResponse = new SingleEntityResponse<>(searchDto);

		return new ResponseEntity<>(singleGermplasmSearchResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get the results of a Pedigree search request ", notes = "Get the results of a Pedigree search request ")
	@RequestMapping(value = "/{crop}/brapi/v2/search/pedigree/{searchResultsDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<PedigreeNodeDTO>> getGermplasmSearchResults(
		@PathVariable final String crop,
		@PathVariable final String searchResultsDbId,
		// TODO IBP-6075
		@ApiParam("<strong>(*Ignored)</strong>")
		@RequestParam(value = "page", required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION + " <strong>(*Ignored)</strong>")
		@RequestParam(value = "pageSize", required = false) final Integer pageSize
	) {

		final PedigreeNodeSearchRequest pedigreeNodeSearchRequest;
		try {
			pedigreeNodeSearchRequest =
				(PedigreeNodeSearchRequest) this.searchRequestService
					.getSearchRequest(Integer.valueOf(searchResultsDbId), PedigreeNodeSearchRequest.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<PedigreeNodeDTO>(new Result<>(new ArrayList<>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		final PagedResult<PedigreeNodeDTO> resultPage =
			this.getPedigreeNodeDTOPagedResult(pedigreeNodeSearchRequest, currentPage,
				pageSize);

		final Result<PedigreeNodeDTO> results = new Result<PedigreeNodeDTO>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(currentPage).withPageSize(pageSize)
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<PedigreeNodeDTO> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}

	private PagedResult<PedigreeNodeDTO> getPedigreeNodeDTOPagedResult(final PedigreeNodeSearchRequest pedigreeNodeSearchRequest,
		final Integer currentPage, final Integer pageSize) {
		final Integer finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		return new PaginatedSearch()
			.executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<PedigreeNodeDTO>() {

					@Override
					public long getCount() {
						return PedigreeResourceBrapi.this.pedigreeServiceBrapi.countPedigreeNodes(pedigreeNodeSearchRequest);
					}

					@Override
					public List<PedigreeNodeDTO> getResults(final PagedResult<PedigreeNodeDTO> pagedResult) {
						return PedigreeResourceBrapi.this.pedigreeServiceBrapi
							.searchPedigreeNodes(pedigreeNodeSearchRequest, new PageRequest(finalPageNumber, finalPageSize));
					}
				});
	}

}
