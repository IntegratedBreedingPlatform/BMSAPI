
package org.ibp.api.java.ontology;

import java.util.List;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableSummary;

public interface VariableService {

	/**
	 * Get List of variables
	 *
	 * @param programId id of program
	 * @param propertyId id of property
	 * @param favourite favourite variable
	 * @return list of variables
	 */
	List<VariableSummary> getAllVariablesByFilter(String programId, String propertyId, Boolean favourite);

	/**
	 * Get variable using given id
	 *
	 * @param programId id of program
	 * @param variableId id of the variable
	 * @return variable that matches id
	 */
	VariableDetails getVariableById(String programId, String variableId);

	/**
	 * Add variable using given data
	 *
	 * @param programId programId
	 * @param variable data to be added
	 * @return newly created variable id
	 */
	GenericResponse addVariable(String programId, VariableSummary variable);

	/**
	 * Add variable using given data
	 *
	 * @param programId programId
	 * @param variableId variable to be updated
	 * @param variable data to be added
	 */
	void updateVariable(String programId, String variableId, VariableSummary variable);

	/**
	 * Delete variable of given Id
	 * 
	 * @param id Variable Id to be deleted
	 */
	void deleteVariable(String id);
}
