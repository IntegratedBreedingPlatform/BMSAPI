package org.ibp.api.java.dataset;

import org.ibp.api.rest.dataset.ObservationUnitRow;

import org.ibp.api.rest.dataset.DatasetDTO;

import java.util.List;
import java.util.Set;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;

public interface DatasetService {
	
	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);

	MeasurementVariable addDatasetVariable(Integer studyId, Integer datasetId, DatasetVariable datasetVariable);

	List<DatasetDTO> getDatasets(final Integer studyId, final Set<Integer> datasetTypeIds);


	List<ObservationUnitRow> getObservationUnitRows(final int studyId, final int datasetId, final int instanceId,
		final int pageNumber, final int pageSize, final String sortBy, final String sortOrder);

	int countTotalObservationUnitsForDataset(final int datasetId, final int instanceId);
}
