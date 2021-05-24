package org.ibp.api.brapi.v2.trial;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.trial.TrialImportRequestDTO;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.brapi.v1.trial.TrialSummary;
import org.ibp.api.brapi.v1.trial.TrialSummaryMapper;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "BrAPI v2 Trial Services")
@Controller(value = "TrialResourceBrapiV2")
public class TrialResourceBrapi {

	@Autowired
	private StudyService studyService;

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@ApiOperation(value = "Retrieve a filtered list of breeding Trials", notes = "Retrieve a filtered list of breeding Trials. A Trial is a collection of Studies")
	@RequestMapping(value = "/{crop}/brapi/v2/trials", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<TrialSummary>> getTrials(@PathVariable final String crop,
		@ApiParam(value = "Filter active status true/false") @RequestParam(value = "active", required = false) final Boolean active,
		@ApiParam(value = "Common name for the crop associated with trial") @RequestParam(value = "commonCropName", required = false)
		final String commonCropName,
		@ApiParam(value = "Filter to only return trials associated with given contact")
		@RequestParam(value = "contactDbId", required = false) final String contactDbId,
		@ApiParam(value = "Filter to only return trials associated with given program id")
		@RequestParam(value = "programDbId", required = false) final String programDbId,
		@ApiParam(value = "Filter to only return trials associated with given location id")
		@RequestParam(value = "locationDbId", required = false) final String locationDbId,
		@ApiParam(value = "Filter to only return trials with end date after specified searchDateRangeStart (yyyy-MM-dd)")
		@RequestParam(value = "searchDateRangeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd")
		final Date searchDateRangeStart,
		@ApiParam(value = "Filter to only return trials with start date before specified searchDateRangeEnd (yyyy-MM-dd)")
		@RequestParam(value = "searchDateRangeEnd", required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") final Date searchDateRangeEnd,
		@ApiParam(value = "Filter to only return trials associated with given study id")
		@RequestParam(value = "studyDbId", required = false) final String studyDbId,
		@ApiParam(value = "Filter to only return trials associated with given trial id")
		@RequestParam(value = "trialDbId", required = false) final String trialDbId,
		@ApiParam(value = "Filter to only return trials associated with given trial name")
		@RequestParam(value = "trialName", required = false) final String trialName,
		@ApiParam(value = "Filter to only return trials associated with given trial PUI")
		@RequestParam(value = "trialPUI", required = false) final String trialPUI,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION) @RequestParam(value = "page", required = false) final Integer page,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION) @RequestParam(value = "pageSize", required = false)
		final Integer pageSize,
		@ApiParam(value = "Sort order. Name of the field to sort by.") @RequestParam(value = "sortBy", required = false)
		final String sortBy,
		@ApiParam(value = "Sort order direction. asc/desc.") @RequestParam(value = "sortOrder", required = false) final String sortOrder) {
		final boolean isSortOrderValid =
			"ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder) || StringUtils.isEmpty(sortOrder);
		Preconditions.checkArgument(isSortOrderValid, "sortOrder should be either ASC or DESC");
		final String validationError = this.parameterValidation(crop, commonCropName, active, sortBy, sortOrder);
		if (!StringUtils.isBlank(validationError)) {
			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", validationError));
			final Metadata metadata = new Metadata(null, status);
			return new ResponseEntity<>(new EntityListResponse<>(metadata, new Result<>()), HttpStatus.BAD_REQUEST);
		}

		final StudySearchFilter filter = new StudySearchFilter();
		filter.setProgramDbId(programDbId);
		filter.setLocationDbId(locationDbId);
		if (trialDbId != null) {
			filter.setTrialDbIds(Collections.singletonList(trialDbId));
		}
		if (studyDbId != null) {
			filter.setStudyDbIds(Collections.singletonList(studyDbId));
		}
		filter.setTrialName(trialName);
		filter.setTrialPUI(trialPUI);
		filter.setContactDbId(contactDbId);
		filter.setSearchDateRangeStart(searchDateRangeStart);
		filter.setSearchDateRangeEnd(searchDateRangeEnd);

		final int finalPageNumber = page == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : page;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest;
		if (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)) {
			pageRequest = new PageRequest(finalPageNumber, finalPageSize, new Sort(Sort.Direction.fromString(sortOrder), sortBy));
		} else {
			pageRequest = new PageRequest(finalPageNumber, finalPageSize);
		}
		final PagedResult<StudySummary> resultPage =
			new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<StudySummary>() {

				@Override
				public long getCount() {
					return TrialResourceBrapi.this.studyService.countStudies(filter);
				}

				@Override
				public List<StudySummary> getResults(final PagedResult<StudySummary> pagedResult) {
					return TrialResourceBrapi.this.studyService.getStudies(filter, pageRequest);
				}
			});

		final List<TrialSummary> trialSummaryList = this.translateResults(resultPage.getPageResults(), crop);
		final Result<TrialSummary> results = new Result<TrialSummary>().withData(trialSummaryList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);

	}

	@ApiOperation(value = "Create new trials", notes = "Create new breeding Trials. A Trial represents a collection of related Studies. trialDbId is generated by the server.")
	@RequestMapping(value = "/{crop}/brapi/v2/trials", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(BrapiView.BrapiV2.class)
	public ResponseEntity<EntityListResponse<TrialSummary>> createTrial(@PathVariable final String crop,
		@RequestBody final List<TrialImportRequestDTO> trialImportRequestDTOs) {
		BaseValidator.checkNotNull(trialImportRequestDTOs, "trial.import.request.null");

		final TrialImportResponse trialImportResponse = this.studyService.createTrials(crop, trialImportRequestDTOs);
		final List<TrialSummary> trialSummaries = this.translateResults(trialImportResponse.getStudySummaries(), crop);
		final Result<TrialSummary> results = new Result<TrialSummary>().withData(trialSummaries);

		final Map<String, String> messages = new HashMap<>();
		messages.put("INFO", trialImportResponse.getStatus());
		if (!CollectionUtils.isEmpty(trialImportResponse.getErrors())) {
			int index = 1;
			for (final ObjectError error : trialImportResponse.getErrors()) {
				messages.put("ERROR" + index++,
					this.messageSource.getMessage(error.getCode(), error.getArguments(), LocaleContextHolder.getLocale()));
			}
		}
		final Metadata metadata = new Metadata().withStatus(Lists.newArrayList(messages));
		final EntityListResponse<TrialSummary> entityListResponse = new EntityListResponse<>(metadata, results);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	private List<TrialSummary> translateResults(final List<StudySummary> studySummaries, final String crop) {
		final ModelMapper modelMapper = TrialSummaryMapper.getInstance();
		final List<TrialSummary> trialSummaryList = new ArrayList<>();
		if(!CollectionUtils.isEmpty(studySummaries)) {
			for (final StudySummary mwStudy : studySummaries) {
				final TrialSummary trialSummaryDto = modelMapper.map(mwStudy, TrialSummary.class);
				trialSummaryDto.setCommonCropName(crop);
				trialSummaryList.add(trialSummaryDto);
			}
		}
		return trialSummaryList;
	}

	private String parameterValidation(final String crop, final String commonCropName, final Boolean active, final String sortBy,
		final String sortOrder) {
		final List<String> sortbyFields = ImmutableList.<String>builder().add("trialDbId").add("trialName").add("programDbId")
			.add("programName").add("locationDbId").add("startDate").add("endDate").build();
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
