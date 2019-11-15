package org.ibp.api.java.derived;

import org.generationcp.middleware.domain.dms.VariableDatasetsDTO;
import org.generationcp.middleware.domain.ontology.FormulaVariable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DerivedVariableService {

	Map<String, Object> execute(
		final int studyId, final int datasetId, final Integer variableId, final List<Integer> geoLocationIds,
		final Map<Integer, Integer> inputVariableDatasetMap,
		final boolean overwriteExistingData);

	Set<FormulaVariable> getMissingFormulaVariablesInStudy(final int studyId, final int datasetId, final int variableId);

	Set<FormulaVariable> getFormulaVariablesInStudy(final int studyId, final int datasetId);

	long countCalculatedVariablesInDatasets(final int studyId, final Set<Integer> datasetIds);

	Map<Integer, VariableDatasetsDTO> getFormulaVariableDatasetsMap(final Integer studyId, final Integer datasetId,
		final Integer variableId);
}
