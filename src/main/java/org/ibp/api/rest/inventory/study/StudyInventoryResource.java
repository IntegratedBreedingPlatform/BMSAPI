package org.ibp.api.rest.inventory.study;

import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.pojos.SortedPageRequest;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.impl.middleware.inventory.common.InventoryLock;
import org.ibp.api.java.impl.middleware.inventory.study.StudyTransactionsService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "Study Inventory Services")
@RestController
public class StudyInventoryResource {

	@Autowired
	private InventoryLock inventoryLock;

	@Autowired
	private StudyTransactionsService studyTransactionsService;

	@ApiOperation(value = "Get transactions asociated to the study")
	@RequestMapping(value = "/crops/{cropName}/programs/{programUUID}/studies/{studyId}/transactions/search", method = RequestMethod.POST)
	public ResponseEntity<StudyInventoryTable> searchStudyTransactions(
		@PathVariable final String cropName,
		@PathVariable final String programUUID,
		@PathVariable final Integer studyId,
		@RequestBody final StudyTransactionsRequest studyTransactionsRequest
	) {

		Preconditions.checkNotNull(studyTransactionsRequest, "params cannot be null");
		final SortedPageRequest sortedRequest = studyTransactionsRequest.getSortedPageRequest();
		Preconditions.checkNotNull(sortedRequest, "sortedRequest inside params cannot be null");

		final Integer pageNumber = sortedRequest.getPageNumber();
		final Integer pageSize = sortedRequest.getPageSize();

		PagedResult<StudyTransactionsDto> pagedResult;

		try {
			inventoryLock.lockRead();
			pagedResult = new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<StudyTransactionsDto>() {
				@Override
				public long getCount() {
					return studyTransactionsService.countAllStudyTransactions(studyId, studyTransactionsRequest);
				}

				@Override
				public long getFilteredCount() {
					return studyTransactionsService.countFilteredStudyTransactions(studyId, studyTransactionsRequest);
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

}
