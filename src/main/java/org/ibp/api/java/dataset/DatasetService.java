package org.ibp.api.java.dataset;

import org.generationcp.middleware.api.brapi.v1.observation.ObservationDTO;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.service.api.dataset.FilteredPhenotypesInstancesCountDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitEntryReplaceRequest;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsParamDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.java.impl.middleware.study.ObservationUnitsMetadata;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DatasetService {

	/**
	 * Given a dataset, it will retrieve the list of variables to be displayed as columns of the observations table.
	 * It will always return the union between the factors of the parent dataset, the variables of the specified dataset
	 * and some virtual columns that needs to be shown in the observations table (i.e. SAMPLES)
	 * draftMode == TRUE indicates that the dataset variables will only retrieve only the variables that contains draft data
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @param draftMode Will only retrieve variables with draft data if TRUE
	 * @return List of Measurement Variables
	 */
	List<MeasurementVariable> getObservationSetColumns(Integer studyId, Integer datasetId, Boolean draftMode);

	/**
	 * Given a dataset, it will retrieve the union between the parent dataset variables and the dataset variables
	 *
	 * @param studyId   Id of the Study
	 * @param datasetId Id of the Dataset
	 * @return List of Measurement Variables.
	 */
	List<MeasurementVariable> getSubObservationSetVariables(
		Integer studyId, Integer datasetId);

	/**
	 * Given a dataset and a list of variables, it will count how many observations it has associated.
	 * Count all the dataset observations if variableIds is EMPTY
	 *
	 * @param studyId     Id of the study
	 * @param datasetId   Id of the dataset
	 * @param variableIds List of variables
	 * @return Number of observations per variables
	 */
	//TODO it does not validate that variableIds cant be NULL
	long countObservationsByVariables(Integer studyId, Integer datasetId, List<Integer> variableIds);

	/**
	 * Given a dataset and an instance, it will count how many observations it has associated.
	 *
	 * @param studyId    Id of the study
	 * @param datasetId  Id of the dataset
	 * @param instanceId Id of the instance.
	 * @return Number of observations by instance
	 */
	//TODO add Precondition in Middleware, instanceId cant be null
	long countObservationsByInstance(Integer studyId, Integer datasetId, Integer instanceId);

	/**
	 * Adds a variable to the dataset. Variable type MUST be Trait or Selection
	 *
	 * @param studyId         Id of the study
	 * @param datasetId       Id of the dataset
	 * @param datasetVariable Variable to be added
	 * @return A measurement variable.
	 */
	MeasurementVariable addDatasetVariable(Integer studyId, Integer datasetId, DatasetVariable datasetVariable);

	/**
	 * Get the list of dataset variables of an specific variable type
	 *
	 * @param studyId      Id of the study
	 * @param datasetId    If of the dataset
	 * @param variableType Variable Type
	 * @return List of variables
	 */
	//TODO Use MeasurementVariable if possible
	//TODO This function and the following one do similar stuff, we should unify them in only one.
	List<MeasurementVariableDto> getDatasetVariablesByType(Integer studyId, Integer datasetId, VariableType variableType);

	/**
	 * Get the list of dataset variables with specific types indicated in variableTypes list
	 *
	 * @param projectId     Id of the project
	 * @param variableTypes
	 * @return List of measurement variables
	 */
	List<MeasurementVariable> getMeasurementVariables(Integer projectId, List<Integer> variableTypes);

	/**
	 * Given a dataset and a list of variables, it will de-associated them from the dataset
	 *
	 * @param studyId     Id of the study
	 * @param datasetId   Id of the dataset
	 * @param variableIds List of variables
	 */
	void removeDatasetVariables(Integer studyId, Integer datasetId, List<Integer> variableIds);

	/**
	 * Return the list of instances for an specific dataset
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @return List of StudyInstances
	 */
	List<StudyInstance> getDatasetInstances(Integer studyId, Integer datasetId);

	/**
	 * Given a list of dataset types and a study, it will retrieve the study datasets with the specified types
	 *
	 * @param studyId        Id of the study
	 * @param datasetTypeIds List of dataset types
	 * @return List of datasets
	 */
	List<DatasetDTO> getDatasets(Integer studyId, Set<Integer> datasetTypeIds);

	/**
	 * Returns the list of observation unit rows (represented as List of HashMap) that matches the search param
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @param searchDTO Search DTO
	 * @return List of Variable (Column) Name and Value Map
	 */
	List<Map<String, Object>> getObservationUnitRowsAsMapList(
		int studyId, int datasetId, ObservationUnitsSearchDTO searchDTO);

	/**
	 * Generates a sub-observation dataset for the indicated parent id
	 *
	 * @param cropName              Crop name
	 * @param studyId               Id of the study
	 * @param parentId              Id of the parent dataset
	 * @param datasetGeneratorInput Dataset Generator Input.
	 * @return The new created dataset
	 */
	DatasetDTO generateSubObservationDataset(
		String cropName, Integer studyId, Integer parentId, DatasetGeneratorInput datasetGeneratorInput);

	/**
	 * Return a dataset given the id
	 *
	 * @param crop      Crop name
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @return
	 */
	DatasetDTO getDataset(String crop, Integer studyId, Integer datasetId);

	/**
	 * Returns a map where the key is the instance id and the value is the list of observation unit rows that belongs to the instance
	 *
	 * @param studyId    Id of the study
	 * @param datasetId  Id of the dataset
	 * @param instanceId Id of the instance
	 * @return Map<Integer, List < ObservationUnitRow>>
	 */
	Map<Integer, List<ObservationUnitRow>> getInstanceObservationUnitRowsMap(int studyId, int datasetId, List<Integer> instanceId);

	/**
	 * Returns the list of observation unit rows that matches the search param
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @param searchDTO Search DTO
	 * @param pageable Pagination parameters
	 * @return List of ObservationUnitRow
	 */
	//TODO ObservationUnitsSearchDTO exposes a set of attributes that can not be used by the user to configure the search.
	List<ObservationUnitRow> getObservationUnitRows(
		int studyId, int datasetId, ObservationUnitsSearchDTO searchDTO, Pageable pageable);

	/**
	 * Create a new observation for the specified dataset and observation unit id
	 * Notice that status, updated date and created date are internally set so values in observation object will be discarded
	 *
	 * @param studyId           Id of the study
	 * @param datasetId         Id of the dataset
	 * @param observationUnitId Id of the observation unit
	 * @param observation       Observation to be added
	 * @return The new created ObservationDto
	 */
	//TODO Add Json views to ObservationDto
	ObservationDto createObservation(Integer studyId, Integer datasetId, Integer observationUnitId, ObservationDto observation);

	/**
	 * Modify an existing observation. Some attributes can not be updated
	 *
	 * @param studyId           Id of the study
	 * @param datasetId         Id of the dataset
	 * @param observationId     Id of the observation
	 * @param observationUnitId Id of the observation unit
	 * @param observationDto    Observation to be updated
	 * @return Updated ObservationDTO
	 */
	ObservationDto updateObservation(
		Integer studyId, Integer datasetId, Integer observationId, Integer observationUnitId, ObservationDto observationDto);

	/**
	 * Delete an existing observation
	 *
	 * @param studyId           Id of the study
	 * @param datasetId         Id of the dataset
	 * @param observationUnitId Id of the observation unit
	 * @param observationId     Id of the observation to be deleted
	 */
	void deleteObservation(Integer studyId, Integer datasetId, Integer observationUnitId, Integer observationId);

	/**
	 * It will import a list of observation presented as a List<List<String>>.
	 * Also allows to specify processing warnings or not.
	 * As of now, it will only import data in draft mode.
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @param input     ObservationsPutRequestInput
	 */
	void importObservations(Integer studyId, Integer datasetId, ObservationsPutRequestInput input);

	/**
	 * Imports a list of observations for the environment
	 *
	 * @param studyDbId   Id of the study/environment
	 * @param input     ObservationsPutRequestInput
	 */
	void importObservations(Integer studyDbId, List<ObservationDTO> input);

	/**
	 * Count how many instances and observations are filtered given a filter with a not null variable
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @param filter    Filter
	 * @return FilteredPhenotypesInstancesCountDTO
	 */
	FilteredPhenotypesInstancesCountDTO countFilteredInstancesAndPhenotypes(Integer studyId, Integer datasetId,
		ObservationUnitsSearchDTO filter);

	/**
	 * Count all observation units for a dataset (draftMode = FALSE to count all of them, draftMode = TRUE to count only observation
	 * units with at least one draft observation)
	 *
	 * @param datasetId  Id of the dataset
	 * @param instanceId Id of the instance
	 * @param draftMode  Indicates to count all observation units  or draft observations
	 * @return Number of observations units that matches the dataset id and draftMode
	 */
	Integer countAllObservationUnitsForDataset(Integer datasetId, Integer instanceId, Boolean draftMode);

	/**
	 * Count how many observation units are affected by a filter
	 * (draftMode = FALSE to count all of them, draftMode = TRUE to count only observation
	 * units with at least one draft observation)
	 *
	 * @param datasetId  Id of the dataset
	 * @param instanceId Id of the instance
	 * @param draftMode  draftMode
	 * @param filter     Filyer
	 * @return Number of observation units that matches the datasetId, draftMode and filter
	 */
	long countFilteredObservationUnitsForDataset(
		Integer datasetId, Integer instanceId, Boolean draftMode, ObservationUnitsSearchDTO.Filter filter);

	/**
	 * It will accept all the draft data even when there are out of bounds values for numerical types.
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 */
	void acceptAllDatasetDraftData(Integer studyId, Integer datasetId);

	/**
	 * Accepts the in bounds values for the draft data and set as missing the out of bounds values
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 */
	void acceptDraftDataAndSetOutOfBoundsToMissing(Integer studyId, Integer datasetId);

	/**
	 * It will reject all the draft data for a dataset
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 */
	void rejectDatasetDraftData(Integer studyId, Integer datasetId);

	/**
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @return a boolean indicating if the dataset draft data has out of bound values or not
	 */
	Boolean hasDatasetDraftDataOutOfBounds(Integer studyId, Integer datasetId);

	/**
	 * Accept the draft values that are retrieved after filtering by searchDTO.
	 * variableId in searchDTO can not be null
	 *
	 * @param studyId   Id of the study
	 * @param datasetId Id of the dataset
	 * @param searchDTO searchDTO
	 */
	void acceptDraftDataFilteredByVariable(Integer studyId, Integer datasetId,
		final ObservationUnitsSearchDTO searchDTO);

	/**
	 * Set an specific variable to an specific variable
	 *
	 * @param datasetId
	 * @param searchDTO
	 * @param studyId
	 */
	void setValueToVariable(Integer studyId, Integer datasetId, ObservationUnitsParamDTO searchDTO);

	List<MeasurementVariable> getAllDatasetVariables(int studyId, int datasetId);

	void replaceObservationUnitsEntry(int studyId, int datasetId, ObservationUnitEntryReplaceRequest request);

	ObservationUnitsMetadata getObservationUnitsMetadata(int studyId, int datasetId, SearchCompositeDto<ObservationUnitsSearchDTO, Integer> request);

	Long countObservationUnits(Integer dataSetId);
}
