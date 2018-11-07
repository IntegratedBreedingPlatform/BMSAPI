package org.ibp.api.java.dataset;

import java.util.List;

import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.dataset.ObservationValue;

public interface DatasetService {

	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);

	MeasurementVariable addDatasetVariable(Integer studyId, Integer datasetId, DatasetVariable datasetVariable);

	void removeVariables(Integer studyId, Integer datasetId, List<Integer> variableIds);

	ObservationDto addObservation(Integer studyId, Integer datasetId, Integer observationUnitId, final ObservationDto observation);

	ObservationDto updateObservation(
		Integer studyId, Integer datasetId, Integer observationId, Integer observationUnitId, ObservationValue observationValue);

}
