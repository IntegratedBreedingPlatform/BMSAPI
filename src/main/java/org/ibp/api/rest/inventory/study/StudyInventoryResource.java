package org.ibp.api.rest.inventory.study;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.pojos.SortedPageRequest;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.inventory.common.InventoryLock;
import org.ibp.api.java.impl.middleware.inventory.study.StudyTransactionsService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
	public ResponseEntity<StudyInventoryTable> searchStudyTransactions(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody final StudyTransactionsRequest studyTransactionsRequest
	) {

		BaseValidator.checkNotNull(studyTransactionsRequest, "param.null", new String[] {"studyTransactionsRequest"});
		final SortedPageRequest sortedRequest = studyTransactionsRequest.getSortedPageRequest();
		BaseValidator.checkNotNull(sortedRequest, "sortedrequest.empty");

		final Integer pageNumber = sortedRequest.getPageNumber();
		final Integer pageSize = sortedRequest.getPageSize();

		PagedResult<StudyTransactionsDto> pagedResult;

		try {
			inventoryLock.lockRead();
			pagedResult = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<StudyTransactionsDto>() {
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
					return studyTransactionsService.searchStudyTransactions(studyId, studyTransactionsRequest);
				}
			});
		} finally {
			inventoryLock.unlockRead();
		}

		final StudyInventoryTable studyInventoryTable = new StudyInventoryTable();
		studyInventoryTable.setData(pagedResult.getPageResults());
		studyInventoryTable.setDraw(studyTransactionsRequest.getDraw());
		studyInventoryTable.setRecordsTotal((int) pagedResult.getTotalResults());
		studyInventoryTable.setRecordsFiltered((int) pagedResult.getFilteredResults());

		return new ResponseEntity<>(studyInventoryTable, HttpStatus.OK);
	}

	@ApiOperation(value = "Cancel pending Study Transactions", notes = "Cancel any transaction with pending status")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/transactions/cancellation", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','BREEDING_ACTIVITIES','MANAGE_STUDIES','MS_MANAGE_OBSERVATION_UNITS','MS_WITHDRAW_INVENTORY','MS_CANCEL_PENDING_TRANSACTIONS')")
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
