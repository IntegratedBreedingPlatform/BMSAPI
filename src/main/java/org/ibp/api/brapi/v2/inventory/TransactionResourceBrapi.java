package org.ibp.api.brapi.v2.inventory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collections;
import java.util.List;

@Api(value = "BrAPI Transaction Services")
@Controller
public class TransactionResourceBrapi {

	@Autowired
	private TransactionService transactionService;

	@ApiOperation(value = "Get a filtered list of Seed Lot Transactions", notes = "Get a filtered list of Seed Lot Transactions")
	@RequestMapping(value = "/crops/{cropName}/transactions", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.")
	})
	@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT','MANAGE_INVENTORY', 'MANAGE_TRANSACTIONS', 'VIEW_TRANSACTIONS')")
	@ResponseBody
	public ResponseEntity<List<TransactionDto>> getTransaction(@PathVariable final String cropName,
		@ApiParam(value = "Unique id for a transaction on this server") @RequestParam(value = "transactionDbId", required = false)
		final String transactionDbId,
		@ApiParam(value = "Unique id for a seed lot on this server") @RequestParam(value = "seedLotDbId", required = false)
		final String seedLotDbId,
		@ApiParam(value = "The internal id of the germplasm") @RequestParam(value = "germplasmDbId", required = false)
		final String germplasmDbId,
		@ApiIgnore
		final Pageable pageable
	) {
		final TransactionsSearchDto searchDTO = this.getTransactionsSearchDto(transactionDbId, seedLotDbId, germplasmDbId);

		final PagedResult<TransactionDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<TransactionDto>() {

				@Override
				public long getCount() {
					return TransactionResourceBrapi.this.transactionService.countSearchTransactions(searchDTO);
				}

				@Override
				public List<TransactionDto> getResults(final PagedResult<TransactionDto> pagedResult) {
					return TransactionResourceBrapi.this.transactionService.getTransactions(searchDTO,	pageable);
				}
			});

		final List<TransactionDto> transactionDtos = resultPage.getPageResults();

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));

		return new ResponseEntity<>(transactionDtos, headers, HttpStatus.OK);
	}

	TransactionsSearchDto getTransactionsSearchDto(final String transactionDbId, final String seedLotDbId, final String germplasmDbId) {
		final TransactionsSearchDto searchDTO = new TransactionsSearchDto();

		if(!StringUtils.isEmpty(transactionDbId)) {
			searchDTO.setTransactionIds(Collections.singletonList(Integer.valueOf(transactionDbId)));
		}

		if(!StringUtils.isEmpty(germplasmDbId)) {
			searchDTO.setGids(Collections.singletonList(Integer.valueOf(germplasmDbId)));
		}

		if(!StringUtils.isEmpty(seedLotDbId)) {
			searchDTO.setLotUUIDs(Collections.singletonList(seedLotDbId));
		}

		//Retrieve transactions with active lot only
		searchDTO.setLotStatus(0);
		return searchDTO;
	}
}
