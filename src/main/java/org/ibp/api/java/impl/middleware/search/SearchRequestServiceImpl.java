package org.ibp.api.java.impl.middleware.search;

import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.search.SearchRequest;
import org.ibp.api.brapi.v1.search.SearchRequestDto;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchRequestServiceImpl implements org.ibp.api.java.search.SearchRequestService {

	@Autowired
	private SearchRequestService searchRequest;

	@Autowired
	private SearchRequestMapper searchRequestMapper;

	@Override
	public SearchRequestDto saveSearchRequest(
		final String crop, final SearchRequestDto searchRequestDto) {

		SearchRequest searchRequest = this.searchRequestMapper.map(searchRequestDto);
		searchRequest = this.searchRequest.saveSearchRequest(searchRequest);

		return searchRequestDto;
	}

	@Override
	public SearchRequestDto getSearchRequest(final Integer requestId) {

		final SearchRequest searchRequest = this.searchRequest.getSearchRequest(requestId);
		final SearchRequestDto searchRequestDto = this.searchRequestMapper.map(searchRequest);
		return searchRequestDto;
	}
}
