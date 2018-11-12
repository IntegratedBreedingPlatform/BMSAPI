package org.ibp.api.java.impl.middleware.dataset;

import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.dataset.ObservationUnitTable;

import java.util.List;
import org.ibp.api.rest.dataset.DatasetDTO;

import java.util.List;
import java.util.Set;

/**
 * Created by clarysabel on 10/24/18.
 */
public interface DatasetService {


	List<DatasetDTO> getDatasetByStudyId(final Integer studyId, final Set<Integer> filterByTypeIds);

	int countTotalObservationUnitsForDataset(final int datasetId, final int instanceId);

	List<ObservationUnitRow> getObservationUnitRows(
		final int studyId, final int datasetId, final int instanceId, final int pageNumber, final int pageSize,
		final String sortBy, final String sortOrder);

}
