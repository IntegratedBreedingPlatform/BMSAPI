package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.ontology.IdName;
import org.ibp.api.domain.ontology.VariableTypeResponse;

import java.util.List;

public interface OntologyModelService {

	/**
	 * get all data types
	 *
	 * @return list of data types
	 * @throws MiddlewareException
	 */
	List<IdName> getAllDataTypes() throws MiddlewareException;

	/**
	 * get all classes
	 *
	 * @return list of classes
	 * @throws MiddlewareException
	 */
	List<String> getAllClasses() throws MiddlewareException;

	/**
	 * Get List of all variable types
	 *
	 * @return List of Variable Types
	 */
	List<VariableTypeResponse> getAllVariableTypes();
}
