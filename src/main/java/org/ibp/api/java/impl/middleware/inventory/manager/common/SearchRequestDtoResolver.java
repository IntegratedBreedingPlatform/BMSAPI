package org.ibp.api.java.impl.middleware.inventory.manager.common;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.domain.search_request.GidSearchDto;
import org.generationcp.middleware.domain.search_request.SearchRequestDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.api.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SearchRequestDtoResolver {

	@Autowired
	private SearchRequestService searchRequestService;

	public LotsSearchDto getLotsSearchDto(
		final SearchCompositeDto<Integer, String> searchCompositeDto) {

		final LotsSearchDto searchDTO;
		if (searchCompositeDto.getSearchRequest() != null) {
			searchDTO =
				(LotsSearchDto) this.searchRequestService.getSearchRequest(searchCompositeDto.getSearchRequest(), LotsSearchDto.class);
		} else {
			searchDTO = new LotsSearchDto();
			searchDTO.setLotUUIDs(searchCompositeDto.getItemIds());
		}
		return searchDTO;
	}

	public TransactionsSearchDto getTransactionsSearchDto(final SearchCompositeDto<Integer, Integer> searchCompositeDto) {
		TransactionsSearchDto transactionsSearchDto;
		if (searchCompositeDto.getSearchRequest() != null) {
			transactionsSearchDto =
				(TransactionsSearchDto) this.searchRequestService
					.getSearchRequest(searchCompositeDto.getSearchRequest(), TransactionsSearchDto.class);
		} else {
			transactionsSearchDto = new TransactionsSearchDto();
			transactionsSearchDto.setTransactionIds(searchCompositeDto.getItemIds());
		}
		return transactionsSearchDto;
	}

	public List<Integer> resolveGidSearchDto(final SearchCompositeDto<Integer, Integer> searchCompositeDto) {
		if (searchCompositeDto.getSearchRequest() != null) {
			final GidSearchDto searchRequest =
				(GidSearchDto) this.searchRequestService.getSearchRequest(searchCompositeDto.getSearchRequest(), GidSearchDto.class);
			return searchRequest.getGids();
		} else if (!Util.isNullOrEmpty(searchCompositeDto.getItemIds())) {
			return searchCompositeDto.getItemIds();
		}
		return Collections.EMPTY_LIST;
	}

}
