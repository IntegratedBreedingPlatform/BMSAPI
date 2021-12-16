package org.ibp.api.brapi.v2.sample;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.search_request.brapi.v2.SampleSearchRequestDTO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.sample.SampleObservationDto;
import org.ibp.api.brapi.SampleServiceBrapi;
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

@Api(value = "BrAPI v2 Sample Services")
@Controller(value = "SampleResourceBrapiV2")
public class SampleResourceBrapi {

	@Autowired
	private SampleServiceBrapi sampleServiceBrapi;

	@Autowired
	private SearchRequestService searchRequestService;

	@ApiOperation(value = "Get samples", notes = "Get samples")
	@RequestMapping(value = "/{crop}/brapi/v2/samples", method = RequestMethod.GET)
	@JsonView(BrapiView.BrapiV2.class)
	@ResponseBody
	public ResponseEntity<EntityListResponse<SampleObservationDto>> getSamples(@PathVariable final String crop,
		@ApiParam(value = "the internal DB id for a sample")
		@RequestParam(value = "sampleDbId", required = false) final String sampleDbId,
		@ApiParam(value = "the internal DB id for an observation unit where a sample was taken from")
		@RequestParam(value = "observationUnitDbId", required = false) final String observationUnitDbId,
		@ApiParam(value = "the internal DB id for a plate of samples")
		@RequestParam(value = "plateDbId", required = false) final String plateDbId,
		@ApiParam(value = "the internal DB id for a germplasm")
		@RequestParam(value = "germplasmDbId", required = false) final String germplasmDbId,
		@ApiParam(value = "Filter by study DbId")
		@RequestParam(value = "studyDbId", required = false) final String studyDbId,
		@ApiParam(value = "An external reference ID. Could be a simple string or a URI. (use with externalReferenceSource parameter)")
		@RequestParam(value = "externalReferenceID", required = false) final String externalReferenceID,
		@ApiParam(value = "An identifier for the source system or database of an external reference (use with externalReferenceID parameter)")
		@RequestParam(value = "externalReferenceSource", required = false) final String externalReferenceSource,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page", required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize", required = false) final Integer pageSize) {

		final SampleSearchRequestDTO requestDTO = new SampleSearchRequestDTO(sampleDbId, observationUnitDbId, plateDbId,
			germplasmDbId, studyDbId, externalReferenceID, externalReferenceSource);

		return new ResponseEntity<>(this.getSampleObservationDtoEntityListResponse(requestDTO, currentPage, pageSize), HttpStatus.OK);
	}

	@ApiOperation(value = "Search samples", notes = "Submit a search request for samples")
	@RequestMapping(value = "/{crop}/brapi/v2/search/samples", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<BrapiSearchDto>> postSearchSamples(
		@PathVariable final String crop,
		@RequestBody final SampleSearchRequestDTO samplesSearchRequest) {
		final BrapiSearchDto searchDto =
			new BrapiSearchDto(this.searchRequestService.saveSearchRequest(samplesSearchRequest, SampleSearchRequestDTO.class)
				.toString());
		final SingleEntityResponse<BrapiSearchDto> sampleSearchResponse = new SingleEntityResponse<>(searchDto);

		return new ResponseEntity<>(sampleSearchResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get search samples results", notes = "Get the results of samples search request")
	@RequestMapping(value = "/{crop}/brapi/v2/search/samples/{searchResultsDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<SampleObservationDto>> getSamplesSearchResults(
		@PathVariable final String crop,
		@PathVariable final String searchResultsDbId,
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final SampleSearchRequestDTO samplesSearchRequest;
		try {
			samplesSearchRequest =
				(SampleSearchRequestDTO) this.searchRequestService
					.getSearchRequest(Integer.valueOf(searchResultsDbId), SampleSearchRequestDTO.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<SampleObservationDto>(new Result<>(new ArrayList<>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(this.getSampleObservationDtoEntityListResponse(samplesSearchRequest,
			currentPage, pageSize), HttpStatus.OK);

	}

	private EntityListResponse<SampleObservationDto> getSampleObservationDtoEntityListResponse(
		final SampleSearchRequestDTO sampleSearchDTO,
		final Integer currentPage, final Integer pageSize) {
		final PagedResult<SampleObservationDto> resultPage =
			this.getSamplesDtoPagedResult(sampleSearchDTO, currentPage,
				pageSize);

		final Result<SampleObservationDto> results = new Result<SampleObservationDto>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		return new EntityListResponse<>(metadata, results);
	}

	private PagedResult<SampleObservationDto> getSamplesDtoPagedResult(
		final SampleSearchRequestDTO sampleSearchDTO,
		final Integer currentPage, final Integer pageSize) {
		final Integer finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		return new PaginatedSearch()
			.executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<SampleObservationDto>() {

					@Override
					public long getCount() {
						return SampleResourceBrapi.this.sampleServiceBrapi.countSampleObservations(sampleSearchDTO);
					}

					@Override
					public List<SampleObservationDto> getResults(final PagedResult<SampleObservationDto> pagedResult) {
						return SampleResourceBrapi.this.sampleServiceBrapi
							.getSampleObservations(sampleSearchDTO, new PageRequest(finalPageNumber, finalPageSize));
					}
				});
	}

}
