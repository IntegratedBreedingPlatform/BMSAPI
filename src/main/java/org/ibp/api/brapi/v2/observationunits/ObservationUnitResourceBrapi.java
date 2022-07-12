package org.ibp.api.brapi.v2.observationunits;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitImportRequestDto;
import org.generationcp.middleware.domain.search_request.brapi.v2.ObservationUnitsSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitDto;
import org.generationcp.middleware.service.api.phenotype.ObservationUnitSearchRequestDTO;
import org.generationcp.middleware.service.api.study.ObservationLevel;
import org.generationcp.middleware.service.api.study.ObservationLevelFilter;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.v2.BrapiResponseMessageGenerator;
import org.ibp.api.brapi.v2.sample.SampleResourceBrapi;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.BrapiSearchDto;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.observationunits.ObservationUnitService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
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

@Api(value = "BrAPI Observation Unit Services")
@Controller
public class ObservationUnitResourceBrapi {

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private ObservationUnitService observationUnitService;

	@Autowired
	private org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitService middlewareObservationUnitService;

	@Autowired
	private BrapiResponseMessageGenerator<ObservationUnitDto> responseMessageGenerator;


	@ApiOperation(value = "Post observation units search", notes = "Post observation units search")
	@RequestMapping(value = "/{crop}/brapi/v2/search/observationunits", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<BrapiSearchDto>> postSearchObservationUnits(
		@PathVariable final String crop,
		@RequestBody final ObservationUnitsSearchRequestDto observationUnitsSearchRequestDto) {

		final String searchRequestId =
			this.searchRequestService.saveSearchRequest(observationUnitsSearchRequestDto, ObservationUnitsSearchRequestDto.class)
				.toString();

		final BrapiSearchDto searchDto = new BrapiSearchDto(searchRequestId);
		final SingleEntityResponse<BrapiSearchDto> singleObservationUnitsResponse = new SingleEntityResponse<>(searchDto);

		return new ResponseEntity<>(singleObservationUnitsResponse, HttpStatus.OK);

	}

	@ApiOperation(value = "Get Observation Unit search", notes = "Get the results of a Observation Unit search request")
	@RequestMapping(value = "/{crop}/brapi/v2/search/observationunits/{searchResultsDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<ObservationUnitDto>> getObservationUnitsSearch(
		@PathVariable final String crop, @PathVariable final String searchResultsDbId,
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
					.getSearchRequest(Integer.valueOf(searchResultsDbId), ObservationUnitsSearchRequestDto.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<>(new Result<>(new ArrayList<ObservationUnitDto>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		final ModelMapper mapper = ObservationUnitMapper.getInstance();
		final ObservationUnitSearchRequestDTO observationUnitSearchRequestDTO =
			mapper.map(observationUnitsSearchRequestDto, ObservationUnitSearchRequestDTO.class);

		final PagedResult<ObservationUnitDto> resultPage =
			this.getObservationUnitDtoPagedResult(observationUnitSearchRequestDTO, currentPage, pageSize);

		final Result<ObservationUnitDto> results = new Result<ObservationUnitDto>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		final EntityListResponse<ObservationUnitDto> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);

	}

	@ApiOperation(value = "Add new Observation Units", notes = "Add new Observation Units")
	@RequestMapping(value = "/{crop}/brapi/v2/observationunits", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<ObservationUnitDto>> createObservationUnits(@PathVariable final String crop,
		@RequestBody final List<ObservationUnitImportRequestDto> observationUnitImportRequestDtos) {
		BaseValidator.checkNotNull(observationUnitImportRequestDtos, "observation.unit.import.request.null");

		final ObservationUnitImportResponse observationUnitImportResponse =
			this.observationUnitService.createObservationUnits(crop, observationUnitImportRequestDtos);
		final Result<ObservationUnitDto> results =
			new Result<ObservationUnitDto>().withData(observationUnitImportResponse.getEntityList());


		final Metadata metadata = new Metadata().withStatus(this.responseMessageGenerator.getMessagesList(observationUnitImportResponse));
		final EntityListResponse<ObservationUnitDto> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	private PagedResult<ObservationUnitDto> getObservationUnitDtoPagedResult(
		final ObservationUnitSearchRequestDTO phenotypeSearchDTO,
		final Integer currentPage, final Integer pageSize) {
		final Integer finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		return new PaginatedSearch()
			.executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<ObservationUnitDto>() {

					@Override
					public long getCount() {
						return ObservationUnitResourceBrapi.this.observationUnitService.countObservationUnits(phenotypeSearchDTO);
					}

					@Override
					public List<ObservationUnitDto> getResults(final PagedResult<ObservationUnitDto> pagedResult) {
						return ObservationUnitResourceBrapi.this.observationUnitService
							.searchObservationUnits(finalPageSize, finalPageNumber, phenotypeSearchDTO);
					}
				});
	}

	// TODO complete PUT see plantbreeding/API/issues/411
	@ApiOperation(value = "Patch Observation Unit", notes = "Modified some fields from an Observation Unit <p><strong>Note:</strong> non-standard BrAPI call</p>")
	@RequestMapping(value = "/{crop}/brapi/v2/observationunits/{observationUnitDbId}", method = RequestMethod.PATCH)
	public ResponseEntity<SingleEntityResponse<ObservationUnitPatchRequestDTO>> patchObservationUnit(
		@PathVariable final String crop,
		@PathVariable final String observationUnitDbId,
		@RequestBody final ObservationUnitPatchRequestDTO requestDTO) {

		final ModelMapper mapper = new ModelMapper();
		final org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitPatchRequestDTO observationUnitPatchRequestDTO
			= mapper.map(requestDTO, org.generationcp.middleware.api.brapi.v2.observationunit.ObservationUnitPatchRequestDTO.class);
		this.middlewareObservationUnitService.update(observationUnitDbId, observationUnitPatchRequestDTO);

		return new ResponseEntity<>(new SingleEntityResponse<>(requestDTO), HttpStatus.OK);
	}

	@ApiOperation(value = "Get the Observation Levels", notes = "Get the Observation Levels")
	@RequestMapping(value = "/{crop}/brapi/v2/observationlevels", method = RequestMethod.GET)
	public ResponseEntity<EntityListResponse<ObservationLevel>> getObservationLevels(
		@PathVariable final String crop,
		@ApiParam(value = "Filter by study DbId")
		@RequestParam(value = "studyDbId", required = false) final String studyDbId,
		@ApiParam(value = "Filter by trial DbId")
		@RequestParam(value = "trialDbId", required = false) final String trialDbId,
		@ApiParam(value = "Filter by program DbId")
		@RequestParam(value = "programDbId", required = false) final String programDbId,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false)
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final ObservationLevelFilter observationLevelFilter = new ObservationLevelFilter(studyDbId, trialDbId, programDbId);
		final Integer finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final Integer finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		final PagedResult<ObservationLevel> resultPage = new PaginatedSearch()
			.executeBrapiSearch(finalPageNumber, finalPageSize,
				new SearchSpec<ObservationLevel>() {

					@Override
					public long getCount() {
						return ObservationUnitResourceBrapi.this.middlewareObservationUnitService.countObservationLevels(observationLevelFilter);
					}

					@Override
					public List<ObservationLevel> getResults(final PagedResult<ObservationLevel> pagedResult) {
						return ObservationUnitResourceBrapi.this.middlewareObservationUnitService
							.getObservationLevels(observationLevelFilter, new PageRequest(finalPageNumber, finalPageSize));
					}
				});

		final Result<ObservationLevel> results = new Result<ObservationLevel>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);

		return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);
	}

}
