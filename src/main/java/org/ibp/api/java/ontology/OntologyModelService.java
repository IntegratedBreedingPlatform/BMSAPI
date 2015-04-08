package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.ontology.IdName;
import org.ibp.api.domain.ontology.VariableTypeResponse;

import java.util.List;

public interface OntologyModelService {

	/**
	 * get all data types
	 *
	 * @return list of data types
	 * @throws MiddlewareQueryException
	 */
	List<IdName> getAllDataTypes() throws MiddlewareQueryException;

	/**
	 * get all classes
	 *
	 * @return list of classes
	 * @throws MiddlewareQueryException
	 */
	List<String> getAllClasses() throws MiddlewareQueryException;

	/**
	 * Get List of all variable types
	 *
	 * @return List of Variable Types
	 */
	List<VariableTypeResponse> getAllVariableTypes();
}
