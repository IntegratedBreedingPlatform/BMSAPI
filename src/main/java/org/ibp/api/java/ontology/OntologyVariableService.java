package org.ibp.api.java.ontology;

import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.*;

import java.util.List;

public interface OntologyVariableService {

	/**
	 * Get List of variables
	 *
	 * @param programId id of program
	 * @param propertyId id of property
	 * @param favourite favourite variable
	 * @return list of variables
	 */
	List<VariableSummary> getAllVariablesByFilter(String programId, Integer propertyId, Boolean favourite);

	/**
	 * Get variable using given id
	 *
	 * @param programId id of program
	 * @param variableId id of the variable
	 * @return variable that matches id
	 */
	VariableResponse getVariableById(String programId, Integer variableId);

	/**
	 * Add variable using given data
	 *
	 * @param request data to be added
	 * @return newly created variable id
	 */
	GenericResponse addVariable(VariableRequest request);

	/**
	 * Add variable using given data
	 *
	 * @param request data to be added
	 */
	void updateVariable(VariableRequest request);

	/**
	 * Delete variable of given Id
	 * @param id Variable Id to be deleted
	 */
	void deleteVariable(Integer id);
}
