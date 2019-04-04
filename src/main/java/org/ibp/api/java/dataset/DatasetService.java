package org.ibp.api.java.dataset;

import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DatasetService {

	List<MeasurementVariable> getObservationSetColumns(Integer studyId, Integer subObservationSetId, Boolean draftMode);

	List<MeasurementVariable> getSubObservationSetVariables(
		Integer studyId, Integer subObservationSetId);

	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> variableIds);

	long countPhenotypesByInstance(Integer studyId, Integer datasetId, Integer instanceId);

	MeasurementVariable addDatasetVariable(Integer studyId, Integer datasetId, DatasetVariable datasetVariable);

	List<MeasurementVariableDto> getVariables(Integer studyId, Integer datasetId, VariableType variableType);

	void removeVariables(Integer studyId, Integer datasetId, List<Integer> variableIds);

	List<DatasetDTO> getDatasets(Integer studyId, Set<Integer> datasetTypeIds);

	DatasetDTO generateSubObservationDataset(
		String cropName, Integer studyId, Integer parentId, DatasetGeneratorInput datasetGeneratorInput);

	DatasetDTO getDataset(String crop, Integer studyId, Integer datasetId);

	Map<Integer, List<ObservationUnitRow>> getInstanceObservationUnitRowsMap(int studyId, int datasetId, List<Integer> instanceId);

	List<ObservationUnitRow> getObservationUnitRows(
		int studyId, int datasetId, ObservationUnitsSearchDTO searchDTO);

	ObservationDto addObservation(Integer studyId, Integer datasetId, Integer observationUnitId, ObservationDto observation);

	ObservationDto updateObservation(
		Integer studyId, Integer datasetId, Integer observationId, Integer observationUnitId, ObservationDto observationDto);

	Integer countAllObservationUnitsForDataset(final Integer datasetId, final Integer instanceId, final Boolean draftMode);

	long countFilteredObservationUnitsForDataset(
		Integer datasetId, Integer instanceId, final Boolean draftMode, ObservationUnitsSearchDTO.Filter filter);

	void deleteObservation(final Integer studyId, final Integer datasetId, final Integer observationUnitId, final Integer observationId);

	void importObservations(Integer studyId, Integer datasetId, ObservationsPutRequestInput input);

	List<StudyInstance> getDatasetInstances(Integer studyId, Integer datasetId);

	List<MeasurementVariable> getMeasurementVariables(Integer projectId, List<Integer> variableTypes);

	void acceptDraftData(Integer studyId, Integer datasetId);

	void rejectDraftData(Integer studyId, Integer datasetId);

	Boolean checkOutOfBoundDraftData(Integer studyId, Integer datasetId);

	void setValuesToMissing(Integer studyId, Integer datasetId);
}
