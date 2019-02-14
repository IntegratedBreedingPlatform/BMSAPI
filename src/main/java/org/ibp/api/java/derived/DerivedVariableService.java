package org.ibp.api.java.derived;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DerivedVariableService {

	Map<String, Object> execute(
		final int studyId, final int datasetId, final Integer variableId, final List<Integer> geoLocationIds);

	Set<String> getDependencyVariables(final int studyId, final int datasetId);

}
