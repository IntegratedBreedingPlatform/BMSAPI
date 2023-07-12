package org.ibp.api.brapi.v2.observation;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationDto;
import org.generationcp.middleware.api.brapi.v2.observation.ObservationSearchRequestDto;
import org.generationcp.middleware.api.brapi.v2.observationunit.ObservationLevelRelationship;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.ObservationServiceBrapi;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.v2.BrapiResponseMessageGenerator;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.BrapiSearchDto;
import org.ibp.api.java.impl.middleware.permission.validator.BrapiPermissionValidator;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI Observation Services")
@Controller(value = "ObservationResourceBrapiV2")
public class ObservationResourceBrapi {

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private ObservationServiceBrapi observationServiceBrapi;

	@Autowired
	private BrapiResponseMessageGenerator<ObservationDto> responseMessageGenerator;

	@Autowired
	private BrapiPermissionValidator permissionValidator;

	@ApiOperation(value = "Retrieve observations", notes = "Retrieve all observations where there are measurements for the given observation variables.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/brapi/v2/observations", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<ObservationDto>> getObservations(
		@PathVariable final String crop,
		@ApiParam(value = "The unique ID of an Observation")
		@RequestParam(required = false) final String observationDbId,
		@ApiParam(value = "The unique ID of an Observation Unit")
		@RequestParam(required = false) final String observationUnitDbId,
		@ApiParam(value = "The unique ID of a germplasm (accession) to filter on")
		@RequestParam(required = false) final String germplasmDbId,
		@ApiParam(value = "The unique ID of an observation variable")
		@RequestParam(required = false) final String observationVariableDbId,
		@ApiParam(value = "The unique ID of a study to filter on")
		@RequestParam(required = false) final String studyDbId,
		@ApiParam(value = "The unique ID of a location where these observations were collected")
		@RequestParam(required = false) final String locationDbId,
		@ApiParam(value = "The unique ID of a trial to filter on")
		@RequestParam(required = false) final String trialDbId,
		@ApiParam(value = "The unique ID of a program to filter on")
		@RequestParam(required = false) final String programDbId,
		@ApiParam(value = "The year or Phenotyping campaign of a multi-annual study (trees, grape, ...)")
		@RequestParam(required = false) final String seasonDbId,
		@ApiParam(value = "The Observation Unit Level. Returns only the observation unit of the specified Level. References ObservationUnit->observationUnitPosition->observationLevel->levelName")
		@RequestParam(required = false) final String observationUnitLevelName,
		@ApiParam(value = "The Observation Unit Level Order Number. Returns only the observation unit of the specified Level. References ObservationUnit->observationUnitPosition->observationLevel->levelOrder")
		@RequestParam(required = false) final String observationUnitLevelOrder,
		@ApiParam(value = "The Observation Unit Level Code. This parameter should be used together with observationUnitLevelName or observationUnitLevelOrder. References ObservationUnit->observationUnitPosition->observationLevel->levelCode")
		@RequestParam(required = false) final String observationUnitLevelCode,
		@ApiParam(value = "Timestamp range start")
		@RequestParam(required = false) final String observationTimeStampRangeStart,
		@ApiParam(value = "Timestamp range end")
		@RequestParam(required = false) final String observationTimeStampRangeEnd,
		@ApiParam(value = "An external reference ID. Could be a simple string or a URI. (use with externalReferenceSource parameter)")
		@RequestParam(required = false) final String externalReferenceID,
		@ApiParam(value = "An identifier for the source system or database of an external reference (use with externalReferenceID parameter)")
		@RequestParam(required = false) final String externalReferenceSource,
		@ApiParam(value = "Used to request a specific page of data to be returned.The page indexing starts at 0 (the first page is 'page'= 0). Default is 0.")
		@RequestParam(required = false) final Integer page,
		@ApiParam(value = "The size of the pages to be returned. Default is 1000")
		@RequestParam(required = false) final Integer pageSize) {

		final ObservationSearchRequestDto observationSearchRequestDto = new ObservationSearchRequestDto();
		// validate and set programDbId, sets all valid program for user if request param is empty
		observationSearchRequestDto.setProgramDbIds(
			StringUtils.isNotEmpty(programDbId) ? this.permissionValidator.validateProgramByProgramDbIds(crop, Arrays.asList(programDbId), false) : new ArrayList<>());
		observationSearchRequestDto.setObservationDbIds(
			StringUtils.isNotEmpty(observationDbId) ? Arrays.asList(observationDbId) : new ArrayList<>());
		observationSearchRequestDto.setObservationUnitDbIds(
			StringUtils.isNotEmpty(observationUnitDbId) ? Arrays.asList(observationUnitDbId) : new ArrayList<>());
		observationSearchRequestDto.setGermplasmDbIds(
			StringUtils.isNotEmpty(germplasmDbId) ? Arrays.asList(germplasmDbId) : new ArrayList<>());
		observationSearchRequestDto.setObservationVariableDbIds(
			StringUtils.isNotEmpty(observationVariableDbId) ? Arrays.asList(observationVariableDbId) : new ArrayList<>());
		observationSearchRequestDto.setStudyDbIds(
			StringUtils.isNotEmpty(studyDbId) ? Arrays.asList(studyDbId) : new ArrayList<>());
		observationSearchRequestDto.setLocationDbIds(
			StringUtils.isNotEmpty(locationDbId) ? Arrays.asList(locationDbId) : new ArrayList<>());
		observationSearchRequestDto.setTrialDbIds(
			StringUtils.isNotEmpty(trialDbId) ? Arrays.asList(trialDbId) : new ArrayList<>());
		observationSearchRequestDto.setSeasonDbIds(
			StringUtils.isNotEmpty(seasonDbId) ? Arrays.asList(seasonDbId) : new ArrayList<>());
		final ObservationLevelRelationship observationLevelRelationship = new ObservationLevelRelationship();
		observationLevelRelationship.setLevelName(observationUnitLevelName);
		observationLevelRelationship.setLevelCode(observationUnitLevelCode);
		final Integer order = NumberUtils.isNumber(observationUnitLevelOrder) ? NumberUtils.createInteger(observationUnitLevelOrder) : null;
		observationLevelRelationship.setLevelOrder(order);
		observationSearchRequestDto.setObservationLevelRelationships(Arrays.asList(observationLevelRelationship));
		observationSearchRequestDto.setObservationTimeStampRangeStart(observationTimeStampRangeStart);
		observationSearchRequestDto.setObservationTimeStampRangeEnd(observationTimeStampRangeEnd);
		observationSearchRequestDto.setExternalReferenceIds(
			StringUtils.isNotEmpty(externalReferenceID) ? Arrays.asList(externalReferenceID) : new ArrayList<>());
		observationSearchRequestDto.setExternalReferenceSources(
			StringUtils.isNotEmpty(externalReferenceSource) ? Arrays.asList(externalReferenceSource) : new ArrayList<>());

		return this.getObservationResponseEntity(page, pageSize, observationSearchRequestDto);
	}

	private ResponseEntity<EntityListResponse<ObservationDto>> getObservationResponseEntity(final Integer page, final Integer pageSize,
		final ObservationSearchRequestDto observationSearchRequestDto) {
		final PagedResult<ObservationDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(page, pageSize, new SearchSpec<ObservationDto>() {

				@Override
				public long getCount() {
					return ObservationResourceBrapi.this.observationServiceBrapi.countObservations(observationSearchRequestDto);
				}

				@Override
				public List<ObservationDto> getResults(final PagedResult<ObservationDto> pagedResult) {
					// BRAPI services have zero-based indexing for pages but paging for Middleware method starts at 1
					final int finalPageNumber = page == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : page + 1;
					final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
					final PageRequest pageRequest = new PageRequest(finalPageNumber, finalPageSize);
					return ObservationResourceBrapi.this.observationServiceBrapi
						.searchObservations(observationSearchRequestDto, pageRequest);
				}
			});

		final List<ObservationDto> observationDtos = resultPage.getPageResults();

		final Result<ObservationDto> results = new Result<ObservationDto>().withData(observationDtos);
		final Pagination pagination = new Pagination() //
			.withPageNumber(resultPage.getPageNumber()) //
			.withPageSize(resultPage.getPageSize()) //
			.withTotalCount(resultPage.getTotalResults()) //
			.withTotalPages(resultPage.getTotalPages());
		final Metadata metadata = new Metadata().withPagination(pagination);

		return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);
	}

	@ApiOperation(value = "Add new Observation entities", notes = "Add new Observation entities")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'MS_OBSERVATIONS', 'MS_MANAGE_CONFIRMED_OBSERVATIONS')")
	@RequestMapping(value = "/{crop}/brapi/v2/observations", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<ObservationDto>> createObservations(@PathVariable final String crop,
		@RequestBody final List<ObservationDto> observations) {
		if (CollectionUtils.isNotEmpty(observations)) {
			// empty study db id not allowed if user has program only role
			this.permissionValidator.validateProgramByStudyDbId(crop, observations.get(0).getStudyDbId());
		}

		final ObservationImportResponse observationImportResponse = this.observationServiceBrapi.createObservations(observations);
		final Result<ObservationDto> results =
			new Result<ObservationDto>().withData(observationImportResponse.getEntityList());

		final Metadata metadata = new Metadata().withStatus(this.responseMessageGenerator.getMessagesList(observationImportResponse));
		final EntityListResponse<ObservationDto> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Update multiple Observation entities", notes = "Update multiple Observation entities")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES', 'MS_OBSERVATIONS', 'MS_MANAGE_CONFIRMED_OBSERVATIONS')")
	@RequestMapping(value = "/{crop}/brapi/v2/observations", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<ObservationDto>> updateObservations(@PathVariable final String crop,
		@RequestBody final Map<String, ObservationDto> observations) {
		this.permissionValidator.validateUserHasAtLeastCropRoles(crop);

		final ObservationUpdateResponse observationUpdateResponse = this.observationServiceBrapi.updateObservations(observations);
		final Result<ObservationDto> results =
			new Result<ObservationDto>().withData(observationUpdateResponse.getEntityList());

		final Metadata metadata = new Metadata().withStatus(this.responseMessageGenerator.getMessagesList(observationUpdateResponse));
		final EntityListResponse<ObservationDto> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Submit a search request for Observations", notes = "Submit a search request for Observations")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/brapi/v2/search/observations", method = RequestMethod.POST)
	@JsonView(BrapiView.BrapiV2_1.class)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<BrapiSearchDto>> postSearchObservations(@PathVariable final String crop,
		@RequestBody final ObservationSearchRequestDto observationSearchRequestDto) {
		this.permissionValidator.validateUserHasAtLeastCropRoles(crop);

		final BrapiSearchDto searchDto =
			new BrapiSearchDto(this.searchRequestService.saveSearchRequest(observationSearchRequestDto, ObservationSearchRequestDto.class)
				.toString());
		final SingleEntityResponse<BrapiSearchDto> observationSearchResponse = new SingleEntityResponse<>(searchDto);

		return new ResponseEntity<>(observationSearchResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get search Observations results", notes = "Get the results of Observations search request")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/brapi/v2/search/observations/{searchResultsDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2_1.class)
	public ResponseEntity<EntityListResponse<ObservationDto>> getObservationsSearchResults(
		@PathVariable final String crop,
		@PathVariable final String searchResultsDbId,
		@RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false)
		@RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {
		this.permissionValidator.validateUserHasAtLeastCropRoles(crop);

		final ObservationSearchRequestDto searchRequestDto;
		try {
			searchRequestDto =
				(ObservationSearchRequestDto) this.searchRequestService
					.getSearchRequest(Integer.valueOf(searchResultsDbId), ObservationSearchRequestDto.class);
		} catch (final NumberFormatException | MiddlewareException e) {
			return new ResponseEntity<>(
				new EntityListResponse<ObservationDto>(new Result<>(new ArrayList<>())).withMessage("no search request found"),
				HttpStatus.NOT_FOUND);
		}

		return this.getObservationResponseEntity(currentPage, pageSize, searchRequestDto);

	}
}
