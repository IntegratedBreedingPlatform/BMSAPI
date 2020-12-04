package org.ibp.api.brapi.v2.study;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.api.brapi.v1.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.location.LocationFilters;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyInstanceDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.location.Location;
import org.ibp.api.brapi.v1.location.LocationMapper;
import org.ibp.api.brapi.v1.study.StudyDetails;
import org.ibp.api.brapi.v1.study.StudyDetailsData;
import org.ibp.api.brapi.v1.study.StudyMapper;
import org.ibp.api.brapi.v1.study.StudySummariesDto;
import org.ibp.api.brapi.v1.study.StudySummaryDto;
import org.ibp.api.brapi.v1.trial.TrialSummary;
import org.ibp.api.brapi.v1.trial.TrialSummaryMapper;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(value = "BrAPI v2 Study Services")
@Controller(value = "StudyResourceBrapiV2")
public class StudyResourceBrapi {

	@Autowired
	private StudyService studyService;

	@Autowired
	private LocationDataManager locationDataManager;

	@ApiOperation(value = "Get the details for a specific Study", notes = "Get the details for a specific Study")
	@RequestMapping(value = "/{crop}/brapi/v2/studies/{studyDbId}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<StudyDetails> getStudyDetails(@PathVariable final String crop, @PathVariable final Integer studyDbId) {

		final StudyDetailsDto mwStudyDetails = this.studyService.getStudyDetailsByGeolocation(studyDbId);

		if (mwStudyDetails != null) {
			final StudyDetails studyDetails = new StudyDetails();
			final Metadata metadata = new Metadata();
			final Pagination pagination = new Pagination().withPageNumber(1).withPageSize(1).withTotalCount(1L).withTotalPages(1);
			metadata.setPagination(pagination);
			metadata.setStatus(Collections.singletonList(new HashMap<>()));
			studyDetails.setMetadata(metadata);
			final ModelMapper studyMapper = StudyMapper.getInstance();
			final StudyDetailsData result = studyMapper.map(mwStudyDetails, StudyDetailsData.class);
			if (mwStudyDetails.getMetadata().getLocationId() != null) {
				final Map<LocationFilters, Object> filters = new EnumMap<>(LocationFilters.class);
				filters.put(LocationFilters.LOCATION_ID, String.valueOf(mwStudyDetails.getMetadata().getLocationId()));
				final List<LocationDetailsDto> locations = this.locationDataManager.getLocationsByFilter(0, 1, filters);
				if (!locations.isEmpty()) {
					final ModelMapper locationMapper = LocationMapper.getInstance();
					final Location location = locationMapper.map(locations.get(0), Location.class);
					result.setLocation(location);
				}
			}
			result.setCommonCropName(crop);
			studyDetails.setResult(result);

			return ResponseEntity.ok(studyDetails);
		} else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
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
		@RequestParam(value = "germplasmDbid", required = false) final Integer germplasmDbid,
		@ApiParam(value = "Filter to only return studies associated with given observation variable id")
		@RequestParam(value = "observationVariableDbId", required = false) final Integer observationVariableDbId,
		@ApiParam(value = "Filter active status true/false") @RequestParam(value = "active", required = false) final Boolean active,
		@ApiParam(value = "Sort order. Name of the field to sort by.") @RequestParam(value = "sortBy", required = false)
		final String sortBy,
		@ApiParam(value = "Sort order direction. asc/desc.") @RequestParam(value = "sortOrder", required = false) final String sortOrder,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION) @RequestParam(value = "page", required = false) final Integer page,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION) @RequestParam(value = "pageSize", required = false)
		final Integer pageSize
		) {
		final String validationError = this.parameterValidation(crop, commonCropName, active, sortBy, sortOrder);
		if (!StringUtils.isBlank(validationError)) {
			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", validationError));
			final Metadata metadata = new Metadata(null, status);
			final EntityListResponse<StudyInstanceDto> entityListResponse = new EntityListResponse<>(metadata, new Result<StudyInstanceDto>());

			return new ResponseEntity<>(entityListResponse, HttpStatus.BAD_REQUEST);
		}

		final StudySearchFilter filter = new StudySearchFilter().withStudyTypeDbId(studyTypeDbId).withProgramDbId(programDbId)
			.withLocationDbId(locationDbId).withGermplasmDbid(germplasmDbid).withObservationVariableDbId(observationVariableDbId)
			.withStudyDbId(studyDbId).withTrialDbId(trialDbId).withTrialName(trialName).withStudyPUI(studyPUI).withSeasonDbId(seasonDbId);

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
					return StudyResourceBrapi.this.studyService.countStudyInstances(filter);
				}

				@Override
				public List<StudyInstanceDto> getResults(final PagedResult<StudyInstanceDto> pagedResult) {
					return StudyResourceBrapi.this.studyService.getStudyInstances(filter, pageRequest);
				}
			});

		final List<StudyInstanceDto> studyInstanceDtoList = resultPage.getPageResults().stream()
			.peek(studyInstanceDto -> {studyInstanceDto.setCommonCropName(crop);}).collect(Collectors.toList());

		final Result<StudyInstanceDto> result = new Result<StudyInstanceDto>().withData(studyInstanceDtoList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final EntityListResponse<StudyInstanceDto> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	private String parameterValidation(final String crop, final String commonCropName, final Boolean active, final String sortBy,
		final String sortOrder) {
		final List<String> sortbyFields = ImmutableList.<String>builder().add("studyDbId").add("trialDbId").add("programDbId")
			.add("locationDbId").add("studyTypeDbId").add("trialName").add("programName").add("seasonDbId").build();
		final List<String> sortOrders = ImmutableList.<String>builder().add("asc")
			.add("desc").build();

		if (!StringUtils.isEmpty(commonCropName) && !crop.equals(commonCropName)) {
			return "Invalid commonCropName value";
		}
		if (active != null && !active) {
			return "No inactive studies found.";
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
