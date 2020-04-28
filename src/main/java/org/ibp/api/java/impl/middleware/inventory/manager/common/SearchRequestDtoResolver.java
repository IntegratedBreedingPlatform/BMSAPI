package org.ibp.api.java.impl.middleware.inventory.manager.common;

import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SearchRequestDtoResolver {

	@Autowired
	private SearchRequestService searchRequestService;

	public LotsSearchDto getLotsSearchDto(
		final SearchCompositeDto searchCompositeDto) {

		final LotsSearchDto searchDTO;
		if (searchCompositeDto.getSearchRequestId() != null) {
			searchDTO =
				(LotsSearchDto) this.searchRequestService.getSearchRequest(searchCompositeDto.getSearchRequestId(), LotsSearchDto.class);
		} else {
			searchDTO = new LotsSearchDto();
			searchDTO.setLotIds(new ArrayList<>(searchCompositeDto.getItemIds()));
		}
		return searchDTO;
	}

	public TransactionsSearchDto getTransactionsSearchDto(final SearchCompositeDto searchCompositeDto) {
		TransactionsSearchDto transactionsSearchDto;
		if (searchCompositeDto.getSearchRequestId() != null) {
			transactionsSearchDto =
				(TransactionsSearchDto) this.searchRequestService
					.getSearchRequest(searchCompositeDto.getSearchRequestId(), TransactionsSearchDto.class);
		} else {
			transactionsSearchDto = new TransactionsSearchDto();
			transactionsSearchDto.setTransactionIds(new ArrayList<>(searchCompositeDto.getItemIds()));
		}
		return transactionsSearchDto;
	}

}
