package org.ibp.api.brapi.v2.inventory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

@Api(value = "BrAPI Transaction Services")
@Controller
public class TransactionResourceBrapi {

	@Autowired
	private TransactionService transactionService;

	@ApiOperation(value = "Get a filtered list of Seed Lot Transactions", notes = "Get a filtered list of Seed Lot Transactions")
	@RequestMapping(value = "/{cropName}/brapi/v2/transactions", method = RequestMethod.GET)
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
		final TransactionsSearchDto searchDTO = this.getTransactionsSearchDto(transactionDbId, seedLotDbId, germplasmDbId);

		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;

		final PagedResult<TransactionDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(finalPageNumber, finalPageSize, new SearchSpec<TransactionDto>() {

				@Override
				public long getCount() {
					return TransactionResourceBrapi.this.transactionService.countSearchTransactions(searchDTO);
				}

				@Override
				public List<TransactionDto> getResults(final PagedResult<TransactionDto> pagedResult) {
					return TransactionResourceBrapi.this.transactionService.getTransactions(searchDTO,	new PageRequest(finalPageNumber, finalPageSize));
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

	TransactionsSearchDto getTransactionsSearchDto(final String transactionDbId, final String seedLotDbId, final String germplasmDbId) {
		final TransactionsSearchDto searchDTO = new TransactionsSearchDto();

		if(!StringUtils.isEmpty(transactionDbId)) {
			searchDTO.setTransactionIds(Collections.singletonList(Integer.valueOf(transactionDbId)));
		}

		if(!StringUtils.isEmpty(germplasmDbId)) {
			// GermplasmDbID = Germplasm_UUID
			searchDTO.setGermplasmUUIDs(Collections.singletonList(germplasmDbId));
		}

		if(!StringUtils.isEmpty(seedLotDbId)) {
			searchDTO.setLotUUIDs(Collections.singletonList(seedLotDbId));
		}

		//Retrieve transactions with active lot only
		searchDTO.setLotStatus(LotStatus.ACTIVE.getIntValue());
		return searchDTO;
	}
}
