package org.ibp.api.java.call;

import org.ibp.api.brapi.v1.calls.BrapiCall;

import java.util.List;

public interface CallService {

	List<BrapiCall> getAllCalls(final String dataType, final Integer pageSize, final Integer pageNumber);
}
