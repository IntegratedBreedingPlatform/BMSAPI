
package org.ibp.api.java.search;

import org.generationcp.middleware.domain.search_request.SearchRequestDto;

public interface SearchRequestService {

	SearchRequestDto saveSearchRequest(String crop, SearchRequestDto searchRequestDto);

	SearchRequestDto getSearchRequest(Integer requestId);
}
