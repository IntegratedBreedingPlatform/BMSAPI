package org.ibp.api.java.dataset;

import org.ibp.api.rest.dataset.ObservationUnitRow;

import java.util.List;

public interface DatasetService {
	
	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);

	List<ObservationUnitRow> getObservationUnitRows(final int studyId, final int datasetId, final int instanceId,
		final int pageNumber, final int pageSize, final String sortBy, final String sortOrder);

	int countTotalObservationUnitsForDataset(final int datasetId, final int instanceId);
}
