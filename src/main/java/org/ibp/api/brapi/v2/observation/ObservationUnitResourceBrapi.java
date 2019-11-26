package org.ibp.api.brapi.v2.observation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.search_request.ObservationUnitsSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.ObservationUnitDto;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Api(value = "BrAPI Observation Unit Services")
@Controller
public class ObservationUnitResourceBrapi {

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private StudyDataManager studyDataManager;

	@ApiOperation(value = "Get Observation Unit search", notes = "Get the results of a Observation Unit search request")
	@RequestMapping(value = "/{crop}/brapi/v1/search/observationunits/{searchResultsDbid}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<ObservationUnitDto>> getSearchObservationUnit(
		@PathVariable final String crop, @PathVariable final String searchResultsDbid,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize
	) {
		final ObservationUnitsSearchRequestDto observationUnitsSearchRequestDto;

		try {
			observationUnitsSearchRequestDto =
				(ObservationUnitsSearchRequestDto) this.searchRequestService
					.getSearchRequest(Integer.valueOf(searchResultsDbid), ObservationUnitsSearchRequestDto.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<>(new Result<>(new ArrayList<ObservationUnitDto>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		final PagedResult<ObservationUnitDto> resultPage = this.getObservationUnitDtoPagedResult(observationUnitsSearchRequestDto, currentPage, pageSize);

		final Result<ObservationUnitDto> results = new Result<ObservationUnitDto>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<ObservationUnitDto> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}

	private PagedResult<ObservationUnitDto> getObservationUnitDtoPagedResult(final ObservationUnitsSearchRequestDto observationUnitsSearchRequestDto,
		final Integer currentPage, final Integer pageSize) {
		final Integer finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		return new PaginatedSearch()
			.executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<ObservationUnitDto>() {

					@Override
					public long getCount() {
						return ObservationUnitResourceBrapi.this.datasetService.countObservationUnitDTOs(observationUnitsSearchRequestDto);
					}

					@Override
					public List<ObservationUnitDto> getResults(final PagedResult<ObservationUnitDto> pagedResult) {
						return ObservationUnitResourceBrapi.this.datasetService
							.searchObservationUnitDTOs(observationUnitsSearchRequestDto, finalPageNumber, finalPageSize);
					}
				});
	}
}
