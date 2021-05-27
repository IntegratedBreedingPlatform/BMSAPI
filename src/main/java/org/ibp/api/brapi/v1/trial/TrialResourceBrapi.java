
package org.ibp.api.brapi.v1.trial;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
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
import java.util.List;
import java.util.Map;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/#reference/trials">BrAPI Trial services</a>.
 */
@Api(value = "BrAPI Trial Services")
@Controller
public class TrialResourceBrapi {

	@Autowired
	private StudyService studyService;

	private static final String ORDER_BY_ASCENDING = "asc";
	private static final String ORDER_BY_DESCENDING = "desc";

	@ApiOperation(value = "List of trial summaries", notes = "Get a list of trial summaries.")
	@RequestMapping(value = "/{crop}/brapi/v1/trials", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(BrapiView.BrapiV1_3.class)
	public ResponseEntity<EntityListResponse<TrialSummary>> listTrialSummaries(@PathVariable final String crop,
		@ApiParam(value = "Program filter to only return studies associated with given program id.") @RequestParam(value = "programDbId",
			required = false) final String programDbId,
		@ApiParam(value = "Location filter to only return studies associated with given location id.") @RequestParam(value = "locationDbId",
			required = false) final String locationDbId,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION) @RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION) @RequestParam(value = "pageSize",
			required = false) final Integer pageSize,
		@ApiParam(value = "Filter active status true/false") @RequestParam(value = "active",
			required = false) final Boolean active,
		@ApiParam(value = "Sort order. Name of the field to sort by.") @RequestParam(value = "sortBy",
			required = false) final String sortBy,
		@ApiParam(value = "Sort order direction. asc/desc.") @RequestParam(value = "sortOrder",
			required = false) final String sortOrder) {

		final String validationError = this.parameterValidation(sortBy, sortOrder);
		if (!StringUtils.isBlank(validationError)) {
			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", validationError));
			final Metadata metadata = new Metadata(null, status);
			return new ResponseEntity<>(new EntityListResponse<>(metadata, new Result<>()), HttpStatus.NOT_FOUND);
		}

		final StudySearchFilter filter = new StudySearchFilter();
		filter.setProgramDbId(programDbId);
		filter.setLocationDbId(locationDbId);
		filter.setActive(active);

		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PageRequest pageRequest;
		if (StringUtils.isNotEmpty(sortBy) && StringUtils.isNotEmpty(sortOrder)) {
			pageRequest = new PageRequest(finalPageNumber, finalPageSize, new Sort(Sort.Direction.fromString(sortOrder), sortBy));
		} else {
			pageRequest = new PageRequest(finalPageNumber, finalPageSize);
		}
		final PagedResult<StudySummary> resultPage =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<StudySummary>() {

				@Override
				public long getCount() {
					return TrialResourceBrapi.this.studyService.countStudies(filter);
				}

				@Override
				public List<StudySummary> getResults(final PagedResult<StudySummary> pagedResult) {
					return TrialResourceBrapi.this.studyService.getStudies(filter, pageRequest);
				}
			});

		final List<TrialSummary> trialSummaryList = this.translateResults(resultPage, sortBy, sortOrder);
		final Result<TrialSummary> results = new Result<TrialSummary>().withData(trialSummaryList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);

	}

	private List<TrialSummary> translateResults(final PagedResult<StudySummary> resultPage, final String sortBy, final String sortOrder) {
		final ModelMapper modelMapper = TrialSummaryMapper.getInstance();
		final List<TrialSummary> trialSummaryList = new ArrayList<>();

		for (final StudySummary mwStudy : resultPage.getPageResults()) {
			final TrialSummary trialSummaryDto = modelMapper.map(mwStudy, TrialSummary.class);
			trialSummaryList.add(trialSummaryDto);
		}
		return trialSummaryList;
	}

	private String parameterValidation(final String sortBy, final String sortOrder) {
		final List<String> sortbyFields = ImmutableList.<String>builder().add("trialDbId").add("trialName").add("programDbId")
			.add("programName").add("startDate").add("endDate").build();
		final List<String> sortOrders = ImmutableList.<String>builder().add(TrialResourceBrapi.ORDER_BY_ASCENDING)
			.add(TrialResourceBrapi.ORDER_BY_DESCENDING).build();

		if (!StringUtils.isBlank(sortBy) && !sortbyFields.contains(sortBy)) {
			return "sortBy bad filter, expect trialDbId/trialName/programDbId/programName/startDate/endDate";

		}
		if (!StringUtils.isBlank(sortOrder) && !sortOrders.contains(sortOrder)) {
			return "sortOrder bad filter, expect asc/desc";
		}
		return "";
	}

	@ApiOperation(value = "Get trial observation details as table", notes = "Get trial observation details as table "
		+ "<p><strong>Note: </strong> non-standard BrAPI call</p>")
	@RequestMapping(value = "/{crop}/brapi/v1/trials/{trialDbId}/table", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<org.ibp.api.brapi.v1.trial.TrialObservationTable>> getTrialObservationsAsTable(
		@PathVariable final String crop,
		@PathVariable final Integer trialDbId) {

		org.ibp.api.brapi.v1.trial.TrialObservationTable trialObservationsTable = new org.ibp.api.brapi.v1.trial.TrialObservationTable();

		final TrialObservationTable mwTrialObservationTable = this.studyService.getTrialObservationTable(trialDbId);

		final int resultNumber = mwTrialObservationTable == null ? 0 : 1;

		if (resultNumber != 0) {
			final PropertyMap<TrialObservationTable, org.ibp.api.brapi.v1.trial.TrialObservationTable> mappingSpec =
				new PropertyMap<TrialObservationTable, org.ibp.api.brapi.v1.trial.TrialObservationTable>() {

					@Override
					protected void configure() {
						this.map(this.source.getStudyDbId(), this.destination.getTrialDbId());
					}
				};
			final ModelMapper modelMapper = new ModelMapper();
			modelMapper.addMappings(mappingSpec);
			trialObservationsTable = modelMapper.map(mwTrialObservationTable, org.ibp.api.brapi.v1.trial.TrialObservationTable.class);
		}

		final Pagination pagination =
			new Pagination().withPageNumber(1).withPageSize(resultNumber).withTotalCount((long) resultNumber).withTotalPages(1);

		final Metadata metadata = new Metadata().withPagination(pagination);
		return new ResponseEntity<>(new SingleEntityResponse<>(metadata, trialObservationsTable), HttpStatus.OK);
	}
}
