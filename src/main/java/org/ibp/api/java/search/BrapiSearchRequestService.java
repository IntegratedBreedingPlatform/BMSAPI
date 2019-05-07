
package org.ibp.api.java.search;

import org.ibp.api.brapi.v1.search.SearchRequestDto;

public interface BrapiSearchRequestService {

	SearchRequestDto saveSearchRequest(String crop, SearchRequestDto searchRequestDto);

	SearchRequestDto getSearchRequest(Integer requestId);
}
