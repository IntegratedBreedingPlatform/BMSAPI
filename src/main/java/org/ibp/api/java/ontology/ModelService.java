
package org.ibp.api.java.ontology;

import java.util.List;

import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.VariableType;

public interface ModelService {

	/**
	 * get all data types
	 *
	 * @return list of data types
	 */
	List<DataType> getAllDataTypes();

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
