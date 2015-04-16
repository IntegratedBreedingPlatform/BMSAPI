package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.*;

import java.util.List;

public interface OntologyVariableService {

	/**
	 * Get List of variables
	 *
	 * @param programId
	 *            id of program
	 * @param propertyId
	 *            id of property
	 * @param favourite
	 *            favourite variable
	 * @return list of variables
	 * @throws MiddlewareException
	 */
	List<VariableSummary> getAllVariablesByFilter(String programId, Integer propertyId, Boolean favourite) throws MiddlewareException;

	/**
	 * Get variable using given id
	 *
	 * @param programId
	 *            id of program
	 * @param variableId
	 *            id of the variable
	 * @return variable that matches id
	 * @throws MiddlewareException
	 */
	VariableResponse getVariableById(String programId, Integer variableId) throws MiddlewareException;

	/**
	 * Add variable using given data
	 *
	 * @param request
	 *            data to be added
	 * @return newly created variable id
	 * @throws MiddlewareException
	 */
	GenericResponse addVariable(VariableRequest request) throws MiddlewareException;

	/**
	 * Add variable using given data
	 *
	 * @param request
	 *            data to be added
	 * @throws MiddlewareException
	 */
	void updateVariable(VariableRequest request) throws MiddlewareException;
}
