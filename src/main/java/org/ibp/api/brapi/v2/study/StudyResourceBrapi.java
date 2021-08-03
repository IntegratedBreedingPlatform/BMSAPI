package org.ibp.api.brapi.v2.study;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.study.StudyImportRequestDTO;
import org.generationcp.middleware.api.location.Location;
import org.generationcp.middleware.api.location.LocationService;
import org.generationcp.middleware.api.location.search.LocationSearchRequest;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.brapi.StudyServiceBrapi;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.v1.study.StudyDetailsData;
import org.ibp.api.brapi.v1.study.StudyMapper;
import org.ibp.api.brapi.v2.BrapiResponseMessageGenerator;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Api(value = "BrAPI v2 Study Services")
@Controller(value = "StudyResourceBrapiV2")
public class StudyResourceBrapi {

	@Autowired
	private StudyServiceBrapi studyServiceBrapi;

	@Autowired
	private LocationService locationService;

	@Autowired
	private BrapiResponseMessageGenerator<StudyInstanceDto> responseMessageGenerator;

	@ApiOperation(value = "Get the details for a specific Study", notes = "Get the details for a specific Study")
	@RequestMapping(value = "/{crop}/brapi/v2/studies/{studyDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<SingleEntityResponse<StudyDetailsData>> getStudyDetails(@PathVariable final String crop,
		@PathVariable final Integer studyDbId) {

		final StudyDetailsDto mwStudyDetails = this.studyServiceBrapi.getStudyDetailsByInstance(studyDbId);
		if (Objects.isNull(mwStudyDetails)) {
			final BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
			;
			errors.reject("studydbid.invalid", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final Metadata metadata = new Metadata();
		final Pagination pagination = new Pagination().withPageNumber(1).withPageSize(1).withTotalCount(1L).withTotalPages(1);
		metadata.setPagination(pagination);
		final ModelMapper studyMapper = StudyMapper.getInstance();
		final StudyDetailsData result = studyMapper.map(mwStudyDetails, StudyDetailsData.class);
		if (mwStudyDetails.getMetadata().getLocationId() != null) {
			final LocationSearchRequest locationSearchRequest = new LocationSearchRequest();
			locationSearchRequest.setLocationIds(Collections.singletonList(mwStudyDetails.getMetadata().getLocationId()));
			final List<Location> locations = this.locationService.getLocations(locationSearchRequest, new PageRequest(0, 10));
			if (!locations.isEmpty()) {
				result.setLocationDbId(locations.get(0).getLocationDbId());
				result.setLocationName(locations.get(0).getLocationName());
			}
		}
		result.setCommonCropName(crop);

		return ResponseEntity.ok(new SingleEntityResponse<>(metadata, result));
	}

	@ApiOperation(value = "Get a filtered list of Studies", notes = "Get a filtered list of Studies")
	@RequestMapping(value = "/{crop}/brapi/v2/studies", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<StudyInstanceDto>> getStudies(@PathVariable final String crop,
		@ApiParam(value = "Common name for the crop associated with study")
		@RequestParam(value = "commonCropName", required = false) final String commonCropName,
		@ApiParam(value = "Filter based on studies type unique identifier")
		@RequestParam(value = "studyTypeDbId", required = false) final String studyTypeDbId,
		@ApiParam(value = "Filter to only return studies associated with given program id")
		@RequestParam(value = "programDbId", required = false) final String programDbId,
		@ApiParam(value = "Filter to only return studies associated with given location id")
		@RequestParam(value = "locationDbId", required = false) final String locationDbId,
		@ApiParam(value = "Filter to only return studies associated with given season id")
		@RequestParam(value = "seasonDbId", required = false) final String seasonDbId,
		@ApiParam(value = "Filter to only return study associated with given trial id")
		@RequestParam(value = "trialDbId", required = false) final String trialDbId,
		@ApiParam(value = "Filter to only return study associated with given study id")
		@RequestParam(value = "studyDbId", required = false) final String studyDbId,
		@ApiParam(value = "Filter to only return study associated with given study name")
		@RequestParam(value = "trialName", required = false) final String trialName,
		@ApiParam(value = "Filter to only return studies associated with given study PUI")
		@RequestParam(value = "studyPUI", required = false) final String studyPUI,
		@ApiParam(value = "Filter to only return studies associated with given germplasm id")
		@RequestParam(value = "germplasmDbid", required = false) final String germplasmDbid,
		@ApiParam(value = "Filter to only return studies associated with given observation variable id")
		@RequestParam(value = "observationVariableDbId", required = false) final Integer observationVariableDbId,
		@ApiParam(value = "Filter to only return studies associated with given external reference ID. Could be a simple string or a URI. (use with externalReferenceSource parameter")
		@RequestParam(value = "externalReferenceId", required = false) final String externalReferenceId,
		@ApiParam(value = "An identifier for the source system or database of an external reference (use with externalReferenceID parameter")
		@RequestParam(value = "externalReferenceSource", required = false) final String externalReferenceSource,
		@ApiParam(value = "Filter active status true/false") @RequestParam(value = "active", required = false) final Boolean active,
		@ApiParam(value = "Sort order. Name of the field to sort by.") @RequestParam(value = "sortBy", required = false)
		final String sortBy,
		@ApiParam(value = "Sort order direction. asc/desc.") @RequestParam(value = "sortOrder", required = false) final String sortOrder,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION) @RequestParam(value = "page", required = false) final Integer page,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION) @RequestParam(value = "pageSize", required = false) final Integer pageSize
	) {
		final String validationError = this.parameterValidation(crop, commonCropName, sortBy, sortOrder);
		if (!StringUtils.isBlank(validationError)) {
			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", validationError));
			final Metadata metadata = new Metadata(null, status);
			final EntityListResponse<StudyInstanceDto> entityListResponse = new EntityListResponse<>(metadata, new Result<>());

			return new ResponseEntity<>(entityListResponse, HttpStatus.BAD_REQUEST);
		}

		final StudySearchFilter studySearchFilter = new StudySearchFilter();
		studySearchFilter.setStudyTypeDbId(studyTypeDbId);
		studySearchFilter.setProgramDbId(programDbId);
		studySearchFilter.setLocationDbId(locationDbId);
		studySearchFilter.setSeasonDbId(seasonDbId);
		if (trialDbId != null) {
			studySearchFilter.setTrialDbIds(Collections.singletonList(trialDbId));
		}
		if (studyDbId != null) {
			studySearchFilter.setStudyDbIds(Collections.singletonList(studyDbId));
		}
		studySearchFilter.setActive(active);
		studySearchFilter.setGermplasmDbId(germplasmDbid);
		studySearchFilter.setObservationVariableDbId(observationVariableDbId);
		studySearchFilter.setTrialName(trialName);
		studySearchFilter.setStudyPUI(studyPUI);
		studySearchFilter.setExternalReferenceID(externalReferenceId);
		studySearchFilter.setExternalReferenceSource(externalReferenceSource);

		final int finalPageNumber = page == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : page;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest;
		if (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)) {
			pageRequest = new PageRequest(finalPageNumber, finalPageSize, new Sort(Sort.Direction.fromString(sortOrder), sortBy));
		} else {
			pageRequest = new PageRequest(finalPageNumber, finalPageSize);
		}

		final PagedResult<StudyInstanceDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<StudyInstanceDto>() {

				@Override
				public long getCount() {
					return StudyResourceBrapi.this.studyServiceBrapi.countStudyInstances(studySearchFilter);
				}

				@Override
				public List<StudyInstanceDto> getResults(final PagedResult<StudyInstanceDto> pagedResult) {
					return StudyResourceBrapi.this.studyServiceBrapi.getStudyInstancesWithMetadata(studySearchFilter, pageRequest);
				}
			});

		resultPage.getPageResults().stream().forEach(studyInstanceDto -> studyInstanceDto.setCommonCropName(crop));

		final Result<StudyInstanceDto> result = new Result<StudyInstanceDto>().withData(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final EntityListResponse<StudyInstanceDto> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Create new Studies", notes = "Create new Studies.")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'STUDIES', 'MANAGE_STUDIES')")
	@RequestMapping(value = "/{crop}/brapi/v2/studies", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<StudyInstanceDto>> createStudies(@PathVariable final String crop,
		@RequestBody final List<StudyImportRequestDTO> studyImportRequestDTOS) {
		BaseValidator.checkNotNull(studyImportRequestDTOS, "study.import.request.null");
		final StudyImportResponse
			studyImportResponse = this.studyServiceBrapi.createStudies(crop, studyImportRequestDTOS);
		final Result<StudyInstanceDto> results = new Result<StudyInstanceDto>().withData(studyImportResponse.getEntityList());

		final Metadata metadata = new Metadata().withStatus(this.responseMessageGenerator.getMessagesList(studyImportResponse));
		final EntityListResponse<StudyInstanceDto> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	private String parameterValidation(final String crop, final String commonCropName, final String sortBy,
		final String sortOrder) {
		final List<String> sortbyFields = ImmutableList.<String>builder().add("studyDbId").add("trialDbId").add("programDbId")
			.add("locationDbId").add("studyTypeDbId").add("trialName").add("programName").add("seasonDbId").build();
		final List<String> sortOrders = ImmutableList.<String>builder().add("asc")
			.add("desc").build();

		if (!StringUtils.isEmpty(commonCropName) && !crop.equals(commonCropName)) {
			return "Invalid commonCropName value";
		}
		if (!StringUtils.isBlank(sortBy) && !sortbyFields.contains(sortBy)) {
			return "sortBy bad filter, expect " + StringUtils.join(sortbyFields, "/");
		}
		if (!StringUtils.isBlank(sortOrder) && !sortOrders.contains(sortOrder.toLowerCase())) {
			return "sortOrder bad filter, expect asc/desc";
		}
		return "";
	}

}
