package org.ibp.api.java.dataset;

import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.dataset.ObservationValue;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DatasetService {

	List<MeasurementVariable> getSubObservationSetColumns(final Integer studyId, final Integer subObservationSetId);

	List<MeasurementVariable> getAllDatasetVariables(final Integer studyId, final Integer subObservationSetId);

	long countPhenotypes(final Integer studyId, final Integer datasetId, final List<Integer> variableIds);

	long countPhenotypesByInstance(final Integer studyId, final Integer datasetId, final Integer instanceId);

	MeasurementVariable addDatasetVariable(final Integer studyId, final Integer datasetId, final DatasetVariable datasetVariable);

	void removeVariables(final Integer studyId, final Integer datasetId, final List<Integer> variableIds);

	List<DatasetDTO> getDatasets(final Integer studyId, final Set<Integer> datasetTypeIds);

	DatasetDTO generateSubObservationDataset(String cropName, Integer studyId, Integer parentId, DatasetGeneratorInput datasetGeneratorInput);

	DatasetDTO getDataset(final String crop, final Integer studyId, final Integer datasetId);

	Map<Integer, List<ObservationUnitRow>> getInstanceObservationUnitRowsMap(final int studyId, final int datasetId, final List<Integer> instanceId);

	List<ObservationUnitRow>  getObservationUnitRows(final int studyId, final int datasetId, final int instanceId, final int pageNumber,
		final int pageSize, final String sortBy, final String sortOrder);

	ObservationDto addObservation(Integer studyId, Integer datasetId, Integer observationUnitId, final ObservationDto observation);

	ObservationDto updateObservation(
		Integer studyId, Integer datasetId, Integer observationId, Integer observationUnitId, ObservationValue observationValue);

	int countTotalObservationUnitsForDataset(final int datasetId, final int instanceId);
	
	void deleteObservation(final Integer studyId, final Integer datasetId, final Integer observationUnitId, final Integer observationId);

	void importObservations(Integer studyId, Integer datasetId, ObservationsPutRequestInput input);

	List<StudyInstance> getDatasetInstances(final Integer studyId, final Integer datasetId);

	List<MeasurementVariable> getMeasurementVariables(final Integer projectId, final List<Integer> variableTypes);
}
