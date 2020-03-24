
package org.ibp.api.brapi.v1.trial;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.StringUtil;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.service.api.study.StudyFilters;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

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
	@RequestMapping(value = {"/{crop}/brapi/v1/trials", "/brapi/v1/trials"}, method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<TrialSummaries> listTrialSummaries(@PathVariable final Optional<String> crop,
			@ApiParam(value = "Program filter to only return studies associated with given program id.",
					required = false) @RequestParam(value = "programDbId", required = false) final String programDbId,
			@ApiParam(value = "Location filter to only return studies associated with given location id.",
					required = false) @RequestParam(value = "locationDbId", required = false) final String locationDbId,
			@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
					required = false) final Integer currentPage,
			@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
					required = false) final Integer pageSize,
			@ApiParam(value = "Filter active status true/false", required = false) @RequestParam(value = "active",
					required = false) final Boolean active,
			@ApiParam(value = "Sort order. Name of the field to sorty by.", required = false) @RequestParam(value = "sortBy",
					required = false) final String sortBy,
			@ApiParam(value = "Sort order direction. asc/desc.", required = false) @RequestParam(value = "sortOrder",
					required = false) final String sortOrder) {

		final String validationError = this.parameterValidation(active, sortBy, sortOrder);
		if (!StringUtils.isBlank(validationError)) {
			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", validationError));
			final Metadata metadata = new Metadata(null, status);
			final TrialSummaries trialSummaries = new TrialSummaries().withMetadata(metadata).withResult(new Result<TrialSummary>());
			return new ResponseEntity<>(trialSummaries, HttpStatus.NOT_FOUND);
		}

		final Map<StudyFilters, String> parameters = this.setParameters(programDbId, locationDbId, sortBy, sortOrder);
		final PagedResult<StudySummary> resultPage =
				new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<StudySummary>() {

					@Override
					public long getCount() {
						return TrialResourceBrapi.this.studyService.countStudies(parameters);
					}

					@Override
					public List<StudySummary> getResults(final PagedResult<StudySummary> pagedResult) {
						// BRAPI services have zero-based indexing for pages but paging for Middleware method starts at 1
						final int pageNumber = pagedResult.getPageNumber() + 1;
						return TrialResourceBrapi.this.studyService.getStudies(parameters, pagedResult.getPageSize(), pageNumber);
					}
				});

		final List<TrialSummary> trialSummaryList = this.translateResults(resultPage, sortBy, sortOrder);
		final Result<TrialSummary> results = new Result<TrialSummary>().withData(trialSummaryList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
				.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final TrialSummaries trialSummaries = new TrialSummaries().withMetadata(metadata).withResult(results);

		return new ResponseEntity<>(trialSummaries, HttpStatus.OK);

	}

	private List<TrialSummary> translateResults(final PagedResult<StudySummary> resultPage, final String sortBy, final String sortOrder) {
		final ModelMapper modelMapper = TrialSummaryMapper.getInstance();
		final List<TrialSummary> trialSummaryList = new ArrayList<>();

		for (final StudySummary mwStudy : resultPage.getPageResults()) {
			final TrialSummary trialSummaryDto = modelMapper.map(mwStudy, TrialSummary.class);
			trialSummaryList.add(trialSummaryDto);
		}
		if ("programName".equals(sortBy)) {
			this.orderListByProgramName(trialSummaryList, sortOrder);
		} else if ("startDate".equals(sortBy)) {
			this.orderListByStartDate(trialSummaryList, sortOrder);
		}
		return trialSummaryList;
	}

	private void orderListByStartDate(final List<TrialSummary> trialSummaryList, final String sortOrder) {
		if (StringUtil.isEmpty(sortOrder) || TrialResourceBrapi.ORDER_BY_DESCENDING.equalsIgnoreCase(sortOrder)) {
			final Comparator desc = Collections.reverseOrder(TrialResourceBrapi.getComparatorStartDate());
			Collections.sort(trialSummaryList, desc);
		} else {
			Collections.sort(trialSummaryList, TrialResourceBrapi.getComparatorStartDate());
		}
	}

	private void orderListByProgramName(final List<TrialSummary> trialSummaryList, final String sortOrder) {
		if (StringUtil.isEmpty(sortOrder) || TrialResourceBrapi.ORDER_BY_DESCENDING.equalsIgnoreCase(sortOrder)) {
			final Comparator desc = Collections.reverseOrder(TrialResourceBrapi.getComparatorProgramName());
			Collections.sort(trialSummaryList, desc);
		} else {
			Collections.sort(trialSummaryList, TrialResourceBrapi.getComparatorProgramName());
		}
	}

	private static Comparator<TrialSummary> getComparatorProgramName() {
		return new Comparator<TrialSummary>() {

			@Override
			public int compare(final TrialSummary trialSummary1, final TrialSummary trialSummary2) {
				return trialSummary1.getProgramName().compareTo(trialSummary2.getProgramName());
			}
		};
	}

	private static Comparator<TrialSummary> getComparatorStartDate() {
		return new Comparator<TrialSummary>() {

			@Override
			public int compare(final TrialSummary trialSummary1, final TrialSummary trialSummary2) {
				return trialSummary1.getStartDate().compareTo(trialSummary2.getStartDate());
			}
		};
	}

	private Map<StudyFilters, String> setParameters(final String programDbId, final String locationDbId, final String sortByField,
			final String sortOrder) {

		final Map<StudyFilters, String> parametersMap = new EnumMap<>(StudyFilters.class);
		if (!StringUtils.isBlank(programDbId)) {
			parametersMap.put(StudyFilters.PROGRAM_ID, programDbId);
		}
		if (!StringUtils.isBlank(locationDbId)) {
			parametersMap.put(StudyFilters.LOCATION_ID, locationDbId);
		}

		if (!StringUtils.isBlank(sortByField) && "trialName".equals(sortByField)) {
			parametersMap.put(StudyFilters.SORT_BY_FIELD, "name");
		} else {
			parametersMap.put(StudyFilters.SORT_BY_FIELD, "projectId");

		}
		if (StringUtils.isBlank(sortOrder) || TrialResourceBrapi.ORDER_BY_ASCENDING.equalsIgnoreCase(sortOrder)) {
			parametersMap.put(StudyFilters.ORDER, "asc");
		} else if (!StringUtils.isBlank(sortOrder) && TrialResourceBrapi.ORDER_BY_DESCENDING.equalsIgnoreCase(sortOrder)) {
			parametersMap.put(StudyFilters.ORDER, "desc");
		}
		return parametersMap;
	}

	private String parameterValidation(final Boolean active, final String sortBy, final String sortOrder) {
		final List<String> sortbyFields = ImmutableList.<String>builder().add("trialDbId").add("trialName").add("programDbId")
				.add("programName").add("startDate").add("endDate").add("active").build();
		final List<String> sortOrders = ImmutableList.<String>builder().add(TrialResourceBrapi.ORDER_BY_ASCENDING)
				.add(TrialResourceBrapi.ORDER_BY_DESCENDING).build();

		if (active != null && !active) {
			return "No inactive studies found.";
		}
		if (!StringUtils.isBlank(sortBy) && !sortbyFields.contains(sortBy)) {
			return "sortBy bad filter, expect trialDbId/trialName/programDbId/programName/startDate/endDate/active";

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
	public ResponseEntity<TrialObservations> getTrialObservationsAsTable(@PathVariable final String crop,
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
		final TrialObservations trialObservations = new TrialObservations().setMetadata(metadata).setResult(trialObservationsTable);
		return new ResponseEntity<>(trialObservations, HttpStatus.OK);
	}
}
