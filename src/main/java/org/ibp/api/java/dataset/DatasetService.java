package org.ibp.api.java.dataset;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.ObservationUnitRow;

import java.util.List;
import java.util.Set;

public interface DatasetService {

	List<MeasurementVariable> getSubObservationSetColumns(final Integer studyId, final Integer subObservationSetId);

	long countPhenotypes(final Integer studyId, final Integer datasetId, final List<Integer> traitIds);

	MeasurementVariable addDatasetVariable(final Integer studyId, final Integer datasetId, final DatasetVariable datasetVariable);

	List<DatasetDTO> getDatasets(final Integer studyId, final Set<Integer> datasetTypeIds);

	DatasetDTO getDataset(final String crop, final Integer studyId, final Integer datasetId);

	List<ObservationUnitRow> getObservationUnitRows(final int studyId, final int datasetId, final int instanceId, final int pageNumber,
		final int pageSize, final String sortBy, final String sortOrder);

	int countTotalObservationUnitsForDataset(final int datasetId, final int instanceId);


}
