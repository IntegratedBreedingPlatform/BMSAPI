package org.ibp.api.java.dataset;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.rest.dataset.DatasetDTO;

import java.util.List;
import java.util.Set;

public interface DatasetService {

	List<MeasurementVariable> getSubObservationSetColumns(final Integer studyId, Integer subObservationSetId);

	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);
	
	MeasurementVariable addDatasetVariable(Integer studyId, Integer datasetId, DatasetVariable datasetVariable);

	List<DatasetDTO> getDatasets(final Integer studyId, final Set<Integer> datasetTypeIds);

}
