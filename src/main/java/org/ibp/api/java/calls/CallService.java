package org.ibp.api.java.calls;

import java.util.List;
import java.util.Map;

public interface CallService {

	List<Map<String, Object>> getAllCalls(final String dataType, final String version, final Integer pageSize, final Integer pageNumber);
}
