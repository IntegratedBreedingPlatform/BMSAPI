package org.ibp.api.brapi.v2.server.info;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.calls.CallService;
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

import java.util.List;
import java.util.Map;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a>
 * Server info services.
 */
@Api(value = "BrAPI Server Info Services")
@Controller
public class ServerinfoResourceBrapi {

	@Autowired
	private CallService callService;

	@ApiOperation(value = "Get the list of implemented Calls", notes = "Get a list of available calls.")
	@RequestMapping(value = "/brapi/v2/serverinfo", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<Map<String, Object>>> listAvailableCalls(
		@ApiParam(value = "data format supported by call", required = false, allowableValues = "application/json, text/csv, text/tsv, application/flapjack")
		@RequestParam(value = "dataType", required = false) final String dataType) {
		return this.getBrapiCallsResponseEntity(null, null, dataType);
	}

	@ApiOperation(value = "Get the list of implemented Calls", notes = "Get a list of available calls.")
	@RequestMapping(value = "/{crop}/brapi/v2/serverinfo", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<Map<String, Object>>> listAvailableCalls(@PathVariable final String crop,
		@ApiParam(value = "data format supported by call", required = false, allowableValues = "application/json, text/csv, text/tsv, application/flapjack")
		@RequestParam(value = "dataType", required = false) final String dataType) {
		return this.getBrapiCallsResponseEntity(null, null, dataType);
	}

	private ResponseEntity<EntityListResponse<Map<String, Object>>> getBrapiCallsResponseEntity(final Integer currentPage,
		final Integer pageSize,	final String dataType) {
		final PagedResult<Map<String, Object>> resultPage = new PaginatedSearch().executeBrapiSearch(currentPage, pageSize,
			new SearchSpec<Map<String, Object>>() {

			@Override
			public long getCount() {
				return ServerinfoResourceBrapi.this.callService.getAllCallsForV2(dataType).size();
			}

			@Override
			public List<Map<String, Object>> getResults(final PagedResult<Map<String, Object>> pagedResult) {
				return ServerinfoResourceBrapi.this.callService.getAllCallsForV2(dataType);
			}
		});

		final Result<Map<String, Object>> results = new Result<Map<String, Object>>().withCalls(resultPage.getPageResults());
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		return new ResponseEntity<>(new EntityListResponse<>(metadata, results), HttpStatus.OK);
	}
}
