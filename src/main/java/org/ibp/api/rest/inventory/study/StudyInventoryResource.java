package org.ibp.api.rest.inventory.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.inventory.common.InventoryLock;
import org.ibp.api.java.impl.middleware.inventory.study.StudyTransactionsService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "Study Inventory Services")
@RestController
public class StudyInventoryResource {

	@Autowired
	private InventoryLock inventoryLock;

	@Autowired
	private StudyTransactionsService studyTransactionsService;

	@ApiOperation(value = "Get transactions associated to the study")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/transactions/search", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (1..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = false, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property,asc|desc. ")
	})
	public ResponseEntity<List<StudyTransactionsDto>> searchStudyTransactions(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody final StudyTransactionsRequest studyTransactionsRequest,
		@ApiIgnore @PageableDefault(page = 0, size = PagedResult.DEFAULT_PAGE_SIZE) final Pageable pageable
	) {

		BaseValidator.checkNotNull(studyTransactionsRequest, "param.null", new String[] {"studyTransactionsRequest"});
		PagedResult<StudyTransactionsDto> pagedResult;
		try {
			inventoryLock.lockRead();
			pagedResult =
				new PaginatedSearch().execute(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<StudyTransactionsDto>() {

					@Override
					public long getCount() {
						return studyTransactionsService.countStudyTransactions(studyId, null);
					}

					@Override
					public long getFilteredCount() {
						return studyTransactionsService.countStudyTransactions(studyId, studyTransactionsRequest);
					}

					@Override
					public List<StudyTransactionsDto> getResults(final PagedResult<StudyTransactionsDto> pagedResult) {
						return studyTransactionsService.searchStudyTransactions(studyId, studyTransactionsRequest, pageable);
					}
				});
		} finally {
			inventoryLock.unlockRead();
		}

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(pagedResult.getFilteredResults()));
		headers.add("X-Total-Pages", Long.toString(pagedResult.getTotalPages()));
		return new ResponseEntity<>(pagedResult.getPageResults(), headers, HttpStatus.OK);

	}

	@ApiOperation(value = "Cancel pending Study Transactions", notes = "Cancel any transaction with pending status")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/transactions/cancellation", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','STUDIES','MANAGE_STUDIES','MS_MANAGE_OBSERVATION_UNITS','MS_WITHDRAW_INVENTORY','MS_CANCEL_PENDING_TRANSACTIONS')")
	public ResponseEntity<Void> cancelPendingTransaction(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@ApiParam("List of transactions to be cancelled, use a searchId or a list of transaction ids")
		@RequestBody final SearchCompositeDto<Integer, Integer> searchCompositeDto) {
		try {
			this.
				inventoryLock.lockWrite();
			this.studyTransactionsService.cancelPendingTransactions(studyId, searchCompositeDto);
			return new ResponseEntity<>(HttpStatus.OK);
		} finally {
			inventoryLock.unlockWrite();
		}
	}

}
