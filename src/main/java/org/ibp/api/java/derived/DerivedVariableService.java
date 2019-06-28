package org.ibp.api.java.derived;

import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DerivedVariableService {

	Map<String, Object> execute(
		final int studyId, final int datasetId, final Integer variableId, final List<Integer> geoLocationIds,
		final Map<Integer, Integer> inputVariableDatasetMap,
		final boolean overwriteExistingData);

	Set<String> getDependencyVariables(final int studyId, final int datasetId);

	Set<String> getDependencyVariables(int studyId, int datasetId, int variableId);

	long countCalculatedVariablesInDatasets(final int studyId, final Set<Integer> datasetIds);

	Map<Integer, Map<String, Object>> getInputVariableDatasetMap(final Integer studyId, final Integer variableId);
}
