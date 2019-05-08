package org.ibp.api.java.impl.middleware.search;

import org.generationcp.middleware.manager.api.BrapiSearchRequestService;
import org.generationcp.middleware.pojos.search.BrapiSearchRequest;
import org.ibp.api.brapi.v1.search.SearchRequestDto;
import org.springframework.beans.factory.annotation.Autowired;

public class BrapiSearchRequestServiceImpl implements org.ibp.api.java.search.BrapiSearchRequestService {

	@Autowired
	private BrapiSearchRequestService brapiSearchService;

	@Autowired
	private SearchRequestMapper searchRequestMapper;

	@Override
	public SearchRequestDto saveSearchRequest(
		final String crop, final SearchRequestDto searchRequestDto) {

		BrapiSearchRequest brapiSearchRequest = this.searchRequestMapper.map(searchRequestDto);
		brapiSearchRequest = this.brapiSearchService.saveSearchRequest(brapiSearchRequest);

		return searchRequestDto;
	}

	@Override
	public SearchRequestDto getSearchRequest(final Integer requestId) {

		final BrapiSearchRequest brapiSearchRequest = this.brapiSearchService.getSearchRequest(requestId);
		final SearchRequestDto searchRequestDto = this.searchRequestMapper.map(brapiSearchRequest);
		return searchRequestDto;
	}
}
