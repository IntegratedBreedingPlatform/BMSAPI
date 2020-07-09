package org.ibp.api.brapi.v2.inventory;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.pojos.SortedPageRequest;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "BrAPI Transaction Services")
@Controller
public class TransactionResourceBrapi {

	@Autowired
	private TransactionService transactionService;

	@ApiOperation(value = "Get a filtered list of Seed Lot Transactions", notes = "Get a filtered list of Seed Lot Transactions")
	@RequestMapping(value = "/crops/{cropName}/transactions", method = RequestMethod.GET)
	@JsonView(BrapiView.BrapiV2.class)
	@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_INVENTORY', 'MANAGE_TRANSACTIONS', 'VIEW_TRANSACTIONS')")
	@ResponseBody
	public ResponseEntity<EntityListResponse<TransactionDto>> getTransaction(@PathVariable final String cropName,
		@ApiParam(value = "Unique id for a transaction on this server") @RequestParam(value = "transactionDbId", required = false)
		final String transactionDbId,
		@ApiParam(value = "Unique id for a seed lot on this server") @RequestParam(value = "seedLotDbId", required = false)
		final String seedLotDbId,
		@ApiParam(value = "The internal id of the germplasm") @RequestParam(value = "germplasmDbId", required = false)
		final String germplasmDbId,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
			required = false) final Integer pageSize
	) {

		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage + 1;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		final SortedPageRequest sortedPageRequest = new SortedPageRequest(finalPageNumber, finalPageSize, null, null);
		final PagedResult<TransactionDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<TransactionDto>() {

				@Override
				public long getCount() {
					return TransactionResourceBrapi.this.transactionService.countTransactions(transactionDbId, seedLotDbId, germplasmDbId);
				}

				@Override
				public List<TransactionDto> getResults(final PagedResult<TransactionDto> pagedResult) {
					return TransactionResourceBrapi.this.transactionService.getTransactions(transactionDbId, seedLotDbId, germplasmDbId,
						sortedPageRequest);
				}
			});

		final List<TransactionDto> transactionDtoList = resultPage.getPageResults();

		final Result<TransactionDto> result = new Result<TransactionDto>().withData(transactionDtoList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final EntityListResponse<TransactionDto> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}
}
