package org.ibp.api.brapi.v1.calls;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
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
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a>
 * Call services.
 */
@Api(value = "BrAPI Call Services")
@Controller
public class CallResourceBrapi {

	@Autowired
	private CallService callService;

	@ApiOperation(value = "List of available calls", notes = "Get a list of available calls.")
	@RequestMapping(value = "/{crop}/brapi/v1/calls", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<BrapiCalls> listAvailableCalls(@PathVariable final String crop,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
			required = false) final Integer pageSize,
		@ApiParam(value = "data format supported by call", required = false, allowableValues = "csv, text/csv, tsv, text/tsv, json, application/json, application/flapjack")
		@RequestParam(value = "dataType",
			required = false) final String dataType) {

		PagedResult<Map<String, Object>> resultPage = null;

		resultPage = new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<Map<String, Object>>() {

			@Override
			public long getCount() {
				return CallResourceBrapi.this.callService.getAllCalls(dataType, null, null).size();
			}

			@Override
			public List<Map<String, Object>> getResults(final PagedResult<Map<String, Object>> pagedResult) {
				return CallResourceBrapi.this.callService.getAllCalls(dataType, pageSize, currentPage);
			}
		});

		if (resultPage != null && resultPage.getTotalResults() > 0) {

			final Result<Map<String, Object>> results = new Result<Map<String, Object>>().withData(resultPage.getPageResults());
			final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
				.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

			final Metadata metadata = new Metadata().withPagination(pagination);
			final BrapiCalls brapiCalls = new BrapiCalls().withMetadata(metadata).withResult(results);
			return new ResponseEntity<>(brapiCalls, HttpStatus.OK);

		} else {

			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", "not found calls"));
			final Metadata metadata = new Metadata(null, status);
			final BrapiCalls brapiCalls = new BrapiCalls().withMetadata(metadata);
			return new ResponseEntity<>(brapiCalls, HttpStatus.NOT_FOUND);
		}
	}

}
