package org.ibp.api.java.derived;

import org.ibp.api.rest.derived.CalculateVariableRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface DerivedVariableService {

	Map<String, Object> execute(
		final int studyId, final int datasetId, final Integer variableId, final List<Integer> geoLocationIds);
}
