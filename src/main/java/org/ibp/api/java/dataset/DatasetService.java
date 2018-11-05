package org.ibp.api.java.dataset;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;

public interface DatasetService {
	
	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);

	long countPhenotypesByInstance(Integer studyId, Integer datasetId, Integer instanceId);

	MeasurementVariable addDatasetVariable(Integer studyId, Integer datasetId, DatasetVariable datasetVariable);
	
	void removeVariables(Integer studyId, Integer datasetId, List<Integer> variableIds);

}
