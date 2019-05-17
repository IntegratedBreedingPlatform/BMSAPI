package org.ibp.api.brapi.v1.search;

import com.fasterxml.jackson.annotation.JsonView;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.search_request.GermplasmSearchRequestDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.search.SearchRequest;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.germplasm.Germplasm;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

@Api(value = "BrAPI Search Services")
@Controller
public class SearchResourceBrapi {

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private SearchRequestService searchRequestService;

	@ApiOperation(value = "Post germplasm search", notes = "Post germplasm search")
	@RequestMapping(value = "/{crop}/brapi/v1/search/germplasm", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Integer> postSearchGermplasm(
		@PathVariable final String crop, @RequestBody final GermplasmSearchRequestDto germplasmSearchRequestDto) {
		final SearchRequest searchRequest =
			this.searchRequestService.saveSearchRequest(germplasmSearchRequestDto, GermplasmSearchRequestDto.class);
		return new ResponseEntity<>(searchRequest.getRequestId(), HttpStatus.OK);

	}

	@ApiOperation(value = "Post germplasm search", notes = "Get germplasm search")
	@RequestMapping(value = "/{crop}/brapi/v1/search/germplasm/{searchResulstDbid}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Germplasm.View.SearchGermplasmConfigurationBrapi.class)
	public ResponseEntity<EntityListResponse<Germplasm>> getSearchGermplasm(
		@PathVariable final String crop, @PathVariable final Integer searchResulstDbid,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize
	) {

		final GermplasmSearchRequestDto germplasmSearchRequestDTO =
			(GermplasmSearchRequestDto) this.searchRequestService.getSearchRequest(searchResulstDbid, GermplasmSearchRequestDto.class);

		final PagedResult<GermplasmDTO> resultPage = new PaginatedSearch()
			.executeBrapiSearch(
				(currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage),
				(pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize),
				new SearchSpec<GermplasmDTO>() {

					@Override
					public long getCount() {
						return SearchResourceBrapi.this.germplasmService.countGermplasmDTOs(germplasmSearchRequestDTO);
					}

					@Override
					public List<GermplasmDTO> getResults(final PagedResult<GermplasmDTO> pagedResult) {
						return SearchResourceBrapi.this.germplasmService
							.searchGermplasmDTO(germplasmSearchRequestDTO, currentPage, pageSize);
					}
				});

		final List<Germplasm> germplasmList = new ArrayList<>();

		if (resultPage.getPageResults() != null) {
			final ModelMapper mapper = new ModelMapper();
			for (final GermplasmDTO germplasmDTO : resultPage.getPageResults()) {
				final Germplasm germplasm = mapper.map(germplasmDTO, Germplasm.class);
				germplasm.setCommonCropName(crop);
				germplasmList.add(germplasm);
			}
		}

		final Result<Germplasm> results = new Result<Germplasm>().withData(germplasmList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<Germplasm> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}
}
