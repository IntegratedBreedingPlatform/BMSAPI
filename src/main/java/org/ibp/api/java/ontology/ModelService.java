package org.ibp.api.java.ontology;

import org.ibp.api.domain.ontology.IdName;
import org.ibp.api.domain.ontology.VariableType;

import java.util.List;

public interface ModelService {

	/**
	 * get all data types
	 *
	 * @return list of data types
	 */
	List<IdName> getAllDataTypes();

	/**
	 * get all classes
	 *
	 * @return list of classes
	 */
	List<String> getAllClasses();

	/**
	 * Get List of all variable types
	 *
	 * @return List of Variable Types
	 */
	List<VariableType> getAllVariableTypes();
}
