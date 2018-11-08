package org.ibp.api.java.dataset;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;

import java.util.List;

public interface DatasetService {

	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);

	long countPhenotypesByInstance(Integer studyId, Integer datasetId, Integer instanceId);

	MeasurementVariable addDatasetVariable(Integer studyId, Integer datasetId, DatasetVariable datasetVariable);

	void removeVariables(Integer studyId, Integer datasetId, List<Integer> variableIds);

}
