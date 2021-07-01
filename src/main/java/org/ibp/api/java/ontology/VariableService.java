
package org.ibp.api.java.ontology;

import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.service.api.study.VariableDTO;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;

import java.util.List;

public interface VariableService {

	/**
	 * Get List of variables
	 *
	 * @param cropName name of the crop
	 * @param programId id of program
	 * @param propertyId id of property
	 * @param favourite favourite variable
	 * @return list of variables
	 */
	List<VariableDetails> getAllVariablesByFilter(String cropName, String programId, String propertyId, Boolean favourite);

	/**
	 * Get List of variable by applying filter
	 * @param cropName name of the crop
	 * @param programId id of program
	 * @param variableFilter variable filter that to be applied to get variables
	 * @return
	 */
	List<VariableDetails> getVariablesByFilter(String cropName, String programId, VariableFilter variableFilter);

	/**
	 * Get variable using given id
	 *
	 * @param cropName name of the crop
	 * @param programId id of program
	 * @param variableId id of the variable
	 * @return variable that matches id
	 */
	VariableDetails getVariableById(String cropName, String programId, String variableId);

	/**
	 * Add variable using given data
	 *
	 * @param cropName name of the crop
	 * @param programId programId
	 * @param variable data to be added
	 * @return newly created variable id
	 */
	GenericResponse addVariable(String cropName, String programId, VariableDetails variable);

	/**
	 * Add variable using given data
	 *
	 * @param cropName name of the crop
	 * @param programId programId
	 * @param variableId variable to be updated
	 * @param variable data to be added
	 */
	void updateVariable(String cropName, String programId, String variableId, VariableDetails variable);

	/**
	 * Delete variable of given Id
	 * 
	 * @param id Variable Id to be deleted
	 */
	void deleteVariable(String id);

	/**
	 * Remove variable from cache
	 * @param cropname name of the crop
	 * @param variablesIds array of ids to remove
	 * @param programId program unique id
	 */
	void deleteVariablesFromCache(String cropname, final Integer[] variablesIds, String programId);

	List<VariableDetails> getVariablesByFilter(VariableFilter variableFilter);

	long countVariablesByDatasetId(final int studyDbId, final List<Integer> variableTypes);

	List<VariableDTO> getVariablesByDatasetId(final int datasetId, final String cropname,
		final List<Integer> variableTypes, final int pageSize, final int pageNumber);

	long countAllVariables(final List<Integer> variableTypes);

	List<VariableDTO> getAllVariables(String cropname, final List<Integer> variableTypes, int pageSize, int pageNumber);

	List<Variable> searchAttributeVariables(String query, String programUUID);
}
